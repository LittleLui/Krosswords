package littlelui.krosswords;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Word;

import com.amazon.kindle.kindlet.input.Gestures;
import com.amazon.kindle.kindlet.ui.KPages;
import com.amazon.kindle.kindlet.ui.pages.PageProviders;

/** The panel where all the hints are listed in the puzzle view.
 * 
 * @author LittleLui
 * 
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
public class HintsPanel extends KPages {
	private final Puzzle model;
	
	public HintsPanel(Puzzle model) {
        super(PageProviders.createBoxLayoutProvider(BoxLayout.Y_AXIS));
        this.model = model;
        addHints(this);
        first();
	}

	private void doAdd(Container cont, Component comp) {
		if (cont instanceof KPages) 
			((KPages)cont).addItem(comp);
		else
			cont.add(comp);
	}

	private void addHints(Container c) {
		addLabel(c, "Horizontal");

        Iterator/*<Word>*/ i = model.getHorizontalWords().iterator();
        int location = 1; //horizontal label already there
        while (i.hasNext()) {
        	Word w = (Word)i.next();
        	JComponent jta = createHintText(w);
        	doAdd(c, jta);
        	w.setLocation(location++);
        }
        
        i = model.getVerticalWords().iterator();
		
        doAdd(c, new JLabel(" "));
		addLabel(c, "Vertikal");
		location ++;
		
        while (i.hasNext()) {
        	Word w = (Word)i.next();
        	JComponent jta = createHintText(w);
        	doAdd(c, jta);
        	w.setLocation(location++);
        }
	}

	private void addLabel(Container c, String string) {
		JTextArea l = new JTextArea(string);
		l.setEditable(false);
		l.setEnabled(false); //should still allow mouse clicks
		l.setBorder(null);
		l.setDisabledTextColor(Color.BLACK);
		
		doAdd(c, l);
	}

	private JComponent createHintText(final Word w) {
		JLabel llNr = new JLabel(w.getKey()+" ");
		llNr.setForeground(Color.DARK_GRAY);
		JPanel lNr = new JPanel();
		lNr.setLayout(new BoxLayout(lNr, BoxLayout.Y_AXIS));
		lNr.add(llNr);

		JTextArea jta = new JTextArea(w.getHint());
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jta.setEditable(false);
		jta.setEnabled(false); //should still allow mouse clicks
		jta.setBorder(null);
		jta.setDisabledTextColor(Color.DARK_GRAY);

		 Action tap = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				cp.stopEditing();
				cp.startEditing(w);
			}
		 };

		 ActionMap actionMap = jta.getActionMap();
		 actionMap.put(Gestures.ACTION_TAP, tap);
		
		JPanel p = new JPanel(new BorderLayout());

		p.add(lNr, BorderLayout.WEST);
		p.add(jta, BorderLayout.CENTER);
		
		return p;
	}

	private CrosswordPanel cp;
	public void setCrosswordPanel(CrosswordPanel cp) {
		this.cp = cp;
	}

}
