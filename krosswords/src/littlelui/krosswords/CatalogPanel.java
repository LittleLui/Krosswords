package littlelui.krosswords;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import littlelui.krosswords.catalog.Catalog;
import littlelui.krosswords.catalog.CatalogListener;
import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Settings;

import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.event.GestureDispatcher;
import com.amazon.kindle.kindlet.input.Gestures;
import com.amazon.kindle.kindlet.ui.KPages;
import com.amazon.kindle.kindlet.ui.pages.PageProviders;

/** Root UI for the Catalog View.
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
 **/
public class CatalogPanel extends KPages implements CatalogListener {
	
	private final Main main;
	private final Catalog catalog;
	private final Settings settings;

	
	public CatalogPanel(Main main, Settings settings, KindletContext ctx, final Catalog catalog) {
        super(PageProviders.createBoxLayoutProvider(BoxLayout.Y_AXIS));
        this.main = main;
        this.catalog = catalog;
        this.settings = settings;
        
        catalog.addListener(this);
		
        Iterator i = catalog.getEntries().iterator();
        while (i.hasNext()) {
        	PuzzleListEntry ple = (PuzzleListEntry)i.next();
        	final JComponent jc = createItemPanel(ple, settings);
           	addItem(jc); 
        }
	}


	

	private JComponent createItemPanel(final PuzzleListEntry ple, Settings settings) {
		PuzzleListEntryPanel plep = new PuzzleListEntryPanel(ple, settings);
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
		JComponent jc = createItemPanel(entry, settings);
		if (index >= getComponentCount())
			addItem(jc);
		else
			addItem(jc, index);
		
		validate();
	}
}
