package littlelui.krosswords.fetch;


import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.model.Puzzle;
import littlelui.krosswords.model.Word;

import org.apache.regexp.RE;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//TODO: respect encoding! (hints have broken umlauts)
//TODO: error handling, running feedback
//TODO: close streams when done!
public class DerStandardFetcher implements Fetcher {
	private final class PuzzleProducingContentHandler extends DefaultHandler {
		private boolean mobile = false;
		private boolean inCopyText = false;
		private boolean primedHorizontalHints = false;
		private boolean primedVerticalHints = false;
		private boolean inHorizontalHints = false;
		private boolean inVerticalHints = false;
		private Map/*<String, String>*/ horizontalHints = new HashMap();
		private Map/*<String, String>*/ verticalHints = new HashMap();
		boolean[][] grid;
		private String buffer = "";
		private Puzzle puzzle;
		
		public Puzzle getPuzzle() {
			return puzzle;
		}

		public void endDocument() {
			if (grid != null) {
			  Puzzle pz = new Puzzle(grid.length, grid[0].length);
			  makeWordsHorizontal(grid, horizontalHints, pz);
			  makeWordsVertical(grid, verticalHints, pz);
			  this.puzzle = pz;
			} else {
				System.out.println("grid fail");
			}
		  }

		private void makeWordsHorizontal(boolean[][] g, Map m, Puzzle pz) {
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

		private List getKeysInOrder(Map m) {
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

		private void makeWordsVertical(boolean[][] g, Map m, Puzzle pz) {
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

		private int createWord(Map hints, Puzzle pz, List hintKeysInOrder, int wordCount, int x, int y, int length, int direction) {
			String key = (String)hintKeysInOrder.get(wordCount++);
			String hint = (String)hints.get(key);
			pz.add(new Word(x, y, length, direction, key, hint));
			return wordCount;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("div".equals(localName) && "copytext".equals(attributes.getValue("class"))) {
				inCopyText = true;
			} else if ("div".equals(localName) && "bodycopy".equals(attributes.getValue("class"))) {
				inCopyText = true;
				mobile = true;
			}
			
			else if (inCopyText) {
				if ("img".equals(localName) && (mobile || "kreuzworträtsel".equalsIgnoreCase(attributes.getValue("alt")))) {
					String src = attributes.getValue("src");
					grid = fetchPuzzleImage(src);
				} if (primedHorizontalHints && "p".equals(localName)) {
					primedHorizontalHints = false;
					inHorizontalHints = true;
				} else if (primedVerticalHints && "p".equals(localName)) {
					primedVerticalHints = false;
					inVerticalHints = true;
				} else if (inHorizontalHints || inVerticalHints) {
					if ("em".equals(localName)) {
						buffer += "/";
					}
				}
				
			}
		}

		public void endElement(String uri, String localName,String qName) throws SAXException {
			if ("p".equals(localName)) {
				
				if (inHorizontalHints) {
					parseHints(buffer, horizontalHints);
					buffer = "";
				} else if (inVerticalHints) {
					parseHints(buffer, verticalHints);
					buffer = "";
				}
				
				inHorizontalHints = false;
				inVerticalHints = false;
			} else if (inHorizontalHints || inVerticalHints) {
				if ("em".equals(localName)) {
					buffer = buffer.trim() + "/ ";
				} if ("br".equals(localName)) {
					buffer += "___";
				}
			}
		}

		private void parseHints(String b, Map r) {
			int offset = 0;
			RE re = new RE(P_HINT);
			String s = b+"___";
			int i = 0;
			while (re.match(s, i)) {
				String key = re.getParen(1);
				String value = re.getParen(2);
				r.put(key, value.trim());
				i = re.getParenEnd(0);
			}
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inCopyText) {
				String s = String.valueOf(ch, start, length);
				String t = s.trim();
				if (t.startsWith("Waagrecht:")) {
					primedHorizontalHints = true;
					primedVerticalHints = false;
					inHorizontalHints = false;
					inVerticalHints = false;
				} else if (t.startsWith("Senkrecht:")) {
					primedHorizontalHints = false;
					primedVerticalHints = true;
					inHorizontalHints = false;
					inVerticalHints = false;
				} else {
					if (inHorizontalHints || inVerticalHints) {
						buffer += s;
					}
				}
			}
		}


	}

	private final String URL_INDEX = "http://mobil.derstandard.at/r1256744634465/Kreuzwortraetsel";
	
	private static final String P_ID_FROM_HREF = "[A-Za-z\\-]+([0-9]+)";
	private static final String P_HINT= "([0-9]+)([^_]+)___";
	private static final String P_CHARSET_IN_TYPE = "[A-Za-z0-9\\-/]+;\\s*charset=([A-Za-z0-9\\-]+)";

	private static final String P_HREF_PUZZLE = "/Kreuzwortraetsel-Nr-";
	private static final String P_HREF_SOLUTION = "Loesung-Kreuzwortraetsel";

	private String parseIdFromHref(String href) {
		RE re = new RE(P_ID_FROM_HREF);
		if (re.match(href))
			return re.getParen(1);
		
		return null;
	}
	public List fetchAvailablePuzzleIds(FetchCallback listener) {
		final Map /*<String, PuzzleListEntry>*/ ples = new HashMap();
		
		try {
		  InputSource is = fetchViaHttp(URL_INDEX);
		  Parser p = new Parser();
		  
		  p.setContentHandler(new DefaultHandler() {
			  //<a href="/1319184020935/Kreuzwortraetsel-Nr-6941?_lexikaGroup=1">Kreuzworträtsel Nr. 6941</a>
			  //<a href="/1319184020416/Loesung-Kreuzwortraetsel-Nr-6940?_lexikaGroup=1">Lösung: Kreuzworträtsel Nr. 6940</a>

			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				if ("a".equals(localName)) {
					String href = attributes.getValue("href");
					if (href != null) {
						if (new RE(P_HREF_SOLUTION).match(href)) {
							String id = parseIdFromHref(href);
							if (id != null) {
								PuzzleListEntry ple = createOrGet(ples, id);
								ple.setAttribute("solution-url", "http://derstandard.at"+href);
							}
							
						} else if (new RE(P_HREF_PUZZLE).match(href)) {
							String id = parseIdFromHref(href);
							if (id != null) {
								PuzzleListEntry ple = createOrGet(ples, id);
								ple.setAttribute("puzzle-url", "http://derstandard.at"+href);
								
							}
						} else  {
							System.out.println(href);
						}
					}
				}
			}

		  });
		  
		  p.parse(is);
		} catch (IOException ioe) {
			//TODO
		} catch (SAXException se) {
			//TODO
		}
		
		return new ArrayList(ples.values());
	}
	
	
	public Puzzle fetchPuzzle(PuzzleListEntry listEntry) {
		try {
			Puzzle p = fetchPuzzle(listEntry.getAttribute("puzzle-url"));
			
			if (p != null) {
				listEntry.setPuzzle(p);
				listEntry.setPuzzleDownloadState(PuzzleListEntry.DOWNLOADED);
			}
			
			return p;
		} catch (Throwable t) {
			System.out.println(t);
			t.printStackTrace();
			return null;
		}
	}
	
