/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ft
 *
 */
public class AutoRecognize extends RecipeTextFormatter {
	public static final int MAX_CHARS_TO_SEARCH_FOR_MARK = 40000;

	RecipeTextFormatter[] formats = new RecipeTextFormatter[] {
		new LivingCookbookFDX(),
		new MasterCookExport(),
		new MasterCookXml(),
		new McTagIt(),
		new MealMaster(),
		new NycExport(),
		new RecipeProcessor2000Format(),
		new AdvantageCooking(),
		new MacGourmetXml(),
		new RezKonvExport(),
	};

	HashMap<Pattern, RecipeTextFormatter> recognizePatterns = new HashMap<Pattern, RecipeTextFormatter>();

	/**
	 * 
	 */
	public AutoRecognize() {
		super();
		for (RecipeTextFormatter rf : formats) {
			recognizePatterns.put(rf.getRecognizePattern(), rf);
		}
	}

	// ---------------------------------------------------------------
	//
	//          reading 
	//
	// ---------------------------------------------------------------
	
    @Override
	public List<Recipe> readRecipes(LineNumberReader in) {
		RecipeTextFormatter formatter = getRecognizedFormatter(in);
		return (formatter==null) ? null : formatter.readRecipes(in);
	}
	
	/**
	 * @param in Input stream
	 * @return the recipe formatter that matches the input
	 */
	public RecipeTextFormatter getRecognizedFormatter(LineNumberReader in) {
		final Logger log = LoggerFactory.getLogger(AutoRecognize.class);
		
		// gmail has a LOT of text (hidden) before the actual mail content
		int recognizeLength = MAX_CHARS_TO_SEARCH_FOR_MARK; 
		char[] buf = new char[recognizeLength];
		try {
			in.mark(recognizeLength);
			in.read(buf);
			in.reset();
		} catch (IOException e) {
			log.error("Error trying to recognize the format.", e);
			throw new RecipeFoxException(e);
		} 
		String str = new String(buf);
		
		for (Pattern p : recognizePatterns.keySet()) {
			if (p!= null && p.matcher(str).find()) {
				return recognizePatterns.get(p);
			}
		}
		return null;
	}
	
	/**
	 * @param str input string with possible recipe
	 * @return True if the given string can be recognized as a recipe format
	 */
	public boolean recognize(String str) {
		LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(str));
		RecipeTextFormatter f = getRecognizedFormatter(lineNumberReader);
		return f != null;
	}
	

	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
    @Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		throw new RecipeFoxException("Writing to the Auto Regonizer not supported.");
	}

}
