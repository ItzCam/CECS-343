package MiiTunes;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TableView.TableRow;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the GUI of our MiiTunes Player
 * @author Antonio Hughes
 * @author Noah Avina
 *
 * Version 1.6 - Updated lots of stuff
 */

class View extends JFrame {
	
    JMenuBar menuBar;
    JPopupMenu popupMenu;
    JTable songTable;
    JScrollPane scrollPane, sidePanel;
    JTextPane currentSong;
    JCheckBox repeatSong, repeatPlaylist; 
    
    JButton play, stop, pause_resume, next, previous;
    JPanel framePanel, controlPanel, songInfoPanel, bottomPanel;

    String[] tableHeaders = {"Title", "Artist", "Album", "Year", "Genre", "Comment", "Path"};
    Object[][] songData;
    DefaultTableModel tableModel;

    MiiTunesController controller;
    MiiTunesDatabase database;

    JFileChooser fileChooser;
    FileNameExtensionFilter extensionFilter;
    
    JTree playlistTree;
    DefaultTreeModel treeModel;
    DefaultMutableTreeNode sidePanelTreeRoot;
    
    boolean row_is_selected = false;
    int index;

    public View() {
    	
    	// Displayed at the top of the GUI window
    
        super("MiiTunes");
        
        // Estahblishing components GUI will need to operate correctly
        
        this.controller = new MiiTunesController();
        this.database = new MiiTunesDatabase();
        

        // Setting up connection to database & core components to GUI 
        
        boolean connectionCreated = database.Connect();
        if(!connectionCreated) System.exit(0);
        
        setupFileChooser();
        setupSongTable();
        setupMenuBar();
        setupSidePanel();
        setupButtons();
        setupControlPanel();
        setupSongArea();
        setupFramePanel();
        setupGuiWindow();
        
    }
    
    /**
     * This method displays text in the GUI window showing what song is currently playing
     * @param song - Song being played
     */
    public void updatePlayer(Song song) {
    	currentSong.setText(song.getTitle() + "\n" + song.getAlbum() + " by " + song.getArtist());
        songInfoPanel.updateUI();
    }

    /**
     * This method clears the text pane displaying what song is playing
     */
    public void clearPlayer() {
    	currentSong.setText("");
        songInfoPanel.updateUI();
    }

    /**
     * This method updates the pause/resume button being displayed based on condition its in
     * @param text - changes what the pause_resume button says 
     */
    public void updatePauseResumeButton(String text) {
    	pause_resume.setText(text);
    }

    /**
     * This method adds a song to the MiiTunes database and updates the Library table displaying all songs if song is added
     * @param song - object containing tag information and file path
     */
    public void addSong(Song song) {
        boolean wasSongInserted = database.insertSong(song, null); // Inserts song into the database
        if(wasSongInserted) {
            controller.addSong(song); // Adds song to player list
            Object[] rowData = {song.getTitle(),song.getArtist(),song.getAlbum(),song.getYear(),controller.genres.get(song.getGenre()),song.getComment()};
            tableModel.addRow(rowData); // Adds song to Library table
        }
    }
    
    /**
     * This method updates the view of the database tables in the GUI
     * @param playlistName
     */
    public void updateView(String playlistName) {
        int initialTableSize = tableModel.getRowCount();
        for(int row = initialTableSize-1; row >= 0; row--)
            tableModel.removeRow(row);

        if(playlistName == "Library") playlistName = null;
        Object[][] songData = database.returnAllSongs(playlistName);
        for(int i = 0; i < songData.length; i++) {
            Object[] rowData = {songData[i][0], songData[i][1], songData[i][2], songData[i][3], songData[i][4], songData[i][5], songData[i][6]};
            tableModel.addRow(rowData);
        }
        tableModel.fireTableDataChanged();
    }

    class popupMenuListener extends MouseAdapter {
    	
        @Override
        public void mouseReleased(MouseEvent e) {
            if(SwingUtilities.isRightMouseButton(e))
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    class addSongListener implements ActionListener {
    	
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnValue = fileChooser.showOpenDialog(framePanel);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
            	File[] files = fileChooser.getSelectedFiles();
            	for(File file : files) {
            		Song song = new Song(file.getPath());
                    boolean wasSongInserted = database.insertSong(song, null);
                    if(wasSongInserted) {
                        controller.addSong(song);
                        Object[] rowData = {song.getTitle(),song.getArtist(),song.getAlbum(),song.getYear(),controller.genres.get(song.getGenre()),song.getComment()};
                        tableModel.addRow(rowData);
                    }
                }
            }
        }
    }

