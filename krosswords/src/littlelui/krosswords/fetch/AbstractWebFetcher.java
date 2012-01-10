package littlelui.krosswords.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import littlelui.krosswords.Main;
import littlelui.krosswords.catalog.PuzzleListEntry;

import org.apache.regexp.RE;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractWebFetcher implements Fetcher {

	private static final String P_CHARSET_IN_TYPE = "[A-Za-z0-9\\-/]+;\\s*charset=([A-Za-z0-9\\-]+)";

	public AbstractWebFetcher() {
		super();
	}

	protected InputSource fetchViaHttp(String url) throws IOException,
			MalformedURLException, UnsupportedEncodingException {
				Reader r = getURLReader(url);
				InputSource is = new InputSource(r);
				return is;
			}

	protected Reader getURLReader(String url) throws IOException, MalformedURLException, UnsupportedEncodingException {
		HttpURLConnection conn = getHttpConnection(url);

		InputStream in = (InputStream) conn.getContent();
		String encoding = conn.getContentEncoding();
		String type = conn.getContentType();
		
		
		if (encoding == null) {
			RE re = new RE(P_CHARSET_IN_TYPE);
			if (re.match(type)) {
				encoding = re.getParen(1);
			} else {
				encoding = "ISO-8859-1";
			}
		}
		
		Reader r = new InputStreamReader(in, encoding);
		return r;
	}

	protected InputStream getURLInputStream(String url) throws IOException, MalformedURLException {
		HttpURLConnection conn = getHttpConnection(url);
		InputStream in = (InputStream) conn.getContent();
		return in;
	}
	
	private HttpURLConnection getHttpConnection(String url) throws IOException,
			MalformedURLException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		
		//so we don't neccessarily get the mobile version 
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; rv:8.0) Gecko/20100101 Firefox/8.0");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		return conn;
	}
	
	
	protected boolean fetchViaHttp(String url, ContentHandler handler) {
		InputSource is = null;
		try {
		  is = fetchViaHttp(url);
		  Parser p = new Parser();
		  p.setContentHandler(handler);
		  p.parse(is);
		  return true;
		} catch (IOException ioe) {
			Main.getInstance().logError("Unable to update list from <"+url+">", ioe);
			return false;
		} catch (SAXException se) {
			Main.getInstance().logError("Unable to update list from <"+url+">", se);
			return false;
		} finally {
			if (is != null) try {
				is.getCharacterStream().close();
			} catch (Exception e) {} //fair to ignore exceptions when closing
		}
	}

}