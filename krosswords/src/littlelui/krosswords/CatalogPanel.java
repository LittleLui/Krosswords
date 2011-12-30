package littlelui.krosswords;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.fetch.DerStandardFetcher;
import littlelui.krosswords.model.Puzzle;

import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.event.GestureDispatcher;
import com.amazon.kindle.kindlet.input.Gestures;
import com.amazon.kindle.kindlet.ui.KPages;
import com.amazon.kindle.kindlet.ui.pages.PageProviders;

public class CatalogPanel extends KPages {
	private File catalogDir;
	private DerStandardFetcher dsf = new DerStandardFetcher();
	
	private Main main;

	
	public CatalogPanel(Main main, KindletContext ctx, File catalogDir) {
        super(PageProviders.createBoxLayoutProvider(BoxLayout.Y_AXIS));
        this.main = main;
		this.catalogDir = catalogDir;
		
		new Thread() {
			public void run() {
				List/*<PuzzleListEntry>*/ ples = loadFromDiskOrNetwork(); 
				
				Iterator i = ples.iterator();
				while (i.hasNext()) {
					PuzzleListEntry ple = (PuzzleListEntry)i.next();
					
					if (ple.getPuzzleDownloadState() != PuzzleListEntry.DOWNLOADED) {
						Puzzle p = dsf.fetchPuzzle(ple);
					}
					
					//TODO: also download solutions
				}
			}
		}.start();
		
		
	}


	//TODO: this should also look on the net (after a day or so) even if files exist so the list gets longer (and also a solution might have been added to a recent puzzle)
	public List /*<PuzzleListEntry>*/ loadFromDiskOrNetwork() {
		List r = new LinkedList();
		
		File[] files = catalogDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".puzzle");
			}
		});
		
		
		
		if (files.length == 0) {
			try {
				List puzzleIds = dsf.fetchAvailablePuzzleIds(null);
				Iterator iPLEs = puzzleIds.iterator();
				while (iPLEs.hasNext()) {
					final PuzzleListEntry ple = (PuzzleListEntry)iPLEs.next();
					r.add(ple);
					
					addItem(createItemPanel(ple)); 
					
					
					
					try {
						File f = new File(catalogDir, ple.getFileName());
						FileOutputStream fos = new FileOutputStream(f);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(ple);
						oos.close();
					} catch (IOException ioe) {
						//TODO
					}
				}
				validate();
			} catch (Throwable t) {
				System.out.println(t);
			}
		} else {
			final List puzzles = new LinkedList();
			
			for (int i=0; i<files.length; i++) {
				File f = files[i];
				try {
					FileInputStream fis = new FileInputStream(f);
					ObjectInputStream ois = new ObjectInputStream(fis);
					PuzzleListEntry p = (PuzzleListEntry)ois.readObject();
					puzzles.add(p);
				} catch (IOException e) {
					//TODO
				} catch (ClassNotFoundException cnfe) {
					//TODO
				}
			}
			
			r.addAll(puzzles);
			
			removeAllItems(); //TODO KPaged
			Iterator iPuzzles = puzzles.iterator();
			while (iPuzzles.hasNext()) {
				PuzzleListEntry p = (PuzzleListEntry)iPuzzles.next();
				addItem(createItemPanel(p)); //TODO KPaged
			}
			validate(); 

		}		

		return r;
	}


	private JComponent createItemPanel(final PuzzleListEntry ple) {
		PuzzleListEntryPanel plep = new PuzzleListEntryPanel(ple);
		plep.addMouseListener(new GestureDispatcher());
		plep.getActionMap().put(Gestures.ACTION_TAP, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Puzzle p = ple.getPuzzle();
				
				if (p != null)
					main.navigateToPuzzle(p);
			}
		});
		
		return plep;
	}
}
