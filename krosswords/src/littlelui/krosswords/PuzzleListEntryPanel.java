package littlelui.krosswords;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.catalog.PuzzleListEntry.Listener;

public class PuzzleListEntryPanel extends JPanel {
	private PuzzleListEntry entry;

	private Font F_TITLE = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
	private Font F_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
	
	public PuzzleListEntryPanel(PuzzleListEntry entry) {
		super();
		this.entry = entry;
		
		setLayout(new BorderLayout(2, 4));
		setBorder(BorderFactory.createMatteBorder(1, 0, 1,0,Color.DARK_GRAY));
		
		final JLabel lTitle = new JLabel(entry.getName());
		final JLabel lOrigin = new JLabel(entry.getProvider());
		final JLabel lSummary = new JLabel(summarize(entry));
		
		entry.addListener(new Listener() {
			public void changed(PuzzleListEntry ple) {
				lTitle.setText(ple.getName());
				lOrigin.setText(ple.getProvider());
				lSummary.setText(summarize(ple));
			}
		});
		
		lTitle.setFont(F_TITLE);
		lOrigin.setFont(F_SMALL);
		lSummary.setFont(F_SMALL);
		
		add(lTitle, BorderLayout.CENTER);
		add(lOrigin, BorderLayout.EAST);
		add(lSummary, BorderLayout.SOUTH);
	}

	private String summarize(PuzzleListEntry e) {
		String s = "";
		
		String pd = getDownloadStateString(e.getPuzzleDownloadState());
		
		s += pd;
		
		if (e.isSolutionAvailable()) {
			String sd = getDownloadStateString(e.getSolutionDownloadState());
			s += " - Solution: "+sd;
		}
		
		String pl = getSolutionStateString(e.getPuzzleSolutionState());
		s+= " - " + pl;
			
		return s;
	}

	private String getDownloadStateString(int state) {
		switch (state) {
			case PuzzleListEntry.NOT_DOWNLOADED: return "not downloaded" ; 
			case PuzzleListEntry.DOWNLOADING: return "downloading..." ; 
			case PuzzleListEntry.DOWNLOADED: return "available to play" ; 
			case PuzzleListEntry.DOWNLOAD_FAILED: return "download error!" ; 
		}
		return "unknown";
	}

	private String getSolutionStateString(int puzzleSolutionState) {
		switch (puzzleSolutionState) {
			case PuzzleListEntry.NOT_PLAYED: return "New";
			case PuzzleListEntry.IN_PROGRESS: return "In Progress";
			case PuzzleListEntry.FINISHED: return "Solved";
			case PuzzleListEntry.FINISHED_VERIFIED: return "Solved and Checked";
		}
		
		return "unknown";
	}
	
	

}
