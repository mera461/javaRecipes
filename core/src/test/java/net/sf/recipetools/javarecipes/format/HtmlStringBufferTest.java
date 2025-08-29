/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * @author Frank
 *
 */
public class HtmlStringBufferTest {

	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.format.HtmlStringBuffer#removeHtml()}.
	 */
	@Test
	public void testRemoveHtml() {
		//fail("Not yet implemented");
	}

	@Test
	public void testRemoveEndOfLineSpaces() {
		HtmlStringBuffer b = new HtmlStringBuffer("test\ntest1 \ntest2    ");
		assertEquals("test\ntest1\ntest2", b.trimEndOfLines().toString());
	}
	
	@Test
	public void testTrimTrailingLines() {
		HtmlStringBuffer b = new HtmlStringBuffer(" \n\ntest\ntest1\ntest2\n  \n    ");
		assertEquals("test\ntest1\ntest2", b.trimTrailingLines().toString());
	}
	
	/**
	 * Test method for {@link net.sf.recipetools.javarecipes.format.HtmlStringBuffer#preprocessHtml()}.
	 */
	@Test
	public void testPreprocessHtml() {
		String t = "<ol> "
					+"								<li>Preheat oven to 350&deg;F.</li>"
					+"												<li>Cream margarine, sugar and eggs."
					+"</li>"
					+"												<li>Add vanilla."
+"</li>"
+"																	<li>In separate bowl, mix flour, soda and salt."
+"</li>";
		HtmlStringBuffer b = new HtmlStringBuffer(t);
		b.preprocessHtml();
		assertEquals("Preheat oven to 350°F.\n\nCream margarine, sugar and eggs.\n\nAdd vanilla.\n\nIn separate bowl, mix flour, soda and salt.",
				     b.toString());
		
	}

	@Test
	public void testParagraphBreaks() {
		String t = "										<div class='steps'>"+
				"				<ol>" +
				"													<li>Melt butter in a 4-5 quart pan over medium heat.  Stir in flour until bubbly.  Stir in green onion, yellow onion, bell pepper, celery, garlic, bay leaf, thyme and basil.</li>" +
				"																	<li>Reduce heat to low and cook, uncovered, stirring often until vegetables are soft, 20-30 minutes.</li>" +
				"																	<li>Add the tomato sauce, wine, clam juice, water, Worcestershire sauce, white pepper and hot pepper sauce to taste.  Bring to a boil while stirring over high heat.</li>" +
				"																	<li>Turn down heat and simmer, uncovered, stirring occasionally, until thickened and reduced to 4 1/2 to 4 3/4 cups, (about 45 minutes).</li>" +
				"																	<li>Stir in lemon peel, lemon juice, parsley and shrimp. Simmer until shrimp are heated through.</li>" +
				"" +
				"																	<li>Serve over hot cooked rice. Makes 6 servings.</li>" +
				"												</ol>";
		HtmlStringBuffer b = new HtmlStringBuffer(t);
		b.preprocessHtml();
		assertEquals("Melt butter in a 4-5 quart pan over medium heat. Stir in flour until bubbly. Stir in green onion, yellow onion, bell pepper, celery, garlic, bay leaf, thyme and basil.\n\n" +
				"Reduce heat to low and cook, uncovered, stirring often until vegetables are soft, 20-30 minutes.\n\n" +
				"Add the tomato sauce, wine, clam juice, water, Worcestershire sauce, white pepper and hot pepper sauce to taste. Bring to a boil while stirring over high heat.\n\n" +
				"Turn down heat and simmer, uncovered, stirring occasionally, until thickened and reduced to 4 1/2 to 4 3/4 cups, (about 45 minutes).\n\n" +
				"Stir in lemon peel, lemon juice, parsley and shrimp. Simmer until shrimp are heated through.\n\n" +
				"Serve over hot cooked rice. Makes 6 servings.",
				b.toString());
		t = "25 to175 ml)</li></ul></div><h2>Filling</h2><div>next";
		b = new HtmlStringBuffer(t);
		b.preprocessHtml();
		assertEquals("25 to175 ml)\n\nFilling\n\nnext", b.toString());

		// also for </p>
		t = "<p>line 1</p><b>line 2</b><p>line 3</p>";
		b = new HtmlStringBuffer(t);
		b.preprocessHtml();
		assertEquals("line 1\n\nline 2\n\nline 3", b.toString());

		// also for </p>
		t = "<p>line 1</p><p>line 2</p><p>line 3</p>";
		b = new HtmlStringBuffer(t);
		b.preprocessHtml();
		assertEquals("line 1\n\nline 2\n\nline 3", b.toString());

		// but not for </div>
		t = "<div>line 1</div><div>line 2</div><div>line 3</div>";
		b = new HtmlStringBuffer(t);
		b.preprocessHtml();
		assertEquals("line 1\nline 2\nline 3", b.toString());
		
		// TODO: What about: <div>a</div>b<div>c</div>
		
	}

