/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sf.recipetools.javarecipes.model.RecipeFoxException;


/**
 * @author Frank
 *
 */
public class HtmlStringBufferPerformanceTest {

	//@Test
	public void testPerformance() {
		String s = getContent();
		for (int i=0; i<100; i++) {
			//System.out.println(i);
			HtmlStringBuffer b = new HtmlStringBuffer(s);
			b.preprocessHtml().toString();
		}
	}
	
	String getContent() {
		StringBuilder b = new StringBuilder();
		String line = null;
		File f = new File("c:/temp/huge.txt");
		try {
			BufferedReader myInput = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			while ((line = myInput.readLine()) != null) {
				b.append(line);
			}
			myInput.close();
		} catch (FileNotFoundException e) {
            throw new RecipeFoxException(e);
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
		return b.toString();
	}
	
}
