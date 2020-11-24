package MiiTunes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class sets up our Miitunes Database to be connected
 * Version 1.6 - Updated to use song paths instead of Song Objects
 * 
 * @author Antonio Hughes
 * @author Noah Avina
 *
 */
public class MiiTunesDatabase {
	
	// Components needed to connect to database using XAMMP program
	
    private String user = "root";
    private String password = "";
    private String DatabaseURL = "jdbc:mysql://localhost:3306/MiiTunes?serverTimezone=PST";
    private Connection connection = null;
   

    /**
     * This method estahblishes the connection to the MiiTunes database
     * @return true if the connection was initialized. otherwise false
     */
    public boolean Connect() {
    	
        System.out.println("Connecting to MiiTunes database now...");
        
        try {
            connection = DriverManager.getConnection(DatabaseURL, user, password);
            return true;
        }
        catch (SQLException ex) {
        	
            Logger.getLogger(MiiTunesDatabase.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("\nUnable to establish connection to MiiTunes database");
            return false;
        }
    }
    
    
    /**
     * This method will execute statements to the MiiTunes database
     * @param args - the values to be placed inside the query
     * @param query - the statement to be executed
     * @return true if executing the prepared statement threw no exceptions
     */
    public boolean executeStatement(String query, Object[] args) {
    	
    	PreparedStatement statement;
    	
    	try {
    		statement = connection.prepareStatement(query);
    		for(int i = 0; i < args.length; i++) {
                if(args[i].getClass() == String.class)
                    statement.setString(i+1, (String)args[i]);
                else if(args[i].getClass() == Integer.class)
                    statement.setInt(i+1, (int)args[i]);
    		}
    		
    		statement.executeUpdate();
    		return true;
    		
    	} catch (SQLException e) {
    		
    		e.printStackTrace();
    		return false;
    	}
    	
    }
    
     
    /**
     * This method will insert a playlist into the Playlists table
     * @param playlistName - the name of the playlist to be inserted
     * @return true if the playlist was succefully inserted, otherwise false if the playlist name already exists
     */
    public boolean insertPlaylist(String playlistName) {
    	return executeStatement("INSERT INTO Playlists VALUE (?)", new Object[] {playlistName});
    }
    
    
    /**
     * This method will insert a Song's string info into the MiiTunes database
     * @param song - the song to be inserted
     * @param playlistName - the playlist to associate the song with
     * @return true if the song was succesfully added, otherwise false due to error
     */
    public boolean insertSong(Song song, String playlistName) {
    	String query;
    	Object[] args;
    	int genreOverride;
    	
    	if(playlistName.equals("Library")) {
    		query = "INSERT INTO Songs VALUES (?,?,?,?,?,?,?)";
    		if(song.getGenre() == -1) genreOverride = 2;
    		else genreOverride = song.getGenre();
    		args = new Object[] {song.getTitle(), song.getArtist(), song.getAlbum(), song.getYear(), song.getGenre(), song.getComment(), song.getPath()};
    	}
    	else {
    		query = "INSERT INTO SongPlaylist VALUES (?,?)";
    		args = new Object[] {playlistName, song.getPath()};
    	}
    	
    	boolean wasInserted = executeStatement(query, args);
    	if (!wasInserted) System.out.println("\nUnable to add song!");
    	return wasInserted;
    }
    
    /**
     * This method will remove a song from the MiiTunes Database based on the pathname of the Song
     * @param pathname - the path of the Song to be removed
     * @return true if deleteion was succesful, otherwise return false due to error
     */
    public boolean deleteSong(Song song) {	
    	return executeStatement("DELETE FROM Songs WHERE path = ?", new Object[] {song.getPath()});
    }
    
    
    /**
     * This method will return all the songs from the MiiTunes database
     * @param playlistName
     * @return a 2D array containing the song info for our table in the GUI
     */
    public Object[][] returnAllSongs(String playlistName) {
    	
    	PreparedStatement statement;
    	String additionalClause = "";
    	if(playlistName.equals("Library")) additionalClause = " INNER JOIN SongPlaylist USING (path) INNER JOIN Playlists USING (playlistName) WHERE playlistName = '" + playlistName + "'";

        try {
            statement = connection.prepareStatement("SELECT COUNT(*) FROM Songs" + additionalClause);
            ResultSet results = statement.executeQuery();
            int tableSize = 0;
            while(results.next()) tableSize = results.getInt(1);
            
            // Create 2D table to be returned with correct size
            Object[][] songData = new Object[tableSize][7];

            // Execute query again to actually get info from ResultSet
            if(playlistName.equals("Library")) statement = connection.prepareStatement("SELECT title, artist, album, yearCreated, genre, comment, path, playlistName FROM Songs" + additionalClause);
            else statement = connection.prepareStatement("SELECT * FROM Songs");
            results = statement.executeQuery();

           int row = 0;
           while(results.next()) {
        	   
                songData[row][0] = results.getString(1);
                songData[row][1] = results.getString(2);
                songData[row][2] = results.getString(3); 
                songData[row][3] = results.getString(4);
                   
                //Updated Code to show genres hopefully
                if(results.getInt(5) == -1) songData[row][4] = MiiTunesController.genres.get(2);
                else songData[row][4] = MiiTunesController.genres.get(results.getInt(5));
        
                songData[row][5] = results.getString(6);
                songData[row][6] = results.getString(7);
                
                row++;
            }

            return songData;
        }
        catch(SQLException e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }
    
    
    /**
     * This method will get all the playlist names from the database
     * @return the ArrayList of strings that are the names of different playlists in the Playlist table in the database
     */
    public ArrayList<String> returnAllPlaylists() {
    	ArrayList<String> playlists = new ArrayList<>();
    	PreparedStatement statement;
    	try {
    		statement = connection.prepareStatement("SELECT * FROM Playlists");
    		ResultSet results = statement.executeQuery();
    		
    		while(results.next())
    			playlists.add(results.getString("playlistName"));
    	} catch(SQLException e) { e.printStackTrace(); }
    	return playlists;
    	
    }

    
    /**
     * Method disconnects from the database
     */
    public void shutdown() {
        try {
            if(connection != null) connection.close();
        } catch(SQLException e) {
        	
            e.printStackTrace();
        }
    }       
}




