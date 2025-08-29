/**
 * 
 */
package net.sf.recipetools.javarecipes.format.cookenpro;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.sf.recipetools.javarecipes.model.Chapter;
import net.sf.recipetools.javarecipes.model.Cookbook;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank
 *
 */
public class CookenProV9BinaryReader extends CookenProBinaryReader {
	private static final Logger log = LoggerFactory.getLogger(CookenProV9BinaryReader.class);
	
	private static final String LINE_SEP = "!@#%\\^\\&\\*\\(\\)";
	private static final String FIELD_SEP = "\\|\\|\\|\\|";
	
	ZipFile zip = null;
	Map<String, ZipEntry> files = null;

	
	Map<String, Map<String, String>> food = null;
	Map<String, Map<String, String>> unit = null;
	Map<String, Image> medias = null;
	Map<String, Map<String, String>> chapters = null;
	
	public CookenProV9BinaryReader() {
	}
	
	public CookenProV9BinaryReader(File file) {
		initializeWithFile(file);
	}

    @Override
	public void initializeWithFile(File file) {
		if (! file.exists()) {
			throw new RecipeFoxException("The file does not exists: file="+file.getAbsolutePath());
		}
		
		if (! file.getName().toLowerCase().endsWith("dvo")) {
			throw new RecipeFoxException("The reader should point to a .dvo export file:\nfile="+file.getAbsolutePath());
		}
		
		// clean up if any old file
		if (zip != null) {
			try {
				zip.close();
			} catch (IOException e) {
				throw new RecipeFoxException("Could not close the zip file.", e);
			}
			files = null;
		}
		
		files = new HashMap<String, ZipEntry>();
		
		try {
			zip = new ZipFile(file);
		} catch (ZipException e) {
			throw new RecipeFoxException("Could not open the zip file.", e);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not read the zip file.", e);
		}
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			files.put(entry.getName(), entry);
		}
	}
	
	/**
	 * Return all recipes in the file
	 * @see net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor#read(java.io.File)
	 */
    @Override
	public List<Recipe> read(File f) {
		initializeWithFile(f);
		
		return readAllRecipes();
	}
	
	/**
	 * Returns a map with a map with all the records from given table.
	 * Map: ID -> record
	 *    record: map: field name -> value 
	 * @param tableName
	 * @return
	 */
	public Map<String, Map<String, String>> readTable(String tableName) {
		TreeMap<String, Map<String, String>> result = new TreeMap<String, Map<String, String>>();
		String str = getStringContent("temp_"+tableName+".dsv");
		
		String[] lines = str.split(LINE_SEP);
		String[] fields = lines[0].split(FIELD_SEP);
		
		for (int i=1; i<lines.length; i++) {
			String[] values = lines[i].split(FIELD_SEP);
			Map<String, String> record = new HashMap<String, String>();
			for (int j=0; j<values.length; j++) {
				// remove the "[null]" string
				String s = values[j].trim();
				if ("[null]".equals(s)) {
					s = "";
				}
				// remove possible spill over from a text field, eg.
				// Avocado Breakfast Toast||||Yields: 6 servings | Serving Size: 1 tablespoon avocado spread and 1 slice of toast | Calories: 140 | Total Fat: 6 g | Saturated Fat: 1 g | Trans Fat: 0 g | Cholesterol: 0 mg | Sodium: 65 mg | Carbohydrates: 17 g | Dietary Fiber: 5 g | Sugars: 2 g | Protein: 5 g | SmartPoints: 4 |||||738772||||6.0||||4||||69
				// The Nutritional info mail contain an extra "|"
				s = s.replaceFirst("^\\|+", "");
				record.put(fields[j].trim(), s);
			}
			result.put(record.get("ID"), record);
		}
		return result;
	}
	
	/**
	 * Test if a given table exists
	 * @param tableName
	 * @return
	 */
	public boolean existsTable(String tableName) {
		Boolean exists = false;
		ZipEntry zipEntry = files.get("temp_"+tableName+".dsv");
		if (zipEntry == null) {
			exists = false;
		} else {
			exists = true;
		}
		return exists;
	}
	
	public byte[] getContent(String filename) {
		ZipEntry zipEntry = files.get(filename);
		if (zipEntry == null) {
			throw new RecipeFoxException("Could not find a file with name: "+filename);
		}
		
		byte data[] = null;
		BufferedInputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(zip.getInputStream(zipEntry));
			long size = zipEntry.getSize();
			data = new byte[(int) size];
			inputStream.read(data, 0, (int)size);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not read from the DVO file.", e);
		} finally {
			try {
				if (inputStream != null) inputStream.close();
			} catch (IOException e) {
			}
		}
		return data;
	}
	
	/**
	 * Get the content of a file from the dvo
	 * @param filename
	 * @return
	 */
	public String getStringContent(String filename) {
		return new String(getContent(filename));
	}
	
	
	/**
	 * Return all recipes in the file
	 */
    @Override
	List<Recipe> readAllRecipes() {
		
		Map<String, Recipe> recipes = new TreeMap<String, Recipe>();
		
		// read the ingredients and units
		readSupportTables();
		
		// read titles and notes
		Map<String, Map<String, String>> records = readTable("recipe_desc");
		for (String key : records.keySet()) {
			Map<String, String> record = records.get(key);
			Recipe recipe = new Recipe();
			recipes.put(record.get("ID"), recipe);
			recipe.setId(Long.parseLong(record.get("ID")));
			recipe.setTitle(record.get("TITLE"));
			recipe.setNote(record.get("DESCRIPTION"));
			// set the chapter
			long parent = Long.parseLong(record.get("PARENT"));
			recipe.setFolder(Folder.get(parent));
			//System.out.println("Recipe parent="+parent);
		}
		
		// other recipe data
		records = readTable("recipe");
		for (String key : records.keySet()) {
			Map<String, String> record = records.get(key);
			Recipe recipe = recipes.get(record.get("ID"));
			recipe.setDirections(record.get("INSTRUCTIONS"));
			if (!"0".equals(record.get("SERVES"))) recipe.setServings(record.get("SERVES"));
			if (!"0".equals(record.get("YIELD"))) recipe.setYield(record.get("YIELD"));
			//if (!record.get("COOKTIME").equals("0")) recipe.setYield(record.get("COOKTIME"));
			//if (!record.get("COOKTIME_STRING").equals("0")) recipe.setYield(record.get("COOKTIME_STRING"));
			//if (!record.get("PREPTIME").equals("0")) recipe.setPrep(record.get("PREPTIME"));
			String value = record.get("PREPTIME_STRING"); 
			if (value!= null && !"0".equals(value)) recipe.setPreparationTime(value);
		}
		
		// read the ingredients
		records = readTable("ingredient");
		for (String key : records.keySet()) {
			Map<String, String> record = records.get(key);
			Recipe recipe = recipes.get(record.get("PARENT_ID"));
			if (recipe == null) {
				throw new RecipeFoxException("Could not find recipe with id="+record.get("PARENT_ID"));
			}
			
			String ingrString = record.get("AMOUNT_QTY_STRING")+ " ";
			float amount = Float.parseFloat(record.get("AMOUNT_QTY"));

			// set the unit
			String unitId = record.get("AMOUNT_UNIT");
			if (unitId.length()!=0) {
				Map<String, String> unitRecord = unit.get(unitId);
				String unitString = unitRecord.get("NAME");
				if (amount > 1.0f
					&& unitRecord.get("PLURAL_NAME").length() > 0) {
					unitString = unitRecord.get("PLURAL_NAME");
				}
				ingrString += unitString + " ";
			}
			
			// set the ingredient
			if (record.get("PRE_QUALIFIER").length()>0) ingrString += record.get("PRE_QUALIFIER") + " ";  
			String foodId = record.get("INGREDIENT_FOOD_ID");
			if (foodId.length()!=0) {
				Map<String, String> foodRecord = food.get(foodId);
				String name = foodRecord.get("NAME");
				if (amount > 1.0f
					&& foodRecord.get("PLURAL_NAME").length() > 0) {
					name = foodRecord.get("PLURAL_NAME");
				}
				ingrString += name + " ";
			}
			
			// set the processing
			String processing = record.get("POST_QUALIFIER");
			processing = processing.replaceAll("^\\s*,\\s*", "");
			if (processing.length() > 0) {
				if (ingrString.trim().length() > 0) {
					ingrString += "-- "+processing;
				} else {
					ingrString = processing;
				}
			}
			
			// TODO: type?
			RecipeIngredient ingr = new RecipeIngredient(ingrString);
			ingr.normalize();
			
			// add it to the recipe
			int ingrIndex = Integer.parseInt(record.get("DISPLAY_ORDER"));
			if (recipe.getIngredients() == null) recipe.setIngredients(new ArrayList<RecipeIngredient>());
			List<RecipeIngredient> recipeIngr = recipe.getIngredients();
			if (ingrIndex >= recipeIngr.size() ) {
				// add empty slots if needed
				int noToAdd = ingrIndex-recipeIngr.size()+1;
				for (int i=0; i<noToAdd; i++) {
					recipeIngr.add(null);
				}
			}
		
			recipeIngr.set(ingrIndex, ingr);
		}
	
		// clean up ingredient for all recipes
		for (Recipe r : recipes.values()) {
			List<RecipeIngredient> ingr = r.getIngredients();
			for (int i=0; i<ingr.size();i++) {
				if (ingr.get(i)==null) {
					ingr.remove(i);
					i--;
				}
			}
		}
		
		
		// add all images
		for (String key : medias.keySet()) {
			Recipe recipe = recipes.get(key);
			if (recipe == null) {
				// Test if it is the cookbook image.
				Folder folder = Folder.get(Integer.parseInt(key));
				if (folder != null && folder instanceof Cookbook) {
					Cookbook cb = (Cookbook) folder;
					cb.setImage(medias.get(key));
				} else {
					log.debug("Could not find out where to put image no:"+key+", name="+medias.get(key).getName());
				}
			} else {
				recipe.addImage(medias.get(key));
			}
		}

		return new ArrayList<Recipe>(recipes.values());
	}
	
	/**
	 * Read the FOOD table
	 */
	void readSupportTables() {
		food = readTable("food");
		unit = readTable("unit");
		readCookbooks();
		readChapters();
		readMedias();
	}
	
	/**
	 * Read all cookbooks
	 */
	void readCookbooks() {
		Map<String, Map<String, String>> books = readTable("cookBook_desc");
		for (String key : books.keySet()) {
			Cookbook cookbook = new Cookbook();
			Map<String, String> record = books.get(key);
			cookbook.setName(record.get("TITLE"));
			cookbook.setDescription(record.get("DESCRIPTION"));
			cookbook.setId(Long.parseLong(record.get("ID")));
			//System.out.println("COOKBOOK: id="+cookbook.getId());
		}
	}

	/**
	 * Read all chapters
	 */
	void readChapters() {
		chapters = readTable("chapter");
		for (String key : chapters.keySet()) {
			Chapter chapter = new Chapter();
			Map<String, String> record = chapters.get(key);
			chapter.setName(record.get("TITLE"));
			chapter.setDescription(record.get("DESCRIPTION"));
			chapter.setId(Long.parseLong(record.get("ID")));
			long parent = Long.parseLong(record.get("PARENT"));
			chapter.setParent(Folder.get(parent));
			//System.out.println("CHAPTER: id="+chapter.getId());
		}
	}
	
	void readMedias() {
		medias = new TreeMap<String, Image>();
		Map<String, Map<String, String>> records = null;
		if (existsTable("media_usage")) {
			records = readTable("media_usage");
		} else {
			records = readTable("media");
		}
		for (String key : records.keySet()) {
			Map<String, String> record = records.get(key);
			String entityId = record.get("ENTITY_ID");
			ZipEntry zipEntry = files.get(key);
			if (zipEntry == null) {
				throw new RecipeFoxException("Could not find file with media id="+key);
			}

			Image image = new Image();
			image.setName(key);
			image.setImage(getContent(key));
			medias.put(entityId, image);
		}
	}
	@Override
	public void write(List<Recipe> recipe) {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}

	@Override
	public void startFile(File f) {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}

	@Override
	public void endFile() {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}
}
