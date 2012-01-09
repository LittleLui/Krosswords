package littlelui.krosswords.fetch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Word;

public final class GridUtil {
	public static void makeWordsAnyDirection(boolean[][] g, Map h, Map v, Puzzle pz) {
		int wordCount=0;
		for (int y=0; y<g[0].length; y++) {
			for (int x=0; x<g.length; x++) {
				boolean startH = horizontalWordStartsAt(x, y, g);
				boolean startV = verticalWordStartsAt(x, y, g);
				
				if (startH || startV)
					++wordCount;
				
				if(startH) {
					int length = getRunLength(x, y, 1, 0, g);
					createWordByKey(h, pz, ""+wordCount, x, y, length, Word.DIRECTION_HORIZONTAL);
				}
				
				if(startV) {
					int length = getRunLength(x, y, 0, 1, g);
					createWordByKey(v, pz, ""+wordCount, x, y, length, Word.DIRECTION_VERTICAL);
				}
			}
		}
	}
	
	public static void makeWordsAnyDirectionFromSingleWordList(boolean[][] g, List l, Puzzle pz) {
		int wordCount=0;
		int hintNumbering=0;
		for (int y=0; y<g[0].length; y++) {
			for (int x=0; x<g.length; x++) {
				boolean startH = horizontalWordStartsAt(x, y, g);
				boolean startV = verticalWordStartsAt(x, y, g);
				
				if (startH || startV)
					++hintNumbering;

				if(startH) {
					int length = getRunLength(x, y, 1, 0, g);
					String hint = (String)l.get(wordCount++);
					String key = ""+hintNumbering;
					pz.add(new Word(x, y, length, Word.DIRECTION_HORIZONTAL, key, hint));
				}
				
				if(startV) {
					int length = getRunLength(x, y, 0, 1, g);
					String hint = (String)l.get(wordCount++);
					String key = ""+hintNumbering;
					pz.add(new Word(x, y, length, Word.DIRECTION_VERTICAL, key, hint));
				}
				
			}
		}
	}

	private static int getRunLength(int x, int y, int dx, int dy, boolean[][] g) {
		int count = 0;
		
		do {
			count ++;
			x += dx;
			y += dy;
		} while(x<g.length && y<g[x].length && g[x][y]);
		
		return count;
	}

	private static void createWordByKey(Map hints, Puzzle pz, String key, int x, int y, int length, int direction) {
		String hint = (String)hints.get(key);
		pz.add(new Word(x, y, length, direction, key, hint));
	}


	private static boolean horizontalWordStartsAt(int x, int y, boolean[][] g) {
		if (g[x][y]) { //curent square white?
			if (x < g.length-1 && g[x+1][y]) //next square white?
				return (x == 0) || !g[x-1][y]; //previous square non-white?
		}
		return false;
	}
	
	private static boolean verticalWordStartsAt(int x, int y, boolean[][] g) {
		if (g[x][y]) {
			if (y < g[x].length-1 && g[x][y+1])
				return (y == 0) || !g[x][y-1];
		}
		return false;
	}		

	public static void makeWordsHorizontal(boolean[][] g, Map m, Puzzle pz) {
		List/*String*/ hintKeysInOrder = getKeysInOrder(m);
		
		int wordCount=0;
		for (int y=0; y<g[0].length; y++) {
			int startx = -1;
			for (int x=0; x<g.length; x++) {
				if (g[x][y] && startx == -1) {
					startx = x;
				} else if (!g[x][y] && startx != -1 && startx < x-1) { //minimum length = 2
					int endx = x; //endx is PAST the end actually, so endx-startx will be the actual length
					wordCount = createWord(m, pz, hintKeysInOrder, wordCount, startx, y, endx-startx, Word.DIRECTION_HORIZONTAL);
					startx = -1;
				} else if (g[x][y] && startx != -1 && x == g.length - 1) { //end of line also ends
					int endx = x+1;
					wordCount = createWord(m, pz, hintKeysInOrder, wordCount, startx, y, endx-startx, Word.DIRECTION_HORIZONTAL);
					startx = -1;
				} else if (!g[x][y]) {
					startx = -1;
				}
			}
		}
	}

	private static List getKeysInOrder(Map m) {
		SortedSet/*<String>*/ keysInOrder = new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = (String)o1;
				String s2 = (String)o2;
				
				int i1 = Integer.parseInt(s1);
				int i2 = Integer.parseInt(s2);
				
				return i1 - i2;
			}
			
		});
		keysInOrder.addAll(m.keySet());
		return new ArrayList(keysInOrder);
	}

	public static void makeWordsVertical(boolean[][] g, Map m, Puzzle pz) {
		List/*String*/ hintKeysInOrder = getKeysInOrder(m);
		
		int wordCount=0;
		for (int x=0; x<g.length; x++) {
			int starty = -1;
			for (int y=0; y<g[0].length; y++) {
				if (g[x][y] && starty == -1) {
					starty = y;
				} else if (!g[x][y] && starty != -1 && starty < y-1) { //minimum length = 2
					int endy = y; //endx is PAST the end actually, so endx-startx will be the actual length
					wordCount = createWord(m, pz, hintKeysInOrder, wordCount, x, starty, endy-starty, Word.DIRECTION_VERTICAL);
					starty = -1;
				} else if (g[x][y] && starty != -1 && y == g[x].length - 1) { //end of column also ends
					int endy = y+1;
					wordCount = createWord(m, pz, hintKeysInOrder, wordCount, x, starty, endy-starty, Word.DIRECTION_VERTICAL);
					starty = -1;
				} else if (!g[x][y]) {
					starty = -1;
				}
			}
		}
	}

	private static int createWord(Map hints, Puzzle pz, List hintKeysInOrder, int wordCount, int x, int y, int length, int direction) {
		String key = (String)hintKeysInOrder.get(wordCount++);
		String hint = (String)hints.get(key);
		pz.add(new Word(x, y, length, direction, key, hint));
		return wordCount;
	}
}
