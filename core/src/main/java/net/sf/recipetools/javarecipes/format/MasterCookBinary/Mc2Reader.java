/**
 * 
 */
package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.format.FormattingUtils;
import net.sf.recipetools.javarecipes.format.RecipeFormatter;
import net.sf.recipetools.javarecipes.model.Cookbook;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;
import net.sf.recipetools.javarecipes.model.Unit;

/**
 * @author Frank
 *
 */
public class Mc2Reader extends MastercookReader implements BinaryInputProcessor, RecipeFormatter {
	
	boolean addCookbookTitleAsCategory = true;
	
	boolean extractImages = true;
	
	String altTimLabel = null;
	
//	static String TAG_PACKAGE_ZIPPED = "PKWr";

	public Map<Integer, String> titleMap = new HashMap<Integer, String>();
	
	// to monitor that the part-tag always is ascending.
	String lastPartTag = "";
	int lastPartNo = 0;
	McxReader mcxReader = null;
	private Map<Integer, String> cuisineStrings = new HashMap<Integer, String>();
	private Map<Integer, String> categoryStrings = new HashMap<Integer, String>();
	private Map<Integer, String> categoryCombinations = new HashMap<Integer, String>();


	public Mc2Reader() {
	}
	
	public Mc2Reader(File file) {
		setFile(file);
		initializeForRead();
	}
	
	void initializeForRead() {
		lastPartTag = "";
		lastPartNo = 0;
		mcxReader = null;
		titleMap.clear();
		cuisineStrings.clear();
		categoryStrings.clear();
		categoryCombinations.clear();
		
		readDatabaseSections();
		mcxReader = null;
		File mcxFile = new File(filename.getAbsolutePath().replaceAll("\\.(?i:mc2)$", ".mcx"));
		if (extractImages && mcxFile.exists()) {
			mcxReader = new McxReader(mcxFile);
		} 
	}

	void readDatabaseSections() {
		readStringTable();
		readCuisines();
		readCategoryStrings();
		readCategoryCombinations();
	}
	
	
	public void readCuisines() {
		cuisineStrings.clear();
		if (! dbSection.containsKey("#nat")) {
			return;
		}
		byte[] dbSection = readDbSection("#nat");
		int addr = 0;
		while (addr<dbSection.length) {
			int strIndex = readIntAt(dbSection, addr);
			int id = readIntAt(dbSection, addr+4);
			@SuppressWarnings("unused")
			int noRecipeWithThisCuisine = readIntAt(dbSection, addr+8);
			@SuppressWarnings("unused")
			int x1 = readIntAt(dbSection, addr+12);
			@SuppressWarnings("unused")
			int x2 = readShortAt(dbSection, addr+16);
			@SuppressWarnings("unused")
			int x3 = readShortAt(dbSection, addr+18);
			cuisineStrings.put(id, keyStrings.get(strIndex));
			addr += 20;
		}
	}

	public void readCategoryStrings() {
		categoryStrings.clear();
		if (! dbSection.containsKey("#cat")) {
			return;
		}
		byte[] dbSection = readDbSection("#cat");
		int addr = 0;
		while (addr<dbSection.length) {
			int strIndex = readIntAt(dbSection, addr);
			int id = readIntAt(dbSection, addr+4);
			@SuppressWarnings("unused")
			int noRecipeWithThisCuisine = readIntAt(dbSection, addr+8);
			@SuppressWarnings("unused")
			int x1 = readIntAt(dbSection, addr+12);
			@SuppressWarnings("unused")
			int x2 = readShortAt(dbSection, addr+16);
			@SuppressWarnings("unused")
			int x3 = readShortAt(dbSection, addr+18);
			categoryStrings.put(id, keyStrings.get(strIndex));
			addr += 20;
		}
	}

