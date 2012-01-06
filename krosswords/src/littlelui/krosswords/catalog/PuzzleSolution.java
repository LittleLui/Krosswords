package littlelui.krosswords.catalog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Word;

public class PuzzleSolution {
	private Map/*<String, String>*/ horizontal = new HashMap();
	private Map/*<String, String>*/ vertical = new HashMap();
	
	public void addHorizontalWord(String key, String word) {
		horizontal.put(key, word);
	}

	public void addVerticalWord(String key, String word) {
		vertical.put(key, word);
	}

	public void fillInto(Puzzle puzzle) {
		fill(puzzle.getHorizontalWords(), horizontal);
		fill(puzzle.getVerticalWords(), vertical);
	}

	//TODO: solution might be wrong. what then?
	private void fill(List words, Map solution) {
		Iterator i = words.iterator();
		while (i.hasNext()) {
			Word w = (Word) i.next();
			String s = (String)solution.get(w.getKey());
			s = s.substring(0, w.getLength());
			w.setExpectedSolution(s);
		}
	}

	public String toString() {
		return "PuzzleSolution [horizontal=" + horizontal + ", vertical="
				+ vertical + "]";
	}


	
	
}
