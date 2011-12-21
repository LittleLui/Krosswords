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
import javax.swing.JTextField;

import littlelui.krosswords.model.Panel;
import littlelui.krosswords.model.Word;

import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.event.GestureDispatcher;
import com.amazon.kindle.kindlet.event.GestureEvent;
import com.amazon.kindle.kindlet.input.Gestures;
import com.amazon.kindle.kindlet.input.keyboard.OnscreenKeyboardUtil;
import com.amazon.kindle.kindlet.ui.KRepaintManager;

public class CrosswordPanel extends JComponent {
	private Panel model;
	
	private KindletContext ctx;
	
	private Word currentlyEditing;
	private JTextField editingTf;
	
	private int scale = 40;
	private Font keyFont = new Font("SansSerif", Font.PLAIN, 9);
	private Font solutionFont = new Font("SansSerif", Font.PLAIN, 22);

	private Dimension preferredSize;
	

	public CrosswordPanel(Panel model, KindletContext ctx, JTextField tf) {
		super();

		this.model = model;
		this.ctx = ctx;
		this.editingTf = tf;
		
		tf.setVisible(false);
		
		preferredSize = new Dimension(model.getWidth() * scale, model.getHeight() * scale);
		
		OnscreenKeyboardUtil.configure(this);
		
		addMouseListener(new GestureDispatcher());
		Action flick = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				GestureEvent ge = (GestureEvent) e;
				boolean horizontal = ge.getActionCommand().equals(Gestures.ACTION_FLICK_EAST) || ge.getActionCommand().equals(Gestures.ACTION_FLICK_WEST);
				Point p = ge.getLocation();
				
				Word w = getWordAt(p.x, p.y, horizontal);
				currentlyEditing = w;
				
				boolean showKeyboard = currentlyEditing != null;
				editingTf.setVisible(showKeyboard);
				
				if (showKeyboard) {
					editingTf.setBounds(getWordRectangle(w));
					editingTf.requestFocus();
					

//					String word = KOptionPane.showInputDialog(CrosswordPanel.this, "Word", w.getSolution());
//					w.setSolution(word.toUpperCase());
				}

//				paint(getGraphics());
				KRepaintManager.getInstance().repaint(CrosswordPanel.this, false);
//				repaint(250);
			}
			
		};
		
		
		 final ActionMap actionMap = getActionMap();
		 actionMap.put(Gestures.ACTION_FLICK_EAST, flick);
		 actionMap.put(Gestures.ACTION_FLICK_WEST, flick);
		 actionMap.put(Gestures.ACTION_FLICK_NORTH, flick);
		 actionMap.put(Gestures.ACTION_FLICK_SOUTH, flick);
		 setActionMap(actionMap);
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
		Rectangle bounds = getBounds();
		g.translate(bounds.x, bounds.y);
		
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, model.getWidth() * scale, model.getHeight() * scale);

		FontMetrics fmKey = g.getFontMetrics(keyFont);
		FontMetrics fmSol = g.getFontMetrics(solutionFont);
		
		List/*<Word>*/ words = model.getWords();

		g.setFont(keyFont);
		Iterator i = words.iterator();
		while (i.hasNext()) {
			Word w = (Word)i.next();
			
			Rectangle r = getWordRectangle(w);
			
			g.setColor(Color.white);
			g.fillRect(r.x, r.y, r.width, r.height);
			
			g.setColor(Color.black);
			g.drawString(w.getKey(), r.x+2, r.y+2+fmKey.getHeight());
		}

		if (currentlyEditing != null) {
			Rectangle r = getWordRectangle(currentlyEditing);
			g.setColor(Color.lightGray);
			g.fillRect(r.x, r.y, r.width, r.height);
//			paintString(g, fmSol, currentlyEditing, r, editingBuffer);
		}

		
		g.setColor(Color.black);
		g.setFont(solutionFont);

		i = words.iterator();
		while (i.hasNext()) {
			Word w = (Word)i.next();
			Rectangle r = getWordRectangle(w);
			
			String s = w.getSolution();
			paintString(g, fmSol, w, r, s);
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
			Rectangle r, String s) {
		for (int is = 0; is<s.length(); is++) {
			char c = s.charAt(is);
			int dx = 0;
			int dy = 0;
			
			if (w.getDirection() == Word.DIRECTION_HORIZONTAL)
				dx = is * scale;
			else
				dy = is * scale;
			
			g.drawString(""+c, dx+r.x+(scale - fmSol.charWidth(c)), dy+r.y+fmSol.getHeight());
			
		}
	}





	private Rectangle getWordRectangle(Word w) {
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
	
	

}