    class deleteSongListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
        	int rows[] = songTable.getSelectedRows();
            if(rows.length > 0) {
                for(int row = rows.length-1; row >= 0; row--) {
                	String path = (String)songTable.getValueAt(row, 6);
                    Song song = controller.getSong(path);
                    boolean wasSongDeleted = database.deleteSong(song);
                    if(wasSongDeleted) {
                        controller.deleteSong(path);
                        tableModel.removeRow(rows[row]);
                    } else System.out.println("Couldn't delete " + song.getTitle());
                }
            }
        }
    }
    
    class repeatSongButtonListener implements ActionListener {
    	
    	@Override
        public void actionPerformed(ActionEvent e) {
            controller.updateRepeatSongStatus(repeatSong.isSelected());
         
    	}
    }

    class playIndividualSongListener implements ActionListener {
    	
    	@Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = fileChooser.showOpenDialog(new JPanel());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                Song song = new Song(fileChooser.getSelectedFile().getAbsolutePath());
                controller.play(song, true);
            }
        }
    }
    
    class createPlaylistListener implements ActionListener {
    	
        @Override
        public void actionPerformed(ActionEvent e) {
            String playlistName = JOptionPane.showInputDialog("Playlist name: ");
            boolean wasInserted = database.insertPlaylist(playlistName);
            if(wasInserted) {
                DefaultMutableTreeNode playlist = new DefaultMutableTreeNode(playlistName);
                ((DefaultMutableTreeNode)sidePanelTreeRoot.getChildAt(1)).add(playlist);
                treeModel.reload(sidePanelTreeRoot.getChildAt(1));
            }
        }
    }
    
    class sidePanelSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            JTree tree = (JTree)e.getSource();
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
            if (selectedNode.isLeaf())
                updateView(selectedNode.toString());
        }
    }

    class quitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            database.shutdown();
            System.exit(0);
        }
    }
    
    class repeatPlaylistButtonListener implements ActionListener {
    	@Override
        public void actionPerformed(ActionEvent e) {
            controller.updateRepeatPlaylistStatus(repeatPlaylist.isSelected());
    	}
    }
    
    class playButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(songTable.getSelectedRow() != -1) {
            	String path = (String)songTable.getValueAt(songTable.getSelectedRow(), 6);
            	controller.play(controller.getSong(path), false);
            }
            else System.out.println("Select a song first to play it!");
        }
    }

    class stopButtonListener implements ActionListener {
    	@Override
        public void actionPerformed(ActionEvent e) {
            controller.stop();
    	}
    }

    class pause_resumeButtonListener implements ActionListener {
    	@Override
        public void actionPerformed(ActionEvent e) {
            controller.pause_resume();
    	}
    }

    class nextSongButtonListener implements ActionListener {
    	@Override
        public void actionPerformed(ActionEvent e) {
    		
    		if(row_is_selected == false){
    			tableModel = (DefaultTableModel)songTable.getModel();
    			row_is_selected = true;
    		}
    		index = songTable.getSelectedRow();
    		tableModel.moveRow(index, index, index + 0);
    		songTable.setRowSelectionInterval(index + 1, index + 1);
    		songTable.setSelectionBackground(Color.LIGHT_GRAY);
    		controller.nextSong();   
    	}
    }

    class previousSongButtonListener implements ActionListener {
    	@Override
        public void actionPerformed(ActionEvent e) {
    		
    		if(row_is_selected == false){
    			
    			tableModel = (DefaultTableModel)songTable.getModel();
    			row_is_selected = true;
    		}
    		
    		index = songTable.getSelectedRow();
    		if(index > 0) {
    			
    			tableModel.moveRow(index, index, index + 0);
        		songTable.setRowSelectionInterval(index -1, index - 1);
        		songTable.setSelectionBackground(Color.LIGHT_GRAY);	
    		}	
            controller.previousSong();
    	}
    }  
    
    
    /**
     * Setting up what files to choose - mp3 files in particular
     */
    public void setupFileChooser() {
    	this.fileChooser = new JFileChooser();
    	this.extensionFilter = new FileNameExtensionFilter("MP3 Files" , "mp3");
    	fileChooser.setFileFilter(extensionFilter);
    	fileChooser.setMultiSelectionEnabled(true);
    }
    
    
    /**
     * Set up the song table
     */
    public void setupSongTable() {

        // Adding all songs in database to song table
        
        songData = database.returnAllSongs(null);
        
        for(int i = 0; i < songData.length; i++) {	
            controller.addSong(new Song((String)songData[i][6]));
        }

        tableModel = new DefaultTableModel(songData, tableHeaders);
        songTable = new JTable(tableModel);
        songTable.addMouseListener(new popupMenuListener());
        

        // Add drag and drop functionality to song table 
        
        songTable.setDropTarget(
        		
            new DropTarget() {
            	
                @Override
                public synchronized void drop(DropTargetDropEvent dtde) {
                	
                	// Enables the program to recognize by boolean drag and drop files, otherwise cancels operation
                	
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    	
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        Transferable t = dtde.getTransferable();
                        List fileList = null;
                        try {
                            fileList = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
                            
                            if(fileList.size() > 0) {
                            	
                                songTable.clearSelection();
                                Point point = dtde.getLocation();
                                int row = songTable.rowAtPoint(point);
                                
                                for(Object value : fileList) {
                                	
                                    if(value instanceof File) {
                                    	
                                        File f = (File) value;
                                        Song song = new Song(f.getAbsolutePath());
                                        addSong(song);
                                    }
                                }
                            }
                        } catch(UnsupportedFlavorException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else 
                        dtde.rejectDrop();
                }

                @Override
                public synchronized void dragOver(DropTargetDragEvent dtde) {
                    Point point = dtde.getLocation();
                    int row = songTable.rowAtPoint(point);
                    if(row < 0)
                        songTable.clearSelection();
                    else
                        songTable.setRowSelectionInterval(row, row);
                    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                }
            });

        songTable.getColumnModel().getColumn(6).setMinWidth(0);
        songTable.getColumnModel().getColumn(6).setMaxWidth(0);
        songTable.getColumnModel().getColumn(6).setResizable(false);
        scrollPane = new JScrollPane(songTable);

    }
    
    
    /**
     * Setting up the file menu bar in application
     */
    public void setupMenuBar() {
        
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem addSongMenuItem = new JMenuItem("Add songs");
        JMenuItem deleteSongMenuItem = new JMenuItem("Delete selected songs");
        JMenuItem playIndividualSongMenuItem = new JMenuItem("Play a song not in the library");
        JMenuItem createPlaylist = new JMenuItem("Create a playlist");
        JMenuItem quitApplicationMenuItem = new JMenuItem("Exit program");

        addSongMenuItem.addActionListener(new addSongListener());
        deleteSongMenuItem.addActionListener(new deleteSongListener());
        playIndividualSongMenuItem.addActionListener(new playIndividualSongListener());
        createPlaylist.addActionListener(new createPlaylistListener());
        quitApplicationMenuItem.addActionListener(new quitButtonListener());

        fileMenu.add(addSongMenuItem);
        fileMenu.add(deleteSongMenuItem);
        fileMenu.add(playIndividualSongMenuItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(createPlaylist);
        fileMenu.add(new JSeparator());
        fileMenu.add(quitApplicationMenuItem);
        menuBar.add(fileMenu);

    }
    
    
    /**
     * Setting up the pop-up menu when right clicking on application
     */
    public void setupPopupMenu() {  
    	
        popupMenu = new JPopupMenu();
        JMenuItem addSongPopupMenuItem = new JMenuItem("Add selected songs");
        JMenuItem deleteSongPopupMenuItem = new JMenuItem("Delete selected songs");
        addSongPopupMenuItem.addActionListener(new addSongListener());
        deleteSongPopupMenuItem.addActionListener(new deleteSongListener());
        popupMenu.add(addSongPopupMenuItem);
        popupMenu.add(deleteSongPopupMenuItem);
    }
    
    
    /**
     * Setting up the side panel displaying different tables in GUI being shown
     */
    public void setupSidePanel() {
        
    	sidePanelTreeRoot = new DefaultMutableTreeNode("Root");
    	treeModel = new DefaultTreeModel(sidePanelTreeRoot);
    	
    	playlistTree = new JTree();
    	playlistTree.setRootVisible(false);
    	playlistTree.setModel(treeModel);
    	playlistTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        playlistTree.addTreeSelectionListener(new sidePanelSelectionListener());
    	
    	DefaultMutableTreeNode library = new DefaultMutableTreeNode("Library");
    	DefaultMutableTreeNode playlists = new DefaultMutableTreeNode("Playlists");
    	
    	library.setAllowsChildren(false);
    	playlists.setAllowsChildren(true);
    	
    	ArrayList<String> playlistNames = database.returnAllPlaylists();
    	for(String playlistName : playlistNames)
    		playlists.add(new DefaultMutableTreeNode(playlistName));
    	
    	treeModel.insertNodeInto(library, sidePanelTreeRoot, 0);
    	treeModel.insertNodeInto(playlists, sidePanelTreeRoot, 1);
    	treeModel.nodeStructureChanged((TreeNode)treeModel.getRoot());
    	
    	sidePanel = new JScrollPane(playlistTree);
    	sidePanel.setPreferredSize(new Dimension((int)(songTable.getPreferredSize().getWidth()/4), (int)songTable.getPreferredSize().getHeight()));
    }
    
    
    /**
     * Setting up the control buttons 
     */
    public void setupButtons() {

        // Instantiate control buttons
        play = new JButton("Play");
        pause_resume = new JButton("Pause");
        stop = new JButton("Stop");  
        previous = new JButton("Previous song");
        next = new JButton("Next song");
        repeatSong = new JCheckBox("Repeat Song");
        repeatPlaylist = new JCheckBox("Repeat Playlist");

        // Add actions to control buttons 
        play.addActionListener(new playButtonListener());
        stop.addActionListener(new stopButtonListener());
        pause_resume.addActionListener(new pause_resumeButtonListener());
        next.addActionListener(new nextSongButtonListener());
        previous.addActionListener(new previousSongButtonListener());
        repeatSong.addActionListener(new repeatSongButtonListener());
        repeatPlaylist.addActionListener(new repeatPlaylistButtonListener());

    }
    
    
    /**
     *  Setting up the control panel of GUI 
     */
    public void setupControlPanel() {
    	
        // Add control buttons to control panel component
        controlPanel = new JPanel();
        controlPanel.add(previous);
        controlPanel.add(play);
        controlPanel.add(pause_resume);
        controlPanel.add(stop);
        controlPanel.add(next);
        controlPanel.add(repeatSong);
        controlPanel.add(repeatPlaylist);
        
    }
    
    
    /**
     *  Setting up song info area on the GUI
     */
    public void setupSongArea() {

        // Set up current song playing area
        currentSong = new JTextPane();
        StyledDocument doc = currentSong.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        
        // Add song text pane to info panel
        songInfoPanel = new JPanel();
        songInfoPanel.add(currentSong);
        currentSong.setBackground(songInfoPanel.getBackground());

        // Add current song playing area and player controls to bottom of gui
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(songInfoPanel, BorderLayout.NORTH);
        bottomPanel.add(controlPanel, BorderLayout.SOUTH);
    }
    
    
    /**
     * Setting up the frame panel of the GUI
     */
    public void setupFramePanel() {
    	
        // Add all components to gui main panel
        framePanel = new JPanel();
        framePanel.setLayout(new BorderLayout());
        framePanel.add(menuBar, BorderLayout.NORTH);
        framePanel.add(scrollPane,BorderLayout.CENTER);
        framePanel.add(sidePanel, BorderLayout.WEST);
        framePanel.add(bottomPanel, BorderLayout.SOUTH);
        framePanel.addMouseListener(new popupMenuListener());
        
    }
    
    
    /**
     * Setting up the size of the GUI window
     */
    public void setupGuiWindow() {
    	
        // Set up the GUI window size
    	this.setPreferredSize(new Dimension(1000, 700));
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().add(framePanel);
        this.pack();
        this.setVisible(true);
    }
}
