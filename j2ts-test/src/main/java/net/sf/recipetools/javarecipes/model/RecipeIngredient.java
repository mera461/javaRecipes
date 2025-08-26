/*
 * Created on 24-10-2004
 *
 */
package net.sf.recipetools.javarecipes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.recipetools.javarecipes.format.FormattingUtils;
import net.sf.recipetools.javarecipes.format.HtmlStringBuffer;


/**
 * @author ft
 *
 */
//@Entity
public class RecipeIngredient extends DbEntity {

	public static final int TYPE_INGREDIENT = 0;
	public static final int TYPE_SUBTITLE = 1;
	public static final int TYPE_TEXT = 2;
	public static final int TYPE_RECIPE = 3;
	
	public static final String FIELD_HEAD = "<<";
	public static final String FIELD_TAIL = ">>";
	public static final String FIELD_SEPARATOR = ">><<";

	/** Hibernate ID: primary key */
	//@Id
	//@GeneratedValue
	Long id;

	/** the hibernate HBM version */
	//@Version
	int hbmVersion;
	
	//@ManyToOne( targetEntity = net.sf.recipetools.javarecipes.model.Recipe.class )
	//@JoinColumn(nullable = false)
	private Recipe		recipe;
	
	//@Basic
	private int			type;
	//@Basic
	private int			no;
	//@Basic
	private float		amount;
	//@ManyToOne( targetEntity = net.sf.recipetools.javarecipes.model.Unit.class )
	//@JoinColumn(nullable = true)
	private Unit		unit;
	//@ManyToOne( targetEntity = net.sf.recipetools.javarecipes.model.Ingredient.class )
	//@JoinColumn(nullable = true)
	private Ingredient	ingredient;
	//@Basic
	private String 		processing;
	
	/**
	 * 
	 */
	public RecipeIngredient() {
		super();
		unit = new Unit();
		ingredient = new Ingredient();
		processing = "";
	}

	/**
	 * Construct a RecipeIngredient from a ingredient line like:
	 * 		"1 oz butter"
	 * 		"1 tsp sugar"
	 * @param line the ingredient line
	 */
	public RecipeIngredient(String line) {
		super();
		setFromString(line);
	}

	/**
	 * Construct a RecipeIngredient from a ingredient line like:
	 * 		"1 oz butter"
	 * 		"1 tsp sugar"
	 * @param type the type of the line (ingredient, subtitle, text, recipe)
	 * @param line the ingredient line
	 */
	public RecipeIngredient(int type, String line) {
		super();
		setFromString(line);
		this.type = type;
	}
	
	
	/**
	 * @param amount
	 * @param units
	 * @param ingredient
	 */
	public RecipeIngredient(float amount, Unit units, Ingredient ingredient) {
		this(amount, units, ingredient, null);
	}

	/**
	 * @param amount
	 * @param units
	 * @param ingredient
	 */
	public RecipeIngredient(float amount, String units, String ingredient) {
		this(amount, units, ingredient, "");
	}

	/**
	 * @param amount
	 * @param units
	 * @param ingredient
	 * @param processing
	 */
	public RecipeIngredient(float amount, String units, String ingredient, String processing) {
		super();
		this.amount = amount;
		if (units==null) {
			this.unit = null;
		} else {
			this.unit = new Unit(units);
		}
		if (ingredient==null) {
			this.ingredient = null;
		} else {
			this.ingredient = new Ingredient(ingredient);
		}
		this.processing = processing;
	}
	
	
	/**
	 * @param amount
	 * @param units
	 * @param ingredient
	 * @param processing
	 */
	public RecipeIngredient(float amount, Unit units, Ingredient ingredient, String processing) {
		super();
		this.amount = amount;
		this.unit = units;
		this.ingredient = ingredient;
		this.processing = processing;
	}