	private Puzzle fetchPuzzle(String url) {
		if (url == null) 
			return null;
		
		if (url.startsWith("http://derstandard.at")) {
			url = "http://mobile." + url.substring("http://".length());
		}
		
		try { 
			  InputSource is = fetchViaHttp(url);
			  
			  Parser p = new Parser();
			  
			  PuzzleProducingContentHandler ppch = new PuzzleProducingContentHandler();
			  p.setContentHandler(ppch);

			  p.parse(is);
			  
			  return ppch.getPuzzle();
		} catch (IOException ioe) {
			//TODO
		} catch (SAXException se) {
			//TODO
		}			  
		
		return null;
	}

	private InputSource fetchViaHttp(String url) throws IOException, MalformedURLException, UnsupportedEncodingException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		Map m = conn.getRequestProperties();

		InputStream in = (InputStream) conn.getContent();
		String encoding = conn.getContentEncoding();
		String type = conn.getContentType();
		
		
		if (encoding == null) {
			RE re = new RE(P_CHARSET_IN_TYPE);
			if (re.match(type)) {
				encoding = re.getParen(1);
			}
		}
		
		Reader r = new InputStreamReader(in, encoding);
		InputSource is = new InputSource(r);
		return is;
	}
	
	protected boolean[][] fetchPuzzleImage(String src) {
		try {
			  HttpURLConnection conn = (HttpURLConnection)new URL(src).openConnection();
			  ImageProducer in = (ImageProducer)conn.getContent();
			  
			  PixelGrabber pg = new PixelGrabber(in, 0, 0, -1, -1, null, 0, -1);
			  boolean success = pg.grabPixels(); //TODO: could be done async, too.
			  
			  if (success) {
				  int[] pixels = (int[])pg.getPixels();
				  return parseImage(pixels, pg.getWidth(), pg.getWidth(), pg.getHeight());
			  } else {
				  int status = pg.getStatus();
				  return null;
			  }
		} catch (IOException ioe) {
			//TODO
		} catch (InterruptedException ie) {
			//TODO
		}
		// TODO Auto-generated method stub
		return null;
	}

