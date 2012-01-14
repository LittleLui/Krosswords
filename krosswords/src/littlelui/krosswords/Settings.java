package littlelui.krosswords;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Settings implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean autoDownload;
	private Set/*<String>*/ enabledFetchers = new HashSet();
	
	private transient File settingsFile;
	private transient boolean isNew = false;

	public boolean isAutoDownload() {
		return autoDownload;
	}
	
	public void setAutoDownload(boolean autoDownload) {
		this.autoDownload = autoDownload;
	}
	
	public Set/*<String>*/ getEnabledFetchers() {
		return enabledFetchers;
	}
	
	public void setEnabledFetchers(Set enabledFetchers) {
		this.enabledFetchers = enabledFetchers;
	}

	
	
	public boolean isNew() {
		return isNew;
	}

	public void save() {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(settingsFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
		} catch (IOException ioe) {
			//ignore
		} finally {
			if (fos != null) try {
			  fos.close();
			} catch (IOException ioe) {}
		}
		
	}
	
	
	public static Settings load(File stateDir) {
		File settingsFile = new File(stateDir, "settings");
		Settings s = null;
		
		FileInputStream fis = null;
		if (settingsFile.exists()) try {
			fis = new FileInputStream(settingsFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			s = (Settings)ois.readObject();
		} catch (Exception ioe) {
			s = new Settings();
			s.isNew = true;
		} finally {
			if (fis != null) try {
				fis.close();
			} catch (IOException ioe) {}
		} else {
			s = new Settings();
			s.isNew = true;
		}
		
		s.settingsFile = settingsFile;
		
		return s;
	}	

}
