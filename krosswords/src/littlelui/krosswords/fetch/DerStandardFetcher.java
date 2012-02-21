package littlelui.krosswords.fetch;


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import littlelui.krosswords.Main;
import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.catalog.PuzzleSolution;
import littlelui.krosswords.model.Puzzle;

import org.apache.regexp.RE;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Fetcher for derStandard.at. 
 * 
 * As puzzles are only available as image+html, we have to do some image analysis and html parsing here.
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
//TODO: error handling, running feedback
//TODO: close streams when done!
public class DerStandardFetcher extends AbstractWebFetcher implements Fetcher {
	final static boolean DEBUG_IMAGE_PARSING = false;
	
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
			  GridUtil.makeWordsAnyDirection(grid, horizontalHints, verticalHints, pz);
//			  makeWordsHorizontal(grid, horizontalHints, pz);
//			  makeWordsVertical(grid, verticalHints, pz);
			  this.puzzle = pz;
			} else {
				System.out.println("grid fail");
			}
		  }



		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("div".equals(localName) && "copytext".equals(attributes.getValue("class"))) {
				inCopyText = true;
			} else if ("div".equals(localName) && "bodycopy".equals(attributes.getValue("class"))) {
				inCopyText = true;
				mobile = true;
			}
			
			else if (inCopyText) {
				if (grid == null && "img".equals(localName) && (mobile || "kreuzworträtsel".equalsIgnoreCase(attributes.getValue("alt")))) {
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


	
	
	
	
	private final class SolutionProducingContentHandler extends DefaultHandler {
		private PuzzleSolution solution = new PuzzleSolution();

		private boolean inCopyText = false;
		private boolean inHorizontal = false;
		private boolean inVertical = false;
		
		private String buffer = "";
		
		public PuzzleSolution getSolution() {
			return solution;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("div".equals(localName) && "copytext".equals(attributes.getValue("class"))) {
				inCopyText = true;
			} else if ("div".equals(localName) && "bodycopy".equals(attributes.getValue("class"))) {
				inCopyText = true;
			}
			
			else if ("p".equals(localName)) {
				buffer = "";
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ((inHorizontal || inVertical) && "p".equals(localName)) {
				String s = buffer.trim();
				buffer = "";
				System.out.println(s);
				
				StringTokenizer st = new StringTokenizer(s);
				if (st.hasMoreTokens()) {
					String key = st.nextToken();
					if (st.hasMoreTokens()) {
						String word = st.nextToken();
						
						if (inHorizontal)
							solution.addHorizontalWord(key, word);
						else
							solution.addVerticalWord(key, word);
					}
				}
			}
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inCopyText) {
				String s = String.valueOf(ch, start, length);
				String t = s.trim();
				if (t.startsWith("W:")) {
					inHorizontal = true;
					inVertical = false;
					buffer += t.substring(2);
				} else if (t.startsWith("S:")) {
					inHorizontal = false;
					inVertical = true;
					buffer += t.substring(2);
				} else {
					if (inHorizontal || inVertical) {
						buffer += s;
					}
				}
			}
		}



	}

	
	
	
	
	
	private final String URL_INDEX = "http://derstandard.at/r1256744634465/Kreuzwortraetsel";
	
	private static final String P_ID_FROM_HREF = "[A-Za-z\\-]+([0-9]+)";
	private static final String P_HINT= "([0-9]+)([^_]+)___";
	private static final String P_HREF_PUZZLE = "/Kreuzwortraetsel-Nr-";
	private static final String P_HREF_SOLUTION = "Loesung-Kreuzwortraetsel";

	private String parseIdFromHref(String href) {
		RE re = new RE(P_ID_FROM_HREF);
		if (re.match(href))
			return re.getParen(1);
		
		return null;
	}
	
	public Collection fetchAvailablePuzzleIds(Collection/*<PuzzleListEntry>*/ known) {
		final Map /*<String, PuzzleListEntry>*/ ples = new HashMap();

		Iterator i = known.iterator();
		while (i.hasNext()) {
			PuzzleListEntry ple = (PuzzleListEntry)i.next();
			ples.put(ple.getId(), ple);
		}
		
		
		boolean success = fetchViaHttp(URL_INDEX, new DefaultHandler() {
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
						}
					}
				}
			}

		  });

		if (!success)
			return null;
		
		Set r = new HashSet(ples.values());
		r.removeAll(known);
		
		return r;
	}
	
	
	
	public Puzzle fetchPuzzle(PuzzleListEntry listEntry) throws Exception {
		String url = listEntry.getAttribute("puzzle-url");

		if (url == null) 
			return null;
		
		PuzzleProducingContentHandler ppch = new PuzzleProducingContentHandler();
		fetchAndParse(url, ppch);
		return ppch.getPuzzle();
	}
	

	public PuzzleSolution fetchSolution(PuzzleListEntry ple) throws Exception {
		return fetchSolution(ple.getAttribute("solution-url"));
	}
	
	private PuzzleSolution fetchSolution(String url) throws Exception {
		if (url == null)
			return null;
		
		SolutionProducingContentHandler spch = new SolutionProducingContentHandler();
		fetchAndParse(url, spch);
		return spch.getSolution();
	}

	private void fetchAndParse(String url, ContentHandler ch) throws Exception {
		if (url.startsWith("http://derstandard.at")) {
			url = "http://mobile." + url.substring("http://".length());
		}
		
		InputSource is = null;
		
		try {
		  is = fetchViaHttp(url);
		  
		  Parser p = new Parser();
		  
		  p.setContentHandler(ch);

		  p.parse(is);
		} finally {
			if (is != null) try {
				is.getCharacterStream().close();
			} catch (Exception e) {} //fair to ignore exceptions when closing
		}
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
		
		int x=0; 
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
		
		int fieldWidth = detectFieldWidth(firstWhiteX, firstBlackX, px, scanSize, pxW, pxH);
		
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
		
		int fieldHeight = detectFieldHeight(firstWhiteY, firstBlackY, px, scanSize, pxW, pxH);
		
		
		//now we have the field dimensions, we can send our probes.
		int w = (pxW / fieldWidth);
		int h = (pxH / fieldHeight);
		
		boolean[][] retVal = logicallyParseImage(px, scanSize, fieldWidth, fieldHeight, w, h);
		
		return retVal;
	}


	private int detectFieldWidth(int firstWhiteX, int firstBlackX, int[] px, int scanSize, int pxW, int pxH) {
		int y = 15;
		
		for (;y<pxH; y+=10) {
			for (int dx = 1; dx <= 20; dx++) {
				if (isWhite(getPixel(firstBlackX + dx, y, px, scanSize))) {
					int whiteAgain = firstBlackX + dx;
					return whiteAgain - firstWhiteX;
				}
			}
		}

		return firstBlackX - firstWhiteX + 2; //assumption: two pixels line
	}
	
	private int detectFieldHeight(int firstWhiteY, int firstBlackY, int[] px, int scanSize, int pxW, int pxH) {
		int x = 15;
		
		for (;x<pxW; x+=10) {
			for (int dy = 1; dy <= 20; dy++) {
				if (isWhite(getPixel(x, firstBlackY + dy, px, scanSize))) {
					int whiteAgain = firstBlackY + dy;
					return whiteAgain - firstWhiteY - 1;
				}
			}
		}

		return firstBlackY - firstWhiteY + 2; //assumption: two pixels line
	}
	private boolean[][] logicallyParseImage(int[] px, int scanSize, int fieldWidth, int fieldHeight, final int w, final int h) {
		final BufferedImage bi = DEBUG_IMAGE_PARSING ? new BufferedImage(scanSize, px.length/scanSize, BufferedImage.TYPE_INT_RGB) : null;
		
		if (DEBUG_IMAGE_PARSING)
		  bi.setRGB(0, 0, scanSize, px.length/scanSize, px, 0, scanSize);
		
		
		int c;
		boolean[][] retVal = new boolean[w][h];
		for (int yy = 0; yy<h; yy++) {
			for (int xx = 0; xx<w; xx++) {
				int pixX = fieldWidth/2 + xx * fieldWidth;
				int pixY = fieldHeight/2 + yy * fieldHeight;
				
				retVal[xx][yy] = isLogicallyWhite(pixX, pixY, px, scanSize);
				if (DEBUG_IMAGE_PARSING) {
					try {
						bi.setRGB(pixX-1, pixY-1, 0x00FF0000);
						bi.setRGB(pixX-1, pixY, 0x00FF0000);
						bi.setRGB(pixX-1, pixY+1, 0x00FF0000);
						bi.setRGB(pixX, pixY-1, 0x00FF0000);
						bi.setRGB(pixX, pixY, 0x00FF0000);
						bi.setRGB(pixX, pixY+1, 0x00FF0000);
						bi.setRGB(pixX+1, pixY-1, 0x00FF0000);
						bi.setRGB(pixX+1, pixY, 0x00FF0000);
						bi.setRGB(pixX+1, pixY+1, 0x00FF0000);
					} catch (Exception e) {}
				}
			}
		}
		
		if (DEBUG_IMAGE_PARSING) {
			JFrame jf = new JFrame() {
				JPanel jp = new JPanel() {
					public void paintComponent(Graphics g) {
						g.drawImage(bi, 0, 0, this);
					}
				};

				{
					setLayout(new BorderLayout());
					add(jp, BorderLayout.CENTER);
				}
			};
			
			jf.setSize(900, 900);
			jf.setVisible(true);
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
	
	
	public static void main(String[] args) throws Exception {
		DerStandardFetcher dsf = new DerStandardFetcher();
		PuzzleSolution ps = dsf.fetchSolution("http://mobil.derstandard.at/1325485432389/Loesung-Kreuzwortraetsel-Nr-6960?_lexikaGroup=1");
		System.out.println(ps);
	}
}
