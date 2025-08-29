/*
 * Created on 25-10-2004
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.model.Category;
import net.sf.recipetools.javarecipes.model.Configuration;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

/**
 * @author ft
 *
 */
public class MasterCookExport extends RecipeTextFormatter {
	private static final Logger log = LoggerFactory.getLogger(MasterCookExport.class);

    private static final String NEWLINE = "\r\n";

    private static Pattern recipeSeparatorPattern = Pattern.compile(
            "^\\s*\\*\\s*Exported\\s+(?:from|for)\\s+MasterCook\\s*.*?\\*\\s*$", Pattern.CASE_INSENSITIVE);
    private static Pattern recipeByPattern = Pattern.compile("Recipe By\\s*:\\s*(.*)$", Pattern.CASE_INSENSITIVE);
    private static Pattern servingSizePattern = Pattern
            .compile("Serving Size\\s*:\\s*(\\d*)", Pattern.CASE_INSENSITIVE);
    private static Pattern prepTimePattern = Pattern.compile("Preparation Time\\s*:\\s*(\\d*)(?::(\\d*))?",
            Pattern.CASE_INSENSITIVE);
    private static Pattern categoriesPattern = Pattern.compile("Categories\\s*:\\s*(.*)$", Pattern.CASE_INSENSITIVE);

    private static Pattern ingredientHeaderPattern = Pattern.compile("^[M-]{5,5}\\W*([\\s\\w]+)\\W*$");

    boolean strictIngredientFormat = false;

    /**
	 * 
	 */
    public MasterCookExport() {
        super();
    }

    // ---------------------------------------------------------------
    //
    // reading
    //
    // ---------------------------------------------------------------
    @Override
    public List<Recipe> readRecipes(LineNumberReader in) {
        Recipe recipe = new Recipe();
        try {
            // find the first line in the recipe
            boolean found = findFirstLineContaining(in, recipeSeparatorPattern);
            if (!found) {
                return null;
            }

            // read the header data: title, categories, no servings
            readHeader(in, recipe);

            // read the ingredients
            readIngredients(in, recipe);

            // read the ingredients
            readDirections(in, recipe);

            extractExtraFieldsFromDirections(recipe);

        } catch (RuntimeException e) {
            throw new RecipeFoxException("*** ERROR in title: " + recipe.getTitle(), e);
        }

        return Arrays.asList(recipe);
    }