	public void readCategoryCombinations() {
		categoryCombinations.clear();
		if (! dbSection.containsKey("#ca#")) {
			return;
		}
		byte[] dbSection = readDbSection("#ca#");
		int addr = 0;
		while (addr<dbSection.length) {
			int noBytes = readShortAt(dbSection, addr);
			if (noBytes == 0) {
				categoryCombinations.put(0, "");
			} else {
				StringBuilder str = new StringBuilder();
				for (int i=0; i<noBytes/2; i++) {
					int categoryId = readShortAt(dbSection, addr+2+2*i);
					str.append(categoryStrings.get(categoryId));
					str.append(';');
				}
				str.deleteCharAt(str.length()-1); // delete the last ';'
				categoryCombinations.put(addr+2, str.toString());
			}
			addr += 2 + noBytes;
		}
	}
	
	
	/**
	 * Return all recipes in the file
	 * @see net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor#read(java.io.File)
	 */
    @Override
	public List<Recipe> read(File f) {
		// ignore anything else than mc2 files.
		if (! f.getName().toLowerCase().endsWith("mc2")) {
			return new ArrayList<Recipe>();
		}
		
		setFile(f);
		initializeForRead();
		return readAllRecipes();
	}
	
	
	/**
	 * Return all recipes in the file
	 */
	List<Recipe> readAllRecipes() {
		List<Recipe> recipes = new ArrayList<Recipe>();
		
		// create the cookbook
		Cookbook cookbook = new Cookbook();
		cookbook.setName(cookbookTitle);
		
		// read the prep and total times.
		
		// get the addresses of the Database index parts (max 500 recipes in each part)
		@SuppressWarnings("unused")
		int noParts = 0;
		seek(dbSection.get("#dbi")+0x48);
		Deque<Long> partAddr = new ArrayDeque<Long>();
		long addr = readInt();
		while (addr != 0xa55a5aa5) {
			if (addr!=0) partAddr.add(addr);
			addr = readInt();
			noParts++;
		}
		
		// move to first index
		seek(partAddr.pop()*pagesize);

		int recipeIndex = 0;
		int noInThisPart = 0;
		while (recipeIndex < noItems) {
			long dataBlockAddr = readInt();
			@SuppressWarnings("unused")
			int no = readInt();
			@SuppressWarnings("unused")
			int recipeId = readInt();
			@SuppressWarnings("unused")
			Date changeDate = readDate();
			@SuppressWarnings("unused")
			int x1 = readInt();
			@SuppressWarnings("unused")
			int x2 = readInt();
			int titleStringAddr = readInt();
			int categoryCombinationAddr = readInt();
			int cuisineId = readInt();
			@SuppressWarnings("unused")
			int x5 = readInt(); // category end?
			int preptime = readShort();
			int totaltime = readShort();
			@SuppressWarnings("unused")
			int x6 = readInt();
			@SuppressWarnings("unused")
			int x7 = readInt();
			@SuppressWarnings("unused")
			int x8 = readInt();

			if (dataBlockAddr!=0) {
				Recipe r = readRecipeAt(dataBlockAddr * pagesize);
				if (r!=null) {
					r.setPreparationTime(preptime);
					r.setTotalTime(totaltime);
					recipes.add(r);

					if (titleStringAddr != 0) {
						r.setTitle(keyStrings.get(titleStringAddr));
					}

					if (cuisineId != 0) {
						r.setCuisine(cuisineStrings.get(cuisineId));
					}
					if (categoryCombinationAddr != 0) {
						r.setCategories(categoryCombinations
								.get(categoryCombinationAddr));
					}
					r.setFolder(cookbook);
				}
			}
			
			//System.out.println(recipeIndex+":"+r.getTitle());
			recipeIndex++;
			noInThisPart++;
			if (noInThisPart >= itemsPerPart) {
				if (partAddr.size()>0) {
					// go to the next block of recipes
					seek(partAddr.pop()*pagesize);
				} else {
					// no more parts -> no more recipes
					break;
				}
				noInThisPart=0;
			}
		}

		return recipes;
	}
	
	Recipe readRecipeAt(long fileAddr) {
		long currentAddr = getFilePointer();
		seek(fileAddr);
		Recipe r = readRecipe();
		seek(currentAddr);
		return r;
	}
	
