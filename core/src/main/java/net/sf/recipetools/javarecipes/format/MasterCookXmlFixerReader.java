/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ft
 *
 */
public class MasterCookXmlFixerReader extends Reader {

	public static final int BUFFER_SIZE = 10000;
	
	private char[] buffer = null;
	private int buflength = 0;
	private Reader in = null;
	
	private Pattern[] xmlFixes = new Pattern[] {
			// new lines as chars gives problems in the XML header like
			// <?xml version="1.0" standalone="yes" encoding="ISO-8859-1"?>&#013;&#010;<!DOCTYPE mx2 SYSTEM "mx2.dtd">
		    Pattern.compile("&#013;&#010;"),
		    // delete stange characters
		    Pattern.compile("\\x1f"),

		    // for fixing the XML header
			Pattern.compile("(<\\?xml .*?)\\s*(standalone=\".*?\")\\s*(encoding=\".*?\")\\s*\\?>",
							Pattern.CASE_INSENSITIVE + Pattern.MULTILINE),

			// ampersand changes
		    // eg. <DirT>Add six packets of Sweet & Lite sugar ...
//			Pattern.compile("((name|author)=\".*?)[&](\\s+.*?\"\\s*(>|author=|unit=|qty=))",
//						    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE),
	        Pattern.compile("&(?!\\w+;)"),
							
							
							// quote in attributes, eg. name="green tomatoes in 3/4" dice"
			// General attribute-quotes fixer
			Pattern.compile("(<\\w+)"+ // start tag
						    "((?:\\s*\\w+=\"[^\"]*?\"(?=\\s*(?:>|\\w+=)))*)" + // a number of wellformed attributes
							"( \\s*\\w+=\""+ // attribute name
							"     [^\"]*?)"+  // start of the value
							"   [\"]"+ // the illegal quote
							"   (?!\\s*(?:>|\\w+=))"+ // NOT followed by another attribute value 
							"   (.*?\")"+ // the rest of the value
							"\\s*>" // end-of-tag
							,
					        Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.COMMENTS),		        
						    
	};
	
	private String[] xmlFixingStrings = new String[] {
			"\n",
			"",
			"$1 $3 $2 ?>",
			"&amp;",
			"$1$2$3&quot;$4>",
	};

	public MasterCookXmlFixerReader(Reader in) {
		this.in = in;
		buffer = new char[BUFFER_SIZE + 1000]; // extra to allow the fixes to grow the string
	}
	
	private void fixBuffer() {
		if (buflength < 1) {
			return;
		}
		
		
		
		String s = new String(buffer, 0, buflength);

		for (int i = 0; i < xmlFixes.length; i++) {
			// repeat the substition as long as there are matches.
			// Work for ingredient names with several quotes eg. ...in stribes 4" to 5" long...
			for (Matcher matcher = xmlFixes[i].matcher(s); matcher.find(); matcher = xmlFixes[i].matcher(s)) {
				s = matcher.replaceAll(xmlFixingStrings[i]);
			}
		}

		// copy the result to the buffer
		char[] result = s.toCharArray();
		System.arraycopy(result, 0, buffer, 0, result.length);
		buflength = result.length;
	}
	
	/**
	 * Refill the buffer from the input stream.
	 * @throws IOException
	 */
	private void refillBuffer() throws IOException {
		int charsread = in.read(buffer, buflength, BUFFER_SIZE-buflength);
		if (charsread>0) {
			buflength += charsread;
			fixBuffer();
		}
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// closed?
		if (buffer == null) {
			return -1;
		}
		
		if (len > BUFFER_SIZE) {
			len = BUFFER_SIZE;
		}
		
		// refill the buffer?
		if (buflength < len || buflength < 1000) {
			refillBuffer();
		}
		
		// still empty?
		if (buflength == 0) {
			return -1; // end of stream
		}
		
		int charsread = Math.min(len, buflength);
		System.arraycopy(buffer, 0, cbuf, off, charsread);
		/* TODO: remove this 
		for (int i = 0; i < charsread; i++) {
			cbuf[off+i] = buffer[i];
		} */
		
		// delete the read part.
		for (int i=charsread; i<buflength; i++) {
			buffer[i-charsread] = buffer[i];
		}
		buflength -= charsread;
		
		return charsread;
	}

	@Override
	public void close() throws IOException {
		buflength = 0;
		buffer = null;
	}
	
	@Override
	public void mark(int readAheadLimit) throws IOException {
		in.mark(readAheadLimit);
	}
	
	@Override
	public boolean markSupported() {
		return in.markSupported();
	}
	
    @Override
	public boolean ready() throws IOException {
		return in.ready();
	}



}
