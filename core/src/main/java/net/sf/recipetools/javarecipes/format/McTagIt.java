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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ft
 * 
 * TODO: TIPS, NUTRITIONALINFO
 */
public class McTagIt extends RecipeTextFormatter {
	private static final Logger log = LoggerFactory.getLogger(McTagIt.class);
	
	static List<String> oneLinerTags = Arrays.asList(new String[] {
		"alts", "altsrclbl", "altsrctxt", "alttim", "alttimlbl", "alttimtxt",
		"author", "cat", "copyright", "cuisine", "image", "preptim", "rate", "serves", 
		"yamt", "yield", "yunit"
		});
	
	static HashMap<String, String> synonyms = new HashMap<String, String>();
	static {
		synonyms.put("as",		"Alts");
		synonyms.put("asl",		"AltSrcLbl");
		synonyms.put("ast",		"AltSrcTxt");
		synonyms.put("at",		"AltTim");
		synonyms.put("atl",		"AltTimLbl");
		synonyms.put("att",		"AltTimTxt");
		synonyms.put("a",		"Author");
		synonyms.put("b",		"Author");
		synonyms.put("by",		"Author");
		synonyms.put("c",		"Cat");
		synonyms.put("cpr",		"Copyright");
		synonyms.put("cus",		"Cuisine");
		synonyms.put("d",		"Description");
		synonyms.put("desc",	"Description");
		synonyms.put("dir",		"Directions");
		synonyms.put("foot",	"Footnote");
		synonyms.put("footer",	"Footnote");
		synonyms.put("fn",		"Footnote");
		synonyms.put("im",		"Image");
		synonyms.put("i",		"Ing");
		synonyms.put("mag",		"magazine");
		synonyms.put("n",		"Notes");
		synonyms.put("nut",		"Nutrition");
		synonyms.put("pt",		"PrepTim");
		synonyms.put("r",		"Rate");
		synonyms.put("s",		"Serves"); 
		synonyms.put("servings","Serves"); 
		synonyms.put("src",		"Source");
		synonyms.put("si",		"SrvIdea");
		synonyms.put("txt",		"Text");
		synonyms.put("t",		"Title");
		synonyms.put("tt",		"TotalTim");
		synonyms.put("w",		"wine");
		synonyms.put("ya",		"YAmt");
		synonyms.put("y",		"Yield");
		synonyms.put("yu",		"YUnit");
	}

	// save the nutritional info to be added to the directions as the second last thing.
	private String nutritionalInfo = null;
	
	// save the footnote to be added to the directions as the last thing.
	private String footnote = null;

	private boolean writeIngredientsWithSeparators = false;
	
	Recipe globalRecipe = new Recipe();
	
	private String altTimeLabel = null;
	
	/**
	 * 
	 */
	public McTagIt() {
		super();
	}

	// ---------------------------------------------------------------
	//
	//          reading 
	//
	// ---------------------------------------------------------------
	
    @Override
	public List<Recipe> readRecipes(LineNumberReader in) {
		List<Recipe> recipes = new ArrayList<Recipe>();
		try {
			boolean more = true;
			while (more) {
				// read the recipe
				Recipe r = processRecipe(in);
				if (r != null) {
					if (r.getTitle()!=null && r.getTitle().length()!=0) {
						recipes.add(r);
					}
				} else {
					more = false;
				}
			}
		} catch (RuntimeException e) {
			log.error("Error reading the recipes: "+e.getMessage());
		}
		
		return recipes;
	}

	Pattern beginningOfRecipePattern = Pattern.compile("^\\s*\\[\\[\\[");
	Pattern endOfRecipePattern = Pattern.compile("^\\s*\\]\\]\\]");
	Pattern tagPattern = Pattern.compile("^\\s*(\\w+)::(.*)$", Pattern.DOTALL);
	
