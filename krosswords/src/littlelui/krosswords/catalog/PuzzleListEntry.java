package littlelui.krosswords.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import littlelui.krosswords.Main;
import littlelui.krosswords.model.Puzzle;

public final class PuzzleListEntry implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	public static final int NOT_DOWNLOADED = 0;
	public static final int DOWNLOADING = 1;
	public static final int DOWNLOADED = 2;
	public static final int DOWNLOAD_FAILED = 3;
	
	public static final int NOT_PLAYED = 0;
	public static final int IN_PROGRESS = 1;
	public static final int FINISHED = 2;
	public static final int FINISHED_VERIFIED_OK = 3;
	public static final int FINISHED_VERIFIED_BAD = 4;
	
	private String id;
	private String name;
	private String provider;
	private boolean solutionAvailable;
	private int puzzleDownloadState;
	private int solutionDownloadState;
	private int puzzleSolutionState;
	
	private Date lastPlayed;
	
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
		persist();
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

	public Date getLastPlayed() {
		return lastPlayed;
	}

	public void setLastPlayed(Date lastPlayed) {
		this.lastPlayed = lastPlayed;
		if (this.puzzleSolutionState == NOT_PLAYED) {
			this.puzzleSolutionState = IN_PROGRESS;
		}
		persist();
		fireChange();
	}

	public void setSolutionAvailable(boolean solutionAvailable) {
		this.solutionAvailable = solutionAvailable;
		persist();
		fireChange();
	}

	public int getPuzzleDownloadState() {
		return puzzleDownloadState;
	}

	public void setPuzzleDownloadState(int puzzleDownloadState) {
		this.puzzleDownloadState = puzzleDownloadState;
		
		if (puzzleDownloadState != DOWNLOADING)
			persist();
		
		fireChange();
	}

	public int getSolutionDownloadState() {
		return solutionDownloadState;
	}

	public void setSolutionDownloadState(int solutionDownloadState) {
		this.solutionDownloadState = solutionDownloadState;
		
		if (solutionDownloadState != DOWNLOADING)
			persist();
		
		fireChange();
	}

	public int getPuzzleSolutionState() {
		return puzzleSolutionState;
	}

	public void setPuzzleSolutionState(int puzzleSolutionState) {
		this.puzzleSolutionState = puzzleSolutionState;
		persist();
		fireChange();
	}

	public String getAttribute(String key) {
		return (String)attributes.get(key);
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	
	public Puzzle getPuzzle() {
		return puzzle;
	}

	public void setPuzzle(Puzzle puzzle) {
		this.puzzle = puzzle;
		persist();
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

	private synchronized void persist() {
		Main m = Main.getInstance();
		
		File f = new File(m.getCatalogDir(), getFileName());
		ObjectOutputStream oos = null;
		try {
			FileOutputStream fos = new FileOutputStream(f);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			if (puzzle != null)
				puzzle.saveSolutionState(oos);
		} catch (IOException ioe) {
			System.out.println(ioe);
			//TODO: what to do?
		} finally {
			if (oos != null) try {
				oos.close();
			} catch (IOException ioe) {
				//really, nothing to do here
			}
		}
	}
	

	public static PuzzleListEntry unpersist(File f) throws FileNotFoundException, IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		PuzzleListEntry p = (PuzzleListEntry)ois.readObject();
		if (p.getPuzzle() != null)
			p.getPuzzle().loadSolutionState(ois);
		return p;
	}


	public interface Listener {
		public void changed(PuzzleListEntry ple);
	}

	public String getFileName() {
		return getProvider()+"___"+getId()+".puzzle";
	}

	public int compareTo(Object o) {
		PuzzleListEntry other = (PuzzleListEntry)o;
		
		if (this.puzzleSolutionState == IN_PROGRESS && other.puzzleSolutionState != IN_PROGRESS)
			return -1;
		
		if (this.puzzleSolutionState != IN_PROGRESS && other.puzzleSolutionState == IN_PROGRESS)
			return 1;
		
		if (this.puzzleSolutionState == IN_PROGRESS && other.puzzleSolutionState == IN_PROGRESS) {
			if (other.lastPlayed != null && lastPlayed != null)
				return other.lastPlayed.compareTo(this.lastPlayed);
		}
		
		int r = other.id.compareTo(id);
		
		if (r != 0)
			return r;
		
		return System.identityHashCode(this) - System.identityHashCode(other);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((lastPlayed == null) ? 0 : lastPlayed.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + ((puzzle == null) ? 0 : puzzle.hashCode());
		result = prime * result + puzzleDownloadState;
		result = prime * result + puzzleSolutionState;
		result = prime * result + (solutionAvailable ? 1231 : 1237);
		result = prime * result + solutionDownloadState;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PuzzleListEntry other = (PuzzleListEntry) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastPlayed == null) {
			if (other.lastPlayed != null)
				return false;
		} else if (!lastPlayed.equals(other.lastPlayed))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (provider == null) {
			if (other.provider != null)
				return false;
		} else if (!provider.equals(other.provider))
			return false;
		if (puzzle == null) {
			if (other.puzzle != null)
				return false;
		} else if (!puzzle.equals(other.puzzle))
			return false;
		if (puzzleDownloadState != other.puzzleDownloadState)
			return false;
		if (puzzleSolutionState != other.puzzleSolutionState)
			return false;
		if (solutionAvailable != other.solutionAvailable)
			return false;
		if (solutionDownloadState != other.solutionDownloadState)
			return false;
		return true;
	}

	public void setExpectedSolution(PuzzleSolution ps) {
		if (puzzle != null)
			ps.fillInto(puzzle);
	}

	public void verify() {
		int state = puzzle.verify(); 
		
		if (state == Puzzle.VERIFY_FINISHED_CORRECT) {
			puzzleSolutionState = FINISHED_VERIFIED_OK;
		} else if (state == Puzzle.VERIFY_FINISHED_BAD) {
			puzzleSolutionState = FINISHED_VERIFIED_BAD;
		}
	}

	
	
	

}
