/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import net.sf.recipetools.javarecipes.model.Chapter;
import net.sf.recipetools.javarecipes.model.Cookbook;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;
import net.sf.recipetools.javarecipes.model.Unit;


/**
 * @author ft
 *
 */
public class LivingCookbookXmlParser extends DefaultHandler  {
	
	/**
	 * 
	 */
	public LivingCookbookXmlParser() {
		super();
	}

	// image dir for FDXZ
	private File imageDir = null; 
	
	private boolean ignore = true;
	private StringBuilder elementValue = new StringBuilder();
	private Recipe recipe = null;
	private List<Recipe> allRecipes = new ArrayList<Recipe>(); 
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (! ignore) {
			elementValue.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if ("ProcedureText".equals(name)) {
			recipe.addDirections(elementValue.toString());
		} else if ("ItemText".equals(name)) { // Technique
			recipe.addDirections(elementValue.toString().trim());
		} else if ("RecipeTip".equals(name)) { // Tip
			recipe.addDirections(elementValue.toString().trim());
		} else if ("RecipeAuthorNote".equals(name)) { // Tip
			recipe.addDirections(elementValue.toString().trim());
		} else if (!ignore && "RecipeImage".equals(name)) {
			Image image = new Image();
			image.setImageFromBase64(elementValue.toString());
			recipe.addImage(image);
		} else if (!ignore && "ProcedureImage".equals(name)) {
			Image image = new Image();
			image.setImageFromBase64(elementValue.toString());
			recipe.setDirectionImage(recipe.getDirections().size()-1, image);
		} else {
		}
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		ignore = true;
		elementValue.delete(0, elementValue.length());

		if ("Cookbook".equals(name)) {
			Cookbook cookbook = new Cookbook();
			if (attributes.getValue("Name") != null) cookbook.setName(attributes.getValue("Name"));
			if (attributes.getValue("ID") != null) cookbook.setId(Long.parseLong(attributes.getValue("ID")));
			if (attributes.getValue("Comments") != null) cookbook.setDescription(attributes.getValue("Comments"));
		} else if ("CookbookChapter".equals(name)) {
			Chapter chapter = new Chapter();
			if (attributes.getValue("Name") != null) chapter.setName(attributes.getValue("Name"));
			if (attributes.getValue("ID") != null) chapter.setId(Long.parseLong(attributes.getValue("ID")));
			if (attributes.getValue("Comments") != null) chapter.setDescription(attributes.getValue("Comments"));
			String cookbookIdStr = attributes.getValue("CookbookID");
			String parentChapterIdStr = attributes.getValue("ParentChapterID");
			if (cookbookIdStr == null || parentChapterIdStr == null) {
				throw new RecipeFoxException("A chapter without Cookbookit or ParentChapter, cookbookid="+cookbookIdStr+", parentChapterId="+parentChapterIdStr);
			}
			long cookbookId = Long.parseLong(cookbookIdStr);
			long parentChapterId = Long.parseLong(parentChapterIdStr);
			if (parentChapterId != 0) {
				chapter.setParent(Folder.get(parentChapterId));
			} else {
				chapter.setParent(Folder.get(cookbookId));
			}
		} else if ("Recipe".equals(name)) {
			recipe = new Recipe();
			allRecipes.add(recipe);
			if (attributes.getValue("Name") != null) {
			    recipe.setTitle(attributes.getValue("Name"));
			}
			if (attributes.getValue("Author") != null) {
			    recipe.setAuthor(attributes.getValue("Author"));
			}
			if (attributes.getValue("Comments") != null) {
			    recipe.setNote(attributes.getValue("Comments"));
			}
			if (attributes.getValue("Copyright") != null) {
			    recipe.setCopyright(attributes.getValue("Copyright"));
			}
			if (attributes.getValue("PreparationTime") != null) {
			    recipe.setPreparationTime(attributes.getValue("PreparationTime"));
			}
			if (attributes.getValue("ReadyInTime") != null) {
			    recipe.setTotalTime(attributes.getValue("ReadyInTime"));
			}
			if (attributes.getValue("RecipeTypes") != null) {
			    recipe.setCategories(attributes.getValue("RecipeTypes"));
			}
			if (attributes.getValue("Servings") != null) {
			    recipe.setServings(attributes.getValue("Servings"));
			}
			if (attributes.getValue("Source") != null) {
			    recipe.setSource(attributes.getValue("Source"));
			}
			if (attributes.getValue("WebPage") != null) {
			    recipe.setUrl(attributes.getValue("WebPage"));
			}
			if (attributes.getValue("Yield") != null) {
			    recipe.setYield(attributes.getValue("Yield"));
			}
			if (attributes.getValue("CookingTime") != null) {
			    recipe.setCookTime(attributes.getValue("CookingTime"));
			}
			if (attributes.getValue("InactiveTime") != null) {
			    recipe.setTime("INACTIVE", attributes.getValue("InactiveTime"));
			}

			// TODO: OvenTemperatureF
			// TODO: OvenTemperatureC
			// TODO: DegreeOfDifficulty

			String cookbookIdStr = attributes.getValue("CookbookID");
			String chapterIdStr = attributes.getValue("CookbookChapterID");
			if (cookbookIdStr != null && chapterIdStr != null) {
				long cookbookId = Long.parseLong(cookbookIdStr);
				long chapterId = Long.parseLong(chapterIdStr);
				long folderId = (chapterId != 0) ? chapterId : cookbookId; 
				recipe.setFolder(Folder.get(folderId));
			}
		
		} else if ("RecipeIngredient".equals(name)) {
			RecipeIngredient ingr = new RecipeIngredient();
			recipe.addIngredient(ingr);
			if (attributes.getValue("Quantity") != null) {
			    ingr.setAmount(RecipeIngredient.getNumber(attributes.getValue("Quantity")));
			}
			if (attributes.getValue("Unit") != null) {
			    ingr.setUnit(new Unit(attributes.getValue("Unit")));
			}
			if (attributes.getValue("Ingredient") != null) {
			    ingr.setIngredient(new Ingredient(attributes.getValue("Ingredient")));
			}
			if (attributes.getValue("Heading")!= null && attributes.getValue("Heading").equals("Y")) {
				ingr.setType(RecipeIngredient.TYPE_SUBTITLE);
			}
		} else if ("RecipeProcedure".equals(name)) {
			// TODO: HEADING="N"
		} else if ("ProcedureText".equals(name)) {
			ignore = false;
		} else if ("ProcedureImage".equals(name)) {
            if (attributes.getValue("FileName") != null) {
                Image image = getImage(attributes.getValue("FileName"));
                recipe.setDirectionImage(recipe.getDirections().size()-1, image);
            } else {
                ignore = false;
            }
		} else if ("RecipeImage".equals(name)) {
            if (attributes.getValue("FileName") != null) {
                Image image = getImage(attributes.getValue("FileName"));
                recipe.addImage(image);
            } else {
                ignore = false;
            }
		} else if ("RecipeTip".equals(name)) {
			ignore = false;
		} else if ("RecipeAuthorNote".equals(name)) {
			ignore = false;
		} else if ("Technique".equals(name)) {
			recipe = new Recipe();
			allRecipes.add(recipe);
			if (attributes.getValue("Name") != null) {
			    recipe.setTitle(attributes.getValue("Name"));
			}
			if (attributes.getValue("Comments") != null) {
			    recipe.setNote(attributes.getValue("Comments"));
			}
			if (attributes.getValue("Copyright") != null) {
			    recipe.setCopyright(attributes.getValue("Copyright"));
			}
			if (attributes.getValue("Source") != null) {
			    recipe.setSource(attributes.getValue("Source"));
			}
			if (attributes.getValue("WebPage") != null) {
			    recipe.setUrl(attributes.getValue("WebPage"));
			}
		} else if ("ItemText".equals(name)) {
			ignore = false;
		}
		
		// TODO: Cookbooks, CookbookChapters
		// TODO: RecipeReviews, RecipeReview
		// TODO: SourceImage, RecipeScan
		// TODO: TechniqueFolders
	}
	
	public Image getImage(String name) {
	    return new Image(name, new File(imageDir, name).getAbsolutePath());
	}

	/**
	 * @return the allRecipes
	 */
	public List<Recipe> getAllRecipes() {
		return allRecipes;
	}

	/**
	 * @param allRecipes the allRecipes to set
	 */
	public void setAllRecipes(List<Recipe> allRecipes) {
		this.allRecipes = allRecipes;
	}

    /**
     * @return the imageDir
     */
    public File getImageDir() {
        return imageDir;
    }

    /**
     * @param imageDir the imageDir to set
     */
    public void setImageDir(File imageDir) {
        this.imageDir = imageDir;
    }
}
