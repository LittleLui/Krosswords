package littlelui.krosswords.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import littlelui.krosswords.Main;

public class Puzzle implements Serializable {
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
	
	
	
	
	
}
