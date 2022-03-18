/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replacefilestoear;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import util.CSVReader;
import utilities.Util;

/**
 *
 * @author yogesh.gandhi
 */
class AddToEARThread extends Thread {

	private static JLabel lblStatus;
	private static JTextField txtExcelFileTextBox;
	private static JTextField txtEARFileTextbox;
	private static JTextArea textarea;

	public AddToEARThread(JLabel lblStatus, JTextField txtExcelFileTextBox, JTextField txtEARFileTextbox,
			JTextArea textarea) {

		AddToEARThread.lblStatus = lblStatus;
		AddToEARThread.txtExcelFileTextBox = txtExcelFileTextBox;
		AddToEARThread.txtEARFileTextbox = txtEARFileTextbox;
		AddToEARThread.textarea = textarea;
	}

	@Override
	public void run() {
		try {
			FileOutputStream fos = null;
			String EARLocation = Util.getFolderFromFile(txtEARFileTextbox.getText());
			File logFile = new File(EARLocation + "\\log.txt");
			FileWriter logFileWriter = new FileWriter(logFile);
			String EARFileName = Util.getFileName(txtEARFileTextbox.getText());
			String extractEARCommand = "jar xvf " + txtEARFileTextbox.getText();
			File f = new File(txtEARFileTextbox.getText());
			if (!f.canWrite()) {
				JOptionPane.showMessageDialog(null, "EAR file is readonly. Please remove readonly");
				return;
			}
			try {
				boolean isAllFilesPresent = checkIfAllFileExists();
				if (!isAllFilesPresent) {
					return; // just stop the thread and return;
				}
			} catch (Throwable t) {
				t.printStackTrace();
				;
				JOptionPane.showMessageDialog(null, "There is some problem with your excel.");
				return;
			}
			try {
				String invalidFiles = checkIfValidACAF();
				if (!invalidFiles.equals("")) {
					String msg = "Few of the AC/AF are probably not in their correct place. \n"
							+ invalidFiles.toString() + "\nDo you want to proceed?";
					int answer = JOptionPane.showConfirmDialog(null, msg, "Invalid files", JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.NO_OPTION) {
						return; // just stop the thread and return;
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
				;
				JOptionPane.showMessageDialog(null, "There is some problem with your excel.");
				return;
			}

			System.out.println("Extracting the EAR file");
			textarea.setText(textarea.getText() + "\nExtracting the EAR file");
			// lblStatus.setText("Extracting the EAR file");
			String output = Util.executeCommand(extractEARCommand, EARLocation);
			logFileWriter.write(output);
			System.out.println(output);
			textarea.setText(textarea.getText() + "\n" + output);
			Set<String> updatedArchives = new HashSet<String>() {
			};
			HashMap<String, String> fileList = new HashMap<String, String>();
			try {
				CSVReader csv = new CSVReader(txtExcelFileTextBox.getText());
				// As CSVReader implements Iterable
				// So, we can use it in the enhanced for loop
				// as follows. Any class that implements Iterable
				// can follow a colon in the enhanced for loop.
				for (HashMap mp : csv) {
					// fileLocation has to match the relative path where the file is to be kept in
					// the
					// destinationJar file.
					// Source folder is the location where it will start looking for the path
					// specified
					// by the fileLocation
					// for e.g.
					// fileLocation = com/patches.txt
					// sourceFolder =
					// M:\yogesh_ENH_85_HDFC_Code_cas2.0\cascd1_datavob\Application\Source\Java\EJB\src
					String destinationJar = (String) mp.get("DestinationJar");
					String sourceFolder = (String) mp.get("SourceFolder");
					String fileLocation = (String) mp.get("FileLocation");
					String existingFiles = fileList.get(destinationJar);
					if (existingFiles == null) {
						fileList.put(destinationJar, " -C " + sourceFolder + " " + fileLocation);
					} else {
						fileList.put(destinationJar, existingFiles + " -C " + sourceFolder + " " + fileLocation);
					}
					updatedArchives.add(destinationJar);
				}
			} catch (Throwable t) {
				Logger.getLogger(AddToEARThread.class.getName()).log(Level.SEVERE, null, t);
				JOptionPane.showMessageDialog(null, t.getMessage());
			}

			// Now we have list of files for both EJB and WEB
			// We should write the two files ejblist.txt and weblist.txt
			try {
				for (String keys : updatedArchives) {
					File filelist = new File(EARLocation + "\\" + keys + ".txt");
					FileWriter fw = new FileWriter(filelist.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					String content = fileList.get(keys);
					bw.write(content);
					bw.close();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}

			// First
			try {
				CSVReader csv = new CSVReader(txtExcelFileTextBox.getText());
				// As CSVReader implements Iterable
				// So, we can use it in the enhanced for loop
				// as follows. Any class that implements Iterable
				// can follow a colon in the enhanced for loop.
				for (String destinationJar : updatedArchives) {
					System.out.println("Adding files to " + destinationJar);
					textarea.setText(textarea.getText() + "\nAdding files to " + destinationJar);
					// String addCommand = "jar uvf " + destinationJar + " -C " + sourceFolder + " "
					// + fileLocation;
					// The command will look something like
					// jar uvf LOSHDFCEJB.jar @xyz.txt
					// xyz.txt will contain some text like the following
					// -C D:\HDFC-11g-desk\EJB\classes
					// com\nucleus\los\bean\application\bde\ejb\BdeMainBean.class
					String addCommand = "jar uvf " + destinationJar + " @" + destinationJar + ".txt";
					String addOutput = Util.executeCommand(addCommand, EARLocation);
					logFileWriter.write(addOutput);
					System.out.println(addOutput);
					textarea.setText(textarea.getText() + "\n" + addOutput);
				}

				// Now update all the archives that have been updated into the EAR
				// Putting .jar and .war into .ear
				System.out.println("Adding .jar and .war to " + EARFileName);
				textarea.setText(textarea.getText() + "\nAdding .jar and .war to " + EARFileName);
				String addArchiveCommand = "jar uvf " + EARFileName;
				for (String destJars : updatedArchives) {
					addArchiveCommand = addArchiveCommand + " " + destJars;
				}
				String addOutput = Util.executeCommand(addArchiveCommand, EARLocation);
				System.out.println(addOutput);
				logFileWriter.write(addOutput);
				textarea.setText(textarea.getText() + "\n" + addOutput);

				logFileWriter.close();

			} catch (Exception ex) {
				Logger.getLogger(AddToEARThread.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
			textarea.setText(textarea.getText() + "\nJob done");
			// lblStatus.setText("Job Done");
			for (String files : updatedArchives) {
				try {
					File ff = new File(EARLocation + "\\" + files);
					ff.delete();
					File ff2 = new File(EARLocation + "\\" + files + ".txt");
					ff2.delete();
				} catch (Throwable t) {
					JOptionPane.showMessageDialog(null, "Error deleting file.");
				}

			}
		} catch (IOException ex) {
			Logger.getLogger(AddToEARThread.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static boolean checkIfAllFileExists() throws Exception {
		CSVReader csv = new CSVReader(txtExcelFileTextBox.getText());
		StringBuffer files = new StringBuffer("");
		// As CSVReader implements Iterable
		// So, we can use it in the enhanced for loop
		// as follows. Any class that implements Iterable
		// can follow a colon in the enhanced for loop.
		for (HashMap mp : csv) {
			String sourceFolder = (String) mp.get("SourceFolder");
			String fileLocation = (String) mp.get("FileLocation");
			String filePath = sourceFolder + "\\" + fileLocation;
			File fff = new File(filePath);
			if (!fff.exists()) {
				files = files.append("\n").append(fff.getAbsolutePath());
			}
		}
		if (!files.toString().equals("")) {
			JOptionPane.showMessageDialog(null, "Following files do not exist : \n" + files.toString());
			return false;
		}
		return true;
	}// ends checkIfAllFileExists

	private String checkIfValidACAF() throws Exception {
		StringBuffer invalidFiles = new StringBuffer("");
		CSVReader csv = new CSVReader(txtExcelFileTextBox.getText());
		// As CSVReader implements Iterable
		// So, we can use it in the enhanced for loop
		// as follows. Any class that implements Iterable
		// can follow a colon in the enhanced for loop.
		for (HashMap mp : csv) {
			String sourceFolder = (String) mp.get("SourceFolder");
			String fileLocation = (String) mp.get("FileLocation");
			String destinationJar = (String) mp.get("DestinationJar");
			if (fileLocation.endsWith(".js")) {
				if (!destinationJar.endsWith(".war")) {
					invalidFiles.append(fileLocation);
					invalidFiles.append(",");
					invalidFiles.append(destinationJar);
					invalidFiles.append("\n");
				}
			}
			if (fileLocation.endsWith(".jsp")) {
				if (!destinationJar.endsWith(".war")) {
					invalidFiles.append(fileLocation);
					invalidFiles.append(",");
					invalidFiles.append(destinationJar);
					invalidFiles.append("\n");
				}
			} 

		}
		return invalidFiles.toString();
	}
}
