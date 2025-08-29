/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;
import net.sf.recipetools.javarecipes.model.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author ft
 *
 */
public class MasterCookXmlParser extends DefaultHandler  {
	private static final Logger log = LoggerFactory.getLogger(MasterCookXmlParser.class);
	
	/**
	 * 
	 */
	public MasterCookXmlParser() {
		super();
	}

	private int masterCookVersion = 11;
	private File theMasterCookProgram = null;
	private File currentDir = null;
	
	private boolean ignore = false;
	private StringBuilder elementValue = new StringBuilder();
	private Recipe recipe = null;
	private List<Recipe> allRecipes = new ArrayList<Recipe>();
	private int dirImageIndex = 0;
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (! ignore) {
			elementValue.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if ("CatT".equals(name)) {
			recipe.getCategories().add(new Category(elementValue.toString().trim()));
		} else if ("CpyR".equals(name)) {
			recipe.setCopyright(elementValue.toString().trim());
		} else if ("Desc".equals(name)) {
			recipe.addNote(elementValue.toString().trim());
		} else if ("DirT".equals(name)) {
			recipe.addDirections(elementValue.toString().trim());
		} else if ("Natn".equals(name)) {
			recipe.setCuisine(elementValue.toString().trim());
		} else if ("Srce".equals(name)) {
			recipe.addSource(elementValue.toString().trim());
		} else if ("Note".equals(name)) {
			recipe.addNote(elementValue.toString().trim());
		} else if ("IPrp".equals(name)) {
			// preparation method for last ingredient
			recipe.getIngredients().get(recipe.getIngredients().size()-1)
			            .setProcessing(elementValue.toString().trim());
		} else if ("RcpE".equals(name)) {
			allRecipes.add(recipe);
		} else if ("SrvI".equals(name)) {
			recipe.setServingIdeas(elementValue.toString().trim());
		} else if ("Wine".equals(name)) {
			recipe.setWine(elementValue.toString().trim());
		}
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		ignore = true;
		if ("Summ".equals(name)) {
		} else if ("AltS".equals(name)) {
			recipe.setAltSourceLabel(attributes.getValue("label"));
			recipe.setAltSourceText(attributes.getValue("source"));
		} else if ("AltT".equals(name)) {
			recipe.setTime(attributes.getValue("label"), attributes.getValue("elapsed"));
		} else if ("CatS".equals(name)) {
		} else if ("CatT".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("CpyR".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("Desc".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("DirS".equals(name)) {
			dirImageIndex = 0;
		} else if ("DirT".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
			// MC14: get the image
			if (masterCookVersion >= 14) {
				String imgPath = attributes.getValue("img");
				if (imgPath!=null && imgPath.length() > 0) {
					Image image = readImage(imgPath);
					if (image!=null) {
						recipe.setDirectionImage(dirImageIndex, image);
					}
				}
				dirImageIndex++;
			}
		} else if ("IngR".equals(name)) {
			RecipeIngredient ingr = new RecipeIngredient();
			ingr.setAmount(RecipeIngredient.getNumber(attributes.getValue("qty")));
			ingr.setUnit(new Unit(attributes.getValue("unit")));
			ingr.setIngredient(new Ingredient(attributes.getValue("name")));
			recipe.addIngredient(ingr);
		} else if ("IPrp".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("mx2".equals(name)) {
			allRecipes.clear();
		} else if ("Natn".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("Note".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("Nutr".equals(name)) {
		} else if ("PrpT".equals(name)) {
			recipe.setPreparationTime(attributes.getValue("elapsed"));
		} else if ("RatE".equals(name)) {
			recipe.setRating(attributes.getValue("name"),
						 	 Integer.parseInt(attributes.getValue("value")), 10);
		} else if ("RatS".equals(name)) {
		} else if ("RcpE".equals(name)) {
			// Start new recipe
			recipe = new Recipe();
			recipe.setAuthor(attributes.getValue("author"));
			recipe.setTitle(attributes.getValue("name"));
			
			String imgPath = attributes.getValue("img");
			Image image = readImage(imgPath);
			if (image!=null) {
				recipe.addImage(image);
			}
		} else if ("RTxt".equals(name)) {
		} else if ("Serv".equals(name)) {
			recipe.setServings(Integer.parseInt(attributes.getValue("qty")));
		} else if ("Srce".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("SrvI".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} else if ("TTim".equals(name)) {
			recipe.setTotalTime(attributes.getValue("elapsed"));
		} else if ("Yield".equals(name)) {
			String str = attributes.getValue("qty")+" "+attributes.getValue("unit");
			recipe.setYield(str);
		} else if ("Wine".equals(name)) {
			elementValue.delete(0, elementValue.length());
			ignore = false;
		} 
	}
	
	/**
	 * Read the image from the path.
	 * For MC14: relative to the current directory
	 * For MC<14: relative to the web directory
	 * @param path
	 * @return
	 */
	Image readImage(String path) {
		if (path==null || path.length() == 0) {
			return null;
		}
		
		File f = null;
		if (masterCookVersion >= 14) {
			f = new File(currentDir, path);
		} else {
			File webDir = new File(theMasterCookProgram.getParentFile(), "web");
			f = new File(webDir, path);
		}
		
		if (! f.exists()) {
			log.error("Could not find photo file: "+path);
			return null;
		}
		
		Image image = new Image(f.getName(), f.getAbsolutePath());
		return image;
	}
	
	File convertImagePath(String imagePath) {
		if (masterCookVersion<14) {
			
		} else {
			
		}
		return null;
	}

	/**
	 * @return the allRecipes
	 */
	public List<Recipe> getAllRecipes() {
		return allRecipes;
	}

	/**
	 * @param allRecipes the allRecipes to set
	 */
	public void setAllRecipes(List<Recipe> allRecipes) {
		this.allRecipes = allRecipes;
	}

	/**
	 * @return the masterCookVersion
	 */
	public int getMasterCookVersion() {
		return masterCookVersion;
	}

	/**
	 * @param masterCookVersion the masterCookVersion to set
	 */
	public void setMasterCookVersion(int masterCookVersion) {
		this.masterCookVersion = masterCookVersion;
	}

	/**
	 * @return the theMasterCookProgram
	 */
	public File getTheMasterCookProgram() {
		return theMasterCookProgram;
	}

	/**
	 * @param theMasterCookProgram the theMasterCookProgram to set
	 */
	public void setTheMasterCookProgram(File theMasterCookProgram) {
		this.theMasterCookProgram = theMasterCookProgram;
	}

	/**
	 * @return the currentDir
	 */
	public File getCurrentDir() {
		return currentDir;
	}

	/**
	 * @param currentDir the currentDir to set
	 */
	public void setCurrentDir(File currentDir) {
		this.currentDir = currentDir;
	}
}
