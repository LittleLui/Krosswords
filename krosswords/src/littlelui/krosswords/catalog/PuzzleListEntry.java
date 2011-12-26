package littlelui.krosswords.catalog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class PuzzleListEntry implements Serializable {
	private String id;
	private String name;
	private String provider;
	private boolean solutionAvailable;
	private boolean downloaded;
	private Map attributes = new HashMap();
	
	public PuzzleListEntry(String id, String name, String provider, boolean solutionAvailable, boolean downloaded) {
		super();
		this.id = id;
		this.name = name;
		this.provider = provider;
		this.solutionAvailable = solutionAvailable;
		this.downloaded = downloaded;
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
	
	public boolean isDownloaded() {
		return downloaded;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public void setSolutionAvailable(boolean solutionAvailable) {
		this.solutionAvailable = solutionAvailable;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}
	
	public String getAttribute(String key) {
		return (String)attributes.get(key);
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	

}
