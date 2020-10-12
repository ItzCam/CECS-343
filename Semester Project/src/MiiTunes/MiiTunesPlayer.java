package MiiTunes;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import javazoom.jlgui.basicplayer.*;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;

/** 
* 
*
* @author Antonio Hughes 
* @author Noah Avina
* 
* Version 1.1
* 
* This class when ran will display our new GUI box
*/

public class MiiTunesPlayer {
    
    /**
     * @param args the command line arguments
     */
    static GUI gui;
    public static void main(String[] args) throws IOException ,UnsupportedTagException, InvalidDataException, BasicPlayerException, UnsupportedAudioFileException {
       gui = new GUI();
       
       //Code below is for testing only. Once we figure everything out we will
       //clean up the main function by putting things in classes and functions
       
       
       //Get the path of the song, this is an example if the mp3 file
       //is on the desktop.
       String path = "/Users/camwilson/Desktop/Halsey - Without Me ft. Juice WRLD.mp3";
       //See if the path actually works
       File file = new File(path);
       if(file.canRead())
       {
           System.out.println("Song found");
           //Get the file with associated path
           Mp3File mp3 = new Mp3File(path); 
           //Get tag info
           ID3v2 id3v2Tag = mp3.getId3v2Tag();
           //Set tag info into variables
           String title = id3v2Tag.getTitle();
           String album = id3v2Tag.getAlbum();
           String artist = id3v2Tag.getArtist();
           String year = id3v2Tag.getYear();
           String comment = id3v2Tag.getComment();
           int genre = id3v2Tag.getGenre();
           //Print out the information 
           System.out.println("Title: " + title );
           System.out.println("Album: " + album);
           System.out.println("Artist: " + artist);
           System.out.println("Year: " + year);
           System.out.println("Comment: " + comment);
           System.out.println("Genre: " + genre);
           System.out.println(mp3.getFilename());
           
           //Start the playback
           //MP3Player playback = new MP3Player(path);
           //playback.testPlay(path);
       }
       else
       {
           System.out.println("Error finding file.");
       }
       
       //System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
       //System.out.println("Bitrate: " + mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
       //System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
       //System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO"));
       //System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO"));
       //System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));
    }    
}