	Recipe readRecipe() {
		boolean found = checkMark('0');
		if (! found) return null;
		
		long startPos = getFilePointer();
		
		@SuppressWarnings("unused")
		int x = readInt();
		int totalLength = readInt(); // after 0-mark to z-mark
		@SuppressWarnings("unused")
		int length1 = readInt();
		String packType = readTag();
		int lengthOfBlock0 = readInt()+8;
		@SuppressWarnings("unused")
		int length4 = readInt();
		int recipeNo = readInt();
		skipBytes(32);
		// here comes the title
		String title = readZeroTerminatedString();
		titleMap.put(recipeNo, title);
		seek(startPos + lengthOfBlock0);
		
		//TODO: PrepTime and TotalTime need to be read from somewhere else.
		// 1f28
		
		// the content of the recipe
		checkMark('1');
		byte[] block1 = readByteBuffer((int) (startPos+totalLength-getFilePointer()));
		byte[] decoded = null;
		if ("PKWr".equals(packType)) {
			Blast blast = new Blast();
			decoded = blast.blast(block1);
		} else {
			decoded = block1;
		}
		
		// DEBUG:
		// save(decoded);
		
		// set recipe fields.
		Recipe r = new Recipe();
		r.setTitle(title.trim());
		lastPartNo = 0;
		lastPartTag = "";
		extractFromBlock1(r, decoded);
		checkMark('z');
		
		// add the cookbook title as category
		if (addCookbookTitleAsCategory && cookbookTitle.length()>0) {
			r.addCategory(cookbookTitle);
		} 
		
		// add the image if any
		if (extractImages && mcxReader!=null) {
			Image image = mcxReader.getImage(recipeNo);
			if (image!=null) {
				r.addImage(image);
			}
			
			// the images for the directives
			HashMap<Integer, Image> stepMap = mcxReader.directiveImagemap.get(recipeNo);
			if (stepMap!=null) {
				for (int stepNo : stepMap.keySet()) {
					r.setDirectionImage(stepNo, mcxReader.getDirectiveImage(recipeNo, stepNo));
				}
			}
		}
		
		return r;
	}

	private void extractFromBlock1(Recipe recipe, byte[] block1) {
		int noTags = readIntAt(block1, 0);
		int startOfValues = readIntAt(block1, 4);
		@SuppressWarnings("unused")
		int lengthOfDatablock = readIntAt(block1, 8);
		
		int pos = 20;
		for (int i=0; i<noTags; i++) {
			String tag = readTagAt(block1, pos);
			int partNo = readIntAt(block1, pos+4);
			String type = readTagAt(block1, pos+8);
			int tagDataPos = readIntAt(block1, pos+12);
			int x1 = readIntAt(block1, pos+16);
			
			// monitor part no.
			if (partNo != 0) {
				if (tag.equals(lastPartTag)) {
					if (partNo < lastPartNo) {
						throw new RecipeFoxException("Assummed that the part numbers are ascending. tag="+tag+", lastPartNo"+lastPartNo+", partNo="+partNo);
					}
				} else {
					lastPartTag = tag;
					lastPartNo = partNo;
				}
			}
			
			if (x1!=0) throw new RecipeFoxException("Found a tag not followed by 0. Tag="+tag+", x="+x1);
			setRecipeField(recipe, block1, tag, type, startOfValues+tagDataPos-4);
			pos += 20;
		}
		
	}

