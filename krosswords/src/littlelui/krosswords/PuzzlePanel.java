package littlelui.krosswords;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import littlelui.krosswords.model.Puzzle;

import com.amazon.kindle.kindlet.KindletContext;

/** The Root UI of the Puzzle View.
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
public class PuzzlePanel extends JPanel {
	public PuzzlePanel(Puzzle model, KindletContext ctx) {
        setLayout(new BorderLayout());
        
        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        add(pTop, BorderLayout.NORTH); 
		CrosswordPanel cp = new CrosswordPanel(model, ctx);
        pTop.add(cp);

        HintsPanel hp = new HintsPanel(model);
        add(hp, BorderLayout.CENTER);

        //TODO: tight coupling is ugly
        hp.setCrosswordPanel(cp);
        cp.setHintsPanel(hp);
	}

}
