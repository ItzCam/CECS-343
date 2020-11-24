package MiiTunes;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import java.io.PrintStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * Controller class represents the function of the MiiTunes MP3 player
 * 
 * @author Antonio Hughes
 * @author Noah Avina
 * 
 * Version 1.3 - Use song paths for play instead of song object
 *
 */

public class MiiTunesController implements BasicPlayerListener {
	
    private PrintStream out = null;
    private BasicController controller;
    private BasicPlayer player = null;

    private int currentIndex;
    private String songPlaying;
    private boolean repeatSong, repeatPlaylist;
   

    private HashMap<String, Song> songs;
    private ArrayList<String> playOrder;
    
    public static ArrayList<String> genres;

    public MiiTunesController() {
    	out = System.out;

        player = new BasicPlayer();
    	player.addBasicPlayerListener(this);

    	controller = (BasicController)player;
    	this.setController(controller);

        currentIndex = -1;
        songPlaying = "";
        repeatSong = false;
        repeatPlaylist = false;
        
        songs = new HashMap<>();
        playOrder = new ArrayList<>();
        genres = new ArrayList<>();
        genres.add("Rap/Hip-Hop");
        genres.add("Unknown");
        // Possibly add more genres
        
    }

    /**
     * Method maps a song's path to its Song object so it can referred to by any playlsit
     * @param song - song to be added
     */
    public void addSong(Song song) {
    	if(!songs.containsKey(song.getPath()))
    		songs.put(song.getPath(), song);
    	else System.out.println("This song already exists in MiiTunes");
    }

    /**
     * Method to get all songs stored in MiiTunes
     * @return all songs
     */
    public ArrayList<Song> getAllSongs() {
    	return new ArrayList<>(songs.values());
    }

    /**
     * Method to find a certain song by its path in MiiTunes
     * @param path - directory of the song is stored
     * @return specific song(s)
     */
    public Song getSong(String path) {
        return songs.get(path);
    }

    /**
     * Method deletes a certain song by its index in MiiTunes
     * @param index - position of the song
     */
    public void deleteSong(Song song) {
        if(song.getPath().equals(songPlaying)) stop();
        songs.remove(song.getPath());
    }
    
    /**
     * Updates the playOrder array lsit to contain the order of songs as they appear in the playlist, so they can be iterated over for consecutive playback
     * @param songPaths - the paths of the songs
     */
    public void updatePlayOrder(ArrayList<String> songPaths) {
    	playOrder.clear();
    	for(String songPath : songPaths)
    		playOrder.add(songPath);
    }

    
    /**
     * Method plays a song
     * @param songPath - the file path of the song to be opened
     * @param currentIndex - the index of the song in the song table, corresponding to its position in the play order
     */
    public void play(String songPath, int currentIndex) {
        try {
            // Updates pause_resume button in GUI to switch display from 'resume' to 'pause'
            if(isPlayerPaused()) MiiTunes.view.updatePauseResumeButton("Pause");

            controller.open(new File(songPath));
            controller.play();

            songPlaying = songPath;
            this.currentIndex = currentIndex;

         // Updates GUI area that displays the song that is currently playing
            MiiTunes.view.updatePlayer(new Song(songPlaying));
        } catch(BasicPlayerException e) { e.printStackTrace(); }     
    }

    
    /**
     * Method stops the current song from playing
     */
    public void stop() {
    	
        // If player isn't playing a song, then do nothing
    	
    	if(isPlayerActive()) {
            try {
            	
                // Stop the currently playing song
            	
                controller.stop();
                songPlaying = "";
                currentIndex = -1;

                // Update the GUI that displays the song that is currently playing to stop
                
                MiiTunes.view.clearPlayer();

                // Updates pause_resume button in GUI to switch display from 'resume' to 'pause'
                
                MiiTunes.view.updatePauseResumeButton("Pause");
            } catch(BasicPlayerException e) { e.printStackTrace(); }
    	}
    	else System.out.println("Nothing is playing!");
    }

    
    /**
     * Method pauses the song that is currently playing
     * or resumes the song that is currently paused
     */
    public void pause_resume() {
    	if(isPlayerPlaying()) {
            try {
            	
                // Pause the song
            	
                controller.pause();

                // Updates pause_resume button in GUI to switch display from 'pause' to 'resume'
                
                MiiTunes.view.updatePauseResumeButton("Resume");
            } catch(BasicPlayerException e) { e.printStackTrace(); }
    	}
    	else if(isPlayerPaused()) {
            try {
            	
                // Resume the song
            	
                controller.resume();

                // Update pause_resume button in GUI to switch display from 'pause' to 'resume'
                
                MiiTunes.view.updatePauseResumeButton("Pause");
            } catch(BasicPlayerException e) { e.printStackTrace(); }
    	}
    	else System.out.println("Nothing is playing!");
    }

    
    /**
     * Method plays the next song in the library
     */
    public boolean nextSong() {
    	
        // If external song was playing, don't play anything after it ends
    	
    	if(currentIndex != -1 && isPlayerActive()) {
    		
            // If user has option to repeat song selected, replay the same song
    		
            if(repeatSong) {
                try {
                	
                    // If the player is paused, update the pause_resume button to display 'pause'
                	
                    if(isPlayerPaused())
                        MiiTunes.view.updatePauseResumeButton("Pause");

                    // Play the song
                    
                    controller.open(new File(songPlaying));
                    controller.play();
                    return true;
                } catch(BasicPlayerException e) { e.printStackTrace(); }
            }
            
            // If the user doesn't have repeat song option selected, so play next song in library
            
            else {
                if(currentIndex == (playOrder.size() - 1)) {
                    if(repeatPlaylist) currentIndex = 0;
                    else {
                        stop();
                        currentIndex = -1;
                        return false;
                    }
                }
                
                else currentIndex++;
                play(playOrder.get(currentIndex), currentIndex);
                return true;
            }
    	}
    	else if(currentIndex == -1) return false;
    	return false;
    }


