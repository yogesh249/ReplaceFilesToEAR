/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replacefilestoear;

import java.util.Date;
import javax.swing.JLabel;

/**
 *
 * @author yogesh.gandhi
 */
class TimerThread extends Thread {
    public Date startTime;
    public Date endTime;
    public Thread jthread;
    public JLabel lblTimer;
    public TimerThread(Thread jobThread, JLabel lblTimer) {
        this.jthread = jobThread;
        this.lblTimer = lblTimer;
        startTime = new java.util.Date();
        endTime = new java.util.Date();
    }
    public void run()
    {
        while(jthread.isAlive())
        {
            endTime = new java.util.Date();
            long numSeconds = (endTime.getTime()-startTime.getTime())/1000;
            lblTimer.setText(String.valueOf(numSeconds) + " seconds.");
            try
            {
                Thread.sleep(500);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();;
            }
        }
    }
    
}