	Recipe processRecipe(LineNumberReader in) {
		Recipe recipe = new Recipe(globalRecipe);
		String line = "";
		nutritionalInfo = null;
		String currentTag = "";
		StringBuilder tagText = new StringBuilder();
		boolean endOfRecipe = false;
		boolean foundBeginningOfRecipe = false;
		try {
			while (! endOfRecipe) {
				in.mark(300);
				line = in.readLine();
				if (line==null) {
					endOfRecipe = true;
					// process the old tag if any
					if (tagText.length() > 0) {
						try {
							processTag(recipe, currentTag, tagText);
						} catch (RuntimeException e) {
							String msg = "Invalid processing of tag ("+currentTag+") with value ("+tagText.toString()
							+") in recipe with title ("+recipe.getTitle()+")\nError="+e.getMessage();
							log.error(msg);
						}
					}
					
					break;
				}
				
				if (! foundBeginningOfRecipe) {
					if (beginningOfRecipePattern.matcher(line).find()) {
						foundBeginningOfRecipe = true;
						continue;
					} else {
						// skip lines until beginning of next recipe
						continue;
					}
				}
				
				if (endOfRecipePattern.matcher(line).find() || beginningOfRecipePattern.matcher(line).find()) {
					endOfRecipe = true;
					// process the last tag
					try {
						processTag(recipe, currentTag, tagText);
					} catch (RuntimeException e) {
						String msg = "Invalid processing of tag ("+currentTag+") with value ("+tagText.toString()
								+") in recipe with title ("+recipe.getTitle()+")\nError="+e.getMessage();
						log.error(msg);
					}
					
					// if found beginning of next recipe then push the line back
					if (beginningOfRecipePattern.matcher(line).find()) {
						in.reset();
					}
					
					break;
				}
				
				Matcher m = tagPattern.matcher(line);
				if (m.find()) {
					// process the old tag
					try {
						processTag(recipe, currentTag, tagText);
					} catch (RuntimeException e) {
						String msg = "Invalid processing of tag ("+currentTag+") with value ("+tagText.toString()
						+") in recipe with title ("+recipe.getTitle()+")\nError="+e.getMessage();
						log.error(msg);
					}
					// set the new tag
					currentTag = m.group(1);
					tagText.delete(0,100000);
					if (m.groupCount()>1) {
						tagText.append(m.group(2));
					}
				} else {
					if (tagText.length() > 0) {
						tagText.append('\n');
					}
					tagText.append(line);
				}
			}
		} catch (IOException e) {
			log.error("Error reading from the file: "+e.getMessage());
		}
		
		// found no recipe?
		if (! foundBeginningOfRecipe) {
			return null;
		}
		
		// any nutritional info to be added to the directions?
		if (nutritionalInfo!=null && nutritionalInfo.length()>0) {
			recipe.addDirectionsAsText(nutritionalInfo);
		}
		
		// any footnotes to be added to the directions?
		if (footnote!=null && footnote.length()>0) {
			recipe.addDirections(footnote);
		}
		
		// replace any [CRLF] in directions
		for (int i=0; i<recipe.getDirections().size(); i++) {
			// ignore CRLFs in the beginning and end of line
			String str = recipe.getDirections().get(i);
			str = str.replaceAll("(?im:^\\s*\\[CR(LF)?\\])", "");
			str = str.replaceAll("(?im:\\[CR(LF)?\\]\\s*$)", "");
			str = str.replaceAll("(?i:\\[CR(LF)?\\])", "\n");
			recipe.getDirections().set(i, str);
		}
		
		// replace any [CRLF] in notes
		String str = recipe.getNote().replaceAll("(?i:\\[CR(LF)?\\])", "\n");
		recipe.setNote(str);
		
		return recipe;
	}
	
	String VALUE_SPLITTER = ":";
	
