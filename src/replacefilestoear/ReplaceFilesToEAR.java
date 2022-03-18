package replacefilestoear;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
public class ReplaceFilesToEAR extends JFrame {

    JLabel lblEARFilePath = new JLabel("Input EAR File : ");
    JLabel lbExcelFilePath = new JLabel("Input Excel File : ");
    JLabel lblStatus = new JLabel("");
    JLabel lblTimer = new JLabel("");
    JTextArea textarea = new JTextArea(20, 50);
    JScrollPane scroller = new JScrollPane(textarea); 
    JScrollBar bar = new JScrollBar();
    JTextField txtEARFileTextBox = new JTextField("", 30);
    JTextField txtCSVFileTextBox = new JTextField("", 30);
    JFileChooser txtEARFileChooser = new JFileChooser();
    JFileChooser txtCSVFileChooser = new JFileChooser();
    JButton btnEARSelectFile = new JButton("Select EAR File");
    JButton btnCSVSelectFile = new JButton("Select Excel File");
    JButton btnAddToEAR = new JButton("Add to EAR");
    JButton btnAddTimeStamp = new JButton("Add timestamp");
    public ReplaceFilesToEAR(String title) {
        super(title);
        // Add the code for selecting a file.
        btnEARSelectFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //Handle open button action.
                if (e.getSource() == btnEARSelectFile) {
                    int returnVal = txtEARFileChooser.showOpenDialog(ReplaceFilesToEAR.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = txtEARFileChooser.getSelectedFile();
                        txtEARFileTextBox.setText(file.getAbsolutePath());
                        if(file.getAbsolutePath().indexOf(" ")!=-1)
                        {
                            JOptionPane.showMessageDialog(null, "EAR File path cannot contain spaces");
                            System.exit(0);
                        }
                    } else {
                    }
                }
            }
        });
        btnCSVSelectFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //Handle open button action.
                if (e.getSource() == btnCSVSelectFile) {
                    int returnVal = txtCSVFileChooser.showOpenDialog(ReplaceFilesToEAR.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = txtCSVFileChooser.getSelectedFile();
                        txtCSVFileTextBox.setText(file.getAbsolutePath());
                        if(file.getAbsolutePath().indexOf(" ")!=-1)
                        {
                            JOptionPane.showMessageDialog(null, "CSV File path cannot contain spaces");
                            System.exit(0);
                        }                        
                    } else {
                    }
                }
            }
        });        
        btnAddToEAR.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // This thread will checkout and update the label as well.
                Thread jobThread = new AddToEARThread(lblStatus, txtCSVFileTextBox, txtEARFileTextBox, textarea) ;
                jobThread.start();
                Thread timerThread = new TimerThread(jobThread, lblTimer);
                timerThread.start();
            }
        });
        btnAddTimeStamp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // This thread will checkout and update the label as well.
                Thread jobThread = new AddTimeStampThread(lblStatus, txtCSVFileTextBox, txtEARFileTextBox, textarea) ;
                jobThread.start();
                Thread timerThread = new TimerThread(jobThread, lblTimer);
                timerThread.start();
            }
        });        
        scroller.add(bar); 

        getContentPane().setLayout(new FlowLayout());
        getContentPane().add(lblEARFilePath);
        getContentPane().add(txtEARFileTextBox);
        getContentPane().add(btnEARSelectFile);        
        getContentPane().add(lbExcelFilePath);
        getContentPane().add(txtCSVFileTextBox);
        getContentPane().add(btnCSVSelectFile);

        getContentPane().add(btnAddToEAR);
        getContentPane().add(btnAddTimeStamp);
        textarea.setLineWrap(true);
        textarea.setBorder(BorderFactory.createLineBorder(Color.gray));
        //getContentPane().add(lblStatus);
        getContentPane().add(lblTimer);
        getContentPane().add(scroller);        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(600, 500);
        setResizable(false);
        setVisible(true);
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        new ReplaceFilesToEAR("Developed by Yogesh Gandhi");
    }



}
