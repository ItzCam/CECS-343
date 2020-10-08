/**
 * @author Antonio Hughes
 * @author Noah Avina
 * 
 * Version 1.0
 * 
 * This class will set up our new GUI box 
 */

package MiiTunes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Antonio Hughes
 * @author Noah Avina
 * 
 * Version 1.0
 * 
 * This class will set up our new GUI box
 */
public class GUI extends JFrame{
    
    JPanel panel;
    
    JButton play, pause,stop,back,forward;
    
    public GUI(){
    	
        super("MiiTunes Player");
        this.setSize(700, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Creating the JPanel
        panel = new JPanel();
        this.add(panel);
        
        // Creating the four buttons on our MiiTunes Player
        play = new JButton("Play");
        play.addActionListener(new playButtonListener());
        
        pause = new JButton("Pause");
        pause.addActionListener(new pauseButtonListener());
        
        stop = new JButton("Stop");
        stop.addActionListener(new stopButtonListener());
        
        back = new JButton("Back");
        back.addActionListener(new backButtonListener());
        
        forward = new JButton("Forward");
        forward.addActionListener(new forwardButtonListener());
        
        panel.add(play);
        panel.add(pause);
        panel.add(stop);
        panel.add(back);
        panel.add(forward);
       
        // Displays the window
        setVisible(true); 
    }
    
    // This will list "Play" in the console
    class playButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("You pressed " + e.getActionCommand());
        }        
    }
    
 // This will list "Stop" in the console
    class stopButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("You pressed " + e.getActionCommand());
        }        
    }
    
    // This will list "Pause" in the console
    class pauseButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("You pressed " + e.getActionCommand());
        }        
    }
    
    //  // This will list "Back" in the console
    class backButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("You pressed " + e.getActionCommand());
        }        
    }
    
    // This will list "Forward" in the console
    class forwardButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("You pressed " + e.getActionCommand());
        }        
    }
}