	void processTag(Recipe recipe, String currentTag, StringBuilder tagText) {
		
		// ignore tags with empty text
		String txt = tagText.toString().trim();
		if (txt.length() == 0) {
			return;
		}
		
		currentTag = currentTag.toLowerCase();
		// is it a global tag?
		if (currentTag.length()>0
			&& currentTag.charAt(0)=='g') {
			// then set the tag in the global recipe and remove the "G" from the tag.
			currentTag = currentTag.substring(1);
			processTag(globalRecipe, currentTag, tagText);
		}

		// a synonym ?
		if (synonyms.containsKey(currentTag)) {
			currentTag = synonyms.get(currentTag).toLowerCase();
		}
		
		if (oneLinerTags.contains(currentTag) && txt.contains("\n")) {
			String[] parts = txt.split("\n", 2);
			txt = parts[0].trim();
			nutritionalInfo = FormattingUtils.join(nutritionalInfo, "\n\n", parts[1].trim());
		}
		
		// remove possible subtag in style DIR::YIELD::
		Matcher m = tagPattern.matcher(txt);
		if (m.find()) {
			txt = m.group(2).trim();
		}
		
		if ("title".equalsIgnoreCase(currentTag)) {
			// remove newlines from title
			if (txt.contains("\n")) {
				txt = txt.replaceAll("\n"," ").trim();
			}
			recipe.setTitle(txt);
			log.info("TITLE:"+recipe.getTitle());
		} else if ("description".equalsIgnoreCase(currentTag)) {
			recipe.setDescription(txt);
		} else if ("serves".equalsIgnoreCase(currentTag)) {
			recipe.setServings(txt);
		} else if ("yield".equalsIgnoreCase(currentTag)) {
			recipe.setYield(txt);
		} else if ("yamt".equalsIgnoreCase(currentTag)) {
			recipe.setYield(txt+" "+recipe.getYield());
		} else if ("yunit".equalsIgnoreCase(currentTag)) {
			recipe.setYield(recipe.getYield()+" "+txt);
		} else if ("ing".equalsIgnoreCase(currentTag)) {
			String[] parts = txt.split("\n");
			for (String ingr : parts) {
				if (ingr.trim().length() > 0) {
					recipe.addIngredient(new RecipeIngredient(ingr.trim()));
				}
			}
		} else if ("directions".equalsIgnoreCase(currentTag)) {
			recipe.addDirectionsAsText(txt);
		} else if ("ignore".equalsIgnoreCase(currentTag)) {
			// do nothing
		} else if ("cat".equalsIgnoreCase(currentTag)) {
			for (String category : txt.split(";")) {
				recipe.addCategory(category.trim());
			}
		} else if ("notes".equalsIgnoreCase(currentTag)) {
			recipe.addNote(txt);
		} else if ("cuisine".equalsIgnoreCase(currentTag)) {
			recipe.setCuisine(txt);
		} else if ("nutrition".equalsIgnoreCase(currentTag)) {
			nutritionalInfo = FormattingUtils.join(nutritionalInfo, "\n\n", txt);
		} else if ("footnote".equalsIgnoreCase(currentTag)) {
			footnote = FormattingUtils.join(footnote, "\n\n", txt);
		} else if ("srvidea".equalsIgnoreCase(currentTag)) {
			toSingleLine(tagText);
			recipe.setServingIdeas(txt.toString().trim());
		} else if ("preptim".equalsIgnoreCase(currentTag)) {
			recipe.setPreparationTime(txt);
		} else if ("totaltim".equalsIgnoreCase(currentTag)) {
			recipe.setTotalTime(txt);
		} else if ("alttim".equalsIgnoreCase(currentTag)) {
			String[] parts = txt.split(VALUE_SPLITTER, 2);
			recipe.setTime(parts[0].trim(), parts[1].trim());
		} else if ("alttimlbl".equalsIgnoreCase(currentTag)) {
			altTimeLabel = txt;
		} else if ("alttimtxt".equalsIgnoreCase(currentTag)) {
			if (altTimeLabel != null) {
				recipe.setTime(altTimeLabel, txt);
				altTimeLabel = null;
			}
		} else if ("author".equalsIgnoreCase(currentTag)) {
			recipe.setAuthor(txt);
		} else if ("source".equalsIgnoreCase(currentTag)) {
			recipe.setSource(txt);
		} else if ("copyright".equalsIgnoreCase(currentTag)) {
			recipe.setCopyright(txt);
		} else if ("alts".equalsIgnoreCase(currentTag)) {
			String[] parts = txt.split(VALUE_SPLITTER, 2);
			recipe.setAltSourceLabel(parts[0].trim());
			recipe.setAltSourceText(parts[1].trim());
		} else if ("altsrclbl".equalsIgnoreCase(currentTag)) {
			recipe.setAltSourceLabel(txt);
		} else if ("altsrctxt".equalsIgnoreCase(currentTag)) {
			recipe.setAltSourceText(txt);
		} else if ("wine".equalsIgnoreCase(currentTag)) {
			recipe.setWine(txt);
		} else if ("image".equalsIgnoreCase(currentTag)) {
			Image image = new Image();
			String filename = txt;
			try {
				File file = new File(filename);
				// relative path filename (relative to the dir with the txt file)
				if (! file.exists()) {
					file = new File(getImageDir(), filename);
				}
				image.setImageFromFile(file);
				recipe.addImage(image);
			} catch (RuntimeException e) {
				log.error("Error reading the image from file: "+filename+", error:"+e.getMessage());
			}
		} else if ("rate".equalsIgnoreCase(currentTag)) {
			String[] parts = txt.split(":", 2);
			recipe.getRatings().put(parts[0].trim(), Integer.parseInt(parts[1].trim())/10.0f);
		} else {
			log.error("Unknown tag: "+currentTag+" in recipe: "+recipe.getTitle());
		}
	}
	
	
	private void toSingleLine(StringBuilder tagText) {
		String t = tagText.toString().replace("\n", " ").trim();
		tagText.delete(0,1000000);
		tagText.append(t);
	}

	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
    @Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		if (recipe.getFileSource()!=null && recipe.getFileSource().length()>0) {
			out.println("# Original file: "+recipe.getFileSource());
		}
		
