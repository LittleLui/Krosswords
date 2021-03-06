package littlelui.krosswords;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;

import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Settings;
import littlelui.krosswords.model.Word;

import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.event.GestureDispatcher;
import com.amazon.kindle.kindlet.event.GestureEvent;
import com.amazon.kindle.kindlet.input.Gestures;
import com.amazon.kindle.kindlet.input.keyboard.OnscreenKeyboardProperties;
import com.amazon.kindle.kindlet.input.keyboard.OnscreenKeyboardUtil;

/** The crossword area, part of the puzzle view.
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
public class CrosswordPanel extends JComponent {
	private Puzzle model;
	
	private KindletContext ctx;
	
	private Word currentlyEditing;
	private Editor editor;
	
	private int scale = 36;
	private final Font keyFont;
	private final Font solutionFont;

	private Dimension preferredSize;
	

	public CrosswordPanel(Puzzle model, KindletContext ctx, Settings settings) {
		super();

		keyFont = new Font("SansSerif", Font.PLAIN, settings.getCrosswordPanelKeyFontSize());
		solutionFont = new Font("SansSerif", Font.PLAIN, settings.getCrosswordPanelSolutionFontSize());

		this.model = model;
		this.ctx = ctx;
		
		preferredSize = new Dimension(model.getWidth() * scale + 1, model.getHeight() * scale + 1);
		
		OnscreenKeyboardProperties kp = new OnscreenKeyboardProperties();
		kp.addProperty(OnscreenKeyboardProperties.KEYBOARD_PROPERTY_DISABLED);
		OnscreenKeyboardUtil.configure(this, OnscreenKeyboardUtil.KEYBOARD_MODE_NORMAL, kp);
		
		addMouseListener(new GestureDispatcher());
		Action flick = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				GestureEvent ge = (GestureEvent) e;
				Point p = ge.getLocation();
				int x = p.x; 
				int y = p.y;
				
				boolean horizontal = ge.getActionCommand().equals(Gestures.ACTION_FLICK_EAST) || ge.getActionCommand().equals(Gestures.ACTION_FLICK_WEST);
				
				Word w = getWordAt(x, y, horizontal);
				
				if (editor != null) {
					editor.save();
					editor = null;
				}
				
				if (w != null) {
					startEditing(w);
				}

				forceRepaint();
			}

		};
		
		Action tap = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				GestureEvent ge = (GestureEvent) e;
				Point p = ge.getLocation();
				int x = p.x;
				int y = p.y;
				
				Word w = getWordAt(x, y);
				if (w != null) {
					if (currentlyEditing == w) {
						editor.focusLetter(getIndexInWord(x, y, w));
					} else {
						if (editor != null) {
							editor.save();
						}

						startEditing(w, getIndexInWord(x, y, w));
					}
					
					
				
				}
				forceRepaint();
			}
		};
		
		 final ActionMap actionMap = getActionMap();
		 actionMap.put(Gestures.ACTION_FLICK_EAST, flick);
		 actionMap.put(Gestures.ACTION_FLICK_WEST, flick);
		 actionMap.put(Gestures.ACTION_FLICK_NORTH, flick);
		 actionMap.put(Gestures.ACTION_FLICK_SOUTH, flick);

		 actionMap.put(Gestures.ACTION_TAP, tap);
		 setActionMap(actionMap);
	}

	
	public void startEditing(Word w, int indexInWord) {
		currentlyEditing = w;
		editor = new Editor(w, CrosswordPanel.this);
		
		if (indexInWord != 0)
			editor.focusLetter(indexInWord); 
	}

	public void startEditing(Word w) {
		startEditing(w, 0);
	}
	

	public int getScale() {
		return scale;
	}
	
	
	public Word getWordAt(int x, int y) {
		Word w = getWordAt(x, y, true);
		if (w == null)
			w = getWordAt(x, y, false);
		return w;
	}


	public Word getWordAt(int x, int y, boolean horizontal) {
		List l = horizontal ? model.getHorizontalWords() : model.getVerticalWords();
		
		Iterator i = l.iterator();
		while (i.hasNext()) {
			Word word = (Word)i.next();
			Rectangle r = getWordRectangle(word);
			if (r.contains(x, y))
				return word;
		}

		return null;
	}

	public int getIndexInWord(int x, int y, Word word) {
		
		Rectangle r = getWordRectangle(word);
		if (r.contains(x, y)) {
			int i = word.getDirection() == Word.DIRECTION_HORIZONTAL ? ((x - r.x) / scale) : ((y - r.y) / scale);
			return i;
		}
		return -1;
	}




	public Dimension getPreferredSize() {
		return preferredSize;
	}

	public Dimension preferredSize() {
		return preferredSize;
	}

	public float getAlignmentX() {
		return Component.CENTER_ALIGNMENT;
	}

	public float getAlignmentY() {
		return Component.CENTER_ALIGNMENT;
	}





	public void paint(Graphics g) {
		super.paint(g);
		
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, model.getWidth() * scale, model.getHeight() * scale);

		FontMetrics fmKey = g.getFontMetrics(keyFont);
		FontMetrics fmSol = g.getFontMetrics(solutionFont);
		
		List/*<Word>*/ words = model.getWords();

		Iterator i = words.iterator();
		while (i.hasNext()) {
			Word w = (Word)i.next();
			
			Rectangle r = getWordRectangle(w);
			
			g.setColor(Color.white);
			g.fillRect(r.x, r.y, r.width, r.height);
		}

		g.setColor(Color.black);

		i = words.iterator();
		while (i.hasNext()) {
			Word w = (Word)i.next();
			Rectangle r = getWordRectangle(w);
			
			g.setFont(keyFont);
			g.drawString(w.getKey(), r.x+2, r.y+2+fmKey.getHeight());

			g.setFont(solutionFont);
			String s = w.getSolution();
			boolean[] marks = w.getMarks();
			paintString(g, fmSol, w, r, s, marks);
		}

		
		g.setColor(Color.black);
		for (int x = 0; x <= model.getWidth(); ++x) {
			g.drawLine(x * scale, 0, x * scale, model.getHeight() * scale);
		}

		for (int y = 0; y <= model.getHeight(); ++y) {
			g.drawLine(0, y * scale, model.getWidth() * scale, y * scale);
		}
		
		super.paintChildren(g); 
	}






	private void paintString(Graphics g, FontMetrics fmSol, Word w,
			Rectangle r, String s, boolean[] marks) {
		for (int is = 0; is<s.length(); is++) {
			char c = s.charAt(is);
			int dx = 0;
			int dy = 0;
			
			if (w.getDirection() == Word.DIRECTION_HORIZONTAL)
				dx = is * scale;
			else
				dy = is * scale;
			
			g.drawString(""+c, dx+r.x+(scale - fmSol.charWidth(c))/2, dy+r.y+fmSol.getHeight());
			
			if (marks[is]) {
				Rectangle rect = getLetterRectangle(w, is);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(rect.x+1, rect.y+1, rect.width - 4 , rect.height - 4);
				g.setColor(Color.black);
			}
			
		}
	}



	Rectangle getLetterRectangle(Word w, int idx) {
		int x = w.getX() * scale + 1;
		int y = w.getY() * scale + 1;
		
		int x2 = x + idx*scale;
		int y2 = y;
		
		if (w.getDirection() == Word.DIRECTION_VERTICAL) {
			x2 = x;
			y2 = y + idx*scale;
		}
		Rectangle r = new Rectangle(x2, y2, scale, scale);
		return r;
	}

	Rectangle getWordRectangle(Word w) {
		int x = w.getX() * scale + 1;
		int y = w.getY() * scale + 1;
		
		int length = w.getLength() * scale - 1;
		
		int width = length;
		int height = scale - 1;
		
		if (w.getDirection() == Word.DIRECTION_VERTICAL) {
			width = scale - 1;
			height = length;
		}
		Rectangle r = new Rectangle(x, y, width, height);
		return r;
	}






	public void stopEditing() {
		currentlyEditing = null;
		editor = null;
	}


	void forceRepaint() {
		repaint();
//		KRepaintManager.getInstance().repaint(CrosswordPanel.this, false);
	}

	private HintsPanel hp;
	public void setHintsPanel(HintsPanel hp) {
		this.hp = hp;
	}
	
	

}
