package littlelui.krosswords;

/* todo next steps:
 * 
 *  - toolbar ausblenden
 *  
 *  - lösungen auch laden und prüfen wenn fertig (oder via menüpunkt)
 *  
 */
import java.awt.Container;
import java.io.File;
import java.util.Date;

import littlelui.krosswords.catalog.Catalog;
import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.fetch.DownloadManager;
import littlelui.krosswords.model.Puzzle;

import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;
import com.sun.xml.internal.bind.v2.TODO;

public class Main extends AbstractKindlet {
	
		private static Main instance;
        
        private KindletContext ctx;
        private File stateDir;
        private File catalogDir;
        
        private PuzzleListEntry currentlyPlaying;
        
        private Catalog catalog;
        private DownloadManager dm;

        public Main() {
			super();
			instance = this;
		}
        
        public static Main getInstance() {
        	return instance;
        }

		public void create(KindletContext context) {
                this.ctx = context;
    			stateDir = ctx.getHomeDirectory();
    			catalogDir = new File(stateDir, "catalog");
    			
    			catalogDir.mkdirs();
                
    			catalog = new Catalog(catalogDir);
    			dm = new DownloadManager(ctx.getConnectivity(), ctx, catalog);
                //this crashed the thing.. damnit
//                ((StandardKindletContext)ctx).getToolbar().setToolbarStyle(ToolbarStyle.TOOLBAR_TRANSIENT);
        }

		public void start() {
			String lastOpenPuzzle = loadLastOpenPuzzleId();
			
			if (lastOpenPuzzle == null) {
				navigateToCatalog();
			} else {
//				navigateToPuzzle(lastOpenPuzzle);
			}
    			
			//TODO: find last panel we worked on and load it, then we can
    		
        }

	private String loadLastOpenPuzzleId() {
			//TODO: implement
			// TODO save puzzle id on stopping, if in puzzle pnale
			return null;
		}

	public void stop() {
		File dir = ctx.getHomeDirectory();
		// save solution state of the panel
		if (currentlyPlaying != null) {
			currentlyPlaying.setLastPlayed(new Date());
		}
		// TODO: save panel's uri so we can re-open it when we open again
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
		
//		//load solution state of the panel
//		try {
//			model.loadSolutionState(dir);
//		} catch (IOException ioe) {
//			//TODO: log?
//		}

	}
		
	public void navigateToCatalog() {
		if (this.currentlyPlaying != null)
			this.currentlyPlaying.setLastPlayed(new Date());
		
		Container c = ctx.getRootContainer();
		c.removeAll();
		c.add(new CatalogPanel(this, ctx, catalog));
	}

	public File getCatalogDir() {
		return catalogDir;
	}

	public void logError(String string, Exception e) {
//		 TODO Auto-generated method stub
		
	}
	
	
		

}