		out.println("[[[");
		// order as requested by John: Title, image, ingredients, directions, others
		printTextIfPresent(out, "Title", false, recipe.getTitle());
		// Images
		List<Image> images = recipe.getImages();
		if (images != null && images.size()>0) {
			// TODO: Only the first image is exported?
			File imageFile = saveMainImage(recipe);
			if (imageFile != null) {
				out.printf("IM:: %s", imageFile.getName());
				out.println();
			}
		}
		
		// export also direction images
		images = recipe.getDirectionImages();
		if (images != null && images.size()>0) {
			for (int i=0; i<images.size(); i++) {
				if (images.get(i) == null) continue;
				saveDirectionImage(recipe, i);
			}
		}

		// split notes in paragraphs
		String note = recipe.getNote(); 
		if (note!=null && note.length()>0) {
			for (String str: note.split("\n\n")) {
				printTextIfPresent(out, "Notes", false, str);
			}
		}
		
		printIngredients(out, recipe.getIngredients());
		// directions with a DIR:: in front of each paragraph.
		for (String str: recipe.getDirections()) {
			printTextIfPresent(out, "Dir", true, str);
		}
		
		printTextIfPresent(out, "Author", false, recipe.getAuthor());
		printTextIfPresent(out, "ASL", false, recipe.getAltSourceLabel());
		printTextIfPresent(out, "AST", false, recipe.getAltSourceText());
		if (recipe.getUrl()!=null) {
			printTextIfPresent(out, "AS", false, "URL: "+recipe.getUrl());
		}
		if (recipe.getCookTime() != 0) {
			printAltTimeIfPresent(out, "COOK", recipe.getTime(Recipe.TIME_COOK));
		}
		for (String key: recipe.getTimes().keySet()) {
			if (! key.startsWith("TIME.")) {
				printAltTimeIfPresent(out, key, recipe.getTime(key));
			}
		}
		printTextIfPresent(out, "Cat", false, recipe.getCategoriesAsString());
		printTextIfPresent(out, "Cpr", false, recipe.getCopyright());
		printTextIfPresent(out, "Cus", false, recipe.getCuisine());
		printTextIfPresent(out, "Desc", false, recipe.getDescription());
		
		printTimeIfPresent(out, "PT", recipe.getPreparationTime());
		if (recipe.getRatings() != null) {
			for (String label: recipe.getRatings().keySet()) {
				out.printf("Rate:: %s: %d", label, recipe.getRating(label, 10));
				out.println();
			}
		}
		out.printf("Serves:: %d", recipe.getServings());
		out.println();
		printTextIfPresent(out, "SI", false, recipe.getServingIdeas());
		printTextIfPresent(out, "Src", false, recipe.getSource());
		printTimeIfPresent(out, "TT", recipe.getTotalTime());
		printTextIfPresent(out, "Yield", false, recipe.getYield());
		printTextIfPresent(out, "Wine", false, recipe.getWine());
		
