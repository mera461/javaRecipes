/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;


/**
 * @author ft
 *
 */
public class MacGourmetXml extends RecipeTextFormatter implements AllFileInOneGo {
	private static final Logger log = LoggerFactory.getLogger(MacGourmetXml.class); 
	
	private XMLReader xr = null;
	private MacGourmetXmlParser handler = new MacGourmetXmlParser(); 
	
	/**
	 * 
	 */
	public MacGourmetXml() {
		super();
		try {
			xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(handler);
			// Turn off validation
			xr.setFeature("http://xml.org/sax/features/validation", false);
			xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); 
		} catch (SAXException e) {
			log.error("Error creating a XML parser", e);
			throw new RecipeFoxException(e);
		}
	}
	
	public String getDefaultCharacterSet() {
		return "UTF-8";
	}


	// ---------------------------------------------------------------
	//
	//          reading 
	//
	// ---------------------------------------------------------------
	
	private LineNumberReader lastInput = null;
	
    @Override
	public List<Recipe> readRecipes(LineNumberReader in) {
		// dont use the same stream twice
		if (lastInput!=null && in==lastInput) {
			// Reading same input so stop it.
			return new ArrayList<Recipe>();
		}
		lastInput = in;
		
		try {
			xr.parse(new InputSource(new MasterCookXmlFixerReader(in)));
		} catch (IOException e) {
			log.error("Input error in the XML parser", e);
			throw new RecipeFoxException(e);
		} catch (SAXException e) {
			log.error("Input error in the XML parser", e);
			throw new RecipeFoxException(e);
		}
		
		return handler.getAllRecipes();
	}

	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
    @Override
	public void writeFileHeader(PrintWriter out) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
		out.println("<plist version=\"1.0\">");
		out.println("<array>");
	}

    @Override
	public void writeFileTail(PrintWriter out) {
		out.println("</array>");
		out.println("</plist>");
	}
	
    @Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		out.println("<dict>");
		writeIntKey(out, "AFFILIATE_ID", -1);
		if (recipe.getCategories()!=null && recipe.getCategories().size()>0) {
			out.println("<key>CATEGORIES</key>");
			out.println("<array>");
			for (Category c : recipe.getCategories()) {
				out.println("<dict>");
				writeIntKey(out, "CATEGORY_ID", -1);
				writeIntKey(out, "ITEM_TYPE_ID", 102);
				writeStringKey(out, "NAME", c.getName());
				writeBooleanKey(out, "USER_ADDED", true);
				out.println("</dict>");
			}
			out.println("</array>");
		}
		writeIntKey(out, "COURSE_ID", -1);
		writeStringKey(out, "COURSE_NAME", "");
		writeIntKey(out, "CUISINE_ID", -1);
		writeIntKey(out, "DIFFICULTY", 0);
		String str = recipe.getDirectionsAsString();
		str += "\n\nTIPS:\n" + recipe.getTipsAsString(); 
		writeStringKey(out, "DIRECTIONS", str);
		if (recipe.getImages().size() > 0) {
			writeStringKey(out, "EXPORT_TYPE", "BINARY");
			writeStringKey(out, "IMAGE", recipe.getImages().get(0).encodeAsBase64());
		}
		
		if (recipe.getIngredients().size() > 0) {
			out.println("<key>INGREDIENTS</key>");
			out.println("<array>");
			for (RecipeIngredient ingr : recipe.getIngredients()) {
				out.println("<dict>");
				if (ingr.hasIngredient()) {
					writeStringKey(out, "DESCRIPTION", ingr.getIngredient().getName());
				}
				if (ingr.hasProcessing()) {
					writeStringKey(out, "DIRECTION", ingr.getProcessing());
				}
				writeIntKey(out, "INCLUDED_RECIPE_ID", -1);
				writeBooleanKey(out, "IS_DIVIDER", ingr.getType() == RecipeIngredient.TYPE_SUBTITLE);
				writeBooleanKey(out, "IS_MAIN", false);
				if (ingr.hasUnit()) {
					writeStringKey(out, "MEASUREMENT", ingr.getPluralisedUnitName());
				}
				writeStringKey(out, "QUANTITY", FormattingUtils.formatNumber(ingr.getAmount()));
				out.println("</dict>");
			}
			out.println("</array>");
		}		
		writeStringKey(out, "KEYWORDS", "");
		writeIntKey(out, "MEASUREMENT_SYSTEM", 0);
		writeStringKey(out, "NAME", recipe.getTitle());
		writeStringKey(out, "NOTE", recipe.getNote());
		writeStringKey(out, "NUTRITION", recipe.getNutritionalInfo());
		writeStringKey(out, "PUBLICATION_PAGE", "");
		writeIntKey(out, "SERVINGS", recipe.getServings());
		writeStringKey(out, "SOURCE", recipe.getSource());
		writeStringKey(out, "SUMMARY", recipe.getDescription());
		writeIntKey(out, "TYPE", -1);
		writeStringKey(out, "URL", recipe.getUrl());
		writeStringKey(out, "YIELD", recipe.getYield());
		out.println("</dict>");
		// TODO:
		//<key>PREP_TIMES</key>
		//<array>
	}
	
	void writeIntKey(PrintWriter out, String key, int value) {
		out.println("<key>"+key+"</key>");
		out.println("<integer>"+value+"</integer>");
	}
	
	void writeStringKey(PrintWriter out, String key, String value) {
		out.println("<key>"+key+"</key>");
		out.println("<string>"+value+"</string>");
	}

	void writeBooleanKey(PrintWriter out, String key, boolean value) {
		out.println("<key>"+key+"</key>");
		out.println(value ? "<true/>" : "<false/>");
	}
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\"", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}
	
}
