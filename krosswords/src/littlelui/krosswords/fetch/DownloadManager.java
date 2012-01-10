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

import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.net.Connectivity;
import com.amazon.kindle.kindlet.net.ConnectivityHandler;
import com.amazon.kindle.kindlet.net.NetworkDisabledDetails;

public class DownloadManager implements ConnectivityHandler {
	//map from provider-name to fetcher.
	private final Map/*<String, Fetcher>*/ fetchers = new HashMap();
	{
		fetchers.put("derStandard.at", new DerStandardFetcher());
		fetchers.put("think.com", new ThinksDotComFetcher());
	}
	
	private final Connectivity connectivity;
	private final KindletContext ctx;
	private final Catalog catalog;
	
	private Thread downloadThread;
	
	private DownloadRunnable downloadRunnable = new DownloadRunnable();


	public DownloadManager(Connectivity connectivity, KindletContext ctx, Catalog catalog) {
		this.connectivity = connectivity;
		this.ctx = ctx;
		this.catalog = catalog;
	}
	
	public void start() {
		if (connectivity.isConnected()) {
			requestConnectionIfWorkToDo();
		}
	}

	private void requestConnectionIfWorkToDo() {
		//TODO: only if work to do
		//TODO: only if no playable puzzles are here do we want to actively prompt the user
		boolean allowPrompting = false;
		
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
		
		Iterator fs = fetchers.values().iterator();
		while (fs.hasNext()) {
			Fetcher f = (Fetcher)fs.next();
			Collection addedOrChanged = f.fetchAvailablePuzzleIds(known);
			
			catalog.addAll(addedOrChanged);
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
					if (ple.getPuzzleDownloadState() != PuzzleListEntry.DOWNLOADED) {
						fetchSinglePuzzleAndUpdate(ple);
					}
					if (ple.getSolutionDownloadState() != PuzzleListEntry.DOWNLOADED) {
						fetchSingleSolutionAndUpdate(ple);
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
		
	};

}


