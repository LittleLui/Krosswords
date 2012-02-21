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

/** The catalog; a list of all puzzles we know about. 
 * 
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
public class Catalog {
	private File catalogDir;
	
	private final SortedSet/*<PuzzleListEntry>*/ entries = new TreeSet();
	
	private List listeners = new LinkedList();
	
	public Catalog(File catalogDir) {
		super();
		this.catalogDir = catalogDir;

		new Thread("Krosswords.Catalog.LoadEntriesFromDisk") {
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
