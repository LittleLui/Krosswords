package littlelui.krosswords.catalog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import littlelui.krosswords.model.Puzzle;

public final class PuzzleListEntry implements Serializable {
	public static final int NOT_DOWNLOADED = 0;
	public static final int DOWNLOADING = 1;
	public static final int DOWNLOADED = 2;
	public static final int DOWNLOAD_FAILED = 3;
	
	public static final int NOT_PLAYED = 0;
	public static final int IN_PROGRESS = 1;
	public static final int FINISHED = 2;
	public static final int FINISHED_VERIFIED = 3;
	
	private String id;
	private String name;
	private String provider;
	private boolean solutionAvailable;
	private int puzzleDownloadState;
	private int solutionDownloadState;
	private int puzzleSolutionState;
	
	private Puzzle puzzle;

	private Map attributes = new HashMap();
	
	private transient List listeners = new LinkedList();
	
	public PuzzleListEntry(String id, String name, String provider, boolean solutionAvailable, int puzzleDownloadState, int solutionDownloadState, int puzzleSolutionState) {
		super();
		this.id = id;
		this.name = name;
		this.provider = provider;
		this.solutionAvailable = solutionAvailable;
		this.puzzleDownloadState = puzzleDownloadState;
		this.solutionDownloadState = solutionDownloadState;
		this.puzzleSolutionState = puzzleSolutionState;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public boolean isSolutionAvailable() {
		return solutionAvailable;
	}
	
	public void setName(String name) {
		this.name = name;
		fireChange();
	}

	public void setProvider(String provider) {
		this.provider = provider;
		fireChange();
	}

	public void setSolutionAvailable(boolean solutionAvailable) {
		this.solutionAvailable = solutionAvailable;
		fireChange();
	}

	public int getPuzzleDownloadState() {
		return puzzleDownloadState;
	}

	public void setPuzzleDownloadState(int puzzleDownloadState) {
		this.puzzleDownloadState = puzzleDownloadState;
		fireChange();
	}

	public int getSolutionDownloadState() {
		return solutionDownloadState;
	}

	public void setSolutionDownloadState(int solutionDownloadState) {
		this.solutionDownloadState = solutionDownloadState;
		fireChange();
	}

	public int getPuzzleSolutionState() {
		return puzzleSolutionState;
	}

	public void setPuzzleSolutionState(int puzzleSolutionState) {
		this.puzzleSolutionState = puzzleSolutionState;
		fireChange();
	}

	public String getAttribute(String key) {
		return (String)attributes.get(key);
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
		fireChange();
	}
	
	
	public Puzzle getPuzzle() {
		return puzzle;
	}

	public void setPuzzle(Puzzle puzzle) {
		this.puzzle = puzzle;
	}


	
	
	public void addListener(Listener l) {
		if (listeners == null)
			listeners = new LinkedList();
		listeners.add(l);
	}
	
	public void removeListener(Listener l) {
		if (listeners == null)
			return;

		listeners.remove(l);
	}
	
	public void fireChange() {
		if (listeners == null)
			return;
		
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			((Listener)i.next()).changed(this);
		}
	}

	public interface Listener {
		public void changed(PuzzleListEntry ple);
	}


}
