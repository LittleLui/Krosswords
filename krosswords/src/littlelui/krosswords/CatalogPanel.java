package littlelui.krosswords;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import littlelui.krosswords.catalog.Catalog;
import littlelui.krosswords.catalog.CatalogListener;
import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.model.Puzzle;

import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.event.GestureDispatcher;
import com.amazon.kindle.kindlet.input.Gestures;
import com.amazon.kindle.kindlet.ui.KPages;
import com.amazon.kindle.kindlet.ui.pages.PageProviders;

public class CatalogPanel extends KPages implements CatalogListener {
	
	private final Main main;
	private final Catalog catalog;

	
	public CatalogPanel(Main main, KindletContext ctx, final Catalog catalog) {
        super(PageProviders.createBoxLayoutProvider(BoxLayout.Y_AXIS));
        this.main = main;
        this.catalog = catalog;
        
        catalog.addListener(this);
		
        Iterator i = catalog.getEntries().iterator();
        while (i.hasNext()) {
        	PuzzleListEntry ple = (PuzzleListEntry)i.next();
        	final JComponent jc = createItemPanel(ple);
           	addItem(jc); 
        }
	}


	

	private JComponent createItemPanel(final PuzzleListEntry ple) {
		PuzzleListEntryPanel plep = new PuzzleListEntryPanel(ple);
		plep.addMouseListener(new GestureDispatcher());
		plep.getActionMap().put(Gestures.ACTION_TAP, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Puzzle p = ple.getPuzzle();
				
				if (p != null)
					main.navigateToPuzzle(ple);
			}
		});
		
		return plep;
	}




	public void entryAdded(PuzzleListEntry entry, int index) {
		JComponent jc = createItemPanel(entry);
		if (index >= getComponentCount())
			addItem(jc);
		else
			addItem(jc, index);
		
		validate();
	}
}
