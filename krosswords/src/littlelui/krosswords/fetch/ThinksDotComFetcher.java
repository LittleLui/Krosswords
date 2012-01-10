package littlelui.krosswords.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.catalog.PuzzleSolution;
import littlelui.krosswords.model.Puzzle;

import org.apache.regexp.RE;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mindprod.ledatastream.LEDataInputStream;
public class ThinksDotComFetcher extends AbstractWebFetcher {

	private static final String baseURL = "http://thinks.com/crosswords/cryptic/";
	private static final String listURL = baseURL + "cryptic.htm";
	private static final String RE_PUZZLE_HREF = "cr[0-9]+\\.htm";
	private static final String RE_PUZZLE_PUZ_HREF = ".\\.puz";
	
	public Collection fetchAvailablePuzzleIds(Collection known) {
		Set k = new HashSet();
		Iterator i = known.iterator();
		while (i.hasNext()) {
			PuzzleListEntry ple = (PuzzleListEntry)i.next();
			k.add(ple.getId());
		}
		
		ListProducingContentHandler lpch = new ListProducingContentHandler(k);
		fetchViaHttp(listURL, lpch);
		
		return lpch.getNewResults();
	}

	public Puzzle fetchPuzzle(PuzzleListEntry listEntry) throws Exception {
		return null;
	}

	public PuzzleSolution fetchSolution(PuzzleListEntry ple) throws Exception {
		return null;
	}

	
	private class ListProducingContentHandler extends DefaultHandler {
		private final List/*<PuzzleListEntry>*/ ret = new ArrayList();
		private final Set/*<String>*/ known;
		
		public ListProducingContentHandler(Set known) {
			this.known = known;
		}

		public Collection getNewResults() {
			return ret;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("a".equals(localName)) {
				String href = attributes.getValue("href");
				RE re = new RE(RE_PUZZLE_HREF);
				if (re.match(href)) {
					String url = baseURL+href;
					
					fetchViaHttp(url, new PLEProducingContentHandler(ret, known));
				}
			}
		}
	}
	
	private class PLEProducingContentHandler extends DefaultHandler {
		private final Set knownIDs;
		private final List/*<PuzzleListEntry>*/ addHere;
		
		private String nameBuffer;
		
		private String name;
		private String id;
		private Puzzle puzzle;
		
		private boolean cancelled = false;
		
		private Exception ex;
		
		public PLEProducingContentHandler(List addHere, Set knownIDs) {
			this.addHere = addHere;
			this.knownIDs = knownIDs;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (cancelled)
				return;
			
			if ("h1".equals(localName)) {
				nameBuffer = "";
			}
			if ("a".equals(localName)) {
				String href = attributes.getValue("href");
				RE re = new RE(RE_PUZZLE_PUZ_HREF);
				if (re.match(href)) {
					String url = baseURL+href;
					
					try {
						InputStream is = getURLInputStream(url);
						try {
							puzzle = PUZParser.parse(new LEDataInputStream(is));
						} finally {
							if (is != null) try {
								is.close();
							} catch (IOException ioe) {}
						}
					} catch (Exception e) {
						ex = e;
					}
				}
			}
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (cancelled)
				return;
			if ("h1".equals(localName)) {
				name = nameBuffer;
				id = name.replace(' ', '_');
				nameBuffer = null;
				
				if (knownIDs.contains(id)) {
					cancelled = true;
				}
			}
		}
		
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (cancelled)
				return;
			
			if (nameBuffer != null) {
				String s = String.valueOf(ch, start, length);
				nameBuffer += s;
			}
		}

		public void endDocument() throws SAXException {
			if (cancelled)
				return;
			
			int downloadState = puzzle == null ? PuzzleListEntry.DOWNLOAD_FAILED : PuzzleListEntry.DOWNLOADED;
			PuzzleListEntry ple = new PuzzleListEntry(id, name, "thinks.com", true, downloadState, downloadState, PuzzleListEntry.NOT_PLAYED);
			
			if (ex != null) {
				DownloadManager.setFailedDownloadError(ple, ex);
			} else {
				ple.setPuzzle(puzzle);
			}
			
			addHere.add(ple);
		}
		
		
		
		
	}
	
	public static void main(String[] args) {
		ThinksDotComFetcher tdcf = new ThinksDotComFetcher();
		Collection c = tdcf.fetchAvailablePuzzleIds(Collections.EMPTY_SET);
		System.out.println(c);
	}

	
	
}
