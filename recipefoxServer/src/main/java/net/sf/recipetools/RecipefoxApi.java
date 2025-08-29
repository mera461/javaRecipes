/**
 * 
 */
package net.sf.recipetools;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.sf.recipetools.javarecipes.format.FormatterFactory;
import net.sf.recipetools.javarecipes.format.JsonFormatter;
import net.sf.recipetools.javarecipes.format.RecipeFormatter;
import net.sf.recipetools.javarecipes.format.RecipeTextFormatter;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author Frank
 *
 */
@Service
public class RecipefoxApi {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final int MINUTES_BEFORE_TEMP_FILES_ARE_DELETED = 60;
	private final JsonFormatter jsonFormatter = new JsonFormatter();
	
	
	// cache all temp files for later cleanup.
	Map<File, LocalDateTime> allTempFiles = new ConcurrentHashMap<File, LocalDateTime>();
	
	File formatRecipes(String format, String config, String recipes) {
		logger.info("Formatting recipes, config={}, recipes.length={}, recipes={}", config, recipes.length(), recipes);
		long start = System.currentTimeMillis();
		Configuration.fromJson(config);
		
		RecipeFormatter formatter = FormatterFactory.getFormatter(format);
		File f = null;
		File dir = null;
		try {
			// is images included in the file or apart?
			if (formatter.isImagesInSameFile()) {
				f = File.createTempFile("recipefox-", "."+format);
				f.deleteOnExit();
				allTempFiles.put(f, LocalDateTime.now());
			} else { // like eg. TXT or MXP formats
				// create a directory first
				dir = RecipeTextFormatter.createTempDirectory();
				formatter.setConfig("imageDir", dir.getAbsolutePath());
				f = File.createTempFile("recipefox-", "."+format, dir);
				f.deleteOnExit();
				allTempFiles.put(f, LocalDateTime.now());
				dir.deleteOnExit();
				allTempFiles.put(dir, LocalDateTime.now());
			}
			formatter.startFile(f);
			List<Recipe> allRecipes = jsonFormatter.recipeFromJson(recipes);
			for (Recipe r: allRecipes) {
				r.normalize();
			}
			formatter.write(allRecipes);
			formatter.endFile();

			// zip all files if needed
			if (! formatter.isImagesInSameFile()) {
				File zipfile = File.createTempFile("recipefox-", ".zip");
				zipfile.deleteOnExit();
				allTempFiles.put(zipfile, LocalDateTime.now());
				RecipeTextFormatter.zip(dir, zipfile);
				f = zipfile;
			}
			
			logger.info("Formatted {} recipes in {} ms ", allRecipes.size(), System.currentTimeMillis()-start);
			
			// cleanup?
			cleanupTempFiles();
			
		} catch (Exception e) {
			throw new RecipeFoxException(e);
		}
		return f;
	}
	
	/**
	 * If there are more than 100 temp files then check if we should cleanup any
	 * of the files. 
	 * 
	 * Files are deleted if they are more than 60 minutes old.
	 */
	void cleanupTempFiles() {
		if (allTempFiles.size() < 100) return;
		
		// find all entries more than 60 minutes old
		LocalDateTime now = LocalDateTime.now(); 
		for (File f: allTempFiles.keySet()) {
			LocalDateTime t = allTempFiles.get(f);
			long minutes = ChronoUnit.MINUTES.between(t, now);
			if (minutes > MINUTES_BEFORE_TEMP_FILES_ARE_DELETED) {
				f.delete();
				allTempFiles.remove(f);
			}
			
		}
		
	}
	

}
