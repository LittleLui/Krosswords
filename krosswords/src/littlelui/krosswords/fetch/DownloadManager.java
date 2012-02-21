package littlelui.krosswords.fetch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import littlelui.krosswords.Main;
import littlelui.krosswords.catalog.Catalog;
import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.catalog.PuzzleSolution;
import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Settings;

import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.net.Connectivity;
import com.amazon.kindle.kindlet.net.ConnectivityHandler;
import com.amazon.kindle.kindlet.net.NetworkDisabledDetails;

/** Manages downloads from the internet. No shit! 
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
public class DownloadManager implements ConnectivityHandler {
	//map from provider-name to fetcher.
	public static final Map/*<String, Fetcher>*/ fetchers = new HashMap();
	{
		fetchers.put("derStandard.at", new DerStandardFetcher());
		fetchers.put("think.com", new ThinksDotComFetcher());
	}
	
	private final Connectivity connectivity;
	private final KindletContext ctx;
	private final Catalog catalog;
	
	private Thread downloadThread;
	
	private DownloadRunnable downloadRunnable = new DownloadRunnable();
	private Settings settings;


	public DownloadManager(Connectivity connectivity, KindletContext ctx, Catalog catalog, Settings settings) {
		this.connectivity = connectivity;
		this.ctx = ctx;
		this.catalog = catalog;
		this.settings = settings;
	}
	
	public void start() {
		if (settings.isAutoDownload() && connectivity.isConnected()) {
			requestConnectionIfWorkToDo(false);
		}
	}

	public void configure(Settings settings) {
		this.settings = settings;
		if (settings.isAutoDownload()) {
			if (downloadRunnable == null) {
				requestConnectionIfWorkToDo(false);
			}
		} else {
			if (downloadRunnable != null) {
				downloadRunnable.stop();
			}
			
		}
	}
	
	public void requestConnectionIfWorkToDo(boolean allowPrompting) {
		//TODO: only if work to do
		connectivity.submitSingleAttemptConnectivityRequest(this, allowPrompting);
	}

	public void connected() throws InterruptedException {
		//we've gone online and can now do stuff. like start a download thread.
		if (downloadThread == null) {
			downloadThread = new Thread(downloadRunnable, "Krosswords.DownloadManager");
			downloadThread.start();
		}
	}
	

	public void disabled(NetworkDisabledDetails arg0) throws InterruptedException {
		//we've gone offline.
		downloadRunnable.stop();
	}

	
	private boolean shouldUpdateLists() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void updateLists() {
		Collection known = catalog.getEntries();
		
		Iterator fs = fetchers.keySet().iterator();
		while (fs.hasNext()) {
			String s = (String)fs.next();
			if (settings.getEnabledFetchers().contains(s)) {
				Fetcher f = (Fetcher)fetchers.get(s);
				Collection addedOrChanged = f.fetchAvailablePuzzleIds(known);
				
				catalog.addAll(addedOrChanged);
			}
		}
	}

	private void fetchSinglePuzzleAndUpdate(PuzzleListEntry ple) {
		Fetcher f = (Fetcher)fetchers.get(ple.getProvider());
		
		if (f == null) {
			ple.setPuzzleDownloadState(PuzzleListEntry.DOWNLOAD_FAILED);
			ple.setAttribute("error", "No fetcher, we only have fetchers for "+Arrays.toString(fetchers.keySet().toArray()));
			return;
		}

		try {
			ple.setPuzzleDownloadState(PuzzleListEntry.DOWNLOADING);

			Puzzle p = f.fetchPuzzle(ple);
			
			if (p != null) {
				ple.setPuzzle(p);
				ple.setPuzzleDownloadState(PuzzleListEntry.DOWNLOADED);
			} else {
				ple.setPuzzleDownloadState(PuzzleListEntry.DOWNLOAD_FAILED);
				ple.setAttribute("error", "Fetcher didn't deliver.");
			}

		} catch (Throwable t) {
			setFailedDownloadError(ple, t);
		}
	}

	public static void setFailedDownloadError(PuzzleListEntry ple, Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		ple.setAttribute("error", t.getMessage()+"\n"+sw.toString());
		ple.setPuzzleDownloadState(PuzzleListEntry.DOWNLOAD_FAILED);
	}
	
	private void fetchSingleSolutionAndUpdate(PuzzleListEntry ple) {
		Fetcher f = (Fetcher)fetchers.get(ple.getProvider());
		
		if (f == null) {
			ple.setSolutionDownloadState(PuzzleListEntry.DOWNLOAD_FAILED);
			ple.setAttribute("error", "No fetcher, we only have fetchers for "+Arrays.toString(fetchers.keySet().toArray()));
			return;
		}

		try {
			ple.setSolutionDownloadState(PuzzleListEntry.DOWNLOADING);

			PuzzleSolution ps = f.fetchSolution(ple);
			
			if (ps != null) {
				ple.setExpectedSolution(ps);
				ple.setSolutionDownloadState(PuzzleListEntry.DOWNLOADED);
			} else {
				ple.setSolutionDownloadState(PuzzleListEntry.DOWNLOAD_FAILED);
				ple.setAttribute("error", "Fetcher didn't deliver.");
			}

		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			ple.setAttribute("error", t.getMessage()+"\n"+sw.toString());
			ple.setSolutionDownloadState(PuzzleListEntry.DOWNLOAD_FAILED);
		}
	}	
	
	class DownloadRunnable implements Runnable {
		private transient boolean running = true;
		
		public void run() {
			try {
				running = true;
				
				if (shouldUpdateLists()) {
					ctx.setSubTitle("Updating");
					updateLists();
				} 
				
				Iterator/*<PuzzleListEntry>*/ entries = catalog.getEntries().iterator();
				
				ctx.setSubTitle("Downloading");

				while(running && entries.hasNext()) {
					PuzzleListEntry ple = (PuzzleListEntry)entries.next();
					
					if (settings.getEnabledFetchers().contains(ple.getProvider())) {
						if (ple.getPuzzleDownloadState() != PuzzleListEntry.DOWNLOADED) {
							fetchSinglePuzzleAndUpdate(ple);
						}
						if (ple.getSolutionDownloadState() != PuzzleListEntry.DOWNLOADED) {
							fetchSingleSolutionAndUpdate(ple);
						}
					}
				}
			} catch (Exception t) {
				Main.getInstance().logError("Error in DownloadManager", t);
			} finally {
				ctx.setSubTitle(null);	
			}
			
		}
		


		public void stop() {
			running = false;
		}
		
	}



}


