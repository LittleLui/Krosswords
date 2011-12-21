package littlelui.krosswords.model;

import java.util.ArrayList;
import java.util.List;

public class Panel {
	private int width;
	private int height;
	
	private List/*<Word>*/ horizontalWords = new ArrayList();
	private List/*<Word>*/ verticalWords = new ArrayList();
	private List/*<Word>*/ words = new ArrayList();

	public Panel(int width, int height) {
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
	
	
	
	
	
}
