/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Image.ImageFormat;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author ft
 *
 */
public class MasterCookXml extends RecipeTextFormatter implements AllFileInOneGo {
	private static final Logger log = LoggerFactory.getLogger(MasterCookXml.class);
	
	private XMLReader xr = null;
	private MasterCookXmlParser handler = new MasterCookXmlParser(); 
	
	private File theMasterCookProgram = null;
	private int masterCookVersion = 14; // defaults to MC version 14 with relative image paths
	
	// Map RecipeIngredient.TYPE_??? to Ingredient codes
	private static char[] IngredientCodes = new char[] {'I', 'S', 'T', 'R'};
	
	/**
	 * 
	 */
	public MasterCookXml() {
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

		// configure the handler
		handler.setCurrentDir(getImageDir());
		
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
		out.println("<?xml version=\"1.0\" encoding=\"win-1252\" standalone=\"yes\" ?>");
		out.println("<!DOCTYPE mx2 SYSTEM \"mx2.dtd\">");
		out.printf("<mx2 source=\"MasterCook\" date=\"%1$tB %1$td, %1$tY\">", new Date());
		out.println();
	}

    @Override
	public void writeFileTail(PrintWriter out) {
		out.println("</mx2>");
	}
	
    @Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		out.printf("<RcpE name=\"%.255s",escapeXmlForMasterCook(recipe.getTitle()));
		out.printf("\" author=\"%.255s",escapeXmlForMasterCook(recipe.getAuthor()));
		
		// save the image
		if (/*isWriteImages()
			&& theMasterCookProgram != null
			&& (theMasterCookProgram.exists() || masterCookVersion>=14)
			&& */ recipe.getImages() != null
			&& recipe.getImages().size()>0
			&& recipe.getImages().get(0).isValid()) {
			// image attribute (eg. img="book.jpg")
			// It has to be a relative path from D:\Programmer\MasterCook 9\Web\
			// It doesnt seems to work with absolute path
			// For MC14 the path is relative to the MX2 file
			// frontslah or backslash doesnt matter.
			
			// It seems that the mx2 file doesn't support gif and bmp images, so
			// convert it to png
			Image image2 = recipe.getImages().get(0);
			if ("GIF".equals(image2.getImageType())
				|| "BMP".equals(image2.getImageType())) {
				image2.convertTo(ImageFormat.PNG);
			}
			
			File savedFile = saveMainImage(recipe);
			String relativePath = null;
			if (masterCookVersion < 14) {
				File webDir = new File(theMasterCookProgram.getParentFile(), "web");
				if (savedFile!=null && webDir!=null) {
					relativePath = RelativePath.getRelativePath(webDir, savedFile);
				}
			} else {
				if (savedFile!=null) {
					relativePath = savedFile.getName();
				}
			}
			if (relativePath != null) {
				out.print("\" img=\""+relativePath);
			}
		}
		out.println("\">");
		
		if (recipe.getFileSource()!=null && recipe.getFileSource().length()>0) {
			out.println("<!-- Original file: "+recipe.getFileSource()+" -->");
		}
		
		out.println("<Serv qty=\""+recipe.getServings()+"\"/>");
		out.printf("<PrpT elapsed=\"%d:%02d\"/>", recipe.getPreparationTime()/60, recipe.getPreparationTime() % 60);
		out.println();
		
		// categories
		if (recipe.getCategories().size()>0 || recipe.getFolder() != null) {
			out.println("<CatS>");
			for (Category c : recipe.getCategories()) {
				out.println("<CatT>");
				out.printf("%.31s", escapeXmlForMasterCook(c.getName()));
				out.println();
				out.println("</CatT>");
			}
			// add the folder as a category
			if (recipe.getFolder() != null) {
				out.println("<CatT>");
				out.printf("%.31s", escapeXmlForMasterCook(recipe.getFolder().getName()));
				out.println();
				out.println("</CatT>");
			}
			out.println("</CatS>");
		}
		
