package MiiTunes;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class sets up our Miitunes Database to be connected
 * Version 1.0
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
    private String tableName = "Libary";

    private Connection connect;
    private Statement stmt;

    /**
     * This method estahblishes the connection to the MiiTunes database
     */
    public void Connect() {
    	
        System.out.println("Connecting to MiiTunes database now...");
        
        try {
            connect = DriverManager.getConnection(DatabaseURL, user, password);
        }
        catch (SQLException ex) {
            Logger.getLogger(MiiTunesDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This method will execute statements to the MiiTunes database
     * @param statement - the statement to be executed
     */
    public void executeStatement(String statement) {
    	
    	try {
    		stmt = connect.createStatement();
    		stmt.execute(statement);
    		
    	} catch (SQLException e) {
    		
    		e.printStackTrace();
    	}
    	
    }
    
    /**
     * This method will insert a Song's string info into the MiiTunes database
     * @param song - the song to be inserted
     * @return true if the song was succesfully added, otherwise false due to error
     */
    public boolean insertSong(Song song) {
    	try {
    		stmt = connect.createStatement();
    		
    		stmt.execute("INSERT INTO " + tableName + " VALUES ('"
                    + song.getTitle() + "','" + song.getArtist() + "','" 
    				+ song.getAlbum() + "','" + song.getYear() + "'," 
                    + song.getGenre() + ",'" + song.getComment() + "','" 
    				+ song.getPath() +"')");
    		
    		return true;
    		
    	} catch(SQLException e) {
    		
    		if(e.getCause().getMessage().equals(
    				"The statement was aborted because it would be a duplicate "
                    + "key value in a unique or primary key constraint "
                    + "or unique index identified by 'LIBRARY_PK' defined "
                    + "on 'LIBRARY'."))
    		{
    			System.out.println("\nUnable to add file because it is a duplicate");
    		}
    		
    		return false;
    	}	
    }
    
    /**
     * This method will remove a song from the MiiTunes Database based on the pathname of the Song
     * @param pathname - the path of the Song to be removed
     * @return true if deleteion was succesful, otherwise return false due to error
     */
    public boolean deleteSong(Song song) {	
    	try {
    		stmt = connect.createStatement();
    		String pathname = song.getPath();
    		
    		stmt.execute("DELETE FROM " + tableName + "WHERE pathname='" + pathname + "'");
    		
    		return true;
    	} catch(SQLException e) {
    		
    		e.printStackTrace();
    		return false;
    	}
    }
    
    /**
     * This method will return all the songs from the MiiTunes database
     * @return a 2D array containing the song info for our table in the GUI
     */
    public Object[][] returnAllSongs() {
        try {
            stmt = connect.createStatement();

            // Set ResultSet to query result to determine how many songs are in the database
            ResultSet size = stmt.executeQuery("select * from " + tableName);
            
            int tableSize = 0;
            while(size.next()) tableSize++;
            size.close();
            
            // Create 2D table to be returned with correct size
            Object[][] songData = new Object[tableSize][7];

            // Execute query again to actually get info from ResultSet
            ResultSet results = stmt.executeQuery("select * from " + tableName);

            for(int row = 0; row < tableSize; row++) {
                results.next();
                songData[row][0] = results.getString(1);
                songData[row][1] = results.getString(2);
                songData[row][2] = results.getString(3); 
                songData[row][3] = results.getString(4);
                songData[row][4] = Integer.toString(results.getInt(5));
                songData[row][5] = results.getString(6);
                songData[row][6] = results.getString(7);
            }

            results.close();

            return songData;
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return new Object[0][0];
    }

    /**
     * Method disconnects from the database
     */
    public void shutdown() {
        try {
            if(stmt != null) stmt.close();
            if(connect != null) connect.close();
        } catch(SQLException e) {
        	
            e.printStackTrace();
        }
    }       
}




