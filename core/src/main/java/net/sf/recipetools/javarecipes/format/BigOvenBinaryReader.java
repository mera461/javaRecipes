/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank
 * 
 *         Tables:
 * 
 *         DBVersionInfo: DBVersion --> 1.1
 * 
 *         IngredientLines int IngredientLinesID bit bHeading String TextQty
 *         String Measure String Name int RecipeID double DblQty String
 *         PrepNotes int LineOrder
 * 
 * 
 *         RecipeNotes: int RecipeNotesID String Notes String User int RecipeID
 *         date Date
 * 
 *         Recipes: int RecipeID String Title String YieldText String
 *         Instructions String Source double YieldNumber bit Edited bit
 *         Vegetarian bit Desserts bit MainDish bit Drinks bit Salad bit
 *         Begetables bit Poultry bit Appetizers bit Breads bit Cakes bit Meats
 *         bit Cookies bit Seafood double ScaleMultiple bit PDABook String
 *         Categories String YieldUnit double TasteRating double
 *         AppearanceRating double EffortRating double AffordableRating String
 *         PrimaryIngredient String Cuisine String PictureFile1 String SubHead
 *         String Notes int ActiveMinutes int TotalMinutes int UniqueID String
 *         AllIngredientsText String AllCategoriesText
 *
 */
public class BigOvenBinaryReader implements AllFileInOneGo, RecipeFormatter, BinaryInputProcessor {
	private static final Logger log = LoggerFactory.getLogger(BigOvenBinaryReader.class);
	
    private final String PASSWORD = "mealpoint123";

    File tempDir = null;
    File dbfile = null;
    Connection connection = null;
    PreparedStatement st = null;

    public BigOvenBinaryReader() {
    }

    public BigOvenBinaryReader(File file) {
        initializeWithFile(file);
    }

