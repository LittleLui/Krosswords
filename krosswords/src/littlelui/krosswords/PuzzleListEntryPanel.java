package littlelui.krosswords;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.catalog.PuzzleListEntry.Listener;

/** The view of a single puzzle in the catalog view.
 *  
 * @author LittleLui
 *
 */
public class PuzzleListEntryPanel extends JPanel {
	private PuzzleListEntry entry;

	private Font F_TITLE = new Font(Font.SANS_SERIF, Font.PLAIN, 22);
	private Font F_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
	
	private static Icon loadIcon(String name) {
		return new ImageIcon(PuzzleListEntryPanel.class.getResource("/icons/"+name+"_24.png"));
	}
	
	private static final Icon I_DL_NOT = loadIcon("tray");
	private static final Icon I_DL_ING = loadIcon("inbox");
	private static final Icon I_DL_OK = loadIcon("tray_full");
	private static final Icon I_DL_ERR = loadIcon("warning");
	
	private static final Icon I_S_NEW = loadIcon("bulb_off");
	private static final Icon I_S_PROGRESS = loadIcon("bulb_on");
	private static final Icon I_S_SOLVED = loadIcon("checkmark");
	
	private static final Icon I_C_GOOD = loadIcon("comment_check");
	private static final Icon I_C_BAD = loadIcon("comment_delete");

	public PuzzleListEntryPanel(PuzzleListEntry entry) {
		super();
		this.entry = entry;
		
		setLayout(new BorderLayout(8, 6));
		setBorder(BorderFactory.createMatteBorder(1, 0, 1,0,Color.DARK_GRAY));
		
		final JLabel lTitle = new JLabel(entry.getName());
		final JLabel lOrigin = new JLabel(entry.getProvider());
		final JLabel lDownload = new JLabel(getDownloadIcon(entry.getPuzzleDownloadState()));
		final JLabel lSolutionDownload = new JLabel(getDownloadIcon(entry.getSolutionDownloadState()));
		final JLabel lSolving = new JLabel(getSolvingIcon());
		
		entry.addListener(new Listener() {
			public void changed(PuzzleListEntry ple) {
				lTitle.setText(ple.getName());
				lOrigin.setText(ple.getProvider());
				lDownload.setIcon(getDownloadIcon(ple.getPuzzleDownloadState()));
				lSolutionDownload.setIcon(getDownloadIcon(ple.getSolutionDownloadState()));
				lSolving.setIcon(getSolvingIcon());
				validate();
				repaint();
			}
		});
		
		lTitle.setFont(F_TITLE);
		lOrigin.setFont(F_SMALL);
		
		add(lTitle, BorderLayout.CENTER);
		add(lOrigin, BorderLayout.EAST);
		
		JPanel pSummary = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pSummary.add(lDownload);
		pSummary.add(lSolutionDownload);
		pSummary.add(lSolving);
		add(pSummary, BorderLayout.SOUTH);
	}

	private Icon getSolvingIcon() {
		switch (entry.getPuzzleSolutionState()) {
			case PuzzleListEntry.NOT_PLAYED : return I_S_NEW;
			case PuzzleListEntry.IN_PROGRESS : return I_S_PROGRESS;
			case PuzzleListEntry.FINISHED : return I_S_SOLVED;
			case PuzzleListEntry.FINISHED_VERIFIED_OK : return I_C_GOOD;
			case PuzzleListEntry.FINISHED_VERIFIED_BAD : return I_C_BAD;
			default : return null;
		}
	}

	private Icon getDownloadIcon(int downloadState) {
		switch (downloadState) {
			case PuzzleListEntry.NOT_DOWNLOADED : return I_DL_NOT;
			case PuzzleListEntry.DOWNLOADING: return I_DL_ING;
			case PuzzleListEntry.DOWNLOADED : return I_DL_OK;
			case PuzzleListEntry.DOWNLOAD_FAILED: return I_DL_ERR;
			default : return null;
		}
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
			case PuzzleListEntry.FINISHED_VERIFIED_OK: return "Solved and Checked";
			case PuzzleListEntry.FINISHED_VERIFIED_BAD: return "Solved and Checked (but wrong!)";
		}
		
		return "unknown";
	}
	
	

}