	public void testRemoveEmptyLines() {
		String t = "										<div class='steps'>"+
				"				<ol>" +
				"													<li>Melt butter in a 4-5 quart pan over medium heat.  Stir in flour until bubbly.  Stir in green onion, yellow onion, bell pepper, celery, garlic, bay leaf, thyme and basil.</li>" +
				"																	<li>Reduce heat to low and cook, uncovered, stirring often until vegetables are soft, 20-30 minutes.</li>" +
				"																	<li>Add the tomato sauce, wine, clam juice, water, Worcestershire sauce, white pepper and hot pepper sauce to taste.  Bring to a boil while stirring over high heat.</li>" +
				"																	<li>Turn down heat and simmer, uncovered, stirring occasionally, until thickened and reduced to 4 1/2 to 4 3/4 cups, (about 45 minutes).</li>" +
				"																	<li>Stir in lemon peel, lemon juice, parsley and shrimp. Simmer until shrimp are heated through.</li>" +
				"" +
				"																	<li>Serve over hot cooked rice. Makes 6 servings.</li>" +
				"												</ol>";
		HtmlStringBuffer b = new HtmlStringBuffer(t);
		b.preprocessHtml()
		 .removeEmptyLines();
		assertEquals("Melt butter in a 4-5 quart pan over medium heat. Stir in flour until bubbly. Stir in green onion, yellow onion, bell pepper, celery, garlic, bay leaf, thyme and basil.\n" +
				"Reduce heat to low and cook, uncovered, stirring often until vegetables are soft, 20-30 minutes.\n" +
				"Add the tomato sauce, wine, clam juice, water, Worcestershire sauce, white pepper and hot pepper sauce to taste. Bring to a boil while stirring over high heat.\n" +
				"Turn down heat and simmer, uncovered, stirring occasionally, until thickened and reduced to 4 1/2 to 4 3/4 cups, (about 45 minutes).\n" +
				"Stir in lemon peel, lemon juice, parsley and shrimp. Simmer until shrimp are heated through.\n" +
				"Serve over hot cooked rice. Makes 6 servings.",
				b.toString());
	}
	
	@Test
	public void testFracs() {
		HtmlStringBuffer b = new HtmlStringBuffer("1&frac12; cup sugar");
		b.preprocessHtml();
		assertEquals("1 1/2 cup sugar", b.toString());
		
		b = new HtmlStringBuffer("1&#189; cup sugar");
		b.preprocessHtml();
		assertEquals("1 1/2 cup sugar", b.toString());
		
		assertEquals("2\" x 1/4\" sticks", new HtmlStringBuffer("2\" x 1&#8260;4\" sticks").changeChars().toString());
		assertEquals(" 1/3 cup apple cider", new HtmlStringBuffer("&#8531; cup apple cider").changeChars().toString());
		assertEquals(" 1/3 cup apple cider", new HtmlStringBuffer("\u2153 cup apple cider").changeChars().toString());
		assertEquals(" 1/2 cup water", new HtmlStringBuffer("&frac12; cup water").changeChars().toString());
		assertEquals(" 1/2 cup water", new HtmlStringBuffer("½ cup water").changeChars().toString());
		assertEquals(" 1/4 cup water", new HtmlStringBuffer("¼ cup water").changeChars().toString());
		
		// html fractions
		assertEquals("2 1/4 cup water", new HtmlStringBuffer("2<sup>1</sup>&frasl;<sub>4</sub> cup water").preprocessHtml().toString());
	}
	
