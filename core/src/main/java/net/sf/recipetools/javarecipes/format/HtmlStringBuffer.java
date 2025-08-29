/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extension for StringBuilder
 * 
 * @author ft
 *
 */
public class HtmlStringBuffer {
	
	// change html comments
	private static Pattern changeHtmlComments = Pattern.compile("(<!--.*?-->)",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE  + Pattern.DOTALL); 
	
	// change html linebreaks
	private static Pattern changeOtherLinebreaks = Pattern.compile("(<div|<br|</br|<tr|<h\\d|</h\\d|<\\/ul)",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String changeOtherLinebreaks_Replace = "\n$1";

	// change html paragraph breaks
	private static Pattern changeParagraphBreaks = Pattern.compile("(<p|</p|<li|<h)",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String changeParagraphBreaks_Replace = "\n\n$1";

	// Change linebreaks in <pre> sections to <br>
	private static Pattern linebreaksInPreSection = Pattern.compile("<pre>(.*?)<\\/pre>",
						Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.DOTALL); 
	private static String linebreaksInPreSection_Replace = "<br/>";

	// remove linebreaks in the HTML
	private static Pattern linebreaksInHtml = Pattern.compile("(\\s\n|\n\\s|\n)",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String linebreaksInHtml_Replace = " ";
	// remove all scripts
	private static Pattern removeScript = Pattern.compile("(<script.*?</script>|<noscript.*?</noscript>|<style.*?</style>)",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.DOTALL); 
	private static Pattern removeSpecials = Pattern.compile(
			// special from myrecipes.com:
			// <core:ifNotEqual object1="<%= amount.trim() %> object2="">
			"object2=\">",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.COMMENTS); 
	// remove all tags
	private static Pattern removeTags = Pattern.compile("<[^>]*>",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String removeTags_Replace = "";
	// remove leading spaces
	private static Pattern removeLeadingSpaces = Pattern.compile("^[ \t]+",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String removeLeadingSpaces_Replace = "";
	// replace more than 3 \n to just normal paragraph breaks
	private static Pattern removeMultipleLinebreaks = Pattern.compile("\n\n\n+"); 
	private static String removeMultipleLinebreaks_Replace = "\n\n"; 

	// remove double spaces tags
	private static Pattern removeDoubleSpaces = Pattern.compile("[ \t]{2,}",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String removeDoubleSpaces_Replace = " ";
	// remove spaces at end of line
	private static Pattern removeEndOfLineSpaces = Pattern.compile("[ \t]+$",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String removeEndOfLineSpaces_Replace = "";
	
	// remove empty lines from the beginning or the end
	private static Pattern removeTrailingEmptyLines = Pattern.compile("(^[\\s\\n]+|[\\s\\n]+$)"); 
	private static String removeTrailingEmptyLines_Replace = "";
	
	// remove all empty lines
	private static Pattern removeEmptyLines = Pattern.compile("(^\\s+$)",
			Pattern.MULTILINE); 
	private static String removeEmptyLines_Replace = "";
	
	// change fractions
	private static Pattern supSubFraction = Pattern.compile("<sup>\\s*(\\d+)\\s*</sup>\\s*(?:/|&frasl;)\\s*<sub>\\s*(\\d+)\\s*</sub>",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	private static String supSubFraction_Replace = " $1/$2";
	
	public static HashMap<String, String> chars = new HashMap<String, String>();
	
	static {
		chars.put("&ETH;",    "\u00d0");
		chars.put("&nbsp;",   " ");
		chars.put("&iexcl;",  "¡");
		chars.put("&Ntilde;", "Ñ");
		chars.put("&cent;",   "¢");
		chars.put("&Ograve;", "Ò");
		chars.put("&pound;",  "£");
		chars.put("&Oacute;", "Ó");
		chars.put("&curren;", "¤");
		chars.put("&Ocirc;",  "Ô");
		chars.put("&yen;",    "¥");
		chars.put("&Otilde;", "Õ");
		chars.put("&brvbar;", "¦");
		chars.put("&Ouml;",   "Ö");
		chars.put("&sect;",   "§");
		chars.put("&times;",  "×");
		chars.put("&uml;",    "¨");
		chars.put("&Oslash;", "Ø");
		chars.put("&copy;",   "©");
		chars.put("&Ugrave;", "Ù");
		chars.put("&ordf;",   "ª");
		chars.put("&Uacute;", "Ú");
		chars.put("&laquo;",  "«");
		chars.put("&Ucirc;",  "Û");
		chars.put("&not;",    "¬");
		chars.put("&Uuml;",   "Ü");
		chars.put("&shy;",    "­");
		chars.put("&Yacute;", "\u00dd");
		chars.put("&reg;",    "®");
		chars.put("&THORN;",  "Þ");
		chars.put("&macr;",   "¯");
		chars.put("&szlig;",  "ß");
		chars.put("&deg;",    "°");
		chars.put("&agrave;", "à");
		chars.put("&plusmn;", "±");
		chars.put("&aacute;", "á");
		chars.put("&sup2;",   "²");
		chars.put("&acirc;",  "â");
		chars.put("&sup3;",   "³");
		chars.put("&atilde;", "ã");
		chars.put("&acute;",  "´");
		chars.put("&auml;",   "ä");
		chars.put("&micro;",  "µ");
		chars.put("&aring;",  "å");
		chars.put("&para;",   "¶");
		chars.put("&aelig;",  "æ");
		chars.put("&middot;", "·");
		chars.put("&ccedil;", "ç");
		chars.put("&cedil;",  "¸");
		chars.put("&egrave;", "è");
		chars.put("&sup1;",   "¹");
		chars.put("&eacute;", "é");
		chars.put("&ordm;",   "º");
		chars.put("&ecirc;",  "ê");
		chars.put("&raquo;",  "»");
		chars.put("&euml;",   "ë");
		chars.put("&frac12;", " 1/2");
		chars.put("&frac14;", " 1/4");
		chars.put("&frac34;", " 3/4");
		chars.put("&frasl;", "/");
		chars.put("&igrave;", "ì");
		chars.put("&iacute;", "í");
		chars.put("&icirc;",  "î");
		chars.put("&iquest;", "¿");
		chars.put("&iuml;",   "ï");
		chars.put("&Agrave;", "À");
		chars.put("&eth;",    "ð");
		chars.put("&Aacute;", "\u00c1");
		chars.put("&ntilde;", "ñ");
		chars.put("&Acirc;",  "Â");
		chars.put("&ograve;", "ò");
		chars.put("&Atilde;", "Ã");
		chars.put("&oacute;", "ó");
		chars.put("&Auml;",   "Ä");
		chars.put("&ocirc;",  "ô");
		chars.put("&Aring;",  "Å");
		chars.put("&otilde;", "õ");
		chars.put("&AElig;",  "Æ");
		chars.put("&ouml;",   "ö");
		chars.put("&Ccedil;", "Ç");
		chars.put("&divide;", "÷");
		chars.put("&Egrave;", "È");
		chars.put("&oslash;", "ø");
		chars.put("&Eacute;", "É");
		chars.put("&ugrave;", "ù");
		chars.put("&Ecirc;",  "Ê");
		chars.put("&uacute;", "ú");
		chars.put("&Euml;",   "Ë");
		chars.put("&ucirc;",  "û");
		chars.put("&Igrave;", "Ì");
		chars.put("&uuml;",   "ü");
		chars.put("&Iacute;", "\u00cd");
		chars.put("&yacute;", "ý");
		chars.put("&Icirc;",  "Î");
		chars.put("&thorn;",  "þ");
		chars.put("&Iuml;",   "\u00cf");
		chars.put("&yuml;",   "ÿ");
		chars.put("&mdash;",   "—");
		chars.put("&amp;", "&");
		chars.put("&gt;", ">");
		chars.put("&lt;", "<");
		chars.put("&quot;", "\"");
	}

	StringBuilder txt = null;

	public HtmlStringBuffer() {
		this.txt = new StringBuilder();
	}
	
	public HtmlStringBuffer(String txt) {
		this.txt = new StringBuilder(txt);
	}
	
	public StringBuilder getStringBuffer() {
		return txt;
	}
	
	/**
	 * Primarily used by javascript. Problem with the constructor
	 * @param txt
	 */
	public void setText(String txt) {
		this.txt = new StringBuilder(txt);
	}
	
	/**
	 * @param old
	 * @param newValue
	 * @return
	 */
	public HtmlStringBuffer replaceAll(String old, String newValue) {
		int pos=0;
		// TODO: What about upper/lower casing?
		while ((pos=txt.indexOf(old, pos)) != -1) {
			txt.replace(pos, pos+old.length(), newValue);
		}
		return this;
	}
	
	public HtmlStringBuffer replaceAll(Pattern p, String replacement) {
		Matcher m = p.matcher(txt);
		StringBuffer newTxt = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(newTxt, replacement);
		}
		m.appendTail(newTxt);
		txt = new StringBuilder(newTxt);
		
		return this;
	}

	static private Pattern hexedCharPattern = Pattern.compile("&#x([\\da-fA-F]{1,4})(;)?");
	static private Pattern decimalCharPattern = Pattern.compile("&#([\\d]{1,4})(;)?");
	
	public HtmlStringBuffer changeHexedChars() {

		// other coded chars?
		if (txt.indexOf("&#")>=0) {
			// Change Hex chrs
			if (txt.indexOf("&#x")>=0) {
				Matcher m = hexedCharPattern.matcher(txt);
				StringBuffer newTxt = new StringBuffer();
				while (m.find()) {
					int value = Integer.parseInt(m.group(1), 16);
					m.appendReplacement(newTxt, new Character((char) value).toString());
				}
				m.appendTail(newTxt);
				txt = new StringBuilder(newTxt);
			}
			// change decimal chars
			if (txt.indexOf("&#")>=0) {
				Matcher m = decimalCharPattern.matcher(txt);
				StringBuffer newTxt = new StringBuffer();
				while (m.find()) {
					int value = Integer.parseInt(m.group(1), 10);
					m.appendReplacement(newTxt, new Character((char) value).toString());
				}
				m.appendTail(newTxt);
				txt = new StringBuilder(newTxt);
			}
		}
		
		changeUtfNumbers();
		
		// smart quotes: http://en.wikipedia.org/wiki/Smart_quotes#Smart_quotes
		replaceAll("\u2018",  "'"); // smart single quote
		replaceAll("\u2019",  "'"); // smart single quote
		replaceAll("\u201A",  "'"); // smart single quote
		replaceAll("\u201B",  "'"); // smart single quote
		replaceAll("\u201C",  "\""); // smart double quote
		replaceAll("\u201D",  "\""); // smart double quote
		replaceAll("\u201E",  "\""); // smart double quote
		replaceAll("\u201F",  "\""); // smart double quote
		replaceAll("\u2032",  "'");
		replaceAll("\u2035",  "'");
		replaceAll("\u2033",  "\"");
		replaceAll("\u2036",  "\"");
		replaceAll("\u02dd",  "\""); // double acute accent
		
		// dashes: http://en.wikipedia.org/wiki/Dash
		replaceAll("\u2012",  "-"); // figure dash
		replaceAll("\u2013",  "-"); // en dash
		replaceAll("\u2014",  "-"); // em dash
		replaceAll("\u2015",  "-"); // horizontal bar
		
		// special concatinated letters
		replaceAll("\ufb00",  "ff"); // ff
		replaceAll("\ufb01",  "fi"); // fi
		replaceAll("\ufb02",  "fl"); // fl
		replaceAll("\ufb03",  "ffi"); // ffi
		replaceAll("\ufb04",  "ffl"); // ffl
		replaceAll("\ufb05",  "ft"); // ft
		replaceAll("\ufb06",  "st"); // st

		// special spaces
		replaceAll("\u2028", " "); // 8232 line separator
		replaceAll("\u2029", " "); // 8233 paragraph separator
		replaceAll("\uc2a0", " "); // non-breaking space
		replaceAll("\u00a0", " "); // non-breaking space
  		

		return this;
	}

	public HtmlStringBuffer changeUtfNumbers() {
		// change special unicode chars til ascii chars.
		// specials for ½ etc
		replaceAll("\u00bc",  " 1/4"); // ¼ -> 1/4
		replaceAll("\u00bd",  " 1/2"); // ¼ -> 1/2
		replaceAll("\u00be",  " 3/4"); // ¼ -> 3/4
		
		replaceAll("\u2150",  " 1/7");
		replaceAll("\u2151",  " 1/9");
		replaceAll("\u2152",  " 1/10");
		replaceAll("\u2153",  " 1/3"); //  -> 1/3
		replaceAll("\u2154",  " 2/3");
		replaceAll("\u2155",  " 1/5");
		replaceAll("\u2156",  " 2/5");
		replaceAll("\u2157",  " 3/5");
		replaceAll("\u2158",  " 4/5");
		replaceAll("\u2159",  " 1/6");
		replaceAll("\u215A",  " 5/6");
		replaceAll("\u215B",  " 1/8");
		replaceAll("\u215C",  " 3/8");
		replaceAll("\u215D",  " 5/8");
		replaceAll("\u215E",  " 7/8");
		// other
		replaceAll("\u00C2\u00BC", " 1/4"); // ¼ -> 1/4
		replaceAll("\u00C2\u00BD", " 1/2"); // ¼ -> 1/2
		replaceAll("\u00C2\u00BE", " 3/4"); // ¼ -> 3/4
		
		replaceAll("×", 	  "x"); // the symbol 'x' -> the letter x
		replaceAll("\u2044",  "/"); // Fraction Slash
		
		return this;
	}

	
	
	/**
	 * Change all HTML special characters like &amp; --> &
	 * @param txt the txt to update
	 * @return the updated txt
	 */
	public HtmlStringBuffer changeEscapedChars() {
		for (String escaped : chars.keySet()) {
			replaceAll(escaped, chars.get(escaped));
		}
		return this;
	}
	
	/**
	 * Change all special characters with the HTML escaped char
	 * @param txt the txt to update
	 * @return the updated txt
	 */
	public HtmlStringBuffer toHtmlChars() {
		for (Entry<String, String> entry : chars.entrySet()) {
			if (!"&amp;".equals(entry.getKey()) && ! "&nbsp;".equals(entry.getKey())) {
				replaceAll(entry.getValue(), entry.getKey());
			}
		}
		return this;
	}

	static private Pattern charToLinebreakPattern = Pattern.compile("[\u000C]");
	static private Pattern charToRemovePattern = Pattern.compile("[\u0000-\u0009\u000B\u000E-\u001F]");
	
	
	/**
	 * Remove any invalid xml chars.
	 * @return
	 */
	public HtmlStringBuffer removeInvalidChars() {
		replaceAll(charToLinebreakPattern, "\n");
		replaceAll(charToRemovePattern, "");
		return this;
	}
	
	public HtmlStringBuffer changeChars() {
		changeEscapedChars();
		changeHexedChars();
		removeInvalidChars();
		return this;
	}

	public HtmlStringBuffer changeLinebreaksInPre () {
		Matcher m = linebreaksInPreSection.matcher(txt);
		StringBuffer newTxt = new StringBuffer();
		while (m.find()) {
			String preTxt = m.group(1);
			m.appendReplacement(newTxt, preTxt.replaceAll("[\n\r]", linebreaksInPreSection_Replace).replace("$", "\\$"));
		}
		m.appendTail(newTxt);
		txt = new StringBuilder(newTxt);
		
		return this;
	}	
	
	public HtmlStringBuffer changeLinebreaks () {
		// Change linebreaks in <pre> sections to <br>
		changeLinebreaksInPre();

		// remove linebreaks in the HTML
		replaceAll(linebreaksInHtml, linebreaksInHtml_Replace);

		// change <br> to linebreaks
		replaceAll("<br>", "\n");

		// other tags to change to linebreaks
		replaceAll(changeOtherLinebreaks, changeOtherLinebreaks_Replace);
		replaceAll(changeParagraphBreaks, changeParagraphBreaks_Replace);
		
		return this;
	}
	
	public HtmlStringBuffer removeHtml() {
	    // remove all script tags
	    replaceAll(removeScript, "");
		//txt = new StringBuilder(removeScript.matcher(txt).replaceAll(""));
		// remove all HTML comments first
		//txt = new StringBuilder(changeHtmlComments.matcher(txt).replaceAll(""));
		replaceAll(changeHtmlComments, "");
		// remove special HTML.
		replaceAll(removeSpecials, "");

		// change linebreaks before removing all tags.
	    changeLinebreaks();
	    
	    // change strange sup/sub fractions
	    replaceAll(supSubFraction, supSubFraction_Replace);
		
		// remove all tags
	    replaceAll(removeTags, removeTags_Replace);
	    // remove leading spaces
	    replaceAll(removeLeadingSpaces, removeLeadingSpaces_Replace);
	    // remove all double spaces
	    replaceAll(removeDoubleSpaces, removeDoubleSpaces_Replace);
	    // remove multiple linebreaks (3 or more)
	    replaceAll(removeMultipleLinebreaks, removeMultipleLinebreaks_Replace);

	    return this;
	}
	
	/**
	 * Remove tailing space of each line.
	 * @return this
	 */
	public HtmlStringBuffer trimEndOfLines() {
	    replaceAll(removeEndOfLineSpaces, removeEndOfLineSpaces_Replace);
		return this;
	}

	/**
	 * Remove tailing empty lines.
	 * @return this
	 */
	public HtmlStringBuffer trimTrailingLines() {
	    replaceAll(removeTrailingEmptyLines, removeTrailingEmptyLines_Replace);
		return this;
	}

	/**
	 * Remove all empty lines.
	 * @return this
	 */
	public HtmlStringBuffer removeEmptyLines() {
	    replaceAll(removeEmptyLines, removeEmptyLines_Replace);
		return this;
	}

	
	public HtmlStringBuffer preprocessHtml () {
	    removeHtml();
		changeChars();
	    trimEndOfLines();
	    trimTrailingLines();
		return this;
	}
	
	public String[] toLines() {
		return txt.toString().split("\n");
	}
	
	
	// ----------------------------------------------------------------------
	//
	// Methods delegated from StringBuilder txt
	//
	// ----------------------------------------------------------------------
	

	/**
	 * @param b
	 * @return
	 * @see java.lang.StringBuilder#append(boolean)
	 */
	public StringBuilder append(boolean b) {
		return txt.append(b);
	}

	/**
	 * @param c
	 * @return
	 * @see java.lang.StringBuilder#append(char)
	 */
	public StringBuilder append(char c) {
		return txt.append(c);
	}

	/**
	 * @param str
	 * @param offset
	 * @param len
	 * @return
	 * @see java.lang.StringBuilder#append(char[], int, int)
	 */
	public StringBuilder append(char[] str, int offset, int len) {
		return txt.append(str, offset, len);
	}

	/**
	 * @param str
	 * @return
	 * @see java.lang.StringBuilder#append(char[])
	 */
	public StringBuilder append(char[] str) {
		return txt.append(str);
	}

	/**
	 * @param s
	 * @param start
	 * @param end
	 * @return
	 * @see java.lang.StringBuilder#append(java.lang.CharSequence, int, int)
	 */
	public StringBuilder append(CharSequence s, int start, int end) {
		return txt.append(s, start, end);
	}

	/**
	 * @param s
	 * @return
	 * @see java.lang.StringBuilder#append(java.lang.CharSequence)
	 */
	public StringBuilder append(CharSequence s) {
		return txt.append(s);
	}

	/**
	 * @param d
	 * @return
	 * @see java.lang.StringBuilder#append(double)
	 */
	public StringBuilder append(double d) {
		return txt.append(d);
	}

	/**
	 * @param f
	 * @return
	 * @see java.lang.StringBuilder#append(float)
	 */
	public StringBuilder append(float f) {
		return txt.append(f);
	}

	/**
	 * @param i
	 * @return
	 * @see java.lang.StringBuilder#append(int)
	 */
	public StringBuilder append(int i) {
		return txt.append(i);
	}

	/**
	 * @param lng
	 * @return
	 * @see java.lang.StringBuilder#append(long)
	 */
	public StringBuilder append(long lng) {
		return txt.append(lng);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.StringBuilder#append(java.lang.Object)
	 */
	public StringBuilder append(Object obj) {
		return txt.append(obj);
	}

	/**
	 * @param str
	 * @return
	 * @see java.lang.StringBuilder#append(java.lang.String)
	 */
	public StringBuilder append(String str) {
		return txt.append(str);
	}

	/**
	 * @param sb
	 * @return
	 * @see java.lang.StringBuilder#append(java.lang.StringBuilder)
	 */
	public StringBuilder append(StringBuilder sb) {
		return txt.append(sb);
	}

	/**
	 * @param codePoint
	 * @return
	 * @see java.lang.StringBuilder#appendCodePoint(int)
	 */
	public StringBuilder appendCodePoint(int codePoint) {
		return txt.appendCodePoint(codePoint);
	}

	/**
	 * @return
	 * @see java.lang.StringBuilder#capacity()
	 */
	public int capacity() {
		return txt.capacity();
	}

	/**
	 * @param index
	 * @return
	 * @see java.lang.StringBuilder#charAt(int)
	 */
	public char charAt(int index) {
		return txt.charAt(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.lang.StringBuilder#codePointAt(int)
	 */
	public int codePointAt(int index) {
		return txt.codePointAt(index);
	}

	/**
	 * @param index
	 * @return
	 * @see java.lang.StringBuilder#codePointBefore(int)
	 */
	public int codePointBefore(int index) {
		return txt.codePointBefore(index);
	}

	/**
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 * @see java.lang.StringBuilder#codePointCount(int, int)
	 */
	public int codePointCount(int beginIndex, int endIndex) {
		return txt.codePointCount(beginIndex, endIndex);
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 * @see java.lang.StringBuilder#delete(int, int)
	 */
	public StringBuilder delete(int start, int end) {
		return txt.delete(start, end);
	}

	/**
	 * @param index
	 * @return
	 * @see java.lang.StringBuilder#deleteCharAt(int)
	 */
	public StringBuilder deleteCharAt(int index) {
		return txt.deleteCharAt(index);
	}

	/**
	 * @param minimumCapacity
	 * @see java.lang.StringBuilder#ensureCapacity(int)
	 */
	public void ensureCapacity(int minimumCapacity) {
		txt.ensureCapacity(minimumCapacity);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof String) txt.equals(obj);
		if (obj instanceof HtmlStringBuffer) txt.equals(((HtmlStringBuffer)obj).txt);
		return false;
	}

	/**
	 * @param srcBegin
	 * @param srcEnd
	 * @param dst
	 * @param dstBegin
	 * @see java.lang.StringBuilder#getChars(int, int, char[], int)
	 */
	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		txt.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
    @Override
	public int hashCode() {
		return txt.hashCode();
	}

	/**
	 * @param str
	 * @param fromIndex
	 * @return
	 * @see java.lang.StringBuilder#indexOf(java.lang.String, int)
	 */
	public int indexOf(String str, int fromIndex) {
		return txt.indexOf(str, fromIndex);
	}

	/**
	 * @param str
	 * @return
	 * @see java.lang.StringBuilder#indexOf(java.lang.String)
	 */
	public int indexOf(String str) {
		return txt.indexOf(str);
	}

	/**
	 * @param offset
	 * @param b
	 * @return
	 * @see java.lang.StringBuilder#insert(int, boolean)
	 */
	public StringBuilder insert(int offset, boolean b) {
		return txt.insert(offset, b);
	}

	/**
	 * @param offset
	 * @param c
	 * @return
	 * @see java.lang.StringBuilder#insert(int, char)
	 */
	public StringBuilder insert(int offset, char c) {
		return txt.insert(offset, c);
	}

	/**
	 * @param index
	 * @param str
	 * @param offset
	 * @param len
	 * @return
	 * @see java.lang.StringBuilder#insert(int, char[], int, int)
	 */
	public StringBuilder insert(int index, char[] str, int offset, int len) {
		return txt.insert(index, str, offset, len);
	}

	/**
	 * @param offset
	 * @param str
	 * @return
	 * @see java.lang.StringBuilder#insert(int, char[])
	 */
	public StringBuilder insert(int offset, char[] str) {
		return txt.insert(offset, str);
	}

	/**
	 * @param dstOffset
	 * @param s
	 * @param start
	 * @param end
	 * @return
	 * @see java.lang.StringBuilder#insert(int, java.lang.CharSequence, int, int)
	 */
	public StringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
		return txt.insert(dstOffset, s, start, end);
	}

	/**
	 * @param dstOffset
	 * @param s
	 * @return
	 * @see java.lang.StringBuilder#insert(int, java.lang.CharSequence)
	 */
	public StringBuilder insert(int dstOffset, CharSequence s) {
		return txt.insert(dstOffset, s);
	}

	/**
	 * @param offset
	 * @param d
	 * @return
	 * @see java.lang.StringBuilder#insert(int, double)
	 */
	public StringBuilder insert(int offset, double d) {
		return txt.insert(offset, d);
	}

	/**
	 * @param offset
	 * @param f
	 * @return
	 * @see java.lang.StringBuilder#insert(int, float)
	 */
	public StringBuilder insert(int offset, float f) {
		return txt.insert(offset, f);
	}

	/**
	 * @param offset
	 * @param i
	 * @return
	 * @see java.lang.StringBuilder#insert(int, int)
	 */
	public StringBuilder insert(int offset, int i) {
		return txt.insert(offset, i);
	}

	/**
	 * @param offset
	 * @param l
	 * @return
	 * @see java.lang.StringBuilder#insert(int, long)
	 */
	public StringBuilder insert(int offset, long l) {
		return txt.insert(offset, l);
	}

	/**
	 * @param offset
	 * @param obj
	 * @return
	 * @see java.lang.StringBuilder#insert(int, java.lang.Object)
	 */
	public StringBuilder insert(int offset, Object obj) {
		return txt.insert(offset, obj);
	}

	/**
	 * @param offset
	 * @param str
	 * @return
	 * @see java.lang.StringBuilder#insert(int, java.lang.String)
	 */
	public StringBuilder insert(int offset, String str) {
		return txt.insert(offset, str);
	}

	/**
	 * @param str
	 * @param fromIndex
	 * @return
	 * @see java.lang.StringBuilder#lastIndexOf(java.lang.String, int)
	 */
	public int lastIndexOf(String str, int fromIndex) {
		return txt.lastIndexOf(str, fromIndex);
	}

	/**
	 * @param str
	 * @return
	 * @see java.lang.StringBuilder#lastIndexOf(java.lang.String)
	 */
	public int lastIndexOf(String str) {
		return txt.lastIndexOf(str);
	}

	/**
	 * @return
	 * @see java.lang.StringBuilder#length()
	 */
	public int length() {
		return txt.length();
	}

	/**
	 * @param index
	 * @param codePointOffset
	 * @return
	 * @see java.lang.StringBuilder#offsetByCodePoints(int, int)
	 */
	public int offsetByCodePoints(int index, int codePointOffset) {
		return txt.offsetByCodePoints(index, codePointOffset);
	}

	/**
	 * @param start
	 * @param end
	 * @param str
	 * @return
	 * @see java.lang.StringBuilder#replace(int, int, java.lang.String)
	 */
	public StringBuilder replace(int start, int end, String str) {
		return txt.replace(start, end, str);
	}

	/**
	 * @return
	 * @see java.lang.StringBuilder#reverse()
	 */
	public StringBuilder reverse() {
		return txt.reverse();
	}

	/**
	 * @param index
	 * @param ch
	 * @see java.lang.StringBuilder#setCharAt(int, char)
	 */
	public void setCharAt(int index, char ch) {
		txt.setCharAt(index, ch);
	}

	/**
	 * @param newLength
	 * @see java.lang.StringBuilder#setLength(int)
	 */
	public void setLength(int newLength) {
		txt.setLength(newLength);
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 * @see java.lang.StringBuilder#subSequence(int, int)
	 */
	public CharSequence subSequence(int start, int end) {
		return txt.subSequence(start, end);
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 * @see java.lang.StringBuilder#substring(int, int)
	 */
	public String substring(int start, int end) {
		return txt.substring(start, end);
	}

	/**
	 * @param start
	 * @return
	 * @see java.lang.StringBuilder#substring(int)
	 */
	public String substring(int start) {
		return txt.substring(start);
	}

	/**
	 * @return
	 * @see java.lang.StringBuilder#toString()
	 */
	@Override
	public String toString() {
		return txt.toString();
	}

	/**
	 * 
	 * @see java.lang.StringBuilder#trimToSize()
	 */
	public void trimToSize() {
		txt.trimToSize();
	}

	
	
	
}
