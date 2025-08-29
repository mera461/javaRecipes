/*
 * Created on 20-10-2004
 *
 */
package net.sf.recipetools.javarecipes.model;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.recipetools.javarecipes.format.FormattingUtils;


/**
 * @author ft
 *
 */

public class Recipe extends DbEntity {

	public static final String TEXTATT_ALT_SOURCE_LABEL 	= "AltSourceLabel";
	public static final String TEXTATT_ALT_SOURCE_TEXT		= "AltSourceText";
	public static final String TEXTATT_ALT_TIME_LABEL	 	= "AltTimeLabel";
	public static final String TEXTATT_ALT_TIME_TEXT		= "AltTimeText";
	public static final String TEXTATT_COPYRIGHT 	= "Copyright";
	public static final String TEXTATT_CUISINE		= "Cuisine";
	public static final String TEXTATT_ID			= "Id";
	public static final String TEXTATT_SERVINGIDEAS = "ServingIdeas";
	public static final String TEXTATT_URL			= "URL";
	public static final String TEXTATT_WINE			= "WINE";
	
	public static final String TIME_PREPARATION 	= "TIME.PREPARATION";
	public static final String TIME_TOTAL	 		= "TIME.TOTAL";
	public static final String TIME_COOK	 		= "TIME.COOK";
	
	
	/** Hibernate ID: primary key */
	Long id;

	/** the hibernate HBM version */
	int hbmVersion;
	
	private String	title;
	private String	author;
	private String	source;
	private String	fileSource;
	private String	description;
	
	
	private int		servings;
	private String	yield;
	private String	note;
	
	Folder folder;

	private HashMap<String, Integer> times;
	
	private List<Category>	categories;
	
	HashMap<String, String> textAttributes; 
	
    private List<RecipeIngredient>	ingredients;

	private List<Image>	images;

	private List<Image>	directionImages;

	
	private List<String>	directions;

	private List<String>	tips;

	private String	nutritionalInfo;
	
	private HashMap<String, Float> ratings;
	
	
	/**
	 * 
	 */
	public Recipe() {
		super();
		this.categories = new ArrayList<Category>();
		this.directions = new ArrayList<String>();
		this.tips = new ArrayList<String>();
		this.ingredients = new ArrayList<RecipeIngredient>();
		this.ratings = new HashMap<String, Float>();
		this.textAttributes = new HashMap<String, String>();
		this.times = new HashMap<String, Integer>();
		this.title = "";
		this.author = "";
		this.source = "";
		this.note = "";
		this.nutritionalInfo = "";
		this.yield = "";
	}
	
	public Recipe(String title) {
		super();
		this.categories = new ArrayList<Category>();
		this.directions = new ArrayList<String>();
		this.tips = new ArrayList<String>();
		this.ingredients = new ArrayList<RecipeIngredient>();
		this.ratings = new HashMap<String, Float>();
		this.textAttributes = new HashMap<String, String>();
		this.times = new HashMap<String, Integer>();
		this.title = title;
		this.author = "";
		this.source = "";
		this.note = "";
		this.nutritionalInfo = "";
		this.yield = "";
	}

	/**
	 * A copy constructor.
	 * Values are copied (except for ingredients)
	 * @param r the recipe to copy from
	 */
	public Recipe(Recipe r) {
		super();
		this.author = r.author;
		this.categories = new ArrayList<Category>();
		this.categories.addAll(r.categories);
		this.description = r.description;
		this.directions = new ArrayList<String>();
		this.directions.addAll(r.directions);
		this.tips = new ArrayList<String>();
		this.tips.addAll(r.tips);
		this.ingredients = new ArrayList<RecipeIngredient>();
		this.note = r.note;
		this.times = new HashMap<String, Integer>();
		this.times.putAll(r.times);
		this.ratings = new HashMap<String, Float>();
		this.ratings.putAll(r.ratings);
		this.servings = r.servings;
		this.source = r.source;
		this.textAttributes = new HashMap<String, String>();
		this.textAttributes.putAll(r.textAttributes);
		this.title = r.title;
		this.yield = r.yield;
		this.nutritionalInfo = r.nutritionalInfo;
	}
	
	
	/**
	 * @return Returns the categories.
	 */
	public List<Category> getCategories() {
		return categories;
	}
	/**
	 * @return Returns the categories.
	 */
	public String getCategoriesAsString() {
		if (categories == null || categories.isEmpty()) {
			return "";
		}
		
		boolean first = true;
		StringBuilder result = new StringBuilder();
		for (Category c : categories) {
			if (first) {
				first = false;
			} else {
				result.append("; "); 
			}
			result.append(c.getName());
		}
		
		return result.toString();
	}
	/**
	 * @param categories The categories to set.
	 */
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	/**
	 * @param categories A string with categories separated by commas or semicolon
	 */
	public void setCategories(String categories) {
		if (categories==null || categories.length()==0) {
			this.categories = new ArrayList<Category>();
			return;
		}
		String splitString = Configuration.getStringProperty("CATEGORY_SPLIT_CHARS");
		if (splitString==null || splitString.length()==0) {
			splitString = "[;,\n]";
		} else {
			splitString = "["+splitString+"]";
			// verify syntax of user string
			try {
				Pattern.compile(splitString);
			} catch (PatternSyntaxException e) {
				splitString = "[;,\n]";
			}
		}

		String[] names = categories.split(splitString);
		for (String name : names) {
			addCategory(name);
		}
	}
	
	
	/**
	 * Add a new category
	 * @param txt The category to add.
	 */
	public void addCategory(String str) {
		if (categories == null) {
			categories = new ArrayList<Category>();
		}
		categories.add(new Category(str.trim()));
	}
	
	/**
	 * @return Returns the tips.
	 */
	public List<String> getTips() {
		return tips;
	}
	/**
	 * @return Returns the tips as a single string.
	 */
	public String getTipsAsString() {
		return getAsString(tips);
	}
	/**
	 * @param directions The directions to set.
	 */
	public void setTips(List<String> tips) {
		this.tips = tips;
	}

	/**
	 * @param tips The tips to set.
	 */
	public void setTips(String tips) {
		this.tips = Arrays.asList(tips.split("\n\n+")); 
	}
	/**
	 * Add an additional string to add to the tips
	 * @param txt The directions to add.
	 */
	public void addTips(String str) {
		if (tips==null) {
			tips = new ArrayList<String>();
		}
		tips.add(str);
	}
	
	/**
	 * Add an additional string to add to the tips
	 * @param txt The tips to add.
	 */
	public void addTipsAsText(String str) {
		if (tips==null) {
			tips = new ArrayList<String>();
		}
		tips.addAll(Arrays.asList(str.split("\n\n+")));
	}
	
	public String getAsString(List<String> paragraphs) {
		if (paragraphs == null
			|| paragraphs.isEmpty()) {
			return "";
		}
		StringBuilder s = new StringBuilder();
		for (String str : paragraphs) {
			s.append(str);
			s.append("\n\n");
		}
		s.delete(s.length()-2, s.length());
		return s.toString();
	}

	/**
	 * @return Returns the directions.
	 */
	public List<String> getDirections() {
		return directions;
	}
	/**
	 * @return Returns the directions as a single string.
	 */
	public String getDirectionsAsString() {
		return getAsString(directions);
	}
	/**
	 * @param directions The directions to set.
	 */
	public void setDirections(List<String> directions) {
		this.directions = directions;
	}

	/**
	 * @param directions The directions to set.
	 */
	public void setDirections(String directions) {
		if (directions==null || directions.trim().length()==0) return;
		// new arraylist to avoid that is read-only
		this.directions = new ArrayList<String>(Arrays.asList(directions.split("\n\n+"))); 
	}
	/**
	 * Add an additional string to add to the directions
	 * @param txt The directions to add.
	 */
	public void addDirections(String str) {
		if (directions==null) {
			directions = new ArrayList<String>();
		}
		directions.add(str);
	}
	