    Pattern descriptionPattern = Pattern.compile("^\\s*Description\\s*:\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE + Pattern.DOTALL);
    Pattern sourcePattern = Pattern.compile("^\\s*Source\\s*:\\s*\"(.*)\"", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);
    Pattern altSourcePattern = Pattern.compile("^\\s*S\\(([\\w\\s]+)\\):\\s*\"(.*)\"", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);
    Pattern copyrightPattern = Pattern.compile("^\\s*Copyright\\s*:\\s*\"(.*)\"", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);
    Pattern cuisinePattern = Pattern.compile("^\\s*Cuisine\\s*:\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);
    Pattern yieldPattern = Pattern.compile("^\\s*Yield\\s*:\\s*\"(.*)\"", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    Pattern totalTimePattern = Pattern.compile("^\\s*Start\\s*to\\s*Finish\\s*Time\\s*:\\s*\"(.*)\"",
            Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    Pattern altTimePattern = Pattern.compile("^\\s*T\\(([\\w\\s]+)\\):\\s*\"(.*)\"", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);
    Pattern winePattern = Pattern.compile("^\\s*Suggested\\s*Wine\\s*:\\s*(.*)$", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);
    Pattern suggestionPattern = Pattern.compile("\\s*Serving\\s*Ideas\\s*:\\s*(.*?)\\s*(NOTES|Nutr\\.\\s+Assoc|$)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
    Pattern nutrAssocPattern = Pattern.compile("^\\s*Nutr\\.\\s*Assoc\\.\\s*:.*$", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);
    Pattern notesPattern = Pattern.compile("\\s*NOTES\\s*:\\s*(.*?)\\s*(Nutr\\.\\s+Assoc|$)", Pattern.CASE_INSENSITIVE
            + Pattern.DOTALL);
    Pattern lineSeparatorPattern = Pattern.compile("^\\s*(?:-\\s+)+-?+$", Pattern.MULTILINE);
    Pattern perServings = Pattern.compile(
            "\\s*Per\\s+Serving\\s*(\\(excluding unknown items\\)\\s*)?:\\s*(.*?)\\.\\s*([\n\r]|$)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
    Pattern ratingsPattern = Pattern.compile("^\\s*Ratings\\s*:\\s*(([a-zA-Z\\s]+\\s+\\d+\\s*)+)",
            Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    Pattern singleRatingPattern = Pattern.compile("\\s*([a-zA-Z\\s]+)\\s+(\\d+)", Pattern.CASE_INSENSITIVE
            + Pattern.MULTILINE);

    public void extractExtraFieldsFromDirections(Recipe recipe) {
        String txt = recipe.getDirectionsAsString();
        if (txt == null || txt.length() == 0) {
            return;
        }

        Matcher m = descriptionPattern.matcher(txt);
        if (m.find()) {
            recipe.setDescription(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = cuisinePattern.matcher(txt);
        if (m.find()) {
            recipe.setCuisine(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = sourcePattern.matcher(txt);
        if (m.find()) {
            recipe.setSource(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = altSourcePattern.matcher(txt);
        if (m.find()) {
            if (m.group(1).equalsIgnoreCase(Recipe.TEXTATT_URL)) {
                recipe.setUrl(m.group(2));
            } else {
                recipe.setAltSourceLabel(m.group(1));
                recipe.setAltSourceText(m.group(2));
            }
            txt = txt.replace(m.group(0), "");
        }
        m = copyrightPattern.matcher(txt);
        if (m.find()) {
            recipe.setCopyright(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = yieldPattern.matcher(txt);
        if (m.find()) {
            recipe.setYield(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = totalTimePattern.matcher(txt);
        if (m.find()) {
            recipe.setTotalTime(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = altTimePattern.matcher(txt);
        if (m.find()) {
            recipe.setTime(m.group(1), m.group(2));
            txt = txt.replace(m.group(0), "");
        }
        m = ratingsPattern.matcher(txt);
        if (m.find()) {
            String str = m.group(1);
            Matcher m2 = singleRatingPattern.matcher(str);
            while (m2.find()) {
                recipe.setRating(m2.group(1), Integer.parseInt(m2.group(2)), 10);
            }
            txt = txt.replace(m.group(0), "");
        }

        m = winePattern.matcher(txt);
        if (m.find()) {
            recipe.setWine(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = suggestionPattern.matcher(txt);
        if (m.find()) {
            recipe.setServingIdeas(m.group(1));
            txt = txt.replace(m.group(0), "\n" + m.group(2));
        }
        m = notesPattern.matcher(txt);
        if (m.find()) {
            recipe.setNote(m.group(1));
            txt = txt.replace(m.group(0), "");
        }
        m = nutrAssocPattern.matcher(txt);
        if (m.find()) {
            txt = txt.replace(m.group(0), "");
        }
        // remove separator line
        m = lineSeparatorPattern.matcher(txt);
        if (m.find()) {
            txt = txt.replace(m.group(0), "");
        }

        // remove calculated energy:
        m = perServings.matcher(txt);
        if (m.find()) {
            txt = txt.replace(m.group(0), "");
        }

        recipe.setDirections(txt);
    }

    private void readHeader(LineNumberReader in, Recipe recipe) {
        String line = "";
        try {
            // skip empty line
            skipEmptyLines(in);

            // get the title
            line = in.readLine();
            recipe.setTitle(line.trim());

            // get the Author
            String[] matches = getMatchesOrThrow(in, recipeByPattern, servingSizePattern, "Recipe By");
            recipe.setAuthor(matches[0]);

            // get the Serving
            line = in.readLine();
            matches = getMatchesOrThrow(line, servingSizePattern, "Serving Size");
            int servings = 0;
            try {
                servings = matches[0] == null ? 0 : Integer.parseInt(matches[0]);
            } catch (NumberFormatException e1) {
                servings = 0;
            }
            recipe.setServings(servings);

            // get the PrepTime
            int minutes = 0;
            try {
                matches = getMatchesOrThrow(line, prepTimePattern, "Preparation Time");
                if (matches.length > 1 && matches[1] != null) {
                    minutes = Integer.parseInt(matches[0]) * 60 + Integer.parseInt(matches[1]);
                } else {
                    minutes = Integer.parseInt(matches[0]);
                }
            } catch (NumberFormatException e) {
                minutes = 0;
            } catch (RuntimeException e) {
                // ignore it if no prep time...
            }
            recipe.setPreparationTime(minutes);

            // get the categories
            line = in.readLine();
            matches = getMatchesOrThrow(line, categoriesPattern, "Categories");
            List<String> categories = splitIntoCategories(matches[0]);

            // more category lines ?
            line = in.readLine();
            while (!line.contains("Amount")) {
                categories.addAll(splitIntoCategories(line));
                line = in.readLine();
            }
            for (String name : categories) {
                recipe.getCategories().add(new Category(name));
            }
        } catch (IOException e) {
            String msg = "Line number=" + in.getLineNumber() + ". MasterCookExport: error reading header from line="
                    + line;
            log.info(msg);
            throw new RecipeFoxException(msg, e);
        }

    }

    List<String> splitIntoCategories(String line) {
        List<String> result = new ArrayList<String>();
        line = line.trim();
        if (line.length() > 0) {
            String category = line.substring(0, Math.min(31, line.length())).trim();
            if (category.length() > 0) {
                result.add(category);
            }
        }
        if (line.length() > 32) {
            String category = line.substring(32, Math.min(80, line.length())).trim();
            if (category.length() > 0) {
                result.add(category);
            }
        }
        return result;
    }

    private void readIngredients(LineNumberReader in, Recipe recipe) {
        String line = "";
        try {
            // the header line was read as part of the categories
            // skip line with '-----------------'
            line = in.readLine();

            // get the next line
            for (line = in.readLine(); line != null; in.mark(5000), line = in.readLine()) {
                // end of recipe ?
                if (recipeSeparatorPattern.matcher(line).find()) {
                    // put the line back
                    in.reset();
                    return;
                }

                // empty line ?
                if (line.trim().length() < 2) {
                    break;
                }

                if (!strictIngredientFormat) {
                    RecipeIngredient ingr = new RecipeIngredient(line.trim());
                    recipe.addIngredient(ingr);
                    continue;
                }

                // linewrapped from previous line ?
                if (line.length() < 24) {
                    // the first ingredient line ..... strange ???
                    if (recipe.getIngredients().isEmpty()) {
                        RecipeIngredient ingr = new RecipeIngredient(0f, null, new Ingredient(line.trim()));
                        recipe.addIngredient(ingr);
                    } else {
                        RecipeIngredient ingr = recipe.getIngredients().get(recipe.getIngredients().size() - 1);
                        if (ingr.getProcessing() != null) {
                            ingr.setProcessing(FormattingUtils.join(ingr.getProcessing(), ";", line.trim()));
                        } else {
                            ingr.getIngredient().setName(FormattingUtils.join(ingr.getIngredient().getName(), ";", line.trim()));
                        }
                    }
                    break;
                }

                // ingredient section (header) ?
                Matcher m = ingredientHeaderPattern.matcher(line);
                if (m.find()) {
                    RecipeIngredient ingredient = new RecipeIngredient();
                    ingredient.setIngredient(new Ingredient(line.trim()));
                    recipe.addIngredient(ingredient);
                    continue;
                }

                // First part of the line.
                RecipeIngredient ingredient = RecipeIngredient.createFromFixedPositionString(line, 0, 10, 14, 80);
                recipe.addIngredient(ingredient);
            }
        } catch (IOException e) {
            String msg = "MasterCookExport: error reading ingredients - " + line;
            log.info(msg);
            throw new RecipeFoxException(msg, e);
        } finally {
        }
    }

    private void readDirections(LineNumberReader in, Recipe recipe) {
        String line = "";
        StringBuilder directions = new StringBuilder();
        try {
            // get the next line
            for (line = in.readLine(); line != null; in.mark(5000), line = in.readLine()) {
                // end of recipe ?
                if (recipeSeparatorPattern.matcher(line).find() || nutrAssocPattern.matcher(line).find()) {
                    // put the line back
                    in.reset();
                    break;
                }

                // directive
                if (directions.length() > 0) {
                    directions.append(linebreak);
                }
                directions.append(line.trim());
            }
            recipe.setDirections(directions.toString());
        } catch (IOException e) {
            String msg = "MasterCookExport: error reading directions - " + line;
            log.info(msg);
            throw new RecipeFoxException(msg);
        }
    }

    // ---------------------------------------------------------------
    //
    // writing
    //
    // ---------------------------------------------------------------
    @Override
    public void writeRecipe(PrintWriter out, Recipe recipe) {
        out.print("                   *  Exported from  MasterCook  *");
        newLine(out);
        newLine(out);
        out.print("                    " + recipe.getTitle());
        newLine(out);
        newLine(out);
        out.printf("Recipe By     : %s", recipe.getAuthor());
        newLine(out);
        out.printf("Serving Size  : %5d Preparation Time : %d:%02d", recipe.getServings(),
                recipe.getPreparationTime() / 60, recipe.getPreparationTime() % 60);
        newLine(out);
        List<String> categoryNames = new ArrayList<String>();
        for (Category c : recipe.getCategories()) {
            String name = c.getName().replace(' ', '_');
            categoryNames.add(name);
        }
        out.print("Categories    : ");
        for (int i = 0; i < categoryNames.size(); i++) {
            // start of a new line?
            if (i != 0 && i % 2 == 0) {
                newLine(out);
                out.printf("                ");
            }
            out.printf("%-31s", categoryNames.get(i));
        }
        newLine(out); // end of categories
        newLine(out);// empty line
        out.print("  Amount  Measure       Ingredient -- Preparation Method");
        newLine(out);
        out.print("--------  ------------  --------------------------------");
        newLine(out);
        for (RecipeIngredient ingr : recipe.getIngredients()) {
            if (ingr.getUnit() == null && ingr.getAmount() < 0.001) {
                out.print("          ");
            } else {
                if (Configuration.getBooleanProperty("USE_FRACTIONS_IN_AMOUNTS")) {
                    out.printf(Locale.US, "%8s  ", FormattingUtils.formatNumber(ingr.getAmount()));
                } else {
                    // MasterCook need numberes to be formated with a '.' not a
                    // ','
                    out.printf(Locale.US, "%8.2f  ", ingr.getAmount());
                }
            }
            if (ingr.hasNoUnit()) {
                out.print("              ");
            } else {
                out.printf("%12s  ", ingr.getPluralisedUnitName());
            }
            if (ingr.getIngredient() != null) {
                out.print(ingr.getIngredient().getName());
            }
            if (ingr.getProcessing() != null && ingr.getProcessing().length() > 0) {
                out.print(" -- " + ingr.getProcessing());
            }
            newLine(out);
        }
        newLine(out);
        out.print(recipe.getDirectionsAsString().replace("\n", NEWLINE));
        newLine(out);
        if (recipe.getTipsAsString().length() > 0) {
            newLine(out);
            out.print("TIPS:");
            newLine(out);
            out.print(recipe.getTipsAsString().replace("\n", NEWLINE));
            newLine(out);
        }
        if (recipe.getNutritionalInfo() != null && recipe.getNutritionalInfo().length() > 0) {
            newLine(out);
            out.print("NUTRITIONAL INFORMATION:");
            newLine(out);
            out.print(recipe.getNutritionalInfo().replace("\n", NEWLINE));
            newLine(out);
        }
        newLine(out);
        newLine(out);
        if (recipe.getDescription() != null && recipe.getDescription().length() > 0) {
            out.print("Description:");
            newLine(out);
            out.print("  \"" + recipe.getDescription() + "\"");
            newLine(out);
        }
        if (recipe.getSource() != null && recipe.getSource().length() > 0) {
            out.print("Source:");
            newLine(out);
            out.print("  \"" + recipe.getSource() + "\"");
            newLine(out);
        }
        for (String attName : recipe.getTextAttributes().keySet()) {
            out.printf("S(\"%s\")", attName);
            newLine(out);
            out.printf("  \"%s\"", recipe.getTextAttributes().get(attName));
            newLine(out);
        }
        if (recipe.getCopyright() != null && recipe.getCopyright().length() > 0) {
            out.print("Copyright:");
            newLine(out);
            out.print("  \"" + recipe.getCopyright() + "\"");
            newLine(out);
        }
        if (recipe.getYield() != null && recipe.getYield().length() > 0) {
            out.print("Yield:");
            newLine(out);
            out.printf("  \"%s %s\"", (Object[]) recipe.splitYield());
            newLine(out);
        }
        if (recipe.getTotalTime() != 0) {
            out.print("Start to Finish Time:");
            newLine(out);
            out.printf("  \"%d:%02d\"", recipe.getTotalTime() / 60, recipe.getTotalTime() % 60);
            newLine(out);
        }
        // check if any other times.
        // Since MC only supports one time, give priority to Cooking time
        if (recipe.getCookTime() != 0) {
            out.printf("T(Cook):");
            newLine(out);
            out.printf("  \"%d:%02d\"", recipe.getCookTime() / 60, recipe.getCookTime() % 60);
            newLine(out);
        } else {
            for (String key : recipe.getTimes().keySet()) {
                if (!key.startsWith("TIME.")) {
                    out.printf("T(%s):", key);
                    newLine(out);
                    out.printf("  \"%d:%02d\"", recipe.getTime(key) / 60, recipe.getTime(key) % 60);
                    newLine(out);
                    break;
                }
            }
        }
        if (recipe.getRatings() != null && recipe.getRatings().size() > 0) {
            out.print("Ratings:");
            int no = 0;
            for (String name : recipe.getRatings().keySet()) {
                no++;
                out.printf(" %s %d", name, recipe.getRating(name, 10));
                if ((no % 2) == 0) {
                    newLine(out);
                }
            }
            newLine(out);
        }
        out.println("- - - - - - - - - - - - - - - - - - -");
        if (recipe.getWine() != null && recipe.getWine().length() > 0) {
            out.printf("Suggested Wine: %s", recipe.getWine());
            newLine(out);
            newLine(out);
        }

        if (recipe.getServingIdeas() != null && recipe.getServingIdeas().length() > 0) {
            out.printf("Serving Ideas : %s", recipe.getServingIdeas().replace("\n", NEWLINE));
            newLine(out);
            newLine(out);
        }

        if (recipe.getNote() != null && recipe.getNote().length() > 0) {
            out.printf("NOTES : %s", recipe.getNote().replace("\n", NEWLINE));
            newLine(out);
        }

        newLine(out);
    }

    /**
     * The import in MasterCook only work correct if the lines are separated by
     * \r (0x0a)
     * 
     * @param out
     *            the printwriter to write to
     */
    void newLine(PrintWriter out) {
        out.print(NEWLINE);
    }

    /**
     * @return a pattern to recognize this type of recipes.
     */
    @Override
    public Pattern getRecognizePattern() {
        return Pattern.compile("^\\s*\\*\\s*Exported\\s+(from|for)\\s+MasterCook", Pattern.CASE_INSENSITIVE
                + Pattern.MULTILINE);
    }

}
