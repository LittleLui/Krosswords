package littlelui.krosswords;

/* todo next steps:
 * 
 *  - toolbar ausblenden
 *  
 *  - kreuzworträtsel automatisch von derstandard.at laden
 *  - zwischen rätseln wechseln usw...
 *  - zwischenstand speichern und laden muss noch auf wechselnde rätsel angepasst werden
 *  - lösungen auch laden und prüfen wenn fertig (oder via menüpunkt)
 *  
 */
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Word;

import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;

public class Main extends AbstractKindlet {
	
		private static Main instance;
        
        private KindletContext ctx;
        private File stateDir;
        private File catalogDir;
        
        private Puzzle model;

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
                
                //this crashed the thing.. damnit
//                ((StandardKindletContext)ctx).getToolbar().setToolbarStyle(ToolbarStyle.TOOLBAR_TRANSIENT);
        }

		public void start() {
			String lastOpenPuzzle = loadLastOpenPuzzleId();
			
			if (lastOpenPuzzle == null) {
				navigateToCatalog();
			} else {
//				navigateToPuzzle(puzzle);
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
		try {
			if (model != null)
				model.saveSolutionState(dir);
		} catch (IOException ioe) {
			// TODO: log?
		}
		// TODO: save panel's uri so we can re-open it when we open again
	}		
	
	public void navigateToPuzzle(Puzzle puzzle) {
		Container c = ctx.getRootContainer();
		c.removeAll();
		c.add(new PuzzlePanel(puzzle, ctx));
		
//		//load solution state of the panel
//		try {
//			model.loadSolutionState(dir);
//		} catch (IOException ioe) {
//			//TODO: log?
//		}

	}
		
	public void navigateToCatalog() {
		Container c = ctx.getRootContainer();
		c.removeAll();
		c.add(new CatalogPanel(this, ctx, catalogDir));
	}

	public File getCatalogDir() {
		return catalogDir;
	}
	
	
		

}