	/**
	 * Add an additional string to add to the directions
	 * @param txt The directions to add.
	 */
	public void addDirectionsAsText(String str) {
		if (directions==null) {
			directions = new ArrayList<String>();
		}
		// new arraylist to avoid that is read-only
		directions.addAll(new ArrayList<String>(Arrays.asList(str.split("\n\n+"))));
	}
	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	/**
	 * @return Returns the note.
	 */
	public String getNote() {
		return note;
	}
	/**
	 * @param note The note to set.
	 */
	public void setNote(String note) {
		if (note == null) note="";
		this.note = note;
	}
	/**
	 * Add an additional string 
	 * @param note The note to set.
	 */
	public void addNote(String str) {
		if (note!=null && note.length() > 0) {
			note = note + "\n\n" + str;
		} else {
			this.note = str;
		}
	}
	
	public HashMap<String, Integer> getTimes() {
		return times;
	}
	
	public void setTimes(HashMap<String, Integer> times) {
		this.times = times;
	}
	/**
	 * @param time The name of the time : Preparation, Total, etc.
	 * @return the given time or 0 if not defined
	 */
	public int getTime(String time) {
		return times.containsKey(time) ? times.get(time) : 0;
	}
	
	public void setTime(String time, int value) {
		times.put(time, value);
	}
	
	public void setTime(String time, String value) {
		times.put(time, extractTime(value));
	}
	
	/**
	 * @return Returns the preparationTime.
	 */
	public int getPreparationTime() {
		return getTime(TIME_PREPARATION);
	}
	/**
	 * @param preparationTime The preparationTime to set.
	 */
	public void setPreparationTime(int preparationTime) {
		setTime(TIME_PREPARATION, preparationTime);
	}

	public void setPreparationTime(String preparationTime) {
		setTime(TIME_PREPARATION, preparationTime);
	}
	
	/**
	 * @return the totalTime
	 */
	public int getTotalTime() {
		return getTime(TIME_TOTAL);
	}
	/**
	 * @param preparationTime The preparationTime to set.
	 */
	public void setTotalTime(int time) {
		setTime(TIME_TOTAL, time);
	}

	public void setTotalTime(String time) {
		setTime(TIME_TOTAL, time);
	}

	/**
	 * @return the cookTime
	 */
	public int getCookTime() {
		return getTime(TIME_COOK);
	}
	/**
	 * @param preparationTime The preparationTime to set.
	 */
	public void setCookTime(int time) {
		setTime(TIME_COOK, time);
	}

	public void setCookTime(String time) {
		setTime(TIME_COOK, time);
	}
	
	/**
	 * Extract the preparationTime from the string. Formats:
	 *      HH:MM
	 *      MM minutes
	 * or the ISO 8601 standard for durations:
	 * 		PnYnMnDTnHnMnS
	 * @param preparationTime The preparationTime to set formatted as "HH:MM"
	 */
	static Pattern prepPattern1 = Pattern.compile("(\\d+)\\s*:\\s*(\\d+)");
	static Pattern prepPattern2 = Pattern.compile("("+RecipeIngredient.SINGLE_AMOUNT_AS_ONE+")\\s*(\\w*)", Pattern.CASE_INSENSITIVE);
	static String numberPatternStr = "[0-9,\\.]+";
	static String isoPatternStr = "P"
								  +"(?:("+numberPatternStr+")Y)?"
								  +"(?:("+numberPatternStr+")M)?"
								  +"(?:("+numberPatternStr+")D)?"
								  +"T"
								  +"(?:("+numberPatternStr+")H)?"
								  +"(?:("+numberPatternStr+")M)?"
								  +"(?:("+numberPatternStr+")S)?"
								  ;
	static Pattern prepPattern3 = Pattern.compile(isoPatternStr, Pattern.CASE_INSENSITIVE);
	
	
	