	@Test
	public void testComments() {
		HtmlStringBuffer b = new HtmlStringBuffer("1 cup sugar<!--spiral shaped pasta</a>--><!--</a>-->");
		b.preprocessHtml();
		assertEquals("1 cup sugar", b.toString());
	}
	
	@Test
	public void testScripts() {
		HtmlStringBuffer b = new HtmlStringBuffer("1 cup sugar<script charset=\"utf-8\" type=\"text/javascript\" src=\"http://l.yimg.com/a/lib/uh/js/uh-1.3.2.js\"\"></script><script language=javascript>\n"
				+ "if(window.yzq_d==null)window.yzq_d=new Object();"
				+ "var x = '<script lang=>something<\\/script>'; "
				+ "window.yzq_d['zPVcPEPDhFI-']='&U=13hs5dtn8%2fN%3dzPVcPEPDhFI-%2fC%3d650008.13105338.13293890.12621715%2fD%3dHEADR%2fB%3d5530067%2fV%3d1';"
				+ "end test</script><noscript>hallo</noscript>");
		b.preprocessHtml();
		assertEquals("1 cup sugar", b.toString());
	}

	@Test
	public void testPreformatted() {
		HtmlStringBuffer b = new HtmlStringBuffer("<pre>line1\nline2\nline3\n</pre>");
		b.preprocessHtml();
		assertEquals("line1\nline2\nline3", b.toString());
		// test with $ char
		b = new HtmlStringBuffer("<p></p><pre>line1\nline2\nline3\n, CI$</pre>");
		b.preprocessHtml();
		assertEquals("line1\nline2\nline3\n, CI$", b.toString());
	}
	
	@Test
	public void testMultipleLinebreaks() {
		HtmlStringBuffer b = new HtmlStringBuffer("<ol><li><p>1. <B>FOR THE CAKE:</B></p></li><li><p>2. In clean bowl</p></li>");

		b.preprocessHtml();
		assertEquals("1. FOR THE CAKE:\n\n2. In clean bowl", b.toString());
		
		b= new HtmlStringBuffer("<Li>Serve with ...\n</li></ul>BLUE CHEESE SAUCE:<ul><li>Whisk together...");
		b.preprocessHtml();
		assertEquals("Serve with ...\nBLUE CHEESE SAUCE:\n\nWhisk together...", b.toString());
	}

	@Test
	public void testToHtml() {
		HtmlStringBuffer b = new HtmlStringBuffer("xxx æøå xxx © 350°F xxx");

		b.toHtmlChars();
		assertEquals("xxx &aelig;&oslash;&aring; xxx &copy; 350&deg;F xxx", b.toString());
	}

	@Test
	public void testGeneralHtml() {
		String[][] tests = new String[][] {
				{"1⁄2˝-thick789", "1/2\"-thick789"},
				{"xxx<span class='test'>yyy</span>xxx", "xxxyyyxxx"},
				{"123<b>456</b>789", "123456789"},
				{"&#65;&#065;&#x41;&#x004f;", "AAAO"},
				{"A\u0000A\u000bA\u0014A\u000CA", "AAAA\nA"},
				{"1 1/2 teaspoons   <a href='/Spices-and-Flavors/Herbs-and-Spices/Spices/Rosemary-Leaves'>McCormickÂ® Rosemary Leaves</a>","1 1/2 teaspoons McCormickÂ® Rosemary Leaves"},
				//{"", ""},
		};
		
		for (int i=0; i<tests.length; i++) {
			HtmlStringBuffer b = new HtmlStringBuffer(tests[i][0]);
			b.preprocessHtml();
			assertEquals(tests[i][1], b.toString());
		}
	}
	
	@Test
	public void testChangeChars( ) {
		String before = "frozen purple hull peas (about 1 3⁄4 lb.)";
		String after = new HtmlStringBuffer(before).changeChars().toString();
		assertEquals("frozen purple hull peas (about 1 3/4 lb.)", after);
	}

}
