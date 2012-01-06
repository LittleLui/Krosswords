package littlelui.krosswords.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Puzzle implements Serializable {
	public static final int VERIFY_FINISHED_BAD = -1;
	public static final int VERIFY_FINISHED_CORRECT = 1;
	public static final int VERIFY_NOT_FINISHED = 0;

	private static final long serialVersionUID = 1L;

	private int width;
	private int height;
	
	private List/*<Word>*/ horizontalWords = new ArrayList();
	private List/*<Word>*/ verticalWords = new ArrayList();
	private List/*<Word>*/ words = new ArrayList();

	public Puzzle(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}
	
	public void add(Word word) {
		words.add(word);
		
		if (word.getDirection()==Word.DIRECTION_HORIZONTAL)
			horizontalWords.add(word);
		else
			verticalWords.add(word);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public List getHorizontalWords() {
		return horizontalWords;
	}

	public List getVerticalWords() {
		return verticalWords;
	}

	public List getWords() {
		return words;
	}

	public void saveSolutionState(ObjectOutputStream oos) throws IOException {
		for (int i=0; i<words.size(); i++) {
			oos.writeObject(((Word)words.get(i)).getSolution());
		}
	}
	
	public void loadSolutionState(ObjectInputStream ois) {
		try {
			for (int i=0; i<words.size(); i++) {
				String s = (String)ois.readObject();
				((Word)words.get(i)).setSolution(s);
			}
		} catch (Exception ioe) {
			//whatever goes wrong (probably just: no solution state), it's fine with me.
		}
	}

	/* verifies all data against the solution
	 * returns 0 for "not finished yet"
	 * returns 1 for "finished and correct"
	 * returns -1 for "finished but has errors"
	 */
	public int verify() {
		boolean finished = true;
		boolean allCorrect = true;
		
		Iterator iWords = words.iterator();
		while (iWords.hasNext()) {
			Word w = (Word)iWords.next();
			
			String s = w.getSolution();
			if (s.length() < w.getLength()) {
				finished = false;
			}
			
			for (int i=0; i<s.length(); i++) {
				if (s.charAt(i) == ' ') {
					finished = false;
					break; 
				}
			}
			
			
			
			
			boolean correct = w.verify();
			if (!correct)
				allCorrect = false;
		}
		
		if (!finished)
			return VERIFY_NOT_FINISHED;
		
		return allCorrect ? VERIFY_FINISHED_CORRECT : VERIFY_FINISHED_BAD;
		
	}
	
	
	
	
	
}