		// Ingredients
		if (recipe.getIngredients() != null) {
			for (RecipeIngredient ingr : recipe.getIngredients()) {
				out.print("<IngR");
				// code="", T:Tekst, S:Subtitle, R:Recipe
				out.printf(" code=\"%c\"", IngredientCodes[ingr.getType()]);

				if (! ingr.hasNoIngredient()) {
					out.printf(" name=\"%.255s\"", escapeXmlForMasterCook(ingr.getIngredient().getName()));
				}

				if (! ingr.hasNoUnit()) {
					out.printf(" unit=\"%.25s\"", escapeXmlForMasterCook(ingr.getPluralisedUnitName()));
				}

				if (! ingr.hasNoAmount()) {
					out.printf(" qty=\"%s\"", FormattingUtils.formatNumber(ingr.getAmount()));
				}

				if (ingr.hasNoProcessing()) {
					// MasterCook is really sensitive about the XML style. "/>" does not work....
					out.println("></IngR>"); 
				} else {
					out.println('>');
					out.println("<IPrp>");
					out.printf("%.255s", escapeXmlForMasterCook(ingr.getProcessing()));
					out.println();
					out.println("</IPrp>");
					out.println("</IngR>");
				}
				// TODO: INtI: Nutritional Links.
			}
		}
		// Directions
		// ensure max 10000 chars in the directions.
		// NOTE: It seems that there is no limit anyhow.
		out.println("<DirS>");
		int imgIndex = 0;
		for (String paragraph : recipe.getDirections()) {
			out.print("<DirT");
			Image dirImage = recipe.getDirectionImage(imgIndex);
			if (isWriteImages()
				&& dirImage!=null) {
				// TODO
				/*
				File savedFile = saveImage(recipe);
				File webDir = new File(theMasterCookProgram.getParentFile(), "web");
				
				if (savedFile!=null && webDir!=null) {
					String relativePath = RelativePath.getRelativePath(webDir, savedFile);
					out.print("\" img=\""+relativePath);
				}
				*/
				
			}
			out.println(">");
			out.println(escapeXmlForMasterCook(paragraph).replaceAll("\n", "&#013;&#010;"));
			out.println("</DirT>");
			imgIndex++;
		}
		if (recipe.getTips() != null && recipe.getTips().size()>0) {
			out.println("<DirT>");
			out.println("TIPS:");
			out.println("</DirT>");
			for (String paragraph : recipe.getTips()) {
				out.println("<DirT>");
				out.println(escapeXmlForMasterCook(paragraph).replaceAll("\n", "&#013;&#010;"));
				out.println("</DirT>");
			}
		}
		if (recipe.getNutritionalInfo().length()>0) {
			out.println("<DirT>");
			out.println("NUTRITIONAL INFORMATION:");
			out.println("</DirT>");
			out.println("<DirT>");
			out.println(escapeXmlForMasterCook(recipe.getNutritionalInfo()).replaceAll("\n", "&#013;&#010;"));
			out.println("</DirT>");
		}
		
		out.println("</DirS>");

		// Source
		if (recipe.getSource() != null && recipe.getSource().length()>0) {
			out.println("<Srce>");
			out.printf("%.255s", escapeXmlForMasterCook(recipe.getSource()));
			out.println();
			out.println("</Srce>");
		}
		
		// Yield
		if (recipe.getYield() != null && recipe.getYield().length()>0) {
			String parts[] = recipe.splitYield();
			out.printf("<Yield unit=\"%.25s\" qty=\"%f\" />",
					escapeXmlForMasterCook(parts[1]),
					   RecipeIngredient.getNumber(parts[0]));
			out.println();
		}
		
		// notes
		if (recipe.getNote() != null && recipe.getNote().length()>0) {
			out.println("<Note>");
			out.printf("%.6000s", escapeXmlForMasterCook(recipe.getNote()));
			out.println();
			out.println("</Note>");
		}
		
		// description
		if (recipe.getDescription() != null && recipe.getDescription().length()>0) {
			out.println("<Desc>");
			out.printf("%.255s", escapeXmlForMasterCook(recipe.getDescription()));
			out.println();
			out.println("</Desc>");
		}
		
		// Alternative Source
		if (recipe.getTextAttribute(Recipe.TEXTATT_ID)!=null) {
			out.printf("<AltS label=\"%s\" source=\"%.255s\" />",
					   Recipe.TEXTATT_ID,
					   escapeXmlForMasterCook(recipe.getTextAttribute(Recipe.TEXTATT_ID)));
			out.println();
		} else if (recipe.getUrl()!=null) {
			out.printf("<AltS label=\"%s\" source=\"%.255s\" />",
					   Recipe.TEXTATT_URL,
					   escapeXmlForMasterCook(recipe.getUrl()));
			out.println();
		} else if (recipe.getAltSourceLabel()!=null){
			out.printf("<AltS label=\"%s\" source=\"%.255s\" />",
					escapeXmlForMasterCook(recipe.getAltSourceLabel()),
					escapeXmlForMasterCook(recipe.getAltSourceText()));
			out.println();
		}
		
