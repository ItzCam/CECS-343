package MiiTunes;

import java.util.ArrayList;

/**
 * Main class of MiiTunes to be run
 * 
 * @author Antonio Hughes
 * @author Noah Avina
 * 
 * Version - 2.0 - Ready for Iteration 2 turn in 
 */
public class MiiTunes_Main {
	
    private static ArrayList<MiiTunes_View> views;
    private static String dragSourcePlaylist = "";

    public static String getDragSourcePlaylist() {
        return dragSourcePlaylist;
    }

    public static void setDragSourcePlaylist(String source) {
        dragSourcePlaylist = source;
    }
    
    public static void main(String[] args) {
        views = new ArrayList<>();
        views.add(new MiiTunes_View());
    }
    
    
    /**
     * This method will create a new view window to be displayed
     * @param title - the title displayed at the top of the window
     * @param controller - the controller that plays the song
     */
    public static void createNewView(String title, MiiTunes_Controller controller) {
        views.add(new MiiTunes_View(title, controller));
    }
    
    
    /**
     * This method will return a view object from an array of views
     * @param index - the current index of the view object
     * @return the view object based on its index from the array
     */
    public static MiiTunes_View getView(int index) {
        return views.get(index);
    }
    
    
    /**
     * This method returns all views displaying the same playlist
     * @param title - the title of the window, also the playlist that window displays
     * @return an array list of different view objects that have the same title
     */
    public static ArrayList<MiiTunes_View> getViews(String title) {
        ArrayList<MiiTunes_View> viewsWithTitle = new ArrayList<>();
        for(MiiTunes_View view : views) {
            if(view.getTitle().equals(title))
                viewsWithTitle.add(view);
        }
        return viewsWithTitle;
    }
    
    
    /**
     * This method will return all view objects that exist
     * @return an array list of different view objects
     */
    public static ArrayList<MiiTunes_View> getAllViews() {
        return views;
    }
    
    
    /**
     * This method will remove a view object from the array list
     * @param view - the view object to be removed
     */
    public static void removeView(MiiTunes_View view) {
        views.remove(view);
    }
    
    
    /**
     * This method will update the song table of all views displaying a playlist
     * @param playlistName - the playlist that needs to be updated in any windows displaying it
     */
    public static void updateWindows(String playlistName) {
        for(MiiTunes_View view : views) {
            if(view.getCurrentPlaylist().equals(playlistName))
                view.updateSongTableView(view.getCurrentPlaylist());
        }
    }
    
    
    /**
     *  This method will refresh all the different view windows.
     */
    public static void updateAllWindows() {
        for(MiiTunes_View view : views)
            view.updateSongTableView(view.getCurrentPlaylist());
    }
    
    
    /**
     * Method updates the first View window that is created
     */
    public static void updateLibrary() {
        views.get(0).updateSongTableView("Library");
    }
}