    /**
     * Method plays the previous song in the library
     */
    public void previousSong() {
    	// If external song is playing or no song is playing, don't do anything
    	if(currentIndex != -1 && isPlayerActive()) {
            // If user has option to repeat song selected, replay the same song
            if(repeatSong) {
                try {
                    // If the player is paused, update the pause_resume button to display 'pause'
                    if(player.getStatus() == BasicPlayer.PAUSED)
                        MiiTunes.view.updatePauseResumeButton("Pause");

                    // Play the song
                    controller.open(new File(songPlaying));
                    controller.play();
                } catch(BasicPlayerException e) { e.printStackTrace(); }
            }
            // User doesn't have repeat song option selected, so play previous song in library
            else {
                if(currentIndex == 0) {
                    if(repeatPlaylist) currentIndex = playOrder.size() - 1;
                    else {
                        stop();
                        currentIndex = -1;
                        return;
                    }
                }
                else currentIndex--;
                play(playOrder.get(currentIndex), currentIndex);
            }
        }
    }

    
    /**
     * Method returns the playing status of the player
     * @return true if the player is playing. Otherwise, false
     */
    public boolean isPlayerPlaying() {
        return player.getStatus() == BasicPlayer.PLAYING;
    }

    /**
     * Method returns the paused status of the player
     * @return true if the player is paused. Otherwise, false
     */
    public boolean isPlayerPaused() {
        return player.getStatus() == BasicPlayer.PAUSED;
    }

    /**
     * Method returns the stopped status of the player
     * true if the player is stopped. Otherwise, false
     * @return 
     */
    public boolean isPlayerStopped() {
        return player.getStatus() == BasicPlayer.STOPPED;
    }
    
    /**
     * Boolean check to see if the MiiTunes player is active or not
     * @return true if player is playing or paused, otherwise return false due to error
     */
    public boolean isPlayerActive() {
        return isPlayerPlaying() || isPlayerPaused();
    }

    /**
     * This method will update the repeat playlist status in our GUI if boolean condition is met
     * @param repeat - boolean to check for repeat playlist status
     */
    public void updateRepeatPlaylistStatus(boolean repeatPlaylist) {
        this.repeatPlaylist = repeatPlaylist;
    }

    /**
     * This method will update the repeat song status in our GUI if boolean condition is met
     * @param repeat - boolean to check for repeat song status
     */
    public void updateRepeatSongStatus(boolean repeatSong) {
    	this.repeatSong = repeatSong;
    }

    /**
     * Simple fuction to represent the different state updates in the MiiTunes player
     */
    @Override
    public void stateUpdated(BasicPlayerEvent e) {
    	display("\nState updated: " + e.toString());
    	if(!songPlaying.equals("") && e.toString().substring(0,3).equals("EOM")) {
            while(!isPlayerStopped());
            if(!nextSong()) MiiTunes.view.clearPlayer();
    	}
    }
    

    /**
     * Simple function to open song objects, pertaining more in the console of the program
     */
    @Override
    public void opened(Object stream, Map properties) {
    	display("\nOpened: " + properties.toString());
    }

    @Override
    public void progress(int bytesread, long ms, byte[] pcmdata, Map properties) {
       Song song = new Song(songPlaying);
       display("\nprogress: {microseconds: " + properties.get("mp3.position.microseconds") + "/" + song.getDuration()
       + ", bytes: " + properties.get("mp3.position.byte") + "/" + song.getBytes()
       + ", frames: " + properties.get("mp3.frame") + "/" + song.getFrames() + "}\r");
    }

    /**
     * Method to set the contoller for all MiiTunes operations, pertaining more in the console of the program
     */
    @Override
    public void setController(BasicController controller) {
    	display("\nsetController: " + controller + "\n");
    }

    /**
     * Simple display function for strings
     * @param s - the string to be displayed
     */
    public void display(String s) {
    	if(out != null) out.print(s);
    }
} 