		// Copyright
		if (recipe.getCopyright()!=null){
			out.println("<CpyR>");
			out.printf("%.255s",
					escapeXmlForMasterCook(recipe.getCopyright()));
			out.println();
			out.println("</CpyR>");
		}
		
		// Cuisine
		if (recipe.getCuisine() != null) {
			out.println("<Natn>");
			out.printf("%.255s",
					escapeXmlForMasterCook(recipe.getCuisine()));
			out.println();
			out.println("</Natn>");
		}

		// Wine
		if (recipe.getCuisine() != null) {
			out.println("<Wine>");
			out.printf("%.132s",
					escapeXmlForMasterCook(recipe.getWine()));
			out.println();
			out.println("</Wine>");
		}

		// Serving Ideas
		if (recipe.getServingIdeas() != null) {
			out.println("<SrvI>");
			out.printf("%.6000s", escapeXmlForMasterCook(recipe.getServingIdeas()));
			// NB: Do NOT replace linebreaks with 10,13.
			out.println();
			out.println("</SrvI>");
		}
		
		// total time
		if (recipe.getTotalTime() != 0) {
			out.printf("<TTim elapsed=\"%d:%02d\"/>", recipe.getTotalTime()/60, recipe.getTotalTime() % 60);
			out.println();
		}

		// ALT time
		// Since MC only supports one time, give priority to Cooking time
		if (recipe.getCookTime() != 0) {
			out.printf("<AltT label=\"%.31s\" elapsed=\"%d:%02d\"/>",
					escapeXmlForMasterCook("Cook"), recipe.getCookTime()/60, recipe.getCookTime()%60);
			out.println();
		} else {
			for(String key : recipe.getTimes().keySet()) {
				if (! key.startsWith("TIME.")) {
					out.printf("<AltT label=\"%.31s\" elapsed=\"%d:%02d\"/>",
							escapeXmlForMasterCook(key), recipe.getTime(key)/60, recipe.getTime(key)%60);
					out.println();
					break;
				}
			}
		}
		
		// Ratings
		if (recipe.getRatings()!=null && recipe.getRatings().size() > 0) {
			out.println("<RatS>");
			for(String label: recipe.getRatings().keySet()) {
				out.printf("<RatE name=\"%.255s\" value=\"%d\"/>",
						escapeXmlForMasterCook(label),
						recipe.getRating(label, 10));
				out.println();
			}
			out.println("</RatS>");
		}
		
		// end of recipe
		out.println("</RcpE>");
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
	Pattern mcfile = Pattern.compile("mastercook(\\d*)\\.exe", Pattern.CASE_INSENSITIVE);
	
	public void setTheMasterCookProgram(File theMasterCookProgram) {
		this.theMasterCookProgram = theMasterCookProgram;
		handler.setTheMasterCookProgram(theMasterCookProgram);
		// check version
		masterCookVersion = 11;
		Matcher m = mcfile.matcher(theMasterCookProgram.getAbsolutePath());
		if (m.find()) {
			String no = m.group(1); 
			if (no!=null && no.length()>0) {
				masterCookVersion = Integer.parseInt(m.group(1));
			}
		} 
		handler.setMasterCookVersion(masterCookVersion);
	}

	/**
	 * @param theMasterCookProgram the theMasterCookProgram to set
	 */
	public void setTheMasterCookProgram(String theMasterCookProgram) {
		setTheMasterCookProgram(new File(theMasterCookProgram));
	}
	
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("<mx2\\s+source\\s*=", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}

	@Override
	public void setConfig(String property, String value) {
		if ("imageDir".equals(property)) {
			setImageDir(value);
		} else {
			setTheMasterCookProgram(value);
		}
	}
	@Override
	public String getConfig(String property) {
		if ("imageDir".equals(property)) {
			return getImageDir().getAbsolutePath();
		} else {
			return getTheMasterCookProgram().getAbsolutePath();
		}
	}
	
}
