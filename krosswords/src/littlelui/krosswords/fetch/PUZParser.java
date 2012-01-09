package littlelui.krosswords.fetch;

import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Word;

import com.mindprod.ledatastream.LEDataInputStream;

public class PUZParser {
	private static final String MAGIC = "ACROSS&DOWN";
	
	public static Puzzle parse(DataInput in) throws IOException {
		short checksum = in.readShort();
		String magic = readString(in, 0x0C); 
		
		if (!MAGIC.equals(magic)) {
			throw new IOException("Magic string didn't match. Was: \""+magic+"\".");
		}
		
		short cibChecksum = in.readShort();
		int mlChecksum = in.readInt();
		int mhChecksum = in.readInt();
		
		String version = readString(in, 4);
		in.skipBytes(2); 
		short scrambledChecksum = in.readShort();
		in.skipBytes(0x0C); 
		byte width = in.readByte();
		byte height = in.readByte();
		short nClues = in.readShort();
		in.skipBytes(2);
		boolean scrambeld = in.readShort() != 0;
		
		String board = readString(in, width*height);
		String player = readString(in, width*height);
		
		String title = readString(in, 1024);
		String author = readString(in, 1024);
		String copyright = readString(in, 1024);

		List clues = new ArrayList();
		for (int i=0; i<nClues; i++) {
			clues.add(i, readString(in, 1024));
		}
		
		String notes = readString(in, 1024);

		
		boolean[][] grid = new boolean[width][height];
		for (int y = 0; y<height;y++) {
			for (int x=0; x<width; x++) {
				char c = board.charAt(y * width + x);
				grid[x][y] = (c != '.');
			}
		}
		
		Puzzle p = new Puzzle(width, height);
		
		GridUtil.makeWordsAnyDirectionFromSingleWordList(grid, clues, p);
		
		Iterator iWords = p.getWords().iterator();
		while (iWords.hasNext()) {
			parseSolution(board, (Word)iWords.next(), width);
		}
		
		return p;
	}

	private static void parseSolution(String board, Word w, int width) {
		int dx = 0;
		int dy = 0;
		
		if (w.getDirection() == Word.DIRECTION_HORIZONTAL) 
			dx = 1;
		else
			dy = 1;
		
		String s = parseSolution(board, w.getX(), w.getY(), dx, dy, width);
		w.setExpectedSolution(s);
	}

	private static String parseSolution(String board, int x, int y, int dx, int dy, int w) {
		String r = "";
		
		do {
			char c = getSolutionChar(board, x, y, w);
			if (c == 0)
				break;
			r = r + c;
			
			x+=dx;
			y+=dy;
		} while (true);

		return r;
	}

	private static char getSolutionChar(String board, int x, int y, int w) {
		int off = y*w + x;
		if (off >= board.length())
			return 0;
		
		char c = board.charAt(off);
		if ('.' == c) 
			return 0;

		return c;
	}

	private static String readString(DataInput in, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		
		int i;
		for (i=0; i<bufferSize; i++) {
			byte b = in.readByte();
			if (b == 0)
				break;
			
			buffer[i] = b;
		}
		
		String s = new String(buffer, 0, i, "ISO-8859-1");
		return s;
	}
	
	
	public static void main(String[] args) throws IOException {
		File f = new File("test-resources/cr001.puz");
		DataInput dis = new LEDataInputStream(new FileInputStream(f));
		Puzzle p = PUZParser.parse(dis);
	}

}
