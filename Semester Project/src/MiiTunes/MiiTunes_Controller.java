package MiiTunes;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

/**
 * Controller class handles mp3 functionality and puts songs into user-created
 * playlists.
 * 
 * @author Antonio Hughes
 * @author Noah Avina
 * 
 * Version - 2.0 - Ready for Iteration 2 turn in 
 */

public class MiiTunes_Controller implements BasicPlayerListener {
	
    private BasicController controller;
    private BasicPlayer player = new BasicPlayer();
    private MiiTunes_Database database;
    
    private double soundGain = 0.5; // Handles adjusting the sound
    private long secondsPlayed = 0; // Handles showing song progression
    private int currentIndex = -1;
    private String songPlaying = "";
    private boolean repeatSong = false;
    private boolean repeatPlaylist = false;
    
    private HashMap<String, MiiTunes_Song> songs = new HashMap<>();
    private ArrayList<String> playOrder = new ArrayList<>();
    public static ArrayList<String> genres = new ArrayList<>();
	
    public MiiTunes_Controller() {
    	player.addBasicPlayerListener(this);
    	controller = (BasicController)player;
        
        genres.add("Hip-Hop/Rap");
        genres.add("Classical");
        genres.add("Unknown");
        genres.add("Rock");
        genres.add("Pop");
        genres.add("Electronic");
        genres.add("Dance");
        genres.add("Mix");
        
        database = new MiiTunes_Database();
        if(!database.createConnection()) System.exit(0);
        
        Object[][] songData = returnAllSongs("Library");
        for(int i = 0; i < songData.length; i++)
            songs.put(songData[i][6].toString(), new MiiTunes_Song(songData[i][6].toString()));
    }
    
    
    /**
     * This method will update the volume of song when adjusted
     * @param volume - the value to adjust higher/lower
     */
    public void changeVolume(double volume) {
        try {
            if(isPlayerActive()) controller.setGain(volume);
            soundGain = volume;
            
            // Updates the volume slider for all views
            for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                view.updateVolumeSlider(soundGain);
        }
        catch(BasicPlayerException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Method gets the volume of the controller
     * @return the gain of the volume
     */
    public double getGain() {
        return soundGain;
    }
    
    
    /**
     * This method will determine if a song already exists in the MiiTunes library
     * @param path - the desired song to check
     * @return true if the song exists in the library. Otherwise return false.
     */
    public boolean songExists(String path) {
        return songs.containsKey(path);
    }
    
    
    /**
     * This method will find a songs path to it's song object so it can be referred
     * to by any playlist in MiiTunes
     * @param song - the song to be added
     * @param playlistName - the playlist for the song to be added to
     * @return true if the song is added. Otherwise, return false.
     */
    public boolean addSong(MiiTunes_Song song, String playlistName) {
        if(database.insertSong(song, playlistName)) {
            if(playlistName.equals("Library"))
                songs.put(song.getPath(), song);
            return true;
        }
        return false;
    }
    
    
    /**
     * This method will delete a song a specified playlist
     * @param song - the song to be deleted
     * @param playlistName - the playlist where the song will be deleted from
     * @param ID - the unique id of a song in a playlist
     * @return true if the song is deleted. Otherwise, return false
     */
    public boolean deleteSong(MiiTunes_Song song, String playlistName, int ID) {
        if(song.getPath().equals(songPlaying)) stop();
        if(database.deleteSong(song, playlistName, ID)) {
            if(playlistName.equals("Library"))
                songs.remove(song.getPath());
            return true;
        }
        return false;
    }
    
    
    /**
     * This method updates one attribute of a song in the database
     * @param songPath - the desired song to update
     * @param updatedColumn - the column relating the view table and database table
     * @param updatedValue - the value to update the database row
     * @return true if the update in the database was successful. Otherwise, return false
     */
    public boolean updateSong(String songPath, int updatedColumn, Object updatedValue) {
        if(database.updateSong(songPath, updatedColumn, updatedValue)) {
            songs.get(songPath).setArtist(updatedValue.toString());
            /*
                songs.get(songPath).saveSong(songPath);
                This line needs to work but it currently doesn't. Without it,
                songs will still display the original tag info they had when
                they were created. The database and song table will contain the
                changes though.
            */
            if(songPath.equals(songPlaying)) {
                for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                    view.updatePlayer(songs.get(songPlaying), secondsPlayed);
            }
            return true;
        }
        else return false;
    }
    
    
    /**
     * This method gets a specific song from the MiiTunes Library
     * @param path - the key of the song object
     * @return the song object based on its path 
     */
    public MiiTunes_Song getSong(String path) {
        return songs.get(path);
    }
    
 
    /**
     * This method gets all of the tags for one row in the songs table
     * @param path - the specific row to pull from the databse
     * @return an array of Objects that are attributes for a database row
     */
    public Object[] getSongData(String path) {
        return database.returnSong(path);
    }
    
    
    /**
     * This method will return the data for all songs in MiiTunes database
     * @param playlistName - specified playlist
     * @return 2D Object array of songs and their attributes
     */
    public Object[][] returnAllSongs(String playlistName) {
        return database.returnAllSongs(playlistName);
    }
    
    
    /**
     * This method will add a playlist to the MiiTunes database
     * @param playlistName - the name of the playlist to be inserted
     * @return true if the playlist was inserted. Otherwise, return false.
     */
    public boolean addPlaylist(String playlistName) {
        return database.insertPlaylist(playlistName);
    }
    
    
    /**
     * This method will delete a playlist from the MiiTunes database
     * @param playlistName - the name of the playlist to be inserted
     * @return true if the playlist was deleted. Otherwise, return false.
     */
    public boolean deletePlaylist(String playlistName) {
        return database.deletePlaylist(playlistName);
    }
    
    
    /**
     * This method returns all playlists in the MiiTunes database
     * @return list of playlist names
     */
    public ArrayList<String> returnAllPlaylists() {
        return database.returnAllPlaylists();
    }
    
    
    /**
     * This method will update the playOrder array list to contain
     *  the order of songs as they appear in the playlist
     * @param songPaths - the paths of the songs
     */
    public void updatePlayOrder(ArrayList<String> songPaths) {
        playOrder.clear();
        for(String songPath : songPaths)
            playOrder.add(songPath);
    }
    
    
    /**
     * This method plays a specified song
     * @param songPath - the file path of the song to be opened
     * @param currentIndex - the index of the song in the song table based 
     * 						 on its position in play order
     */
    public void play(String songPath, int currentIndex) {
        // Updates pause_resume button of all windows to switch text from 'resume' to 'pause'
        if(isPlayerPaused()) {
            for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                view.updatePauseResumeButton("Pause");
        }
        
        try {  
            controller.open(new File(songPath));
            controller.play();
            changeVolume(soundGain);
            
            songPlaying = songPath;
            this.currentIndex = currentIndex;

            // Updates area of all windows that display the currently playing song
            for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                view.updatePlayer(new MiiTunes_Song(songPlaying), this.secondsPlayed);
        } catch(BasicPlayerException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * This method will pause the song that is playing or resume it if it was
     * paused
     */
    public void pause_resume() {
    	if(isPlayerPlaying()) {
            // Updates pause_resume button of all windows to switch text from 'pause' to 'resume'
            for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                view.updatePauseResumeButton("Resume");
            try {
                controller.pause();
            } catch(BasicPlayerException e) {
                e.printStackTrace();
            }
    	}
    	else if(isPlayerPaused()) {
            // Update pause_resume button of all windows to switch text from 'pause' to 'resume'
            for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                view.updatePauseResumeButton("Pause");
            try {
                controller.resume();
            } catch(BasicPlayerException e) {
                e.printStackTrace();
            }
    	}
    	else System.out.println("Nothing is playing!");
    }
    
    
    /**
     * This method will stop the current song from playing.
     */
    public void stop() {
        // If player isn't playing a song, do nothing
    	if(isPlayerActive()) {
            // Update song area & pause/resume button of all windows
            for(MiiTunes_View view : MiiTunes_Main.getAllViews()) {
                view.clearPlayer();
                view.updatePauseResumeButton("Pause");
            }
            try {
                // Stop the song
                controller.stop();
                songPlaying = "";
                currentIndex = -1;
            } catch(BasicPlayerException e) {
                e.printStackTrace();
            }
    	}
    	else System.out.println("Nothing is playing!");
    }
    
    
    /**
     * This method will play the next song in the library.
     */
    public void nextSong() {
        // If external song was playing, this method does nothing
    	if(currentIndex != -1 && !isPlayerStopped()) {
            // If user has option to repeat song selected, replay the same song
            if(repeatSong) {
                // If the player is paused, update the pause_resume button to display 'pause'
                if(isPlayerPaused()) {
                    for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                        view.updatePauseResumeButton("Pause");
                }
                try {
                    controller.open(new File(songPlaying));
                    controller.play();
                    changeVolume(soundGain);
                } catch(BasicPlayerException e) {
                    e.printStackTrace();
                }
            }
            // User doesn't have repeat song option selected, so play next song in library
            else {
                // If song playing was last song in the play order...
                if(currentIndex == (playOrder.size() - 1)) {
                    // If user wants to repeat playlist, play first song in playlist
                    if(repeatPlaylist) currentIndex = 0;
                    else {
                        stop();
                        return;
                    }
                }
                // Else, play next song
                else currentIndex++;
                play(playOrder.get(currentIndex), currentIndex);
            }
        }
    }
    
    
    /**
     * This overridden method of nextSong method is used to
     * play the next song in playOrder once a song finishes playing
     * @param obj - useless parameter used to override method
     * @return true if next song starts playing. False if end of playOrder is reached
     */
    private boolean nextSong(Object obj) {
        // If user has option to repeat song selected, replay the same song
        if(repeatSong) {
            // If the player is paused, update the pause_resume button to display 'pause'
            if(isPlayerPaused()) {
                for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                    view.updatePauseResumeButton("Pause");
            }
            try {
                controller.open(new File(songPlaying));
                controller.play();
                changeVolume(soundGain);
                return true;
            } catch(BasicPlayerException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            // If song playing was last song in the play order...
            if(currentIndex == (playOrder.size() - 1)) {
                // If user wants to repeat playlist, play first song in playlist
                if(repeatPlaylist) currentIndex = 0;
                else {
                    stop();
                    return false;
                }
            }
            else currentIndex++;
            
            play(playOrder.get(currentIndex), currentIndex);
            return true;
        }
    }
    
    
    /**
     * This method will play the previous song in the MiiTunes library.
     */
    public void previousSong() {
    	// If external song is playing or no song is playing, don't do anything
    	if(currentIndex != -1 && isPlayerActive()) {
            // If user has option to repeat song selected, replay the same song
            if(repeatSong) {
                // If the player is paused, update the pause_resume button of all windows to display 'pause'
                if(isPlayerPaused()) {
                    for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                        view.updatePauseResumeButton("Pause");
                }
                try {
                    controller.open(new File(songPlaying));
                    controller.play();
                    changeVolume(soundGain);
                    
                    // Update song area of all windows to display song info of new song
                    for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                        view.updatePlayer(new MiiTunes_Song(songPlaying), secondsPlayed);
                } catch(BasicPlayerException e) {
                    e.printStackTrace();
                }
            }
            // User doesn't have repeat song option selected, so play previous song in library
            else {
                // If the song played is past 2 seconds, just restart it
                if(secondsPlayed > 2);
                // Else, try and play the previous song
                else if(currentIndex == 0) {
                    if(repeatPlaylist) currentIndex = playOrder.size() - 1;
                    else {
                        stop();
                        return;
                    }
                }
                else currentIndex--;
                
                play(playOrder.get(currentIndex), currentIndex);
            }
        }
    }
    
    
    /**
     * This method will return the playing status of the MiiTunes player
     * @return true if the player is playing. Otherwise, return false
     */
    public boolean isPlayerPlaying() {
        return player.getStatus() == BasicPlayer.PLAYING;
    }
    
    
    /**
     * This method will return the paused status of the MiiTunes player
     * @return true if the player is paused. Otherwise, false
     */
    public boolean isPlayerPaused() {
        return player.getStatus() == BasicPlayer.PAUSED;
    }
    
    
    /**
     * This method will return the stopped status of the MiiTunes player
     * @return true if the player is stopped. Otherwise, return false
     */
    public boolean isPlayerStopped() {
        return player.getStatus() == BasicPlayer.STOPPED;
    }
    
    
    /**
     * This method will return the active status of the MiiTunes player
     * @return true if the player is playing or paused. Otherwise, false
     */
    public boolean isPlayerActive() {
        return isPlayerPlaying() || isPlayerPaused();
    }
    
    
    /**
     * This method will update if the playOrder should be repeated after 
     * reaching the last song
     * @param repeatPlaylist - the value to determine if playOrder should repeat
     */
    public void updateRepeatPlaylistStatus(boolean repeatPlaylist) {
        this.repeatPlaylist = repeatPlaylist;
        for(MiiTunes_View view : MiiTunes_Main.getAllViews())
            view.updateRepeatPlaylistButton(repeatPlaylist);
    }
    
    
    /**
     * This method updates if the song should be repeated after playing
     * @param repeatSong - the value to determine if the song should repeat
     */
    public void updateRepeatSongStatus(boolean repeatSong) {
    	this.repeatSong = repeatSong;
        for(MiiTunes_View view : MiiTunes_Main.getAllViews())
            view.updateRepeatSongButton(repeatSong);
    }
    
    @Override
    public void stateUpdated(BasicPlayerEvent e) {
    	System.out.println("\nState updated: " + e.toString());
        // If a song has just finished playing (the next button wasn't pressed)
    	if(!songPlaying.equals("") && e.toString().substring(0,3).equals("EOM")) {
            
            // Wait for the player to officially stop
            while(!isPlayerStopped());
            
            // Try and play the next song
            if(!nextSong(0)) {
                // If the next song doesn't play, clear the song area of all windows
                for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                    view.clearPlayer();
            }
    	}
    }
    
    @Override
    public void opened(Object stream, Map properties) {
        System.out.println("\nOpened: " + properties.toString());
    }
    
    @Override
    public void progress(int bytesread, long ms, byte[] pcmdata, Map properties) {
        // Number of seconds is in microseconds, so convert it into seconds
        long secondsPlayed = ((long)properties.get("mp3.position.microseconds")/1000000);
        
        /* If the seconds displayed isn't up to date, refresh
           all views to display correct song progression */
        if(secondsPlayed != this.secondsPlayed) {
            this.secondsPlayed = secondsPlayed;
            for(MiiTunes_View view : MiiTunes_Main.getAllViews())
                view.updatePlayer(new MiiTunes_Song(songPlaying), secondsPlayed);
        }
    }
    
    @Override
    public void setController(BasicController controller) {
    	System.out.println("\nsetController: " + controller);
    }
    
    /**
     * This method will disconnect MiiTunes database connection
     */
    public void disconnectDatabase() {
    	database.shutdown();
    }
}
