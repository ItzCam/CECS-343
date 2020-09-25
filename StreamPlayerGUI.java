/*
 * Antonio Hughes
 * 09-17-2020
 */


import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamPlayerGUI extends JFrame {

    BasicPlayer player;

    JPanel main;

    JButton playButton; // We only make one button this time (Reference Lab 3)

    JTable table;

    JScrollPane scrollPane;

    int currentSelectedRow;

    ButtonListener bl;

    public StreamPlayerGUI() {

        player = new BasicPlayer();

        main = new JPanel();

        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS)); //Create our new panel w/ a vertical layout


        bl = new ButtonListener(); // Creates a button listener to attach to the play button

        // Now we set the buttons text, attach the listener, set size, and alignment
        playButton = new JButton("Play");
        playButton.addActionListener(bl);
        playButton.setMinimumSize(new Dimension(450, 25));
        playButton.setMaximumSize(new Dimension(450, 25));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Now we implement the data that will be displayed in the table
        String[] columns = {"Station URL", "Description"};
        String[][] data = {
                {"https://mp3.ffh.de/ffhchannels/hqrock.mp3", "Rock"},
                {"https://mp3.ffh.de/ffhchannels/hq80er.mp3", "The 80's"},
                {"https://mp3.ffh.de/ffhchannels/hqdeutsch.mp3", "Per Deutsch"},
                {"https://mp3.ffh.de/ffhchannels/hq90er.mp3", "The 90's"}};

        table = new JTable(data, columns);

        // Now we create a new listener for the mouse attached to the table
        MouseListener mouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                currentSelectedRow = table.getSelectedRow();
                System.out.println("Selected index " + currentSelectedRow);
            }
        };

        // Now we attach the mouseListener to the table
        table.addMouseListener(mouseListener);

        // Now we set the width of the URL column to be larger
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(250);

        scrollPane = new JScrollPane(table);

        // Now we can add all the info we have into the panel
        main.add(scrollPane);
        main.add(playButton);
        this.setTitle("Stream Player by Antonio Hughes"); // Of course add my name in
        this.setSize(500,125);  // Trying to see the scroll panel was tricky but move the corner of the window when you run and it will pop up 
        this.add(main);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            String url=null;

            // If there is a row selected on the table, sets URL to that element
            if(table.getSelectedRow() != 5){   // This is based on the index of elements, was 1 at first but that did not work out so well...
                url = (String)table.getValueAt(currentSelectedRow, 0);
            }

            // Now we will attempt to play the URL selected
            try {
                player.open(new URL(url));
                player.play();

            } catch (MalformedURLException ex) {

                Logger.getLogger(StreamPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);

                System.out.println("Malformed url");

            } catch (BasicPlayerException ex) {

                System.out.println("BasicPlayer exception");

                Logger.getLogger(StreamPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }

}
