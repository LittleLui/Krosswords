package littlelui.krosswords.catalog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Word;

/** A single puzzle's solution.
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
