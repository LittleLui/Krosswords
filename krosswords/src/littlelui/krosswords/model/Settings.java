package littlelui.krosswords.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/** Settings; where Preferences are stored.
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
public class Settings implements Serializable {
	private static final long serialVersionUID = 2L;

	private boolean autoDownload;
	private Set/*<String>*/ enabledFetchers = new HashSet();
	
	private transient File settingsFile;
	private transient boolean isNew = false;

	
	public int getCrosswordPanelKeyFontSize() {
		return 5;
	}
	
	public int getCrosswordPanelSolutionFontSize() {
		return 11;
	}
	
	public int getListPanelTitleFontSize() {
		return 11;
	}
	
	public int getListPanelSmallFontSize() {
		return 9;
	}
	
	public int getSettingsPanelTitleFontSize() {
		return 24;
	}

	
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
