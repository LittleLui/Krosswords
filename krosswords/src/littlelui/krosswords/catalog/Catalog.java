package littlelui.krosswords.catalog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import littlelui.krosswords.Main;

public class Catalog {
	private File catalogDir;
	
	private final SortedSet/*<PuzzleListEntry>*/ entries = new TreeSet();
	
	private List listeners = new LinkedList();
	
	public Catalog(File catalogDir) {
		super();
		this.catalogDir = catalogDir;

		new Thread() {
			public void run() {
				loadEntriesFromDisk();
			}
		}.start();
	}
	
	//threadsafe because it uses add() for manipulation
	private void loadEntriesFromDisk() {
		File[] files = catalogDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".puzzle");
			}
		});
		
		for (int i=0; i<files.length; i++) {
			File f = files[i];
			try {
				PuzzleListEntry p = PuzzleListEntry.unpersist(f);
				add(p);
			} catch (Exception e) {
				Main.getInstance().logError("Unable to unpersist file "+f.getAbsolutePath(), e);
			}
		}
	}
	
	public void addListener(CatalogListener l) {
		listeners.add(l);
	}
	
	public void removeListener(CatalogListener l) {
		listeners.remove(l);
	}

	//synced to prevent concurrent modification of entries
	public synchronized List getEntries() {
		return new ArrayList(entries);
	}

	//synced to prevent concurrent modification of entries
	public synchronized void addAll(Collection/*<PuzzleListEntry>*/ ples) {
		Iterator i = ples.iterator();
		while (i.hasNext()) {
			PuzzleListEntry ple = (PuzzleListEntry) i.next();
			add(ple);
		}
	}

	private synchronized void add(PuzzleListEntry ple) {
		boolean c = entries.add(ple);
		if (c) {
			int index = getEntries().indexOf(ple);
			fireAdded(ple, index);
		}
	}

	private void fireAdded(PuzzleListEntry ple, int index) {
		Iterator i = listeners.iterator();
		
		while (i.hasNext()) {
			CatalogListener cl = (CatalogListener)i.next();
			cl.entryAdded(ple, index);
		}
	}

	public synchronized PuzzleListEntry getPuzzleById(String id) {
		if (id == null)
			return null;
		
		Iterator i = entries.iterator();
		while (i.hasNext()) {
			PuzzleListEntry ple = (PuzzleListEntry) i.next();
			if (ple.getId().equals(id))
				return ple;
		}

		return null;
	}



}
