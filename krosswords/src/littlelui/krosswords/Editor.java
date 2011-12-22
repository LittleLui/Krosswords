package littlelui.krosswords;

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import littlelui.krosswords.model.Word;

public class Editor {
	private List/*<JTextField>*/ textfields = new LinkedList();
	private final Word w;
	private final Container parent;
	private final CrosswordPanel crosswordPanel;

	public Editor(Word w, CrosswordPanel crosswordPanel, Container parent) {
		this.w = w;
		this.parent = parent;
		this.crosswordPanel = crosswordPanel;
		
		String solution = w.getSolution();
		for (int i=0; i<w.getLength(); i++) {
			JTextField tf = createTextField(solution, i);
			textfields.add(tf);
			Rectangle l = crosswordPanel.getLetterRectangle(w, i);
			parent.add(tf);
			tf.setBounds(l);
		}
		
		goTo(0);
	}
	
	public void save() {
		for (int i=0; i<textfields.size(); i++){
			JTextField tf = (JTextField)textfields.get(i);
			String text = tf.getText();
			w.setSolution(i, text);

			int centerX = tf.getBounds().x + crosswordPanel.getScale()/2;
			int centerY = tf.getBounds().y + crosswordPanel.getScale()/2;
			Word crossing = crosswordPanel.getWordAt(centerX, centerY, w.getCrossDirection() == Word.DIRECTION_HORIZONTAL); 
			if (crossing != null) {
				int iCrossing = crosswordPanel.getIndexInWord(centerX, centerY, crossing);
				crossing.setSolution(iCrossing, text);
			}
			
			parent.remove(tf);
		}
		crosswordPanel.requestFocusInWindow();
		crosswordPanel.stopEditing();
	}
	
	private void goTo(int idx) {
		if (idx >= textfields.size())
			return;
		((JTextField)textfields.get(idx)).requestFocusInWindow();
	}

	private JTextField createTextField(String solution, final int idx) {
		final JTextField tf = new JTextField();
		tf.setBackground(Color.lightGray);
		if (solution != null && solution.length() > idx) {
			String text = solution.substring(idx, idx+1);
			if (text == null || text.trim().length() == 0)
				text = "";
			
			tf.setText(text); 
		}
		
		tf.addKeyListener(new KeyAdapter() {

			public void keyTyped(KeyEvent e) {
				if (KeyEvent.VK_ENTER == e.getKeyCode() || '\n' == e.getKeyChar() || '\r' == e.getKeyChar()) {
					save();
				}
			}
			
		});
		
		tf.getDocument().addDocumentListener(new DocumentListener() {
			private void fix() {
				String tx = tf.getText();
				if (tx != null && tx.length() > 1) {
					tf.setText(tx.substring(tx.length()-1));
				}
			}
			
			public void removeUpdate(DocumentEvent e) {
				fix();
				goTo(idx-1);
			}
			
			public void insertUpdate(DocumentEvent e) {
				fix();
				goTo(idx+1);
			}
			
			public void changedUpdate(DocumentEvent e) {
				fix();
				goTo(idx+1);
			}
		});
		
		return tf;
	}

	public void focusLetter(int indexInWord) {
		((JTextField)textfields.get(indexInWord)).requestFocusInWindow();
	}

}
