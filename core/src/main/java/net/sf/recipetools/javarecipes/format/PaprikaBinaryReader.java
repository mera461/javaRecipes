/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author Frank
 * 
 * Json attributes:
 *  - categories
 *  - cook_time
 *  - created
 *  - description (new)
 *  - difficulty
 *  - directions
 *  - hash (new)
 *  - image_url
 *  - name
 *  - notes
 *  - nutritional_info
 *  - photo (new)
 *  - photo_data
 *  - photo_hash (new)
 *  - photo_large (new)
 *  - photos (new)
 *  - prep_time
 *  - rating
 *  - scale
 *  - servings
 *  - source
 *  - source_url
 *  - total_time (new)
 *  - uid (new)

 * 
 *
 */
public class PaprikaBinaryReader implements AllFileInOneGo, RecipeFormatter, BinaryInputProcessor {
	private static final Logger log = LoggerFactory.getLogger(PaprikaBinaryReader.class);
	
    File tempDir = null;

    public PaprikaBinaryReader() {
    }

    public PaprikaBinaryReader(File file) {
        multipleRecipesFile(file);
    }

    /* 
     * unzip the recipes from the .papirkarecipes file
     */
    public void multipleRecipesFile(File file) {
        if (!file.exists()) {
            throw new RecipeFoxException("The file does not exists: file=" + file.getAbsolutePath());
        }

        // cleanup tempDir
        if (tempDir != null) {
        	RecipeTextFormatter.deleteDirectory(tempDir);
            tempDir = null;
        }

        // right file format?
        if (file.getName().toLowerCase().endsWith("paprikarecipes")) {
            tempDir = RecipeTextFormatter.createTempDirectory();
            RecipeTextFormatter.unzip(file, tempDir);
            List<File> files = RecipeTextFormatter.findFilesWithExtension(tempDir, ".paprikarecipe");
            if (files.size() == 0) {
                throw new RecipeFoxException("Found no files with .paprikarecipe extension in the zip file: "
                        + file.getAbsolutePath());
            }
            
            // gunzip the files
            for (File f: files) {
            	singleRecipeFile(f);
            }
            
        } else {
            throw new RecipeFoxException("The reader should point to a .paprikarecipes export file:\nfile="
                    + file.getAbsolutePath());
        }

    }
    
    public static File gunzip(File infile, boolean deleteGzipfileOnSuccess) {
		File outFile = new File(infile.getParent(), infile.getName().replaceAll("\\.[^.]+$", ".json"));
    	try (GZIPInputStream gin = new GZIPInputStream(new BufferedInputStream(new FileInputStream(infile)));
    		 BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {

    		byte[] buf = new byte[100000];
            int len;
            while ((len = gin.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
     
            fos.close();
            gin.close();
            // delete original file on successful gunzip
            if (deleteGzipfileOnSuccess) {
            	infile.delete();
            }
        } catch (FileNotFoundException e) {
			throw new RecipeFoxException("Could not find file: "+infile.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not gunzip file: "+infile.getAbsolutePath(), e);
		}       
        
        return outFile;
    }
    
    public void singleRecipeFile(File f) {
    	gunzip(f, true);
    }

    /**
     * Return all recipes in the file
     * 
     * @see net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor#read(java.io.File)
     */
    @Override
    public List<Recipe> read(File f) {
        multipleRecipesFile(f);

        return readAllRecipes();
    }

    /**
     * Return all recipes in the file
     */
    List<Recipe> readAllRecipes() {
    	List<Recipe> result = new ArrayList<Recipe>();

        List<File> files = RecipeTextFormatter.findFilesWithExtension(tempDir, ".json");
        if (files==null || files.size()==0) return result;
        
        for (File f: files) {
        	result.add(readRecipe(f));
        }
    	
        return result;
    }

    Recipe readRecipe(File f) {
    	Recipe result = new Recipe();
    	
		ObjectMapper mapper =  JsonFactory.create();
		try (FileReader fr = new FileReader(f)) {
			Map<String, Object> att = ((Map<String, Object>) mapper.fromJson(fr, Map.class));

			// first set the title for reference
			setRecipeField(result, "name", att.get("name"));

			for (String key: att.keySet()) {
				setRecipeField(result, key, att.get(key));
			}
			
		} catch (FileNotFoundException e) {
			throw new RecipeFoxException("Could not find file: "+f.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not read the file: "+f.getAbsolutePath(), e);
		}
		
		return result;
    }

    void setRecipeField(Recipe recipe, String key, Object value) {
    	if (value==null) return;
    	switch (key.toLowerCase()) {
    	case "categories": 
    		for (String s: (List<String>) value) {
    			recipe.addCategory(s);
    		}
    		break;
    	case "cook_time": 
    		recipe.setCookTime((String)value);
			break;
    	case "created": 
			break;
    	case "description":
    		recipe.setDescription((String) value);
    		break;
    	case "difficulty":
    		
			break;
    	case "directions": 
    		recipe.setDirections((String)value);
			break;
    	case "hash":
    		// ignore
    		break;
    	case "image_url": 
			break;
    	case "ingredients": 
    		recipe.setIngredients((String)value);
			break;
    	case "name": 
    		recipe.setTitle((String)value);
			break;
    	case "notes": 
    		recipe.setNote((String)value);
			break;
    	case "nutritional_info":
    		recipe.setNutritionalInfo((String)value);
			break;
    	case "photo":
    		// name of photo file. Ignore
    		break;
    	case "photo_data":
    		Image i = new Image();
    		i.setImageFromBase64((String)value);
    		recipe.addImage(i);
			break;
    	case "photo_hash":
    		// ignore
    		break;
    	case "photo_large":
    		// ignore
    		break;
    	case "photos":
    		// ???? ignore
    		break;
    	case "prep_time": 
    		recipe.setPreparationTime((String)value);
			break;
    	case "rating": 
    		recipe.setRating("Rating", (Integer)value, 5); // max 5 stars
			break;
    	case "scale": 
			break;
    	case "servings": 
    		recipe.setServings((String)value);
			break;
    	case "source": 
    		recipe.setSource((String)value);
			break;
    	case "source_url": 
    		recipe.setUrl((String)value);
			break;
    	case "total_time":
    		recipe.setTotalTime((String) value);
    		break;
    	case "uid":
    		// ignore
    		break;
    	default:
    		log.error("Found unknown attribute: "+key+" with value="+(String)value+" in recipe with name:"+recipe.getTitle());
    	}
    }
    

    @Override
    public void write(List<Recipe> recipe) {
        throw new RecipeFoxException("PaprikaBinary formatter does not support writing.");
    }

    @Override
    public void startFile(String name) {
        throw new RecipeFoxException("PaprikaBinary formatter does not support writing.");
    }

    @Override
    public void startFile(File f) {
        throw new RecipeFoxException("PaprikaBinary formatter does not support writing.");
    }

    @Override
    public void endFile() {
        throw new RecipeFoxException("PaprikaBinary formatter does not support writing.");
    }

    @Override
    public void setConfig(String property, String value) {
    }

    @Override
    public String getConfig(String property) {
        return "";
    }

}