	private void setRecipeField(Recipe recipe, byte[] block1, String tag,
			String type, int dataPosition) {
		if ("ASLb".equals(tag)) {
			return;
		}
		
		String stringValue = "";
		String tagName = "";
		int intValue = 0;
		float floatValue = 0.0f;
		
		if ("*tan".equals(type)) {
			stringValue = readStringAt(block1, dataPosition).trim();
		} else if ("#2_i".equals(type)) {
			intValue = readShortAt(block1, dataPosition+4);
		} else if ("#4_i".equals(type)) {
			intValue = readIntAt(block1, dataPosition+4);
		} else if ("#pID".equals(type)) { // photo id
			intValue = readIntAt(block1, dataPosition+4);
			// TODO:: DEBUG: save(block1);
		} else if ("#4_r".equals(type)) {
			floatValue = readFloatAt(block1, dataPosition+4);
		} else if ("###4".equals(type)) {
			// embedded tag.
			@SuppressWarnings("unused")
			int length = readIntAt(block1, dataPosition);
			byte[] embeddedBlock = Arrays.copyOfRange(block1, dataPosition+4, block1.length);
			if ("IngR".equals(tag)) {
				RecipeIngredient ingr = new RecipeIngredient();
				ingr.setType(RecipeIngredient.TYPE_INGREDIENT);
				recipe.addIngredient(ingr);
			}
			extractFromBlock1(recipe, embeddedBlock);
			return;
		} else if ("*tag".equals(type)) {
			// embedded tag.
			tagName = readStringAt(block1, dataPosition);
			stringValue = readStringAt(block1, dataPosition+4+tagName.length());
		} else {
			throw new RecipeFoxException("Unknown tag type. Tag="+tag+", type="+type);
		}
		
		if ("ASrc".equals(tag)) {
			//TODO: Alternative source, but what is the label?
			//System.out.println("ASrc="+stringValue);
		} else if ("ATLb".equals(tag)) {
			altTimLabel = stringValue;
		} else if ("ATim".equals(tag)) {
			// TODO: What if the ATim comes before the label? 
			if (altTimLabel != null) {
				recipe.setTime(altTimLabel, intValue);
				altTimLabel = null;
			}
		} else if ("Auth".equals(tag)) {
			recipe.setAuthor(stringValue);
		} else if ("CtId".equals(tag)) {
			// TODO: What is this?
			//System.out.println("CtId="+intValue);
		} else if ("CtNm".equals(tag)) {
			// TODO: What is this?
			//System.out.println("CtNm="+stringValue);
		} else if ("GrId".equals(tag)) {
			// TODO: What is this?
			//System.out.println("GrId="+intValue);
		} else if ("GrNm".equals(tag)) {
			// TODO: What is this?
			//System.out.println("GrNm="+stringValue);
		} else if ("ObId".equals(tag)) {
			// TODO: What is this?
			//System.out.println("ObId="+intValue);
		} else if ("ObNm".equals(tag)) {
			// TODO: What is this?
			//System.out.println("CtNm="+stringValue);
		} else if ("wtfc".equals(tag)) {
			// TODO: What is this?
			//System.out.println("wtfc="+stringValue+", int="+intValue);
		} else if ("CpyR".equals(tag)) {
			recipe.setCopyright(stringValue);
		} else if ("Desc".equals(tag)) {
			recipe.setDescription(stringValue);
		} else if ("DirP".equals(tag)) { // directive photo
			//TODO: Image?
		} else if ("DirS".equals(tag)) {
			System.out.println("DirS="+stringValue);
			//recipe.set.......(stringValue);
		} else if ("DirT".equals(tag)) {
			recipe.addDirections(stringValue);
		} else if ("IAmt".equals(tag)) {
			int noIngredients = recipe.getIngredients().size();
			RecipeIngredient ingr = recipe.getIngredients().get(noIngredients-1);
			ingr.setAmount(floatValue);
		} else if ("ICod".equals(tag)) {
			// TODO: What is this?????????? Nutrional link?
			//System.out.println("ICod="+intValue);
		} else if ("INam".equals(tag)) {
			int noIngredients = recipe.getIngredients().size();
			RecipeIngredient ingr = recipe.getIngredients().get(noIngredients-1);
			ingr.setIngredient(new Ingredient(stringValue));
		} else if ("IPrp".equals(tag)) {
			int noIngredients = recipe.getIngredients().size();
			RecipeIngredient ingr = recipe.getIngredients().get(noIngredients-1);
			ingr.setProcessing(stringValue);
		} else if ("IRow".equals(tag)) {
			int noIngredients = recipe.getIngredients().size();
			RecipeIngredient ingr = recipe.getIngredients().get(noIngredients-1);
			int ingrType = RecipeIngredient.TYPE_INGREDIENT;
			if (intValue == 82) { // 'R'
				ingrType = RecipeIngredient.TYPE_RECIPE;
			} else if (intValue == 83) { // 'S'
				ingrType = RecipeIngredient.TYPE_SUBTITLE;
			} else if (intValue == 84) { // 'T'
					ingrType = RecipeIngredient.TYPE_TEXT;
			} else if (intValue == 73) { // 'I'
				ingrType = RecipeIngredient.TYPE_INGREDIENT;
			} else {
				throw new RecipeFoxException("Unknown Ingredient type (IRow): type="+intValue);
			}
			ingr.setType(ingrType);
		} else if ("IUnt".equals(tag)) {
			int noIngredients = recipe.getIngredients().size();
			RecipeIngredient ingr = recipe.getIngredients().get(noIngredients-1);
			ingr.setUnit(new Unit(stringValue));
		} else if ("Note".equals(tag)) {
			recipe.setNote(stringValue);
		} else if ("ObTp".equals(tag)) {
			// TODO: What is this????? Reference/Ingredient text/subtitle
		} else if ("PrpT".equals(tag)) {
			// TODO: Preparation Time: Ignore it here. It is set from recipe header.
		} else if ("RPrm".equals(tag)) {
			// TODO: What is this????? Video? Values=ChocolateCoconutCreamCake.jpg
		} else if ("Serv".equals(tag)) {
			recipe.setServings(intValue);
		} else if ("Sour".equals(tag)) {
			recipe.setUrl(stringValue);
		} else if ("Srce".equals(tag)) {
			recipe.setSource(stringValue);
		} else if ("SrvI".equals(tag)) {
			recipe.setServingIdeas(stringValue);
		} else if ("VidL".equals(tag)) {
			// TODO: What is this????? Video? Values=ChocolateCoconutCreamCake.jpg
		} else if ("Wine".equals(tag)) {
			recipe.setWine(stringValue);
		} else if ("YldA".equals(tag)) {
			String value = FormattingUtils.formatNumber(floatValue)+" "+recipe.getYield();
			recipe.setYield(value);
		} else if ("YldU".equals(tag)) {
			String value = recipe.getYield()+" "+stringValue;
			recipe.setYield(value);
		} else {
			throw new RecipeFoxException("Unknown tag: "+tag+", value="+stringValue);
		}
	}

