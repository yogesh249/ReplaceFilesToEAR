/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replacefilestoear;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
class AddTimeStampThread extends Thread {

    private static JLabel lblStatus;
    private static  JTextField txtExcelFileTextBox;
    private static  JTextField txtEARFileTextbox;
    private static  JTextArea textarea;

    public AddTimeStampThread(JLabel lblStatus, JTextField txtExcelFileTextBox,
            JTextField txtEARFileTextbox, JTextArea textarea) {

        AddTimeStampThread.lblStatus = lblStatus;
        AddTimeStampThread.txtExcelFileTextBox = txtExcelFileTextBox;
        AddTimeStampThread.txtEARFileTextbox = txtEARFileTextbox;
        AddTimeStampThread.textarea = textarea;
    }

    @Override
    public void run() {
            String EARLocation = Util.getFolderFromFile(txtEARFileTextbox.getText());
            File logFile = new File(EARLocation + "\\log.txt");       
            FileWriter logFileWriter=null;
        try {
            FileOutputStream fos = null;
             logFileWriter = new FileWriter(logFile);             
//            String EARFileName = Util.getFileName(txtEARFileTextbox.getText());
            String extractEARCommand = "jar xvf " + txtEARFileTextbox.getText();
           
            System.out.println("Extracting the EAR file");
            textarea.setText(textarea.getText() + "\nExtracting the EAR file");
            //lblStatus.setText("Extracting the EAR file");
            String output = Util.executeCommand(extractEARCommand, EARLocation);
            logFileWriter.write(output);
            System.out.println(output);
            //textarea.setText(textarea.getText() + "\n" + output);
            Set<String> updatedArchives = new HashSet<String>();            
            try {
                CSVReader csv = new CSVReader(txtExcelFileTextBox.getText());
                // As CSVReader implements Iterable
                // So, we can use it in the enhanced for loop 
                // as follows. Any class that implements Iterable
                // can follow a colon in the enhanced for loop.
                for (HashMap mp : csv) {
                    String destinationJar = (String) mp.get("DestinationJar");
                    updatedArchives.add(destinationJar);
                }
            } catch (Throwable t) {
                Logger.getLogger(AddToEARThread.class.getName()).log(Level.SEVERE, null, t);
                JOptionPane.showMessageDialog(null, t.getMessage());
            }            
            
            // Now extract the updated Archives...
            // Now we have list of files for both EJB and WEB
            // We should write the two files ejblist.txt and weblist.txt
            try {
                for (String keys : updatedArchives) {
                    File filename = new File(EARLocation + "\\" + keys);
                    String extractCommand = "jar xvf " + filename;                     
                    if(keys.endsWith(".jar"))
                    {
                        Util.executeCommand("mkdir " + EARLocation + "\\EJB");                        
                        output = Util.executeCommand(extractCommand, EARLocation + "\\EJB");
                    }
                    else if(keys.endsWith(".war"))
                    {
                        Util.executeCommand("mkdir " + EARLocation + "\\WEB");                           
                        output = Util.executeCommand(extractCommand, EARLocation + "\\WEB");                        
                    }

                    logFileWriter.write(output);
                    System.out.println(output);
                    //textarea.setText(textarea.getText() + "\n" + output);
                    try
                    {
                        filename.delete();
                    }
                    catch(Throwable t)
                    {
                        JOptionPane.showMessageDialog(null, t.getMessage());
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }            
            
            // We'll use this StringBuilder to hold new CSV
            StringBuilder sb = new StringBuilder("");            
            // Now add a timestamp at the end of each file in CSV.
            try {
                CSVReader csv = new CSVReader(txtExcelFileTextBox.getText());

                sb.append("SourceFolder,FileLocation,DestinationJar,TimeStamp");                
                // As CSVReader implements Iterable
                // So, we can use it in the enhanced for loop 
                // as follows. Any class that implements Iterable
                // can follow a colon in the enhanced for loop.
                for (HashMap mp : csv) {
                    String sourceFolder = (String) mp.get("SourceFolder");
                    String fileLocation = (String) mp.get("FileLocation");        
                    String destinationJar = (String)mp.get("DestinationJar");
                    String filePath ="";
                    if(destinationJar.endsWith(".jar"))
                    {
                        filePath = EARLocation + "\\EJB" +"\\" + fileLocation;
                    }
                    else if(destinationJar.endsWith(".war"))
                    {
                        filePath = EARLocation + "\\WEB" +"\\" + fileLocation;
                    }
                    File ff = new File(filePath);
                    String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(ff.lastModified())) ;
                    textarea.setText(textarea.getText() + "\n" + fileLocation + ":" + timestamp);
                    sb.append("\n")
                            .append(sourceFolder)
                            .append(",")
                            .append(fileLocation)
                            .append(",")
                            .append(destinationJar)
                            .append(",")
                            .append(timestamp);
                }
            } catch (Throwable t) {
                Logger.getLogger(AddToEARThread.class.getName()).log(Level.SEVERE, null, t);
                JOptionPane.showMessageDialog(null, t.getMessage());
            }            
            
            // Now delete the two folders that we created.
            File ff = new File(EARLocation + "\\EJB");
            Util.delete(ff);
            ff = new File(EARLocation + "\\WEB");
            Util.delete(ff);
            
            File newCsv = new File(Util.getFolderFromFile(txtExcelFileTextBox.getText()) + "\\" 
                    + "new"+Util.getFileName(txtExcelFileTextBox.getText()));
            FileWriter fw = new FileWriter(newCsv, false);
            fw.write(sb.toString());
            fw.close();
            
        } catch (IOException ex) {
            Logger.getLogger(AddToEARThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try {
                logFileWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(AddTimeStampThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    private static boolean checkIfAllFileExists() throws Exception
    {
            CSVReader csv = new CSVReader(txtExcelFileTextBox.getText());
            StringBuffer files = new StringBuffer("");
            // As CSVReader implements Iterable
            // So, we can use it in the enhanced for loop 
            // as follows. Any class that implements Iterable
            // can follow a colon in the enhanced for loop.
            for (HashMap mp : csv) 
            {
                String sourceFolder = (String) mp.get("SourceFolder");
                String fileLocation = (String) mp.get("FileLocation");        
                String filePath = sourceFolder + "\\" + fileLocation;
                File fff = new File(filePath);
                if(!fff.exists())
                {
                    files = files.append("\n").append(fff.getAbsolutePath());
                }
            }
            if(!files.toString().equals(""))
            {
                JOptionPane.showMessageDialog(null, "Following files do not exist : \n" + files.toString());
                return false;
            }
            return true;
    }//ends checkIfAllFileExists

}