	private boolean[][] parseImage(int[] px, int scanSize, int pxW, int pxH) {
		if (pxW < 15 || pxH < 15)
			return null;
		
		int x=15; 
		int y=15;
		int c = getPixel(x, y, px, scanSize);
		
		//go right until you hit white
		while(!isWhite(c)) {
			c = getPixel(++x, y, px, scanSize);
		}
		
		int firstWhiteX = x;
		
		//go right until you hit black again
		while(isWhite(c)) {
			c = getPixel(++x, y, px, scanSize);
		}
		
		int firstBlackX = x;
		
		int fieldWidth = firstBlackX - firstWhiteX + 1; //simple substraction would yield only the width of the white, but the black line is also part of it... but TODO: the line being 1px is an assumption that is probably invalid.
		
		//go to the center of the white square
		x = x - fieldWidth/2;
		c = getPixel(x, y, px, scanSize);
		
		//go upwards
		while(isWhite(c)) {
			c = getPixel(x, --y, px, scanSize);
		}
		
		int firstWhiteY = y + 1;
		
		c= getPixel(x, firstWhiteY, px, scanSize);
		
		//go downWards
		while(isWhite(c)) {
			c = getPixel(x, ++y, px, scanSize);
		}
		
		int firstBlackY = y;
		
		int fieldHeight = firstBlackY - firstWhiteY + 1; //TODO: see above.
		
		
		//now we have the field dimensions, we can send our probes.
		int w = pxW / fieldWidth;
		int h = pxH / fieldHeight;
		
		boolean[][] retVal = logicallyParseImage(px, scanSize, fieldWidth, fieldHeight, w, h);
		
		return retVal;
	}


	private boolean[][] logicallyParseImage(int[] px, int scanSize, int fieldWidth, int fieldHeight, int w, int h) {
		int c;
		boolean[][] retVal = new boolean[w][h];
		for (int yy = 0; yy<h; yy++) {
			for (int xx = 0; xx<w; xx++) {
				if (xx == 5 && yy == 11) {
					System.out.println("ha");
				}
				int pixX = fieldWidth/2 + xx * fieldWidth;
				int pixY = fieldHeight/2 + yy * fieldHeight;
				
				retVal[xx][yy] = isLogicallyWhite(pixX, pixY, px, scanSize);
				

			}
		}
		return retVal;
	}

	private boolean isLogicallyWhite(int pixX, int pixY, int[] px, int scanSize) {
		for (int dx=-1; dx<2; dx++) {
			for (int dy = -1; dy<2; dy++) {
				int c = getPixel(pixX+dx, pixY+dy, px, scanSize);
				if (isWhite(c)) {
					return true;
				}
			}
		}
		return false;
	}


	private int getPixel(int x, int y, int[] px, int scanSize) {
		 /* int alpha = (pixel >> 24) & 0xff;
		 *	int red   = (pixel >> 16) & 0xff;
		 *	int green = (pixel >>  8) & 0xff;
		 *	int blue  = (pixel      ) & 0xff;
		 */

		if ((y*scanSize + x) >= px.length) {
			System.out.println("gaaa");
		}
		
		return px[y*scanSize + x];
	}

	private boolean isWhite(int pixel) {
		 int alpha = (pixel >> 24) & 0xff;
		 int red   = (pixel >> 16) & 0xff;
		 int green = (pixel >>  8) & 0xff;
		 int blue  = (pixel      ) & 0xff;
		return red > 220 && green > 220 && blue > 220;
	}

	private PuzzleListEntry createOrGet(Map ples, String id) {
		PuzzleListEntry ple = (PuzzleListEntry)ples.get(id);
		if (ple == null) {
			String name = "Nr. " + id;
			ple = new PuzzleListEntry(id, name, "derStandard.at", false, PuzzleListEntry.NOT_DOWNLOADED, PuzzleListEntry.NOT_DOWNLOADED, PuzzleListEntry.NOT_PLAYED);
			ples.put(id, ple);
		} 
		return ple;
	}
	
	public static void main (String[] args) {
		new DerStandardFetcher().fetchAvailablePuzzleIds(null);
//		new DerStandardFetcher().fetchPuzzleImage("http://images.derstandard.at/2009/11/23/1256825543842.gif");
		new DerStandardFetcher().fetchPuzzle("http://derstandard.at/1324410966911/Kreuzwortraetsel-Nr-6955?_lexikaGroup=1");
	}

}