	/**
	 * @return the noItems
	 */
	public int getNoRecipes() {
		return noItems;
	}

	/**
	 * @return the cookbookTitle
	 */
	public String getCookbookTitle() {
		return cookbookTitle;
	}

	/**
	 * @return the titleMap
	 */
	public Map<Integer, String> getTitleMap() {
		return titleMap;
	}

	/**
	 * @return the addCookbookTitleAsCategory
	 */
	public boolean isAddCookbookTitleAsCategory() {
		return addCookbookTitleAsCategory;
	}

	/**
	 * @param addCookbookTitleAsCategory the addCookbookTitleAsCategory to set
	 */
	public void setAddCookbookTitleAsCategory(boolean addCookbookTitleAsCategory) {
		this.addCookbookTitleAsCategory = addCookbookTitleAsCategory;
	}

	/**
	 * @return the extractImages
	 */
	public boolean isExtractImages() {
		return extractImages;
	}

	/**
	 * @param extractImages the extractImages to set
	 */
	public void setExtractImages(boolean extractImages) {
		this.extractImages = extractImages;
	}

	@Override
	public void write(List<Recipe> recipe) {
		throw new RecipeFoxException("Mc2Reader do not support writing");
	}

	@Override
	public void startFile(String name) {
		throw new RecipeFoxException("Mc2Reader do not support writing");
	}

	@Override
	public void startFile(File f) {
		throw new RecipeFoxException("Mc2Reader do not support writing");
	}

	@Override
	public void endFile() {
		throw new RecipeFoxException("Mc2Reader do not support writing");
	}
}