    public void initializeWithFile(File file) {
        if (!file.exists()) {
            throw new RecipeFoxException("The file does not exists: file=" + file.getAbsolutePath());
        }

        // cleanup tempDir
        if (tempDir != null) {
        	RecipeTextFormatter.deleteDirectory(tempDir);
            tempDir = null;
        }

        // a zip file?
        if (file.getName().toLowerCase().endsWith("zip")) {
            tempDir = RecipeTextFormatter.createTempDirectory();
            RecipeTextFormatter.unzip(file, tempDir);
            List<File> files = RecipeTextFormatter.findFilesWithExtension(tempDir, ".crb");
            if (files.size() == 0) {
                throw new RecipeFoxException("Found no files with crb extension in the zip file: "
                        + file.getAbsolutePath());
            }
            file = files.get(0);
        }

        if (!file.getName().toLowerCase().endsWith("crb")) {
            throw new RecipeFoxException("The reader should point to a .crb export file:\nfile="
                    + file.getAbsolutePath());
        }

        /*
         * // clean up if any old file if (zip != null) { try { zip.close(); }
         * catch (IOException e) { throw new
         * RecipeFoxException("Could not close the zip file.", e); } files =
         * null; }
         */

        // cleanup if not done
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Could not close the BigOven database: " + e.getMessage(), e);
            }
            connection = null;
        }

        // load DB
        try {
            // register the driver
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            // get the connection
            connection = DriverManager.getConnection(
                    "jdbc:ucanaccess://" + file.getPath(), PASSWORD, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RecipeFoxException(e);
        }
        dbfile = file;
    }

    /**
     * Return all recipes in the file
     * 
     * @see net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor#read(java.io.File)
     */
    @Override
    public List<Recipe> read(File f) {
        initializeWithFile(f);

        return readAllRecipes();
    }

    /**
     * Return all recipes in the file
     */
    List<Recipe> readAllRecipes() {
        Map<Integer, Recipe> recipes = new TreeMap<Integer, Recipe>();
        // iterate the recipes
        try (Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = st.executeQuery("select * from Recipes order by RecipeID");
            while (rs.next()) {
                Recipe r = new Recipe();
                int recipeId = rs.getInt("RecipeID");
                r.setTitle(rs.getString("Title"));
                r.setYield(rs.getString("YieldText"));
                // YieldNumber + YieldUnit ???
                r.setDirections(rs.getString("Instructions"));
                r.setSource(rs.getString("Source"));
                try {
                    if (rs.getInt("Vegetarian") == 1)
                        r.addCategory("Vegetarian");
                    if (rs.getInt("Desserts") == 1)
                        r.addCategory("Desserts");
                    if (rs.getInt("MainDish") == 1)
                        r.addCategory("MainDish");
                    if (rs.getInt("Drinks") == 1)
                        r.addCategory("Drinks");
                    if (rs.getInt("Salad") == 1)
                        r.addCategory("Salad");
                    if (rs.getInt("Poultry") == 1)
                        r.addCategory("Poultry");
                    if (rs.getInt("Appetizers") == 1)
                        r.addCategory("Appetizers");
                    if (rs.getInt("Breads") == 1)
                        r.addCategory("Breads");
                    if (rs.getInt("Cakes") == 1)
                        r.addCategory("Cakes");
                    if (rs.getInt("Meats") == 1)
                        r.addCategory("Meats");
                    if (rs.getInt("Cookies") == 1)
                        r.addCategory("Cookies");
                    if (rs.getInt("Seafood") == 1)
                        r.addCategory("Seafood");
                } catch (SQLException e) {
                    // just ignore it.
                    // it seems that not all database has the categories
                }
                String primaryIngredient = rs.getString("PrimaryIngredient");
                if (primaryIngredient != null && primaryIngredient.length() > 0)
                    r.addCategory(primaryIngredient);

                // TODO: AllCategoriesText
                // |Tomato|Garlic Salt|Onion|Garlic|Vegetables|Appetizers

                // TODO: ScaleMultiple, PDABook, Marked, Favorite, Color
                // ratings
                setRating(rs, r, "TasteRating");
                setRating(rs, r, "AppearanceRating");
                setRating(rs, r, "EffortRating");
                setRating(rs, r, "AffordableRating");

                r.setCuisine(rs.getString("Cuisine"));
                r.setDescription(rs.getString("SubHead"));
                r.setNote(rs.getString("Notes"));
                r.setPreparationTime(rs.getInt("ActiveMinutes"));
                r.setTotalTime(rs.getInt("TotalMinutes"));
                // AllIngredientsText

                // picture file
                String imagename = rs.getString("PictureFile1");
                if (imagename != null && imagename.length() > 0) {
                    File photofile = new File(dbfile.getParentFile(), "Pictures/" + imagename);
                    if (!photofile.exists()) {
                        log.error("Could not find the picture file=" + imagename + " for recipe=" + r.getTitle());
                    } else {
                        r.addImage(new Image(imagename, photofile.getAbsolutePath()));
                    }
                }

                // add the recipe to the result
                recipes.put(recipeId, r);
            }
        } catch (SQLException e) {
            log.error("Error reading from the database.", e);
        }

        // read all the ingredients
        readAllIngredients(recipes);

        // RecipeNotes: ???

        // return in ID order.
        ArrayList<Recipe> result = new ArrayList<Recipe>();
        for (int i : recipes.keySet()) {
            result.add(recipes.get(i));
        }

        // if zip file then delete temp dir
        if (tempDir != null) {
        	RecipeTextFormatter.deleteDirectory(tempDir);
            tempDir = null;
        }

        return result;
    }

    private void setRating(ResultSet rs, Recipe r, String name) throws SQLException {
        double rating = rs.getDouble(name);
        if (rating > 0.001) {
            r.setRating(name, (float) rating / 5.0f);
        }
    }

    void readAllIngredients(Map<Integer, Recipe> recipes) {
        // iterate the recipes
        ResultSet rs = null;
        try (Statement st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            rs = st.executeQuery("select * from IngredientLines");
            while (rs.next()) {
                int id = rs.getInt("RecipeID");
                // strange, but there may be ingredients without a recipe...
                if (!recipes.containsKey(id))
                    continue;
                int lineNo = rs.getInt("LineOrder");
                String line = rs.getString("TextQty") + " " + rs.getString("Measure") + " " + rs.getString("Name");
                RecipeIngredient ri = new RecipeIngredient(line);
                // NB: getBoolean() is really slow
                if (rs.getInt("bHeading") == 1)
                    ri.setType(RecipeIngredient.TYPE_SUBTITLE);
                ri.setProcessing(rs.getString("PrepNotes"));
                recipes.get(id).addIngredient(lineNo - 1, ri);
            }
        } catch (SQLException e) {
            log.error("Error reading ingredients from the database.", e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
            }
        }

        // sanitize the ingredient lines to avoid ingredient lines that is null
        for (Recipe r : recipes.values()) {
            List<RecipeIngredient> ingrs = r.getIngredients();
            while (ingrs.remove(null))
                ;
        }

    }

    @Override
    public void write(List<Recipe> recipe) {
        throw new RecipeFoxException("BigOvenBinary formatter does not support writing.");
    }

    @Override
    public void startFile(String name) {
        throw new RecipeFoxException("BigOvenBinary formatter does not support writing.");
    }

    @Override
    public void startFile(File f) {
        throw new RecipeFoxException("BigOvenBinary formatter does not support writing.");
    }

    @Override
    public void endFile() {
        throw new RecipeFoxException("BigOvenBinary formatter does not support writing.");
    }

    @Override
    public void setConfig(String property, String value) {
    }

    @Override
    public String getConfig(String property) {
        return "";
    }

}