	/**
	 * @param id
	 * @param amount
	 * @param units
	 * @param ingredient
	 */
	public RecipeIngredient(Long id, float amount, Unit units, Ingredient ingredient) {
		super();
		this.id = id;
		this.amount = amount;
		this.unit = units;
		this.ingredient = ingredient;
		this.processing = "";
	}
	
	
	/**
	 * @return Returns the amount.
	 */
	public float getAmount() {
		return amount;
	}
	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}
	/**
	 * @return Returns the ingredient.
	 */
	public Ingredient getIngredient() {
		return ingredient;
	}
	/**
	 * @param ingredient The ingredient to set.
	 */
	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}
	/**
	 * @return Returns the units.
	 */
	public Unit getUnit() {
		return unit;
	}
	/**
	 * @param units The units to set.
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}


	/**
	 * @return Returns the processing.
	 */
	public String getProcessing() {
		return processing;
	}
	/**
	 * @param processing The processing to set.
	 */
	public void setProcessing(String processing) {
		this.processing = processing;
	}
	/**
	 * @return Returns the no.
	 */
	public int getNo() {
		return no;
	}
	/**
	 * @param no The no to set.
	 */
	public void setNo(int no) {
		this.no = no;
	}
	/**
	 * @return Returns the recipe.
	 */
	public Recipe getRecipe() {
		return recipe;
	}
	/**
	 * @param recipe The recipe to set.
	 */
	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}
	
	/**
	 * convert the ingredient line to a string. 
	 * @see java.lang.Object#toString()
	 */
    @Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (hasAmount()) {
			b.append(FormattingUtils.formatNumber(amount));
			b.append(' ');
		}
		if (hasUnit()) {
			b.append(getPluralisedUnitName());
			b.append(' ');
		}
		
		if (hasIngredient()) b.append(ingredient.getName());
		if (hasProcessing()) {
			b.append(" -- ");
			b.append(processing);
		}
		
		return b.toString(); 
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
	
	public boolean hasNoUnit() {
		return unit==null || unit.getName()==null || unit.getName().trim().length()==0;
	}

	public boolean hasNoIngredient() {
		return ingredient==null || ingredient.getName()==null || ingredient.getName().trim().length()==0;
	}

	public boolean hasNoProcessing() {
		return processing==null || processing.trim().length()==0;
	}

	public boolean hasNoAmount() {
		return amount < 0.00001;
	}
	
	public boolean hasAmount() {
		return ! hasNoAmount();
	}
	public boolean hasUnit() {
		return ! hasNoUnit();
	}
	public boolean hasIngredient() {
		return ! hasNoIngredient();
	}
	public boolean hasProcessing() {
		return ! hasNoProcessing();
	}
	
	public boolean isEmpty() {
		return hasNoAmount() && hasNoUnit() && hasNoIngredient() && hasNoProcessing();
	}
	
	/** Matching for amount like "1.5" or "1,5" or "3/4" or "1 1/2" */
	public static final String SINGLE_AMOUNT =        "(?:([\\d\\.,]+)|(?:(\\d+)\\s+)?(?:(\\d+)\\s*/\\s*(\\d+))?)";
	// either 2.0 or 2 3/4 or 1/2, but there must be a number
	public static final String SINGLE_AMOUNT_AS_ONE = "(?:\\d+\\s+\\d+\\s*/\\s*\\d+|\\d+\\s*/\\s*\\d+|\\d+[\\d\\.,]*|[\\.,]\\d+)";
	private static Pattern singleAmountPattern = Pattern
			.compile("^"+SINGLE_AMOUNT+"$");

	/**
	 * @param number
	 *            A string with a representation of a number like "1,5", "1.5" or "3/4"
	 *            or "1 1/2"
	 * @return the floating point representation of the string.
	 */
	static public float getNumber(String number) {
		float value = 0.0f;
		if (number == null) {
			return value;
		}
		number = number.trim();
		if (number.length() == 0) {
			return value;
		}
		
		// Change UTF-8 numbers to ascii numbers
		HtmlStringBuffer str = new HtmlStringBuffer(number);
		number = str.changeUtfNumbers().toString().trim();
		
		Matcher m = singleAmountPattern.matcher(number);
		if (m.find()) {
			if (m.group(2) != null && m.group(4) != null) {
				value = Float.parseFloat(m.group(2))
						+ Float.parseFloat(m.group(3))
						/ Float.parseFloat(m.group(4));

			} else if (m.group(4) != null) {
				value = Float.parseFloat(m.group(3))
						/ Float.parseFloat(m.group(4));
			} else {
				
				value = Float.parseFloat(m.group(1).replace(',', '.'));
			}
		}

		return value;
	}	
	
	public void normalize() {
		normalizeTheTypes();
		
		if (type != TYPE_INGREDIENT && type != TYPE_RECIPE) {
			capitalizeIngredient();
			return;
		}
		
		if (hasNoUnit() && hasNoIngredient() && hasNoProcessing()) {
			return;
		}
		
		checkForMalformedIngredient();
		
		if (hasNoUnit()) {
			unit=null;
		} else {
			unit.normalize();
		}

		// normalize the '--' if any
		normalizeStandardProcessing();

		if (Configuration.getBooleanProperty("USE_WEIGHT_NOT_PACKAGE_SIZE")) {
			checkForBetterUnits();
		}
		
		normalizeIngredient();
		
		postNormalizeProcessing();
		
		
		// normalize units with amounts eg. "14 oz can"
		if (Configuration.getBooleanProperty("CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE")) {
			// normalize in-accurate measures
			normalizeInaccurateMeasures();

			checkForCombinedUnit();
			normalizeEmbeddedUnits();
			normalizeUnits();
		}
		
		if (Configuration.getBooleanProperty("MARK_INGREDIENTS_WITH_NO_AMOUNT_OR_UNIT_AS_TEXT")) {
			normalizeIngredientsWithNoAmountUnit();
		}
		
		if (Configuration.getBooleanProperty("MOVE_SMALL_MED_LARGE_TO_INGREDIENTS")) {
			normalizeSmallMediumLarge();
		}
		
		// capitalize ingredients without amount and unit
		if (Configuration.getBooleanProperty("CAPITALIZE_INGREDIENTS_WITHOUT_AMOUNT_UNIT")) {
			capitalizeIngredient();
		}
		
	}

	public void capitalizeIngredient() {
		if (hasNoAmount() && hasNoUnit() && hasIngredient()) {
			 String name = ingredient.getName().trim();
			 if (Character.isLowerCase(name.charAt(0))) {
				 String newName = "";
				 if (name.length()>0) newName = ""+Character.toUpperCase(name.charAt(0));  
				 if (name.length()>1) newName +=  name.substring(1);
				 ingredient.setName(newName);
			 }
		}
	}
	
	/**
	 * Check for malformed ingredient line:
	 * if everything is in the ingredient name
	 * if the units are in the ingredient name 
	 */
	public void checkForMalformedIngredient() {
		String txt = this.toString();
		RecipeIngredient newIngr = new RecipeIngredient(txt);
		if (detailLevel(this) < detailLevel(newIngr)) {
			this.amount = newIngr.amount;
			this.unit = newIngr.unit;
			this.ingredient = newIngr.ingredient;
			this.processing = newIngr.processing;
		}
	}

	public int detailLevel(RecipeIngredient ri) {
		int result = 0;
		if (ri.hasAmount()) result++;
		if (ri.hasUnit()) result++;
		if (ri.hasIngredient()) result++;
		return result;
	}
	
	/**
	 * If the ingredient have no amount and no unit it is turned into a text line
	 */
	private void normalizeIngredientsWithNoAmountUnit() {
		if (hasNoAmount() && hasNoUnit()) {
			type = TYPE_TEXT;
		}
	}

	private void normalizeTheTypes() {
		if (hasNoIngredient()) {
			return;
		}
		
		// heading ?
		setTypeIfSubtitle(ingredient.getName());
		
		if (type==TYPE_INGREDIENT || type==TYPE_RECIPE) {
			return;
		}
		
		String name = ingredient.getName();
		if(type==TYPE_SUBTITLE) {
			int style = Configuration.getIntProperty("SUBTITLE_CASE_PREFERENCE");
			if (style == 1) {
				name = Recipe.toTitleCase(name);
			} else if (style == 2) {
				name = name.toUpperCase();
			} 
			// else NO_CHANGE
		}
		ingredient.setName(name);
	}

	private void normalizeIngredient() {
		if (hasNoIngredient()) {
			return;
		}
		
		// Normalize the ingredient + processing.
		normalizeProcessing();
		
		checkIngredientAliases();
	}


	static String measureModifiers = "(?:large|small|generous)"; 
	static String measures = "(?:drizzle|few|couple|handful|handfull|slash)";
	static Pattern inaccurateMeasures = 
		Pattern.compile("^\\s*(a\\s*"+measureModifiers+"?\\s+"+measures+")(?:\\s+of)?\\s+",
						Pattern.CASE_INSENSITIVE);

	public void normalizeInaccurateMeasures() {
		if (hasNoIngredient()) {
			return;
		}
		
		String txt = ingredient.getName();
		
		Matcher m = inaccurateMeasures.matcher(txt);
		if (m.find()) {
			String unitToMove = m.group(1);
			ingredient.setName(txt.replace(m.group(0), ""));
			processing = join(unitToMove, ",", processing);
		}
	}

	public void normalizeCommas() {
		String name = ingredient.getName();
		// return if no commas in the ingredients
		if (-1 == name.indexOf(',')) {
			return;
		}
		
		String safePart = "";
		int commaPos = -1;
		for (int i=0; i<name.length(); i++) {
			if (name.charAt(i) == '(') {
				// parenthesis are handled elsewhere
				while (i<name.length() && name.charAt(i)!=')') {
					i++;
				}
				if (i<name.length() && name.charAt(i)==')') {
					safePart = safePart + name.substring(0, i+1);
					name = name.substring(i+1);
					i=-1; // start from the beginning again
				} else {
					// no end parenthesis
					return;
				}
			} else if (name.charAt(i)!=',') {
				continue;
			} else {
				// check if the comma is part of the safe structures
				commaPos = i;
				boolean foundSafeComma = false;
				for (String safeString : safeCommaStructures) {
					int pos = name.indexOf(safeString);
					if (pos >= 0 
						&& pos<commaPos
						&& (pos+safeString.length()) > commaPos) {
						safePart = safePart + name.substring(0, pos+safeString.length());
						name = name.substring(pos+safeString.length());
						commaPos = name.indexOf(',');
						foundSafeComma = true;
						i=-1;
						break;
					}
				}
				if (! foundSafeComma) {
					commaPos = i;
					break;
				} else {
					commaPos = -1;
				}
			}
		}
		
		
/*		
		while (commaPos != -1 && foundComma) {
			// is it inside parenthesis?
			if (isInsideParenthesis(name, commaPos)) {
				
			}
			
			foundComma = false;
			for (String safeString : safeCommaStructures) {
				int pos = name.indexOf(safeString);
				if (pos >= 0 
					&& pos<commaPos
					&& (pos+safeString.length()) > commaPos) {
					safePart = safePart + name.substring(0, pos+safeString.length());
					name = name.substring(pos+safeString.length());
					commaPos = name.indexOf(',');
					foundComma = true;
					break;
				}
			}
		}
*/		
		
		// if the name no longer contains any commas then everything is ok
		if (-1 == commaPos) {
			return;
		}
		
		// move everything after the comma to the processing
		String processingPart = name.substring(commaPos+1).trim();
		processing = join(processing, " ", processingPart);

		// remove it from the ingredient name
		ingredient.setName(safePart + name.substring(0, commaPos));
	}


	// replace "kiwis", "kiwi fruits" with "kiwi"
	static final Pattern KIWI = Pattern.compile("kiwis?(\\s*fruits?)?", Pattern.CASE_INSENSITIVE);
	static final Pattern FLOZ = Pattern.compile("^\\s*fluid\\s*ounces?\\s*", Pattern.CASE_INSENSITIVE);
	static final Pattern INGR_HEADING_TERMINATION = Pattern.compile("(:\\s*$|^\\s*[=+-]{2,}.*?\\s*[=+-]{2,}\\s*$)");
	void checkIngredientAliases() {
		String name = ingredient.getName();
		Matcher m = KIWI.matcher(name);
		if (m.find()) {
			name = m.replaceAll("kiwi");
		}
		
		// starting with 'of' (3 sheets of ...)
		if (name.startsWith("of ")) {
			name = name.substring(2).trim();
		}
		
		// starting with a unit?
		m = FLOZ.matcher(name);
		if (m.find()) {
			name = m.replaceAll("").trim();
		}
		
		// TODO: 'juice'-formatting....
		// juice of 2 lemons
		// rind and juice of 3 large lemons
		// zest and juice of 3 oranges
		// oranges - juice of
		// WATCH OUT FOR: pineapple in juice
		
		// TODO: Check for notes ('egg whites*')
		
		ingredient.setName(name);
	}

	/**
	 * Set the type to Subtitle if the given string matches a subtitle
	 * @param name
	 */
	private void setTypeIfSubtitle(String name) {
		if (name==null || name.length()==0) {
			return;
		}
		if (! Configuration.getBooleanProperty("DETECT_AND_MARK_SUBTITLES")) return;
		Matcher m;
		m = INGR_HEADING_TERMINATION.matcher(name);
		if (m.find() && hasNoAmount() && hasNoUnit() && hasNoProcessing()) {
			type = TYPE_SUBTITLE; 
		}
	}


	/**
	 * Check for the standard '--' to separate processing from the ingredients
	 */
	public void normalizeStandardProcessing() {
		if (hasNoIngredient()) {
			return;
		}

		// check for MasterCook syntax '--'
		Matcher m = MASTERCOOK_SPLIT.matcher(getIngredient().getName());  
		if (m.find()) {
			ingredient.setName(m.group(1).trim());
			addToProcessing(m.group(2).trim(), " ", false);
		}
		
	}
	

	static final String ADVERBS1 = "(?:coarse|diagonal|fin|fine|firm|fresh|light|partial|rough|slight|small|stiff|thick|thin|well)";
	static final String ADVERBS2 = "(?:very\\s+)?(?:"+ADVERBS1+"|"+ADVERBS1+"ly)";
	static final String INCH     = "(?:inch\\.?|in\\.?|\")";
	static final String CUTINTO  =  "(?: "
							 +"   (?:broken|cut|sliced|diced)\\s+in(?:to)?\\s*.*?"
							 // sliced 1/2" thick
							 +"  | (?:broken|cut|sliced|diced)\\s*"+SINGLE_AMOUNT_AS_ONE
							 +"    [\\s\\-]* "+INCH+" [\\s\\-]*" // unit 
							 +"    (?:thick|thk|thin)?"
							 // 1.0 ounce 1/4-inch diced onion
							 +"  | "+SINGLE_AMOUNT_AS_ONE+"[\\s\\-]* "+INCH+" [\\s\\-]* diced"
							 // 1/2" thick cod
							 +"  | "+SINGLE_AMOUNT_AS_ONE+"[\\s\\-]* "+INCH+" [\\s\\-]* (?:thick|thin)"
							 +")"
							 ;
	// cut into .... something
	//my $CUTINTO  = qr/cut\s+in(?:to)?\s+.*?(?:bits|bite.*?size|chunks|cubes|cubles|dices?|florets|halves|lengths|matchsticks|pieces|quarters|rings|rounds|segments|sheets|slices|slivers|sticks?|strips|wedges)/i; 
	static final String PROCESS1 = "(?:beaten|blanced|blanched|boiled|boiling|bottled|broken(?:\\s*up)|canned|chilled|chop|chopped|cleaned|cold|cooked|cooled|cored|crumbled|crush|crushed|"+CUTINTO+"|cubed|dice|deseeded|devained|diced|divided|drained|dried|filtered|firm|fresh|frozen|glaced|grated|grilled|ground|halved|heated|hot|hulled|julienned|mashed|melted|minced|packed|pared|peel|peeled|pickled|pitted|pureed|quartered|rinsed|ripe|(?:at\\s*)room temperature|scalded|seed|seeded|seperated|separated|shelled|shredded|sifted|sliced|snipped|soft|soften|softend|softened|squeezed|steamed|strained|thawed|toasted|torn|trimmed|unbeaten|uncooked|unpeeled|unsifted|very\\s*ripe|warm|warmed|washed|whipped|whisked)";
	static final String PROCESS2 = "(?:"+ADVERBS2+"[\\s-]+)?"+PROCESS1+"(?:\\s+"+ADVERBS2+")?";
	static final String PROCESS3 = PROCESS2+"(?:(?:\\s|\\W|and|or|but)+"+PROCESS2+")*";
	static final Pattern PROCESSING = Pattern.compile("(^"+PROCESS3+"(?:[\\s,;-]+))" + // group 1: beginning of the name
			                                    " |((?:[\\s,;-]+)"+PROCESS3+"$)", // group 2: end of the name
											   Pattern.CASE_INSENSITIVE + Pattern.COMMENTS);
	static final Pattern MASTERCOOK_SPLIT = Pattern.compile("^(.*?[^-])--([^-].*?)$"); 
	
	static final Pattern ENDING_DASH = Pattern.compile("\\s*-\\s*$");
	
	// a parenthesis group, but NOT at the beginning
	static final Pattern PARENTHESIS = Pattern.compile("(\\s*\\([^\\)]*\\)\\s*)");
	
	/**
	 * Move parentheses to the processing
	 */
	public void normalizeProcessingMoveParenthesis() {
		if (! Configuration.getBooleanProperty("MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION")) {
			return;
		}
		// move parenthesis to processing
		String strParens = "";
		String name = getIngredient().getName();
		Matcher m = PARENTHESIS.matcher(name);
		while (m.find()) {
			String text = m.group(1);
			name = m.replaceFirst(" ");
			strParens = join(strParens, " ", text);
			m = PARENTHESIS.matcher(name);
		}
		addToProcessing(strParens, " ", true);
		ingredient.setName(name);
	}

	/**
	 * Move anything after the semicolon to processing
	 */
	public void normalizeProcessingSplitSemicolon() {
		if (Configuration.getBooleanProperty("MOVE_TEXT_AFTER_SEMICOLON_TO_PREPARATION")) {
			String newName = ingredient.getName();
			// check for splitting like ';'
			if (newName.contains(";")) {
				String[] parts = newName.split(";", 2);
				newName = parts[0].trim();
				addToProcessing(parts[1].trim(), "; ", true);
				ingredient.setName(newName);
			}
		}
	}
	
	/**
	 * Move anything after the semicolon to processing
	 */
	static final Pattern OPTIONAL = Pattern.compile("\\s*\\W*\\s*(?:optional|opt|opt\'l)\\s*\\W*\\s*",
			  Pattern.CASE_INSENSITIVE); 
	static final Pattern TO_TASTE = Pattern.compile(",?\\s*((?:|or|or\\s*more)\\s*to\\s*taste)",
			  Pattern.CASE_INSENSITIVE);
	public boolean normalizeProcessingOptional() {
		String newName = ingredient.getName();
		boolean optional = false;
		// check for optional
		if (Configuration.getBooleanProperty("MOVE_OPTIONAL_TO_PREPARATION")) {
			Matcher m = OPTIONAL.matcher(newName);
			if (m.find()) {
				newName = m.replaceAll("");
				optional = true;
			}
		}
		
		// check for "to taste"
		if (Configuration.getBooleanProperty("MOVE_TO_TASTE_TO_PREPARATION")) {
			Matcher m = TO_TASTE.matcher(newName);
			if (m.find()) {
				addToProcessing("("+m.group(1).trim()+ ")", "; ", true);
				newName = m.replaceAll("");
			}
		}
		
		ingredient.setName(newName);
		return optional;
	}
	
	int posOfChar(String str, char c) {
		int pos = str.indexOf(c);
		if (pos < 0) pos = 100000;
		return pos;
	}

	public void normalizeProcessing() {
		String name = ingredient.getName();
		if (processing==null) {
			processing = "";
		}
		
		// what to do first?
		int posComma = posOfChar(name, ',');
		int posSemicolon = posOfChar(name, ';');
		int posParens = posOfChar(name, '(');
		
		if (posComma<posSemicolon && posComma<posParens) {
			// Normalize the ingredient + processing.
			if (Configuration.getBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION")) {
				normalizeCommas();
			}
			normalizeProcessingMoveParenthesis();
			normalizeProcessingSplitSemicolon();
		} else if (posParens<posSemicolon && posParens<posComma) {
			normalizeProcessingMoveParenthesis();
			// Normalize the ingredient + processing.
			if (Configuration.getBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION")) {
				normalizeCommas();
			}
			normalizeProcessingSplitSemicolon();
		} else {
			normalizeProcessingSplitSemicolon();
			normalizeProcessingMoveParenthesis();
			// Normalize the ingredient + processing.
			if (Configuration.getBooleanProperty("MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION")) {
				normalizeCommas();
			}
		}

		boolean optional = normalizeProcessingOptional();
		
		String newName = ingredient.getName();
		if (Configuration.getBooleanProperty("INGREDIENT_PROCESSING_WORDS_TO_PREPARATION")) {
			// check for common processing words (chopped, melted, thinly sliced, grated etc.)
			Matcher m = PROCESSING.matcher(newName);
			while (m.find()) {
				String process = "";
				if (m.group(1)!=null) {
					process = m.group(1);
					newName = newName.substring(m.end(1));  //$'
				} else {
					process = m.group(2);
					newName = newName.substring(0, newName.indexOf(process));     //$`
				}

				process = process.replaceAll("^[\\W\\s]+", "");
				process = process.replaceAll("[\\W\\s]+$", "");
				addToProcessing(process, "; ", true);
			}
		}
		
		// remove ending "-" from ingredient eg. "1 tsp of baking soda - (5ml)"
		Matcher m = ENDING_DASH.matcher(newName);
		if (m.find()) {
			newName = m.replaceAll("");
		}
		
		// optional?
		if (optional) {
			addToProcessing("(optional)", "; ", true);
		}
		
		// save the new values
		if (! name.equals(newName)) {
			ingredient = new Ingredient(newName.trim());
		}
	}
	
	// Units with numbers: "14 oz can", 
	static private final Pattern unitWithAmount = Pattern.compile("("+SINGLE_AMOUNT_AS_ONE+")[\\s\\-]+(\\w+)"); // deleted: [\\s+\\-](\\w+)
	public void normalizeEmbeddedUnits() {
		if (hasNoUnit()) {
			return;
		}
		
		String name = unit.getName();
		
		Matcher m = unitWithAmount.matcher(name);
		if (!m.find()) {
			return;
		}
		Unit unit2 = new Unit(m.group(2));
		if (! unit2.isWeight() && ! unit2.isVolumen()) {
			return;
		}

		float unitAmount = getNumber(m.group(1));
		amount *= unitAmount;
		name = m.group(2);

		// Amount = 14, Unit="oz can"
		m = doubleUnit.matcher(name);
		if (m.find()) {
			name = "ounce";
		}

		unit.setName(name);
	}

	/**
	 * If the unit is small/medium/large then move it to the beginning of
	 * the ingredient. This is Mastercook pequliarity: It can not calculate
	 * the nutrional information when the units are small/med/large, but it 
	 * knows about ingredients like "small egg", "large onion", etc.
	 */
	void normalizeSmallMediumLarge() {
		if (! hasUnit()) return;
		
		String name = unit.getName().toLowerCase();
		if ("small".equals(name)
			|| "medium".equals(name)
			|| "large".equals(name)) {
			ingredient.setName(unit.getName()+" "+ingredient.getName());
			unit = null;
		}
	}
	
	
	static private final Pattern unitWithLeveled = Pattern.compile("\\s*level(ed)?\\s*", Pattern.CASE_INSENSITIVE);
	// Amount = 14, Unit="oz can"
	static private final Pattern doubleUnit = Pattern.compile("^[\\s\\-\\.]*(oz|ounces?)\\.?[\\s\\-\\.]*(.*)$", Pattern.CASE_INSENSITIVE);
	public void normalizeUnits() {
		if (hasNoUnit()) {
			return;
		}
		
		String name = unit.getName();
		
		// heaped tbsp is about 1.5*tbsp
		if (name.startsWith("heaped")) {
			amount *= 1.5;
			name = name.substring(6).trim();
		}
		
		// ignore leveled tbsp...
		Matcher m = unitWithLeveled.matcher(name);
		if (m.find()) {
			name = m.replaceAll("").trim();
		}
		
		// 1 square chocolate equals 1 oz
		if (name.contains("square") && ingredient.getName().contains("chocolate")) {
			name = "ounce";
		}

		// 1 stick butter/margarine equals 4 oz
		if (name.contains("stick")
			&& (ingredient.getName().contains("butter") || ingredient.getName().contains("margarine"))) {
			name = "ounce";
			amount *= 4;
		}
		
		// TODO: check if ingredient begins with (can,carton,package,jar,piece etc.) '14 oz can juice'
		// 1 piece of vanilla bean
		// 'pinch of salt'
		
		unit.setName(name);
	}
	