	public static int extractTime(String preparationTime) {
		Matcher m = prepPattern1.matcher(preparationTime);
		if (m.find()) {
			int time = 60 * Integer.parseInt(m.group(1))
						 + Integer.parseInt(m.group(2));
			return time;
		}
			
		m = prepPattern3.matcher(preparationTime);
		if (m.find()) {
			float time = 0.0f; 
			// year
			if (m.group(1)!=null) {
				time += RecipeIngredient.getNumber(m.group(1)) * 365*24*60; 
			}
			if (m.group(2)!=null) {
				time += RecipeIngredient.getNumber(m.group(2)) * 30*24*60; 
			}
			if (m.group(3)!=null) {
				time += RecipeIngredient.getNumber(m.group(3)) * 24*60; 
			}
			if (m.group(4)!=null) {
				time += RecipeIngredient.getNumber(m.group(4)) * 60; 
			}
			if (m.group(5)!=null) {
				time += RecipeIngredient.getNumber(m.group(5)); 
			}
			// ignore seconds
			// if all groups are null then skip
			if (time>0) return Math.round(time);
		}

		m = prepPattern2.matcher(preparationTime);
		int total = 0;
		while (m.find()) {
			float time = RecipeIngredient.getNumber(m.group(1));
			char unit = ' ';
			if (m.group(2)!=null && m.group(2).length()>0) {
				unit =  m.group(2).toLowerCase().charAt(0);
			}
			switch (unit) {
			case 'm': 
			case ' ':
			    total += time;
			    break;
			case 'h': total += time*60; break;
            case 'd': total += time*60*24; break;
            default : 
                //log.debug("Setting time: time="+preparationTime+", unknown unit:"+m.group(2));
			}
		}

		return total;
	}
	/**
	 * @return Returns the source.
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param source The source to set.
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @param source The source to add.
	 */
	public void addSource(String source) {
		if (this.source != null && this.source.length()>0) {
			this.source = this.source + "; " + source;
		} else {
			this.source = source;
		}
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return Returns the yield.
	 */
	public String getYield() {
		return yield;
	}
	
	
	private static Pattern yieldHeader = Pattern.compile(
			"^\\s*(makes?|yields?|about|abt|\\s*)*\\s*:?\\s*("+RecipeIngredient.SINGLE_AMOUNT_AS_ONE+"\\s*.*)$",
			Pattern.CASE_INSENSITIVE);
	
	/**
	 * @param yield The yield to set.
	 */
	public void setYield(String yield) {
		if (yield==null) yield="";
		this.yield = yield.trim();
		if (this.yield!=null && this.yield.length()>0) {
			Matcher m = yieldHeader.matcher(this.yield);
			if (m.find()) {
				this.yield = m.group(2).trim();
			}
		}
	}
	/**
	 * Set the yield amount and unit based on a general text
	 * @param yield The yield to set.
	 */
	private static Pattern simpleYieldPattern = Pattern
		.compile("^\\s*(makes?|serves?|yield|about|abt|\\s*)*\\s*:?\\s*("+RecipeIngredient.SINGLE_AMOUNT_AS_ONE+")\\s*(.*)$", Pattern.CASE_INSENSITIVE);
	
	// TODO: CHANGE THIS....................
	public String[] splitYield() {
		if (yield==null || yield.length()==0) return new String[] {"0", ""};
		
		Matcher matcher = simpleYieldPattern.matcher(yield);
		if (! matcher.find()) {
			return new String[] {"0", yield};
		}
		
		float amount = RecipeIngredient.getNumber(matcher.group(2)); 
		String unit = "";
		if (matcher.groupCount()>2) {
			unit = matcher.group(3).trim();
		} else if (matcher.group(1).contains("serve")) {
			unit = "servings";
		}
		return new String[] {FormattingUtils.formatNumber(amount), unit};
	}
	/**
	 * @return Returns the ingredients.
	 */
	public List<RecipeIngredient> getIngredients() {
		return ingredients;
	}
	
	/**
	 * @return The ingredients as a string.
	 */
	public String getIngredientsAsString() {
		if (ingredients == null) {
			return "";
		}
		StringBuilder str = new StringBuilder();
		for (RecipeIngredient ingr : ingredients) {
			str.append(ingr.toString());
			str.append('\n');
		}
		return str.toString();
	}
	
	public void addIngredient(RecipeIngredient ingredient) {
		ingredient.setRecipe(this);
		ingredients.add(ingredient);
	}
	
	public void addIngredient(String str) {
		RecipeIngredient ingredient = new RecipeIngredient(str);
		ingredient.setRecipe(this);
		ingredients.add(ingredient);
	}

	public void addIngredient(int index, RecipeIngredient ingredient) {
		ingredient.setRecipe(this);
		if (index < 0) {
			ingredients.add(0, ingredient);
		} else if (index < ingredients.size()) {
			ingredients.set(index, ingredient);
		} else if (index == ingredients.size()) {
			ingredients.add(ingredient);
		} else {
			for (int i=ingredients.size(); i<index; i++)
				ingredients.add(null);
			ingredients.add(ingredient);
		}
	}

	/**
	 * @param ingredients The ingredients to set.
	 */
	public void setIngredients(List<RecipeIngredient> ingredients) {
		if (ingredients != null) {
			this.ingredients = ingredients;
		} else {
			this.ingredients = new ArrayList<RecipeIngredient>();
		}
	}

	/**
	 * Set all the ingredients from the given text.
	 * Every ingredient on its own line.
	 * @param ingredientText text to use as the ingredient text.
	 */
	public void setIngredients(String ingredientText) {
		List<RecipeIngredient> ingr = new ArrayList<RecipeIngredient>();
		if (ingredientText != null && ingredientText.length()>0) {
			String[] lines = ingredientText.split("\n");
			for (String line : lines) {
				ingr.add(new RecipeIngredient(line));
			}
		}
		this.ingredients = ingr;
	}
	
	
	public void normalize() {
		try {
			normalizeTitle();
			normalizeLineBreaks();
			normalizeDirections();
			if (Configuration.getBooleanProperty("REMOVE_GRAPHICAL_CHAR_PREFIX_FROM_INGREDIENTS")) {
				normalizeIngredientsWithGraphicalChar();
			}
			if (Configuration.getBooleanProperty("MERGE_INGREDIENT_LINE_CONTINUATIONS")) {
				normalizeIngredientLinesWithContinuations();
				normalizeIngredientLinesWithMatchingParens();
				normalizeIngredientLinesWithOnlyAmounts();
				normalizeIngredientLinesWithEndings();
			}
			if (Configuration.getBooleanProperty("SPLIT_INGREDIENT_LINES_WITH_PLUS")) {
				normalizeIngredientsWithPlus();
			}
			if (Configuration.getBooleanProperty("MARK_ALTERNATE_INGREDIENT_LINES_AS_TEXT")) {
				normalizeIngredientsWithOr();
			}
			for (RecipeIngredient ingr : ingredients) {
				ingr.normalize();
			}
			if (Configuration.getBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_YIELD")) {
				normalizeYield();
			}
			if (Configuration.getBooleanProperty("COPY_YIELD_TO_DIRECTIONS")) {
				normalizeCopyYieldToDirections();
			}
		} catch (RuntimeException e) {
			String msg = "Error normalizing the recipe:"+e.getMessage()+"\n\n"
						 +this.toString()+"\n\n";
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			PrintWriter ps = new PrintWriter(bytes);
			e.printStackTrace(ps);
			ps.close();
			String stacktrace = bytes.toString(); 
			throw new RecipeFoxException(msg+stacktrace, e);
		}
	}
	
	/**
	 * Change line breaks in the given strange to standard line break for the platform
	 * @param str String to be changed
	 * @return the changed string
	 */
	static String normalizeLineBreaks(String str) {
		String linebreak = "\n"; //System.getProperty("line.separator");
		String result = str.replaceAll("(\r\n|(?<!\r)\n|\r(?!\n))", linebreak);
		return result;
	}
	
	/**
	 * Change line breaks in all multi line fields 
	 */
	void normalizeLineBreaks() {
		for (int i = 0; i<directions.size(); i++) {
			directions.set(i, normalizeLineBreaks(directions.get(i)));
		}
		note = normalizeLineBreaks(note);
	}

	/**
	 * If the yield units contains "serving(s)" then move it
	 * to the servings 
	 */
	public void normalizeYield() {
		if (servings==0 && yield!=null) {
			extractServings(yield);
			// NB: Do not delete the Yield field, as it may also contain serving sizes etc.
		}
	}
	
	public void normalizeCopyYieldToDirections() {
		if (yield == null || yield.length()==0) return;
		
		// copy the yield to the end of the directions
		addDirections("Yield: "+yield);
	}
	
	public void normalizeIngredientsWithGraphicalChar() {
		Map<Character, Integer> charCounts = new HashMap<Character, Integer>();
		// count the special chars on the ingr lines
		for (int i=0; i<ingredients.size(); i++) {
			RecipeIngredient ingr = ingredients.get(i);
			if (ingr.hasNoAmount() && ingr.hasNoUnit() && ingr.hasIngredient()) {
				String name = ingr.getIngredient().getName().trim();
				if (name.length() == 0) continue;
				char c = name.charAt(0);
				if (! Character.isLetter(c)) {
					if (charCounts.containsKey(c)) {
						charCounts.put(c, charCounts.get(c)+1);
					} else {
						charCounts.put(c, 1);
					}
				}
			}
		}
		// anyone used by more that 50 %
		char foundChar = ' ';
		for (char c : charCounts.keySet()) {
			if (charCounts.get(c) > ingredients.size()/2) {
				foundChar = c;
				break;
			}
		}
		
		// did find any special chars?
		if (foundChar == ' ') return;
		
		// remove chars and re-process the ingredients
		for (int i=0; i<ingredients.size(); i++) {
			RecipeIngredient ingr = ingredients.get(i);
			if (ingr.hasIngredient()) {
				String name = ingr.getIngredient().getName().trim();
				if (name.startsWith(Character.toString(foundChar))) {
					ingr.setFromString(name.substring(1));
				}
			}
		}		
	}

	static Pattern lineEndingsToMerge = Pattern.compile("(?:or|and|,)\\s*$", Pattern.CASE_INSENSITIVE);

	// 
	static Pattern lineBeginningsToMerge = Pattern.compile("^\\s*(?:"
			// starting words with a following space or EOL
			+ "(?:about|abt|and|by|crosswise|cut|diagonal|diagonally|into|lengthwise|more\\s+for|or|such\\s+as|to)(?:\\s+|$)|"
			// other strange characters 
			+",|[=\\-]{1,2}\\s+|\\(|"
			// or an incl measurement
			+RecipeIngredient.SINGLE_AMOUNT_AS_ONE+"[\\s\\-]*"+RecipeIngredient.INCH
			+")\\s*",
			Pattern.CASE_INSENSITIVE /*+Pattern.COMMENTS*/);
	static Pattern linesWithCompleteProcessings = Pattern.compile("^(?:[\\s,;-]*)"+RecipeIngredient.PROCESS3+"$"); 
	/**
	 * merge ingredient lines with certain endings
	 *  
	 */
	public void normalizeIngredientLinesWithEndings() {
		if (ingredients.size()<2) {
			return;
		}
		List<RecipeIngredient> newList = new ArrayList<RecipeIngredient>();
		for (int i=0; i<ingredients.size(); i++) {
			RecipeIngredient ingr = ingredients.get(i);
			if (ingr.hasNoIngredient() && ingr.hasNoProcessing()) {
				newList.add(ingr);
				continue;
			}
			
			String txt = ingr.getIngredient().getName();
			if (ingr.hasProcessing()) {
				txt = txt + " -- " + ingr.getProcessing();
			}
			
			// ending to merge with next line?
			if (i<ingredients.size()-1
				&& ingredients.get(i+1).hasNoAmount()
				&& ingredients.get(i+1).hasNoUnit()
				&& lineEndingsToMerge.matcher(txt).find()) {
				mergeLines(ingr, ingredients.get(i+1));
				newList.add(ingr);
				i++;
			// beginning to merge with prev line?
			} else if (i>0
					   && ingr.hasNoAmount()
					   && ingr.hasNoUnit()
					   && ingr.hasIngredient()
					   && (lineBeginningsToMerge.matcher(txt).find()
						   || linesWithCompleteProcessings.matcher(txt).find())) {
				mergeLines(newList.get(newList.size()-1), ingr);
			} else {
				newList.add(ingr);
			}
		}		
		ingredients = newList;
	}
	
	/** 
	 * Normalize lines where it is split in the middle with
	 * Amount on one line and unit and name on the next.
	 * 
	 */
	void normalizeIngredientLinesWithOnlyAmounts() {
		if (ingredients.size()<2) {
			return;
		}
		List<RecipeIngredient> newList = new ArrayList<RecipeIngredient>();
		for (int i=0; i<ingredients.size(); i++) {
			RecipeIngredient ingr = ingredients.get(i);
			if (ingr.hasIngredient() || ingr.hasProcessing()
				|| ingr.isEmpty() /* or line is empty */
				|| i==(ingredients.size()-1) // or the last line
				|| ingredients.get(i+1).isEmpty() /* or next line is empty */
				){ 
				newList.add(ingr);
				continue;
			} 
			
			// no amount on the next line?
			if (i<ingredients.size()-1
				&& ingredients.get(i+1).hasNoAmount()
				&& ((ingr.hasUnit() && ingredients.get(i+1).hasNoUnit())
					|| ingr.hasNoUnit())) {
				mergeLines(ingr, ingredients.get(i+1));
				newList.add(ingr);
				i++;
			} else {
				newList.add(ingr);
			}
		}		
		ingredients = newList;
		
	}

	void mergeLines(RecipeIngredient ingr, RecipeIngredient nextIngr) {
		// units
		if (ingr.hasNoUnit() && nextIngr.hasUnit()) {
			ingr.setUnit(nextIngr.getUnit());
		}
		
		String txt = ingr.hasProcessing() ? ingr.getProcessing() : ingr.getIngredient().getName();
		StringBuilder str = new StringBuilder(txt);
		if (nextIngr.hasIngredient()) {
			if (str.length()>0) {
			    str.append(' ');
			}
			str.append(nextIngr.getIngredient().getName());
		}
		if (nextIngr.hasProcessing()) {
			str.append(' ');
			str.append(nextIngr.getProcessing());
		}
		if (ingr.hasProcessing()) {
			ingr.setProcessing(str.toString().trim());
		} else {
			ingr.getIngredient().setName(str.toString().trim());
		}
	}
	
	
	/**
	 * merge ingredient lines with matching parens
	 *  
	 */
	public void normalizeIngredientLinesWithMatchingParens() {
		if (ingredients.isEmpty()) {
			return;
		}
		List<RecipeIngredient> newList = new ArrayList<RecipeIngredient>();
		for (int i=0; i<ingredients.size(); i++) {
			RecipeIngredient ingr = ingredients.get(i);
			String txt = ingr.hasProcessing() ? ingr.getProcessing() : ingr.hasIngredient() ? ingr.getIngredient().getName() : "";
			int lcount = countChars('(', txt);
			int rcount = countChars(')', txt);
			
			// matching parems then just continue
			// if more right than left parens then something strange is happening
			if (lcount <= rcount) {
				newList.add(ingr);
				continue;
			}
			
			StringBuilder str = new StringBuilder(txt.trim()); 
			// find the matching right parens on lines with no amount and no unit
			for (int j=i+1;j<ingredients.size();j++) {
				RecipeIngredient nextIngr = ingredients.get(j);
				if (nextIngr.hasAmount() || nextIngr.hasUnit()) {
					break;
				} else {
					String separator = " ";
					if (str.charAt(str.length()-1) == '(') separator = "";
					if (nextIngr.hasIngredient()) str.append(separator+nextIngr.getIngredient().getName().trim());
					if (nextIngr.hasProcessing()) str.append(separator+nextIngr.getProcessing().trim());
					// found a matching paren?
					lcount = countChars('(', str.toString());
					rcount = countChars(')', str.toString());
					if (lcount == rcount) {
						if (ingr.hasProcessing()) {
							ingr.setProcessing(str.toString());
						} else {
							ingr.getIngredient().setName(str.toString());
						}
						i=j;
						break;
					}
				}
			}
			
			newList.add(ingr);
		}		
		ingredients = newList;
	}
	
	public int countChars(char c, String str) {
		if (str == null || str.length()==0) {
			return 0;
		}
		
		int no = 0;
		int pos = str.indexOf(c, 0);
		while (pos >= 0) {
			no++;
			pos = str.indexOf(c, pos+1);
		}
		return no;
	}
	
	/**
	 * normalize the ingredient lines.
	 * If a line starts with '-' it is a continuation from previous line. 
	 */
	public void normalizeIngredientLinesWithContinuations() {
		if (ingredients.isEmpty()) {
			return;
		}
		List<RecipeIngredient> newList = new ArrayList<RecipeIngredient>();
		newList.add(ingredients.get(0));
		
		for (int i=1; i<ingredients.size(); i++) {
			RecipeIngredient ingr = ingredients.get(i);
			String name = ingr.hasIngredient() ? ingr.getIngredient().getName() : null;
			// Is it a continuation line?
			if (ingr.hasNoAmount()
				&& ingr.hasNoUnit() 
				&& name !=null
				&& name.length() > 3
				&& name.charAt(0)=='-' // only one or two '-'. If more it is a heading.
				&& name.charAt(2)!='-')
			{
				RecipeIngredient lastIngr = newList.get(newList.size()-1);
				RecipeIngredient newIngr = new RecipeIngredient();
				newIngr.setAmount(lastIngr.getAmount());
				newIngr.setUnit(lastIngr.getUnit());
				newIngr.setIngredient(new Ingredient(lastIngr.getIngredient().getName()
									  + " " + name.substring(1)));
				newIngr.setProcessing(lastIngr.getProcessing() + " " + ingr.getProcessing());
				newList.set(newList.size()-1, newIngr);
			} else {
				newList.add(ingr);
			}
		}
		ingredients = newList;
	}
	
	private static Pattern orPattern = Pattern.compile(
			"\\s[-=+*]*or[-=+*\\s]*$", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Check for ingredients ending with ".. or"
	 * Then the ingredient following lines is marked as a text line
	 * in order not to be included in the nutritional calculation 
	 */
	public void normalizeIngredientsWithOr() {
		if (ingredients.isEmpty()) {
			return;
		}
		// no need to check the last line
		for (int i=ingredients.size()-2; i>=0; i--) {
			RecipeIngredient ingr = ingredients.get(i);
			
			// only ingredient lines are checked.
			if (ingr.getType() != RecipeIngredient.TYPE_INGREDIENT) continue;
			
			// if no processing and ingredient name ends with OR...
			if (ingr.hasNoProcessing()
				&& ! ingr.hasNoIngredient()
				&& orPattern.matcher(ingr.getIngredient().getName()).find()
				) {
				ingredients.get(i+1).setType(RecipeIngredient.TYPE_TEXT);
			}
			// if processing ends with OR...
			if (! ingr.hasNoProcessing()
				&& orPattern.matcher(ingr.getProcessing()).find()
				) {
				ingredients.get(i+1).setType(RecipeIngredient.TYPE_TEXT);
			}
			
		}
	}
	
/*	
	-- basic: two unit following each other
	3 cups plus 2 tablespoons flour, divided
	1/2 cup plus 2 tablespoons fine dry unseasoned breadcrumbs
	3 lb plus 11/4 cups packed light brown sugar
	-- two different ingr
	2 whole eggs, plus 2 tablespoons water
	2 tsp minced chipotle in adobo plus 1 tsp adobo sauce
	1/2 teaspoon kosher salt, plus 1/4 teaspoon kosher salt
*/	
	private static Pattern plusWithSameIngrPattern = Pattern.compile(
			"^(.*?),?\\s*(?:plus|\\+)\\s+("+RecipeIngredient.SINGLE_AMOUNT_AS_ONE+")\\s*([\\w\\-]+)\\.?(.*)",
			Pattern.CASE_INSENSITIVE);

	
	/**
	 * Check for ingredients starting with "plus 3 tsp ....."
	 * Then the ingredient is split into two lines. 
	 */
	public void normalizeIngredientsWithPlus() {
		if (ingredients.size()==0) {
			return;
		}
		List<RecipeIngredient> newList = new ArrayList<RecipeIngredient>();
		
		for (int i=0; i<ingredients.size(); i++) {
			RecipeIngredient ingr = ingredients.get(i);
			
			//no check if no ingredient.
			if (ingr.hasNoIngredient()) {
				newList.add(ingr);
				continue;
			}
			
			String name = ingr.getIngredient().getName();
			
			Matcher matcher = plusWithSameIngrPattern.matcher(name);
			if (! matcher.find()) {
				newList.add(ingr);
				continue;
			}

			// is it a know unit? (eg. "3 egg whites")
			Unit unit = new Unit(matcher.group(3));
			String prefix = "";
			if (! unit.isKnown()) {
				prefix = unit.getName();
				unit = null;
			}

			// make the new ingredient
			RecipeIngredient newIngr = new RecipeIngredient();
			newIngr.setAmount(RecipeIngredient.getNumber(matcher.group(2)));
			newIngr.setUnit(unit);
			newIngr.setProcessing(ingr.getProcessing());

			// if there was something before the plus then it is the first ingr
			if (matcher.group(1)!=null && matcher.group(1).length()>0) {
				ingr.getIngredient().setName(matcher.group(1).trim());
				name = (prefix + " " + matcher.group(4)).trim();
				newIngr.setIngredient(new Ingredient(name));
			} else {
				// the same ingredient for both
				name = (prefix + " " + matcher.group(4)).trim();
				ingr.getIngredient().setName(name);
				newIngr.setIngredient(new Ingredient(name));
			}
			
			newList.add(ingr);
			newList.add(newIngr);
		}		
		ingredients = newList;
	}

	public void normalizeTitle() {
		if (title==null || title.trim().length() == 0) {
			title = "UNKNOWN TITLE";
		} else {
			int style = Configuration.getIntProperty("TITLE_CASE_PREFERENCE");
			if (style == 1) {
				title = toTitleCase(title);
			} else if (style == 2) {
				title = title.toUpperCase();
			} 
			// else NO_CHANGE
		}
	}

	private static Pattern DOUBLE_SPACE = Pattern.compile("\\s\\s+", Pattern.MULTILINE);
	public static String removeDoubleSpaces(String str) {
		return DOUBLE_SPACE.matcher(str).replaceAll(" ");
	}
	
	/**
	 * @param str the string to change
	 * @return str converted to TitleCase
	 */
	public static String rawTitleCase(String str) {
		char[] chars = str.trim().toLowerCase().toCharArray();
		boolean found = false;
	 
		for (int i=0; i<chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i])
					|| chars[i] == '-') {
				found = false;
			}
		}
	 