		// end of recipe
		out.println("]]]");
	}

	void printIngredients(PrintWriter out, List<RecipeIngredient> ingredients) {
		if (ingredients == null) {
			return;
		}
		out.println("Ing::");
		for (RecipeIngredient ingr : ingredients) {
			printIngredient(out, ingr);
		}
	}

	/**
	 * @param out
	 * @param ingr
	 */
	void printIngredient(PrintWriter out, RecipeIngredient ingr) {
		String typeString = null;
		if (ingr.getType() == RecipeIngredient.TYPE_RECIPE) {
			typeString = "R>";
		} else if (ingr.getType() == RecipeIngredient.TYPE_SUBTITLE) {
			typeString = "S>";
		} else if (ingr.getType() == RecipeIngredient.TYPE_TEXT) {
			typeString = "T>";
		} 
		
		if (typeString != null) {
			out.print(typeString);
		}
			
		if (writeIngredientsWithSeparators) out.print(RecipeIngredient.FIELD_HEAD);
		if (ingr.hasAmount()) {
			out.printf("%s ", FormattingUtils.formatNumber(ingr.getAmount()));
		}
		if (writeIngredientsWithSeparators) {
			out.print(RecipeIngredient.FIELD_SEPARATOR);
		}
		if (ingr.hasUnit()) {
			out.printf("%s ", ingr.getPluralisedUnitName());
		}
		if (writeIngredientsWithSeparators) {
			out.print(RecipeIngredient.FIELD_SEPARATOR);
		}
		if (ingr.hasIngredient()) {
			out.print(ingr.getIngredient().getName());
		}
		if (writeIngredientsWithSeparators) {
			out.print(RecipeIngredient.FIELD_SEPARATOR);
		} else if (ingr.hasProcessing()) {
			if (ingr.hasIngredient() && ! ingr.getIngredient().getName().matches(".*[;,]\\s*$")) {
				out.print(",");
			}
			out.print(" ");
		}
		if (! ingr.hasNoProcessing()) {
			out.print(ingr.getProcessing());
		}
		if (writeIngredientsWithSeparators) out.print(RecipeIngredient.FIELD_TAIL);
		out.println();
	}

	void printTextIfPresent(PrintWriter out, String header, boolean textAtNewline, String text) {
		if (text!=null && text.length()>0) {
			if (textAtNewline) {
				// use windows line breaks.
				out.printf("%s::\r\n%s", header, text.replace("\n", "\r\n"));
			} else {
				out.printf("%s:: %s", header, text);
			}
			out.println();
		}
	}

	void printTimeIfPresent(PrintWriter out, String header, int time) {
		if (time>0) {
			out.printf("%s:: %d:%02d", header, time/60, time % 60);
			out.println();
		}
	}

	void printAltTimeIfPresent(PrintWriter out, String header, int time) {
		if (time>0) {
			out.printf("ALTTIM:: %s: %d:%02d", header, time/60, time % 60);
			out.println();
		}
	}
	/**
	 * @return the writeIngredientsWithSeparators
	 */
	public boolean isWriteIngredientsWithSeparators() {
		return writeIngredientsWithSeparators;
	}

	/**
	 * @param writeIngredientsWithSeparators the writeIngredientsWithSeparators to set
	 */
	public void setWriteIngredientsWithSeparators(
			boolean writeIngredientsWithSeparators) {
		this.writeIngredientsWithSeparators = writeIngredientsWithSeparators;
	}
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("^\\s*\\[\\[\\[\\s*$", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.recipetools.javarecipes.format.RecipeFormatter#getDefaultCharacterSet()
	 */
	@Override
	public String getDefaultCharacterSet() {
		return "windows-1252"; // or UTF-8
	}

	@Override
	public void setConfig(String property, String value) {
		setImageDir(value);
	}
	@Override
	public String getConfig(String property) {
		return getImageDir().getAbsolutePath();
	}
	
    @Override
	public boolean isImagesInSameFile() {
		return false;
	}
	
}
