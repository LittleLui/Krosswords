package littlelui.krosswords;

/* todo next steps:
 * 
 *  - toolbar ausblenden
 *  
 *  - lösungen auch laden und prüfen wenn fertig (oder via menüpunkt)
 *  - kontextmenü bei download-fehlerhaften rätseln zur anzeige des fehlers
 *  
 */
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;

import littlelui.krosswords.catalog.Catalog;
import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.fetch.DownloadManager;
import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Settings;

import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.ui.KMenu;
import com.amazon.kindle.kindlet.ui.KMenuItem;
import com.amazon.kindle.kindlet.ui.KOptionPane;

/** The Kindlet (= application).
 * @author LittleLui
 * 
 * Copyright 2011-2012 Wolfgang Groiss
 * 
 * This file is part of Krosswords.
 * 
 * Krosswords is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
public class Main extends AbstractKindlet {
	
		private static Main instance;
        
        private KindletContext ctx;
        private File stateDir;
        private File catalogDir;
        
        private PuzzleListEntry currentlyPlaying;
        
        private Catalog catalog;
        private DownloadManager dm;
        private Settings settings;
        
        private KMenu puzzleMenu = new KMenu();
        private KMenu catalogMenu = new KMenu();
        private KMenu settingsMenu = new KMenu();
        
        public Main() {
			super();
			instance = this;
			
			puzzleMenu.add(new KMenuItem(NAVIGATE_TO_SETTINGS));
			puzzleMenu.add(new KMenuItem(NAVIGATE_TO_CATALOG));
			puzzleMenu.add(new KMenuItem(VERIFY));
			puzzleMenu.add(new KMenuItem(FILL_RANDOM_LETTER));
			puzzleMenu.add(new KMenuItem(RESET));
			puzzleMenu.add(new KMenuItem(FINISH));

			catalogMenu.add(new KMenuItem(NAVIGATE_TO_SETTINGS));
			catalogMenu.add(new KMenuItem(NAVIGATE_TO_LAST_PUZZLE));
			catalogMenu.add(new KMenuItem(START_DOWNLOADING));
        }
        
        public static Main getInstance() {
        	return instance;
        }

		public void create(KindletContext context) {
                this.ctx = context;
    			stateDir = ctx.getHomeDirectory();
    			settings = Settings.load(stateDir);
    			
    			catalogDir = new File(stateDir, "catalog");
    			
    			catalogDir.mkdirs();
                
    			catalog = new Catalog(catalogDir);
    			
    			try {
    				dm = new DownloadManager(ctx.getConnectivity(), ctx, catalog, settings);
    			} catch (Throwable t) {
    				t.printStackTrace();
    				System.out.println(t);
    			}
                //this crashed the thing.. damnit
//                ((StandardKindletContext)ctx).getToolbar().setToolbarStyle(ToolbarStyle.TOOLBAR_TRANSIENT);
        }

		public void start() {
			if (settings.isNew())
				navigateToSettings();
			else { 
				PuzzleListEntry ple = getLastPuzzle();
				
				if (ple == null) {
					navigateToCatalog();
				} else {
					navigateToPuzzle(ple);
				}
			}
			
			dm.start();
        }

	private PuzzleListEntry getLastPuzzle() {
		String lastOpenPuzzleId = loadLastOpenPuzzleId();
		PuzzleListEntry ple = lastOpenPuzzleId == null ? null : catalog.getPuzzleById(lastOpenPuzzleId);
		return ple;
	}

	private String loadLastOpenPuzzleId() {
			//TODO: implement
			// TODO save puzzle id on stopping, if in puzzle pnale
			return null;
		}

	public void stop() {
		// save solution state of the panel
		if (currentlyPlaying != null) {
			currentlyPlaying.setLastPlayed(new Date());
		}
		// TODO: save panel's uri so we can re-open it when we open again
	}		
	
	public void navigateToSettings() {
		if (this.currentlyPlaying != null)
			this.currentlyPlaying.setLastPlayed(new Date());
		
		this.currentlyPlaying = null;
		
		Container c = ctx.getRootContainer();
		c.removeAll();
		
		c.add(new SettingsPanel(settings, dm));
		
		c.validate(); 
		c.repaint();
		
		ctx.setMenu(settingsMenu);
	}
	
	public void navigateToPuzzle(PuzzleListEntry ple) {
		Puzzle puzzle = ple.getPuzzle();
		
		if (puzzle == null)
			return;
		
		this.currentlyPlaying = ple;
		
		Container c = ctx.getRootContainer();
		c.removeAll();
		c.add(new PuzzlePanel(puzzle, ctx));
		
		c.validate(); 
		c.repaint();
		
		ctx.setMenu(puzzleMenu);
	}
		
	public void navigateToCatalog() {
		if (this.currentlyPlaying != null)
			this.currentlyPlaying.setLastPlayed(new Date());
		
		this.currentlyPlaying = null;
		
		Container c = ctx.getRootContainer();
		c.removeAll();
		c.add(new CatalogPanel(this, ctx, catalog));

		c.validate(); 
		c.repaint();
		
		ctx.setMenu(catalogMenu);

	}

	public File getCatalogDir() {
		return catalogDir;
	}

	public synchronized void logError(String string, Exception e) {
		File f = new File(ctx.getHomeDirectory(), "error.log");
		PrintWriter pw = null;
		try {
			FileWriter fw = new FileWriter(f,  true);
			pw = new PrintWriter(fw); 
			pw.println(new Date().toString());
			if (string != null) { 
				pw.println(string);
			}
			
			if (e != null) {
				e.printStackTrace(pw);
				pw.println();
				pw.println();
			}
			
			
		} catch (IOException ioe) {
			//ignore.
		} finally {
			pw.close();
		}
	}
	
	private Action NAVIGATE_TO_LAST_PUZZLE = new AbstractAction("Open Last Puzzle") {
		
		public boolean isEnabled() {
			return getLastPuzzle() != null;
		}

		public void actionPerformed(ActionEvent e) {
			PuzzleListEntry last = getLastPuzzle();
			navigateToPuzzle(last);
		}
	};
	
	private Action NAVIGATE_TO_CATALOG = new AbstractAction("Open Catalog") {
		public void actionPerformed(ActionEvent e) {
			if (currentlyPlaying != null) {
				currentlyPlaying.setLastPlayed(new Date());
			}
			
			navigateToCatalog();
		}
	};
	
	private Action NAVIGATE_TO_SETTINGS = new AbstractAction("Settings...") {
		public void actionPerformed(ActionEvent e) {
			if (currentlyPlaying != null) {
				currentlyPlaying.setLastPlayed(new Date());
			}
			
			navigateToSettings();
		}
	};
	
	private Action START_DOWNLOADING = new AbstractAction("Download new Puzzles") {
		public void actionPerformed(ActionEvent e) {
			dm.requestConnectionIfWorkToDo(true);
		}
	};
	private Action FINISH = new AbstractAction("Mark Puzzle Finished") {
		public void actionPerformed(ActionEvent e) {
			if (currentlyPlaying != null) {
				currentlyPlaying.setPuzzleSolutionState(PuzzleListEntry.FINISHED);
				currentlyPlaying.setLastPlayed(new Date());
			}
			
			navigateToCatalog();
		}
	};

	private Action VERIFY = new SolutionAction("Verify Solution") {
		public void actionPerformed(ActionEvent e) {
			if (currentlyPlaying != null) {
				Thread t = new Thread("Krosswords.VerifySolution") {
					public void run() {
						currentlyPlaying.verify();

						Container c = ctx.getRootContainer();
						c.validate();
						c.repaint();
					}
				};
				
				t.start();
			}
			
		}
	};
	
	private Action FILL_RANDOM_LETTER = new SolutionAction("Help me!") {
		public void actionPerformed(ActionEvent e) {
			if (currentlyPlaying != null) {
				Thread t = new Thread("Krosswords.FillRandom") {
					public void run() {
						currentlyPlaying.getPuzzle().fillRandomLetter();

						Container c = ctx.getRootContainer();
						c.validate();
						c.repaint();
					}
				};
				
				t.start();
			}
		}
	};
	
	private Action RESET = new AbstractAction("Reset Puzzle") {
		public void actionPerformed(ActionEvent e) {
			if (currentlyPlaying != null) {
				Thread t = new Thread("Krosswords.Reset") {
					public void run() {
						int dlgOption = KOptionPane.showConfirmDialog(ctx.getRootContainer(), "Do you really want to reset the puzzle?", "Reset", KOptionPane.NO_YES_OPTIONS);
						
						if (dlgOption == KOptionPane.YES_OPTION) {
							currentlyPlaying.getPuzzle().clear();

							Container c = ctx.getRootContainer();
							c.validate();
							c.repaint();
						}
					}
				};
				
				t.start();
			}
		}
	};
	
	private abstract class SolutionAction extends AbstractAction {
		public SolutionAction(String name) {
			super(name);
		}

		public boolean isEnabled() {
			return true; //TODO: we'd need to fire when the currentlyPlaying changes! otherwise they just stay disabled :(
//			return currentlyPlaying != null && currentlyPlaying.isSolutionAvailable();
		}
		
		
		
	}
}