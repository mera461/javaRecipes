/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;


/**
 * @author ft
 *
 */
public class MasterCookXmlFixerReaderTest extends TestCase {

	public void testSomeLines() {
		// lines of input and expected result
		String[] lines= {
				// XML header in wrong format
				"<?xml version=\"1.0\" standalone=\"yes\" encoding=\"ISO-8859-1\"?>",
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\" ?>",
				// XML header in correct format
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\" ?>",
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\" ?>",
				// Ingredient name in wrong format
				"<IngR name=\"green tomatoes in 3/4\" dice\" unit=\"gallons\" qty=\"2\"></IngR>",
				"<IngR name=\"green tomatoes in 3/4&quot; dice\" unit=\"gallons\" qty=\"2\"></IngR>",
				// Correct name and author
				"<RcpE name=\"A Drum Of Eggplant And Bucatini  - ...\" author=\"Mary Ann Esposito\">",
				"<RcpE name=\"A Drum Of Eggplant And Bucatini  - ...\" author=\"Mary Ann Esposito\">",
				// Ingredient with two quotes
				"<IngR name=\"carrot sticks - (4\" by 1/2\")\" qty=\"5\">",
				"<IngR name=\"carrot sticks - (4&quot; by 1/2&quot;)\" qty=\"5\">",
				// OTher quotes examples
				"<RcpE name=\"Seafood Chili\" author=\"Diana Shaw, \"The Essential Vegetarian\"\">",
				"<RcpE name=\"Seafood Chili\" author=\"Diana Shaw, &quot;The Essential Vegetarian&quot;\">",
				"<RcpE name=\"Chile Mashed Potatoes\" author=\"Recipe courtesy of \"Cooking with Patrick Clark\", Ten Speed Press\">",
				"<RcpE name=\"Chile Mashed Potatoes\" author=\"Recipe courtesy of &quot;Cooking with Patrick Clark&quot;, Ten Speed Press\">",
				// ampersand examples
				"<RcpE name=\"one & two\">",
				"<RcpE name=\"one &amp; two\">",
				// strange characters
				"abc\u001fdef",
				"abcdef",

		};
		
		for (int i=0; i<lines.length; i+=2) {
			BufferedReader br = new BufferedReader(new StringReader(lines[i]+ "\n"));
			MasterCookXmlFixerReader fixerReader = new MasterCookXmlFixerReader(br);
			BufferedReader in = new BufferedReader(fixerReader);
			
			String line = null;
			try {
				line = in.readLine();
			} catch (IOException e) {
				fail("Expected no exceptions");
			}
			
			assertEquals(lines[i+1], line);
			
			try {
				in.close();
				br.close();
			} catch (IOException e) {
	            throw new RecipeFoxException(e);
			}
		}
		
	}

	public void testXmlReading() {
		try {
			FileReader fileReader = new FileReader("src/test/data/cannin02.mx2");
			MasterCookXmlFixerReader fixerReader = new MasterCookXmlFixerReader(fileReader);
			BufferedReader in = new BufferedReader(fixerReader);
			
			String line = in.readLine();
			assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\" ?>", line);
			// read some more lines
			for (int i=0; i<459; i++) {
				line = in.readLine();
			}
			assertEquals("<IngR name=\"green tomatoes in 3/4&quot; dice\" unit=\"gallons\" qty=\"2\"></IngR>", line);

			// read till EOF
			while ((line=in.readLine()) != null) {}

			in.close();
			
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}
	
	public void testReaderingGoodFile() {
		try {
			FileReader fileReader = new FileReader("src/test/data/italy-03.mx2");
			MasterCookXmlFixerReader fixerReader = new MasterCookXmlFixerReader(fileReader);
			BufferedReader in = new BufferedReader(fixerReader);
			
			String line = in.readLine();
			assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\" ?>", line);
			// read some more lines
			for (int i=0; i<304; i++) {
				line = in.readLine();
			}
			assertEquals("<RcpE name=\"A Drum Of Eggplant And Bucatini  - ...\" author=\"Mary Ann Esposito\">", line);
			
			// read until EOF
			while ((line=in.readLine()) != null) {}
			in.close();
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}
}