/////////////////////////////////////////////////////////////////////////////
//	
//	 Check for a better unit specification:
//			1 can (12 oz) some-ingredient
//			1 (12 oz.) can some-ingredient
//			3 (150 g each) pkg some-ingredient
//			1 (2 litres) bottle some-ingredient
//			1 cup (2 sticks) butter
//			1 cups (1 pt.) some-ingredient
//			1 cup cookies -- crushed (about 12 oz.)
//	 Difficult:
//			0.5 of 6 oz. pkg. (1/2 c.) some-ingredient
//			(6 oz.) pkg. of some-ingredient, use 3 tbsp. butter)
//	  		2 cups (1 pint) some-ingredient
//			2/3 cups whole dates; (4 oz.)
//			4 (1 oz) sqs. unsweetened chocolate
//			1 small carton (8 oz.) sour cream
//	
	private static HashMap<String, Integer> unitWeight = new HashMap<String, Integer>();
	
	static {
		unitWeight.put("g", 10);
		unitWeight.put("ml", 10);
		unitWeight.put("cl", 10);
		unitWeight.put("l", 10);
		unitWeight.put("bag", 0);
		unitWeight.put("box", 0);
		unitWeight.put("bottle", 0);
		unitWeight.put("bunch", 0);
		unitWeight.put("can", 0);
		unitWeight.put("container", 0);
		unitWeight.put("dash", 0);
		unitWeight.put("envelope", 0);
		unitWeight.put("handful", 0);
		unitWeight.put("jar", 0);
		unitWeight.put("large", 0);
		unitWeight.put("medium", 0);
		unitWeight.put("package", 0);
		unitWeight.put("small", 0);
		unitWeight.put("square", 0);
		unitWeight.put("tin", 0);
		unitWeight.put("cup", 5);
	}
	
	
	/** 
	 * @param unit the unit
	 * @return Get the weight of the given unit
	 */
	int getUnitWeight(String unit) {
		if (unit==null || unit.length()==0) {
			return 0;
		}
		if (unitWeight.containsKey(unit)) {
			return unitWeight.get(unit);
		} else {
			return 5;
		}
	}
	
	static private Pattern combinedUnit = Pattern.compile("\\s*("+SINGLE_AMOUNT_AS_ONE + ")[\\s\\-]*([\\w\\.\\-]+)[\\.]?",
			Pattern.CASE_INSENSITIVE);
	void checkForCombinedUnit()  {
		if (hasNoUnit()) {
			return;
		}
		String str = unit.getName();
		Matcher m = combinedUnit.matcher(str);
		if (! m.find()) {
			return;
		}

		float newAmount = getNumber(m.group(1));
		String newUnit  = m.group(2).replace(".", "");
		
		if (newUnit.charAt(0) == '-') {
			newUnit = newUnit.substring(1);
		}
		
		// do nothing if not a valid unit
		Unit unit2 = new Unit(newUnit);
		if (! unit2.isKnown()) {
			return;
		}
		
		// only for weights like: 2 (10-lb) jar
		unit2.normalize();
		if (! unit2.isWeight() && ! unit2.isVolumen()) {
			return;
		}
		
		
		
		// update the amount and unit name
		if (hasNoAmount()) {
			amount = newAmount;
		} else {
			amount *= newAmount;
		}
		unit.setName(newUnit);
		unit.normalize();
	}
	
	static private final Pattern BETTERUNIT = Pattern.compile("\\s*\\(\\s*(about\\s*|abt\\.?\\s*)?\\s*("+SINGLE_AMOUNT_AS_ONE + ")\\s*([\\w\\.\\-]+)[\\.]?(\\s*each)?\\s*\\)\\s*",
			Pattern.CASE_INSENSITIVE);
	static private final Pattern FIRSTWORD = Pattern.compile("^\\s*(\\w+)\\.?\\s");

	void checkForBetterUnitsInIngredient() {
		if (hasNoIngredient()) {
			return;
		}
		String str = ingredient.getName();
		Matcher m = BETTERUNIT.matcher(str);
		if (! m.find()) {
			return;
		}
		
		float newAmount = getNumber(m.group(2));
		String newUnit  = m.group(3).replace(".", "");
		boolean each = m.group(4) != null;
		boolean atBegining = m.start() == 0; 
		
		if (newUnit.charAt(0) == '-') {
			newUnit = newUnit.substring(1);
		}
		
		// do nothing if not a valid unit
		if (! new Unit(newUnit).isKnown()) {
			return;
		}
		
		// if the unit is "inch" then it probably something about cutting
		if (newUnit.equalsIgnoreCase("inch")) {
			return;
		}
		
		// delete it from the ingredient and move it to the preparations
		addToProcessing(m.group(0), " ", false);
		str = m.replaceFirst("").trim();

		int oldWeight = 0;
		if (! hasNoUnit()) {
			oldWeight = getUnitWeight(unit.getName());
		}
		int newWeight = getUnitWeight(newUnit);
		
		if (oldWeight < newWeight) {
			if (hasAmount() && (each || atBegining)) {
				amount *= newAmount;
			} else {
				amount = newAmount;
			}
			if (hasNoUnit()) {
				unit = new Unit(newUnit);
			} else {
				unit.setName(newUnit);
			}
			unit.normalize();
		}
		
		// check if the first word in ingredient is a unit
		// in case of: 1 (12 oz.) can some-ingredient
/* TODO: This must be wrong. It is just deleted but not added to the unit */		
		m = FIRSTWORD.matcher(str);
		if (m.find() && new Unit(m.group(1)).isKnown()) {
			str = m.replaceFirst("");
		}
	
		ingredient.setName(str.trim());
	}

	void checkForBetterUnits() {
		checkForBetterUnitsInIngredient();
	}
	
	/**
	 * Join the two strings with the separator given. 
	 * If one of the two strings are empty then dont add the separator
	 * If the second string starts with ',' then the spacer is not added.
	 * @param s1 the first string
	 * @param separator the separator string
	 * @param s2 the second string
	 * @return  the joined string
	 */
	static String join(String s1, String separator, String s2) {
		if (s2==null || s2.length()==0) {
			return s1;
		} else if (s1==null || s1.length()==0) {
			return s2;
		}
		
		if (s2.charAt(0)==',') {
			separator = "";
		}
		
		String res = s1 + separator + s2;
		return res.trim();
	}
	

	static String safeSubstring(String text, int start, int end) {
		if (start > text.length()) {
			return "";
		}
		return text.substring(start, Math.min(end, text.length())).trim();
	}
	

	private static final Pattern processingPattern = Pattern.compile("^(.*?[^-])--([^-].+)$");
	static public RecipeIngredient createFromFixedPositionString(String tekst, int startPos, int amountLength, int unitLength, int ingredientLength) {
		RecipeIngredient ingr = new RecipeIngredient();
		
		// amount
		String amount = safeSubstring(tekst, startPos,startPos+amountLength);
		ingr.setAmount(getNumber(amount));
		
		// unit
		String unit = safeSubstring(tekst, startPos+amountLength, startPos+amountLength+unitLength);
		ingr.setUnit(new Unit(unit));
		
		// ingredient name + processing
		String name = safeSubstring(tekst, startPos+amountLength+unitLength,
										   startPos+amountLength+unitLength+ingredientLength);
		Matcher m = processingPattern.matcher(name);
		if (m.find()) {
			ingr.setIngredient(new Ingredient(m.group(1).trim()));
			ingr.setProcessing(m.group(2).trim());
		} else {
			ingr.setIngredient(new Ingredient(name));
		}
		
		return ingr;
	}	
	
	// Amount: "1-2" "1/2 - 3/4", Must be followed by a space in order NOT
	//         to match "1 10-ounce can sugar" AND at least one digit
	// 
	private static final String AMOUNT = "(" +
								   " \\(?"+
								   "     ("+ SINGLE_AMOUNT_AS_ONE + ")" +
								   "   \\s*(?:\\-|to)\\s*"+
								   "   "+ SINGLE_AMOUNT_AS_ONE +
								   " \\)?" +
								   "| "+SINGLE_AMOUNT_AS_ONE+
								   ")";
	
	//private static String INGREDIENTLINE = "^\\s*((?:about|abt\\.?)\\s*)?"+AMOUNT+"\\s+"+UNIT+"\\s*(.*)";
	private static final Pattern amountPattern = Pattern.compile("^\\s*((?:about|abt\\.?)\\s*)?"+AMOUNT,
									Pattern.CASE_INSENSITIVE + Pattern.COMMENTS);
	private static final Pattern specialLineMarker = Pattern.compile("^\\s*(\\w)>\\s*");
	
	private static final Pattern[] keepTogether = {
	     Pattern.compile("(24/7[\\-\\s]*seasoning)", Pattern.CASE_INSENSITIVE), // http://www.rachaelraystore.com/Product/detail/Rachael-Ray-1-76-oz-24-7-Seasoning-Grinder/395757
	};
	
	
	void setFromString(String text) {
		// remove double colons
		// from eg. this site: http://www.rachaelraymag.com/recipe/berry-special-cake/
		// "For the cake::"
		text = text.replace("::", ":");
		
		
		// check if there is a type marker
		type = RecipeIngredient.TYPE_INGREDIENT;
		Matcher matcher = specialLineMarker.matcher(text);
		if (matcher.find()) {
			String typeMarker = matcher.group(1).toLowerCase(); 
			text = matcher.replaceFirst("");
			if ("i".equals(typeMarker)) {
				type = RecipeIngredient.TYPE_INGREDIENT;
			} else if ("r".equals(typeMarker)) {
				type = RecipeIngredient.TYPE_RECIPE;
			} else if ("s".equals(typeMarker)) {
				type = RecipeIngredient.TYPE_SUBTITLE;
			} else if ("t".equals(typeMarker)) {
				type = RecipeIngredient.TYPE_TEXT;
			} else {
				//throw new RecipeFoxException("Invalid ingredient marker. Should be [I,R,S,T] but was '"+typeMarker+"' in text="+text);
				//some strange text, just add the marker again
				text = matcher.group(0)+text;
			}
		}
		
		// formatted ingredient line?
		if (parseFormattedIngredient(text)) {
			return;
		}
		
		// no processing at all for subtitles, everything goes into the ingredient
		if (type == RecipeIngredient.TYPE_TEXT || type == RecipeIngredient.TYPE_SUBTITLE) {
			ingredient = new Ingredient(text);
			return;
		}

		
		// mark things to keep together
		for (int i=0; i<keepTogether.length;i++) {
			matcher = keepTogether[i].matcher(text);
			if (matcher.find()) {
				text = matcher.replaceAll("æøå$1æøå");
			}
		}
		
		// extract the amount if any
		matcher = amountPattern.matcher(text);
		boolean found = matcher.find();
		if (found) {
			// does it contain a amount range?
			if (matcher.group(3) == null) { 
				// get the amount
				amount = getNumber(matcher.group(2));
			} else {
				amount = getNumber(matcher.group(3));
				// 	move the total expression to the processing field.
				String range = matcher.group(2);
				if (range.contains("(")) {
					processing = range;
				} else {
					processing = "("+range+")";
				}
			}
			// contains about? Like "About 1 tsp salt"
			if (matcher.group(1)!=null) {
				addToProcessing("(about)", " ", true);
			}
			text = matcher.replaceFirst("").trim();
		}
		
		
		// extract the unit
		unit = Unit.extract(text);
		if (unit != null) {
			String unitStr = unit.getName();
			unitStr = unitStr.replaceAll("æøå", "");
			text = text.substring(text.indexOf(unitStr)+unitStr.length()).trim();
		}

		text = text.replaceAll("æøå", "");
		
		ingredient = new Ingredient(text);
		// check if subtitle
		setTypeIfSubtitle(text);

	}

	private static Pattern formattedIngredient = Pattern.compile("^\\s*(?:<q:\\s*(.*?)>)?\\s*(?:<u:\\s*(.*?)>)?\\s*(?:<i:\\s*(.*?)>)?\\s*(?:<p:\\s*(.*?)>)?\\s*$", Pattern.CASE_INSENSITIVE);
	/**
	 * Parse a line and return an recipe ingredient
	 * NOTE: The order is assumed to be fixed.
	 * 
	 * @param line
	 * @return true if found a formatted line
	 */
	boolean parseFormattedIngredient(String line) {
		Matcher m = formattedIngredient.matcher(line);
		if (! m.find()) {
			return false;
		}
		
		// amount
		if (m.group(1)!=null && m.group(1).trim().length()>0) {
			setAmount(RecipeIngredient.getNumber(m.group(1)));
		}
		// unit
		if (m.group(2)!=null && m.group(2).trim().length()>0) {
			setUnit(new Unit(m.group(2).trim()));
		}
		// ingredient
		if (m.group(3)!=null && m.group(3).trim().length()>0) {
			setIngredient(new Ingredient(m.group(3).trim()));
		}
		// processing
		if (m.group(4)!=null && m.group(4).trim().length()>0) {
			setProcessing(m.group(4).trim());
		}
		
		return true;
	}
	
	
	
	public String getPluralisedUnitName() {
		if (hasNoUnit()) {
			return "";
		}

		String result = getUnit().getName();
		if (Configuration.getBooleanProperty("PLURALISE_UNITS")) {
			if (amount > 1.0f) {
				result = getUnit().pluralize();
			}
		}
		
		return result;
	}
	
	public String getName(NamedEntity o) {
		if (o == null || o.getName() == null) {
			return "null";
		} else {
			return o.getName();
		}
	}
	
	public String toString1() {
		StringBuilder sb = new StringBuilder();
		sb.append(getAmount());
		sb.append(';');
		sb.append(getName(getUnit()));
		sb.append(';');
		sb.append(getName(getIngredient()));
		sb.append(';');
		sb.append(getProcessing());
		if (type != TYPE_INGREDIENT) {
			sb.append("; (");
			if (type == TYPE_RECIPE) {
				sb.append("RECIPE");
			} else if (type == TYPE_SUBTITLE) {
				sb.append("SUBTITLE");
			} else if (type == TYPE_TEXT) {
				sb.append("TEXT");
			}
			sb.append(')');
		}
		sb.append('\n');
		return sb.toString();
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	public static void setSafeCommaStructures(List<String> list) {
		safeCommaStructures = list;
	}

	public static List<String> getSafeCommaStructures() {
		return safeCommaStructures;
	}

	/**
	 * Move any 'PLUS' to the end of processing 
	 */
	static Pattern plusWordPattern = Pattern.compile("\\s*PLUS\\s*");
	void postNormalizeProcessing() {
		if (hasNoProcessing()) {
			return;
		}
		
		Matcher m = plusWordPattern.matcher(processing);
		if (m.find()) {
			processing = join(m.replaceAll(" ").trim(), " ", "PLUS");
		}
	}
	
	/**
			// if existing processing starts with "or" then nothing can be added before it
	 * @param text
	 * @param beforeExistingText
	 */
	public void addToProcessing(String text, String separatingChars, boolean beforeExistingText) {
		text = text.trim();
		if (hasNoProcessing()) {
			processing = text;
		} else if (   ( beforeExistingText && ! processing.startsWith("or"))
				   || ( ! beforeExistingText && text.startsWith("or"))
			       ) {
			// can only add before if existing text does not starts with "or"
			// if the text added starts with "or" it is always added before
			processing = join(text, separatingChars, processing);
		} else {
			processing = join(processing, separatingChars, text);
		}
	}
	
	
	// a list of all "structures" with comma that should NOT be moved to the processing field
	// 20120118 - Updated by John Shotsky
	// If a term below is found in the ingredients column, keep the comma.
	// Otherwise, delete the comma and space and move everything after the
	// comma and space  to the preparation column. Users may add new combinations
	// to this file.
	// Note that there are leading or trailing commas and spaces sometimes.
	// These below are the new items - also placed in alphebetical order below.
	public static List<String> safeCommaStructures = new ArrayList<String>(Arrays.asList(
			", packed without oil",
			"baked, unsalted",
			"unsalted, baked",
			"basil, garlic,",
			"basil, pine nut,",
			"beans, navy",
			"bite-size, bear-shaped,",
			"bite-size, bear-shaped",
			"boiled, mashed",
			"mashed, boiled",
			"bone-in, lean",
			"lean, bone-in",
			"bone-in, shortshank",
			"shortshank, bone-in",
			"bone-in, skin-on",
			"skin-on, bone-in",
			"bone-in, skinless",
			"skinless, bone-in",
			"bone-in, sliced",
			"sliced, bone-in",
			"boned, lean",
			"lean, boned",
			"boned, skinned",
			"skinned, boned",
			"boned, split",
			"split, boned",
			"boneless, lean",
			"lean, boneless",
			"boneless, fully",
			"boneless, skinless",
			"skinless, boneless",
			"boneless, whole",
			"whole, boneless",
			"butt, ground",
			"butter, for",
			"canned, no-salt",
			"no-salt, canned",
			"chile, canned",
			"chiles, canned",
			"chilled, cooked",
			"cooked, chilled",
			"chopped, cooked",
			"cooked, chopped",
			"chopped, drained",
			"drained, chopped",
			"chopped, dried",
			"dried, chopped",
			"chopped, fresh",
			"fresh, chopped",
			"chopped, frozen",
			"frozen, chopped",
			"chopped, loosely",
			"chopped, packed",
			"packed, chopped",
			"chopped, peeled",
			"peeled, chopped",
			"peeled, chopped,",
			"chopped, pitted",
			"pitted, chopped",
			"chopped, prewashed",
			"prewashed, chopped",
			"chopped, roasted",
			"roasted, chopped",
			"chopped, seeded",
			"seeded, chopped",
			"chopped, shelled",
			"shelled, chopped",
			"chopped, slivered",
			"slivered, chopped",
			"chopped, smoked",
			"smoked, chopped",
			"chopped, toasted",
			"toasted, chopped",
			"chopped, trimmed",
			"trimmed, chopped",
			"chopped, unpeeled",
			"unpeeled, chopped",
			"chopped, unsalted",
			"unsalted, chopped",
			"chunky, natural-style",
			"natural-style, chunky",
			"cooked, crumbled",
			"crumbled, cooked",
			"cooked, cubed",
			"cubed, cooked",
			"cooked, deveined",
			"deveined, cooked",
			"cooked, diced",
			"diced, cooked",
			"cooked, mashed",
			"mashed, cooked",
			"cooked, peeled",
			"peeled, cooked",
			"cooked, shelled",
			"shelled, cooked",
			"cooked, shelled,",
			"shelled, cooked,",
			"cooked, shredded",
			"shredded, cooked",
			"cooked, skinless",
			"skinless, cooked",
			"cooked, spiral",
			"corn, in husks",
			"cored, peeled",
			"peeled, cored",
			"peeled, cored,",
			"creamy, natural",
			"natural, creamy",
			"crushed, dried",
			"dried, crushed",
			"crushed, drained",
			"drained, crushed",
			"crushed, packed",
			"packed, crushed",
			"crushed, undrained",
			"undrained, crushed",
			"cubed, peeled",
			"peeled, cubed",
			"cubed, seeded",
			"seeded, cubed",
			"cubed, unpeeled",
			"unpeeled, cubed",
			"deveined, peeled",
			"peeled, deveined",
			"peeled, deveined, and",
			"deveined, seeded,",
			"seeded, deveined,",
			"deveined, seeded",
			"seeded, deveined",
			"deveined, shelled",
			"shelled, deveined",
			"diced, husked",
			"husked, diced",
			"diced, peeled",
			"peeled, diced",
			"diced, seeded",
			"seeded, diced",
			"diced, unpeeled",
			"unpeeled, diced",
			"drained, finely",
			"drained, sliced",
			"sliced, drained",
			"dried, mixed",
			"mixed, dried",
			"dried, split",
			"dried, sweetened",
			"sweetened, dried",
			"dry, white",
			"dry-roasted, salted",
			"salted, dry-roasted",
			"dry-roasted, unsalted",
			"unsalted, dry-roasted",
			"earthy, fruity",
			"fruity, earthy",
			"fat-free, lower-sodium",
			"lower-sodium, fat-free",
			"fat-free, less-sodium",
			"less-sodium, fat-free",
			"fine, dry",
			"finely diced, uncooked",
			"uncooked, finely diced",
			"firm, fresh",
			"fresh, firm",
			"firm, large",
			"large, firm",
			"firm, medium",
			"medium, firm",
			"medium, firm,",
			"firm, ripe",
			"ripe, firm",
			"firm, peeled",
			"peeled, firm",
			"firm, seedless",
			"seedless, firm",
			"firm, small",
			"small, firm",
			"firm, unpeeled",
			"unpeeled, firm",
			"free, less-sodium",
			"less-sodium, fat",
			"free, lower-sodium",
			"lower-sodium, fat",
			"fresh, minced",
			"minced, fresh",
			"fresh, frozen",
			"frozen, fresh",
			"fresh, ripe",
			"fresh, ripe, small",
			"ripe, fresh",
			"fresh, sliced",
			"sliced, fresh",
			"fresh, soft",
			"soft, fresh",
			"fresh, thinly",
			"fresh, washed",
			"washed, fresh",
			"frozen, sliced",
			"sliced, frozen",
			"grated, peeled",
			"peeled, grated",
			"grated, raw",
			"raw, grated",
			"ground, toasted",
			"toasted, ground",
			"halibut, lingcod",
			"lingcod, halibut",
			"halved, pitted",
			"pitted, halved",
			"halved, seeded,",
			"seeded, halved,",
			"halved, seeded",
			"seeded, halved",
			"halved, trimmed, ",
			"trimmed, halved",
			"hot, spicy",
			"spicy, hot",
			"hulled, split",
			"split, hulled",
			"husked, finely",
			"Jiffy, Bisquick",
			"jumbo, unpeeled",
			"unpeeled, jumbo",
			"large, peeled",
			"peeled, large",
			"large, thick",
			"thick, large",
			"large, unpeeled",
			"unpeeled, large",
			"lean, grass-fed",
			"lean, lower",
			"lingcod, rockfish",
			"low-fat, reduced-sodium",
			"reduced-sodium, low-fat",
			"low-salt, reduced-fat",
			"reduced-fat, low-salt",
			"lower-sodium, reduced-fat",
			"reduced-fat, lower-sodium",
			"lower-sodium, thinly",
			"mashed, peeled",
			"peeled, mashed",
			"mashed, ripe",
			"ripe, mashed",
			"mashed, unsweetened",
			"unsweetened, mashed",
			"medium, peeled",
			"peeled, medium",
			"medium, ripe",
			"ripe, medium",
			"medium, unpeeled",
			"unpeeled, medium",
			"minced, peeled",
			"peeled, minced",
			"minced, seeded",
			"seeded, minced",
			"mozzarella, asagio",
			"natural-style, reduced fat",
			"reduced fat, natural-style",
			"oil, butter",
			"packed, chopped",
			"packed, shredded",
			"shredded, packed",
			"pared, chopped",
			"peeled, (1/2-inch)",
			"peeled, coarsely",
			"peeled, finely",
			"peeled, pitted",
			"pitted, peeled",
			"peeled, seeded,",
			"seeded, peeled,",
			"peeled, seeded",
			"seeded, peeled",
			"peeled, shredded",
			"shredded, peeled",
			"peeled, sliced",
			"sliced, peeled",
			"peeled, small",
			"small, peeled",
			"peeled, thinly",
			"pickled, sliced",
			"sliced, pickled",
			"pieces, white",
			"pitted, dark, sweet",
			"pitted, ripe",
			"ripe, pitted",
			"pitted, sliced",
			"sliced, pitted",
			"plain, low-fat",
			"portion, bone",
			"quartered, sliced",
			"sliced, quartered",
			"raw, shelled",
			"shelled, raw",
			"raw, unblanched",
			"unblanched, raw",
			"raw, unsalted",
			"unsalted, raw",
			"reduced-fat, reduced-sodium",
			"reduced-sodium, reduced-fat",
			"ripe, sliced",
			"sliced, ripe",
			"ripe, small",
			"small, ripe",
			"roasted, salted",
			"salted, roasted",
			"roasted, unsalted",
			"unsalted, roasted",
			"seeded, coarsely",
			"seeded, finely",
			"seeded, sliced",
			"sliced, seeded",
			"seeded, thinly",
			"sirloin, round",
			"shelled, deveined",
			"deveined, shelled",
			"shelled, deveined,",
			"deveined, shelled,",
			";shortshank, uncooked",
			"shortshank, uncooked",
			"uncooked, shortshank",
			"shredded, skinned",
			"skinned, shredded",
			"skinned, toasted",
			"toasted, skinned",
			"sliced, black",
			"sliced, lower-sodium",
			"sliced, smoked",
			"smoked, sliced",
			"sliced, stuffed",
			"stuffed, sliced",
			"sliced, toasted",
			"toasted, sliced",
			"slivered, toasted",
			"toasted, slivered",
			"small, tender",
			"tender, small",
			"small, uncooked",
			"uncooked, small",
			"small, unpeeled",
			"unpeeled, small",
			"split, toasted",
			"toasted, split",
			"smoked, fully",
			"thawed, drained, and",
			"trimmed, coarsely",
			"unbleached, all-purpose",
			"unpeeled, seeded,",
			"white, yellow",
			"whole-grain, low-carb",
			"whole-grain, low-carbohydrate"			
			));
	
}