		return String.valueOf(chars);	}
	
	private static Pattern TITLE_SPECIAL_WORDS = Pattern.compile(
			"([\\s\\-]+(?:and|or|the|of|for|with|a))(?=[\\s\\-]+)", 
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE); 
	/**
	 * @param str
	 * @return
	 */
	public static String toTitleCase(String str) {
		String t = removeDoubleSpaces(str);
		t = rawTitleCase(t);
		Matcher m = TITLE_SPECIAL_WORDS.matcher(t);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toLowerCase());
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public void normalizeDirections() {
		directionsRemoveDoubleColons();
		if (Configuration.getBooleanProperty("MOVE_NOTES_IN_DIRECTIONS_TO_NOTES")) {
			directionsExtractNotes();
		}
		if (Configuration.getBooleanProperty("REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS")) {
			directionsRemoveConvertedBy();
			directionsMoveDateAndFrom();
		}
		if (Configuration.getBooleanProperty("REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS")) {
			directionsDeleteNutritionalInfo();
		}
		directionsExtractYields();
		if (Configuration.getBooleanProperty("DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS")) {
			extractServings(getDirectionsAsString());
		}
		if (Configuration.getBooleanProperty("REMOVE_INCORRECT_LINE_BREAKS_FROM_DIRECTIONS")) {
			for (int i=0; i<directions.size(); i++) {
				directions.set(i, removeLineBreaks(directions.get(i)));
			}
			for (int i=0; i<tips.size(); i++) {
				tips.set(i, removeLineBreaks(tips.get(i)));
			}
			note = removeLineBreaks(note);
		}
		if (Configuration.getBooleanProperty("RESTRUCTURE_SENTENCES_IN_DIRECTION")) {
			setDirections(restructureSentences(getDirectionsAsString()));
		}
		if (Configuration.getBooleanProperty("REMOVE_DIRECTION_STEP_NUMBERS")) {
			directionsRemoveParagraphHeader();
		}
	}


	static Pattern leadingSpaces = Pattern.compile("^\\s+", Pattern.MULTILINE);
	static Pattern tailingSpaces = Pattern.compile("\\s+$", Pattern.MULTILINE);
	
	public String replaceAll(String text, Pattern p, String altText) {
		Matcher m = p.matcher(text);
		return m.replaceAll(altText);
	}
	
	public String fixSpaces(String text) {
		// remove tabs
		text = text.replace('\t', ' ');
		// remove leading and trailing spaces
		text = replaceAll(text, leadingSpaces, "");
		text = replaceAll(text, tailingSpaces, "");
		//remove spaces before a comma, colon, period, semicolon
		text = text.replaceAll("\\s+(?=[;:,\\.])", "");
		// Remove spaces after "(" and before ")"
		text = text.replaceAll("\\(\\s+", "(");
		text = text.replaceAll("\\s+\\)", ")");
		// remove double spaces
		text = text.replaceAll("\\s{2,}", " ");

		return text;
	}
	
	// paragraph numbering
	// Too many line breaks.
	/**
	 * Text from the internet or mails (mailing lists) can cause line breaks
	 * strange places in the directions. Try to fix them.
	 */
	public String restructureSentences(String text) {
		text = fixSpaces(text);
		
		return "";
	}
	
	
	/**
	 * Remove double colons - creates problems for RecipeClips
 	 * from eg. this site: http://www.rachaelraymag.com/recipe/berry-special-cake/
	 *     "For the cake::"
	 *  
	 */
	public void directionsRemoveDoubleColons() {
		for (int i=0; i<directions.size(); i++) {
			directions.set(i, directions.get(i).replace("::", ":"));
		}
	}
	
	
	static Pattern paragraphHeader = Pattern.compile("^[\\s-*\\d=)\\.]+");
	/**
	 * remove numbering from the beginning of each paragraph 
	 * in the directions.
	 */
	public void directionsRemoveParagraphHeader() {
		for (int i=0; i<directions.size(); i++) {
			String str = directions.get(i);
			Matcher m = paragraphHeader.matcher(str);
			if (m.find()) {
				directions.set(i, m.replaceFirst(""));
			}
		}
	}
	
	static Pattern linebreaksToRemove = Pattern.compile("(?<![\\n\\r\\.:]|;;)[\\n\\r](?![\\n\\r])");
	static Pattern linebreaksToKeep = Pattern.compile(";;([\\n\\r])");
	/**
	 * Remove all hardcoded linebreaks from the text.
	 * Paragraph breaks are not removed.
	 * @param text
	 * @return
	 */
	public String removeLineBreaks(String text) {
		Matcher m = linebreaksToRemove.matcher(text);
		String result = text;
		if (m.find()) {
			result = m.replaceAll(" ").trim();
		}
		
		m = linebreaksToKeep.matcher(result);
		if (m.find()) {
			result = m.replaceAll("$1").trim();
		}
		
		return result;
	}
	// ---------------- EXAMPLES: ----------------------
	// pepper.  4 servings.  560 calories.
	// brown slightly.  Serves 8.
	// Serves 10 to 12.
	// Serves at least 4 to 6. 
	//  8 to 10 servings.
	// Serves 8-10 people.
	// 4-6 servings.
	// Makes 4 to 6 servings.
	// Yields: 8 to 10 servings.
	// Each casserole serves 6.
	// Serves about 4.
	private static String INT_INTERVAL = "(\\d+)\\s*(?:(?:to|-)\\s*(\\d+))?";
	private static String UNCERTAIN = "(?:about|at\\s*least)";
	private static Pattern servingPattern = Pattern.compile(
			 "(?:"
			+"  "+INT_INTERVAL+"\\s*servings "
			+" | serves\\s*"+UNCERTAIN+"?\\s*"+INT_INTERVAL
			+")"
			, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);
	
	void extractServings(String text) {
		Matcher m = servingPattern.matcher(text);
		if (! m.find()) {
			return;
		}
		
		int index = (m.group(1)==null && m.group(2)==null) ? 2 : 0;
		
		int lower = Integer.parseInt(m.group(1+index));
		int upper = lower;
		if (m.group(2+index)!=null) {
			upper = Integer.parseInt(m.group(2+index));
		}
		
		setServings((int) Math.round((lower+upper)/2.0));
	}
	

	
	// ---------------- EXAMPLES: ----------------------
	// Makes about 1 1/2 quarts.
	// Makes 9 to 12 servings. 
	// Makes 2/3 cup spread.
	// Makes 2-1/4 cup sauce.
	// Makes about 4 cups or enough to fill 40 to 45 pierogies
	// Makes 4
	// Makes 5 to 6 apples.
	// Makes 1 scary spider.
	// Makes 12 servings with 137 calories per serving.
	// Makes 2 loaves, 8 servings each.
	// Makes about 10 cups of soup.
	// Makes Enough for 2 9-Inch Pizzas
	// Makes 2 large or 3 smaller serving
	// Makes about 4 (2 cup) servings
	// Makes 8-1 cup servings.
	// Makes 1 1/2 to 2 dozen rolls.
	// ---Fra MasterCook exports:
	// Yield:  "3 1/2 pounds"
	// Yield:
	//    "8 cups"

/*
	private static Pattern yieldPattern = Pattern.compile(
			"Makes\\s*(?:about)?\\s*(\\d+)\\s+(\\w+)(?:\\.)?",
			Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);
*/
	private void directionsExtractYields() {
		// TODO:
	}
	
	// Wrong:
	// "In this post, a recipe from that article ....."
	// "I got this recipe from Donvier Ice Cream Cookbook .... "
	static Pattern SOURCE_START = Pattern.compile(
		 "^(" 
		+"\\s*(?:recipe\\s*)?   \n"
	    +"  (?:   \n"
	    +"   adapted\\s*from   \n"
	    +"  |busted\\s*by   \n"
	    +"  |\\s*(?-i:B)y   \n" 
	    +"  |compliments\\s*of\\s*:   \n"
	    +"  |contributed\\s*to\\s*the   \n"
	    +"  |(?-i:C)opyright   \n"
	    +"  |courtesy\\s*of   \n"
	    +"  |date\\s*:   \n"
	    +"  |downloaded\\s*from   \n"
	    +"  |file\\s*:?\\s*(?:ftp)   \n"
	    +"  |formatted\\s*[\\w\\s]*by   \n"
	    +"  |from\\s*:   \n"
	    +"  |\\s*(?-i:F)rom[\\w\\s\\~\\-,]*(?:collection|archive|file|list|book|newspaper)   \n"
	    +"  |from\\s*(?:the\\s*)?~~lt   \n"
	    +"  |from\\s*(?:alt|rec)\\.   \n"
	    +"  |(?-i:(?:ALT|REC)\\.(?:FOOD|RECIPES))   \n"
	    +"  |(?:mc|mastercook)\\s*formatt(?:ed|ing)\\s*by   \n"
	    +"  |origin\\s*:   \n"
	    +"  |posted\\s*(?:to|by)   \n"
	    +"  |(?-i:P)repared\\s*by   \n"
	    +"  |printed\\s*in   \n"
	    +"  |recipe\\s*by\\s*:   \n"
	    +"  |recipe\\s*from   \n"
	    +"  |(?-i:S)ubmitted\\s*(?:by|as|to)   \n"
	    +"  |typed\\s*(?:for\\s*you|in\\s*mmformat)?\\s*by\\s:?   \n"
	    +"  |source\\s*:   \n"
	    +"  )"
	    +" )"
        ,
	    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);
	      
	static Pattern SOURCE_MAIL = Pattern.compile(
	    "from\\s*:\\s*   \n"
	    +"	(?:[\\w\\s\\.\"]+)?\\s* # name in front   \n"
	    +"	[\\w\\d\\-\\;\\~\\.]+>?\\s* # email addr   \n"
	    +"   (?:\\(.*?\\))? # name in paren  \n"
	    ,
	    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);

	//Date: Wed, 05 Jun 1996 07:40:05 -0700 
	//Date: 20 Aug 1996 12:49:26 GMT
	//Date: Thu, 15 Aug 1996 10:21:06 -0700 (PDT)
	static Pattern SOURCE_DATE = Pattern.compile(
		"date\\s*:\\s*   \n"
		+"	(?:\\w+,)\\s* # weekday   \n"
		+"	\\d+\\s*\\w+\\s* # day and month   \n"
		+"	[\\s\\d\\:\\,\\-\\+]+\\s* # year, time, gmt-displacement   \n"
		+"	(?:\\(?[A-EG-Z][A-Z]+\\)?)? #timezone name (not 'From')    \n"
	    ,
	    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);

	// DATE or MAIL
	static Pattern SOURCE_START_DATE_OR_MAIL = Pattern.compile(
			"^\\s*("
			+ SOURCE_DATE.pattern()
			+ "|"
			+ SOURCE_MAIL.pattern()
			+")"
		    ,
		    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);
	
	static Pattern NOTES = Pattern.compile(
			"notes\\s*:(.*)$"
		    ,
		    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);
			

	public void directionsMoveDateAndFrom() {
		// move From and Date fields to the end.
		int count = 0;
		String pretxt = "";
		String txt = getDirectionsAsString();
		while (count++ < 5) { // will loop if only date and mail field in txt.
			Matcher m = SOURCE_START_DATE_OR_MAIL.matcher(txt);
			if (m.find()) {
				String foundTxt = m.group(1); 
				txt = m.replaceAll("");
				if (txt.trim().length()==0) count = 100;
				txt += " " + foundTxt;
				
			// does it still starts with a source start?
			// Then remove the string.	
			} else {
				m = SOURCE_START.matcher(txt);
				if (m.find()) {
					pretxt += m.group(1);
					txt = m.replaceAll("");
				} else {
					count = 100;
				}
			}
		}
		
		// TODO:
		// find a source tag
		Matcher m = SOURCE_START.matcher(txt);
		if (m.find()) {
			String source = m.group(1);
			m.replaceAll("");
			// notes in the source?
			Matcher m1 = NOTES.matcher(source);
			if (m1.find()) {
				String newNote = m1.group(1);
				m1.replaceAll("");
				// also sources in the notes?
				Matcher m2 = SOURCE_START.matcher(newNote);
				if (m2.find()) {
					source += "\n" + m2.group(1);
					m2.replaceAll("");
				}
				addNote(newNote);
			}
			addSource(source);
		}		

		// more notes?
		m = NOTES.matcher(txt);
		if (m.find()) {
			addNote(m.group(1));
			m.replaceAll("");
		}

		setDirections(RecipeIngredient.join(pretxt, " ", txt));
	}
	
	
	//Converted by MC_Buster v5.a.
	static Pattern CONVERTED_BY = Pattern.compile(
		"\\s*Converted\\s*by\\s*(?:MMCONV|M[CM]_Buster)\\s*(?:(?:vers|v)\\.?\\s*[\\d\\.\\w]*)?\\.?\\s*"	
	    ,
	    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);

	/**
	 * Check if any notes tag in the directions and move it the notes field
	 */
	public void directionsRemoveConvertedBy() {
		// remove 'empty' statement with conversion.
		setDirections(CONVERTED_BY.matcher(getDirectionsAsString()).replaceAll(" "));
	}
	
	// Notes
	static Pattern DIRECTIONS_NOTES = Pattern.compile(
			"notes\\s*:(.*)$"	
		    ,
		    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
	/**
	 * Check if any notes tag in the directions and move it the notes field
	 */
	public void directionsExtractNotes() {
		String txt = getDirectionsAsString();
		// notes in the directions?
		Matcher m = DIRECTIONS_NOTES.matcher(txt);
		if (m.find()) {
			addNote(m.group(1));
			txt = m.replaceAll(" ");
		}
		setDirections(txt);
	}
	
	
// --------------------------------- Nutritional information -----------------------------
	
	//
	//
	// Calories per serving: 160,  Fat grams: 0
	// Per serving: 121 Calories; 2g Fat (11% calories from fat); 6g Protein; 21g  Carbohydrate; 6mg Cholesterol; 81mg Sodium
	// Calories/Fat per serving: 108-162 cals
	// Nutritional analysis per serving: 459 calories; 8.3 grams total fat;  (5.3 grams saturated fat); 3.8 grams protein; 28.5 grams  carbohydrates; 25 milligrams cholesterol; 77.4 milligrams sodium.
	// Per serving:  90 calories, .1 g fat, 0 cholesterol, 13 mg sodium.
	// 164 Calories - 3 g.  Protein - 20 mg. Carb. - 8 g. Fat - 56 mg. Chol. - 91  mg. Calcium - Per Serving
	// Per serving: Calories 111 No fat No cholesterol No sodium
	// Per serving: 358 Calories (kcal); 30g Total Fat; (72% calories from fat);  5g Protein; 22g Carbohydrate; 191mg Cholesterol; 27mg Sodium Food
	// Count 1 oz. evaporated skim milk per serving.
	// NUTRITIONAL INFORMATION (PER SERVING): 347 calories (21 percent from fat);  8 grams of fat; 112 mg of calcium; 7 grams of protein
	// PER SERVING: 134 CAL.; 1G PROT.; 0 TOTAL FAT (0 SAT. FAT); 33G CARB.; 0  CHOL.; 8MG SOD.; 2G FIBER
	// PER SERVING: 120 CAL.;OPROT.; 0 TOTAL FAT (0 SAT. FAT); 31G CARB.; 0 CHOL;  5MG SOD.; 2G FIBER.
	// Per serving: Calories 97 No fat No cholesterol Sodium 6mg
	// Nutrition information per serving: Calories ...... 364 Fat ............ 24  g Cholesterol .. 220 mg Sodium ...... 40 mg Carbohydrates .. 38 g Protein  ......... 4 g
	// Per serving:      111    cals, 0.4 g fat (3%cff).
	// PER SERVING: 101 cal., 2g Pro.,  22g Carbo., 1g fat, 0mg chol., 28mg Sodium
	//
	// ----------------
	// Each of 6  servings contains about: 309 calories; 31 mg sodium;
	// Calories 97 No fat No cholesterol Sodium 6mg
	// Nutrition information Calories ...... 364 Fat ............ 24  g Cholesterol .. 220 mg Sodium ...... 40 mg Carbohydrates .. 38 g Protein  ......... 4 g
	// NUTRITIONAL INFORMATION: CALORIES 132 (3% from fat); PROTEIN 0.5g; FAT 0.4g  (sat 0.1g, mono 0.1g, poly 0.1g); CARB 30.2g; FIBER 1.5g; CHOL 1mg; IRON  0.4mg; SODIUM 3mg;CALC 13mg
	// Nutrient Analysis Calories 101 kcal, Protein 2 g ........
	// Each 1/2 cup serving: 406 calories, 30 mg. sodium, 109 mg cholesterol, 
	// Per 1/2 cup serving: 415 calories, 9 g protein,
	// Calories: 122 Protein: 1g Carbohydrates: 31g Fat: trace  Sodium: 8mg Cholesterol: trace
	// Calories: 86. Fat: .40 g.  Sodium 1 mg. Protein 1 g. Carbohydrate 21 g. Cholesterol 0.
	// PER SERVING:  (with 2 tablespoons poaching liquid per pear): 630  calories, 8 g protein, 78 g carbohydrate,
	// 
	// 
	// 


	//
	//
	// Diabetic exchanges:
	// Exchanges: 0 Grain(Starch); 0 Lean Meat; 0 Vegetable; 0 Fruit; 6 Fat;    1 1/2    Other Carbohydrates
	// Exchanges: 1/2 Grain(Starch); 2 Lean Meat; 0 Vegetable; 6 Fruit; 34 Fat; 4  Other Carbohydrates
	// 
	// 
	static String NUTR_HEADER =
	    "(?:"
	    +"  (?:nutrition|nutritional|nutrient)?\\s*(?:analysis|information|details)"
	    +"     |calories(?:/fat)?\\s*per[\\s\\d\\w/]*?serving"
	    +"     |calories\\s*per(?:.*?)serving:?"
	    +"     |(?:each|per)[\\s\\d\\w/]+?serving.*?:"
	    +"    )\\s*"
		+"   (?:\\(?per[\\s\\d\\w/]*?serving\\s*\\)?)?:? # optional '(per 1 c serving):'   \n"
		+"   (?:\\(.*?\\))?:? # additional '(..info..):'\n"
		;
	
	static String NUTR_NAME = 
		 "(?:total|of|saturated|dietary)?\\s*"
	     +"  (?:carbohydrates?"
	     +"  |carbo"
	     +"  |carb"
	     +"  |calcium"
	     +"  |calium"
	     +"  |calories"
	     +"  |cholesterol"
	     +"  |chol"
	     +"  |fat"
	     +"  |fiber"
	     +"  |protein"
	     +"  |pro"
	     +"  |prot"
	     +"  |sodium"
	     +"  |sod"
	     +"  )\\.?"
	     ;
		  
	static String NUTR_AMOUNT = "(?:[\\d\\.,]+|no|trace|less\\s*than\\s*one)";
	static String NUTR_UNIT = "(?:grams|g|gm|mg|milligrams|cal|cals|kcal|joule|kj)\\.?";

	static Pattern NUTR_INFO = Pattern.compile(
		"(?:Nutr.\\s*Assoc.\\s*:\\s*(?:\\d+\\s+)* "
		+"  |"+NUTR_HEADER+"\\s*"
		+"     (?:"
		+"       "+NUTR_AMOUNT+"\\s"
		+"       [\\.,;-]*\\s* # spacing to next info\n"
		+"     )?"
		+"     (?:"
		+"     	(?:"
		+"	       "+NUTR_AMOUNT+"\\s*"+NUTR_UNIT+"\\s*+"+NUTR_NAME+"\\s*"
		+"	      |"+NUTR_NAME+"\\s*+"+NUTR_UNIT+"\\s*:?\\s*"+NUTR_AMOUNT
		+"	      |"+NUTR_NAME+"\\s*,?\\s*+"+NUTR_AMOUNT+"\\s*"+NUTR_UNIT
		+"	      |calories\\s*+"+NUTR_AMOUNT
		+"	      |"+NUTR_AMOUNT+"\\s*calories"
		+"     	)"
		+"       (?:\\(.*?\\))?\\s* # additional info in paren\n"
		+"       [\\.,;\\-\\s]+ # spacing to next info\n"
		+"    )+"  
		+" )"      
		,
	    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.COMMENTS);
	//
	public void  directionsDeleteNutritionalInfo() {

		Matcher m = NUTR_INFO.matcher(getDirectionsAsString());
		if (m.find()) {
			setDirections(m.replaceAll(" "));
		}
	}

	/**
	 * @return the id
	 */
    @Override
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
    @Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the hbmVersion
	 */
    @Override
	public int getHbmVersion() {
		return hbmVersion;
	}

	/**
	 * @param hbmVersion the hbmVersion to set
	 */
    @Override
	public void setHbmVersion(int hbmVersion) {
		this.hbmVersion = hbmVersion;
	}

	public int getServings() {
		return servings;
	}

	public void setServings(int servings) {
		this.servings = servings;
	}

	public void setServings(String servingText) {
		extractServings(servingText + " servings");
	}
	
	/**
	 * @return the textAttributes
	 */
	public Map<String, String> getTextAttributes() {
		return textAttributes;
	}

	/**
	 * @param textAttributes the textAttributes to set
	 */
	public void setTextAttributes(HashMap<String, String> textAttributes) {
		this.textAttributes = textAttributes;
	}

	/**
	 * @param name the textAttributes to set
	 */
	public void setTextAttribute(String name, String value) {
		textAttributes.put(name, value);
	}

	/**
	 * @param name the textAttributes to get
	 */
	public String getTextAttribute(String name) {
		return textAttributes.get(name);
	}
	
	/********* getter/setter helpers for text attributes **********/
	public void setAltSourceLabel(String value) {
		setTextAttribute(TEXTATT_ALT_SOURCE_LABEL, value);
	}
	public String getAltSourceLabel() {
		return getTextAttribute(TEXTATT_ALT_SOURCE_LABEL);
	}
	public void setAltSourceText(String value) {
		setTextAttribute(TEXTATT_ALT_SOURCE_TEXT, value);
	}
	public String getAltSourceText() {
		return getTextAttribute(TEXTATT_ALT_SOURCE_TEXT);
	}
	public void setCuisine(String value) {
		setTextAttribute(TEXTATT_CUISINE, value);
	}
	public String getCuisine() {
		return getTextAttribute(TEXTATT_CUISINE);
	}
	public void setCopyright(String value) {
		setTextAttribute(TEXTATT_COPYRIGHT, value);
	}
	public String getCopyright() {
		return getTextAttribute(TEXTATT_COPYRIGHT);
	}
	public void setUrl(String value) {
		setTextAttribute(TEXTATT_URL, value);
	}
	public String getUrl() {
		return getTextAttribute(TEXTATT_URL);
	}
	public void setServingIdeas(String value) {
		setTextAttribute(TEXTATT_SERVINGIDEAS, value);
	}
	public String getServingIdeas() {
		return getTextAttribute(TEXTATT_SERVINGIDEAS);
	}
	public void setWine(String value) {
		setTextAttribute(TEXTATT_WINE, value);
	}
	public String getWine() {
		return getTextAttribute(TEXTATT_WINE);
	}

	
	
	
	
	/**
	 * @return the images
	 */
	public List<Image> getImages() {
		if (images == null) images = new ArrayList<Image>();
		return images;
	}

	/**
	 * @param images the images to set
	 */
	public void setImages(List<Image> images) {
		this.images = images;
	}
	
	/**
	 * Add another image to the recipe.
	 * Set also the image.recipe.
	 * @param image the image to add.
	 */
	public void addImage(Image image) {
		if (images == null) {
			images = new ArrayList<Image>();
		}
		images.add(image);
		image.setRecipe(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: "+getTitle()+"\n");
		sb.append("Author: "+getAuthor()+"\n");
		sb.append("Note: "+getNote()+"\n");
		sb.append("Preptime: "+getPreparationTime()+"\n");
		sb.append("Servings: "+getServings()+"\n");
		sb.append("Yield: "+getYield()+"\n");
		for (RecipeIngredient ri : getIngredients()) {
			sb.append(ri.toString1());
		}
		sb.append("Directions: "+getDirectionsAsString()+"\n");
		sb.append("Tips: "+getTipsAsString()+"\n");
		return sb.toString();
	}

	/**
	 * Turn a recipe title into a filename by removing all the strange 
	 * characters
	 * @param title recipe title
	 * @return Candidate for a filename.
	 */
	public String titleAsFilename() {
		String name = title;
		name = name.replaceAll("[^\\w\\d-.+=]", "");
		return name;
	}

	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getNutritionalInfo() {
		return nutritionalInfo;
	}

	/**
	 * @param description the description to set
	 */
	public void setNutritionalInfo(String nutritionalInfo) {
		this.nutritionalInfo = nutritionalInfo;
	}

	/**
	 * @return the ratings
	 */
	public HashMap<String, Float> getRatings() {
		return ratings;
	}

	/**
	 * @param ratings the ratings to set
	 */
	public void setRatings(HashMap<String, Float> ratings) {
		this.ratings = ratings;
	}
	
	public void setRating(String label, float value) {
		ratings.put(label, value);
	}
	
	/**
	 * Set rating with a given max value
	 * @param label
	 * @param value
	 */
	public void setRating(String label, int value, int max) {
		ratings.put(label, 1.0f*value/max);
	}

	/**
	 * @param label name of the rating
	 * @return the value of the given rating
	 *    -1 if a rating with the given label is not found.
	 */
	public float getRating(String label) {
		float value = -1;
		if (ratings.containsKey(label)) {
			value = ratings.get(label);
		}
		return value;
	}

	/**
	 * Get a normalized rating to a given max-rating
	 * @param label
	 * @return
	 */
	public int getRating(String label, int max) {
		int value = -1;
		if (ratings.containsKey(label)) {
			value = Math.round(ratings.get(label)*max);
		}
		return value;
	}

	/**
	 * @return the fileSource
	 */
	public String getFileSource() {
		return fileSource;
	}

	/**
	 * @param fileSource the fileSource to set
	 */
	public void setFileSource(String fileSource) {
		this.fileSource = fileSource;
	}

	/**
	 * @return the directionImages
	 */
	public List<Image> getDirectionImages() {
		return directionImages!=null ? directionImages : new ArrayList<Image>();
	}

	public Image getDirectionImage(int index) {
		Image result = null;
		if (directionImages!=null
			&& index>=0
			&& index<directionImages.size()) {
			result = directionImages.get(index);
		}
		return result;
	}
	
	
	/**
	 * @param directionImages the directionImages to set
	 */
	public void setDirectionImages(List<Image> directionImages) {
		this.directionImages = directionImages;
	}

	public void setDirectionImage(int index, Image image) {
		if (directionImages==null) {
			directionImages = new ArrayList<Image>();
		}
		// expand the list as needed
		if (index>=directionImages.size()) {
			for (int i=directionImages.size()-1; i<index; i++) {
				directionImages.add(null);
			}
		}
		directionImages.set(index, image);
	}

	/**
	 * @return the chapter
	 */
	public Folder getFolder() {
		return folder;
	}

	/**
	 * @param chapter the chapter to set
	 */
	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	/**
	 * Set an attribute in groovy style
	 * @param attributeName
	 * @param value
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public void set(String attributeName, String value) {
	    String name = attributeName;
		if ("preptime".equals(name)) {
		    name = "preparationTime";
		} else if ("cooktime".equals(name)) {
		    name = "cookTime";
		} else if ("yieldunit".equals(name)) {
			// ignore old attribute
			return;
		} else if ("servingideas".equals(name)) {
		    name = "servingIdeas";
		} else if ("totaltime".equals(name)) {
		    name = "totalTime";
		}
		String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		try {
			Method method = this.getClass().getDeclaredMethod(methodName, String.class);
			method.invoke(this, value);
		} catch (SecurityException e) {
			throw new RecipeFoxException("Security problem for getting method="+methodName, e);
		} catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new RecipeFoxException("Invalid attribute="+attributeName, e);
		}
	}

    public static void setJsonField(Recipe r, String key, String value) {
    	switch (key) {
    	case "author":
    		r.setAuthor(value);
    		break;
    	case "cooktime": 
    		r.setCookTime(value);
    		break;
    	case "copyright": 
    		r.setCopyright(value);
    		break;
    	case "cusine": 
    		r.setCuisine(value);
    		break;
    	case "description": 
    		r.setDescription(value);
    		break;
    	case "directions": 
    		r.setDirections(value);
    		break;
    	case "_imageurl": 
    		// ignore
    		break;
    	case "image": 
    		// TBD
    		break;
    	case "ingredients":
    		r.setIngredients(value);
    		break;
    	case "note":
    		r.setNote(value);
    		break;
    	case "preptime":
    		r.setPreparationTime(value);
    		break;
    	case "servings": 
    		r.setServings(value);
    		break;
    	case "servingideas": 
    		r.setServingIdeas(value);
    		break;
    	case "source": 
    		r.setSource(value);
    		break;
    	case "title": 
    		r.setTitle(value);
    		break;
    	case "totaltime": 
    		r.setTotalTime(value);
    		break;
    	case "url": 
    		r.setUrl(value);
    		break;
    	case "yield": 
    		r.setYield(value);
    		break;
    	case "wine": 
    		r.setWine(value);
    		break;
    	}
    	
    }
	
}
