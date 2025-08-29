/*
 * Created on 25-10-2004
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

/**
 * @author ft
 *
 */
public class JsonFormatter extends RecipeTextFormatter {

    /**
	 * 
	 */
    public JsonFormatter() {
        super();
    }

    // ---------------------------------------------------------------
    //
    // reading
    //
    // ---------------------------------------------------------------
    @Override
    public List<Recipe> readRecipes(LineNumberReader in) {
        List<Recipe> recipes = new ArrayList<Recipe>();
        try {
        	StringBuilder str = new StringBuilder();
        	String line = in.readLine();
        	while (line!=null) {
        		str.append(line);
        		line = in.readLine();
        	}
      
        	if (str.length()>0) {
        		recipes = recipeFromJson(str.toString());
        	}
        	
        } catch (RuntimeException | IOException e) {
            throw new RecipeFoxException("*** ERROR reading file.", e);
        }

        return recipes;
    }


    // ---------------------------------------------------------------
    //
    // writing
    //
    // ---------------------------------------------------------------
    @Override
	public void writeRecipe(PrintWriter out, List<Recipe> recipes) {
    	out.println(recipeToJson(recipes));
	}
    
    @Override
    public void writeRecipe(PrintWriter out, Recipe recipe) {
    	out.print(recipeToJson(recipe));
    	out.println(",");
    }

    @Override
	public void writeFileHeader(PrintWriter out) {
    	out.print("[");
	}

    @Override
	public void writeFileTail(PrintWriter out) {
    	out.println("]");
	}
    
    
    @Override
	public String getDefaultCharacterSet() {
		return "utf-8";
	}
    
    /**
     * @return a pattern to recognize this type of recipes.
     */
    @Override
    public Pattern getRecognizePattern() {
        return Pattern.compile("^\\[\\{", Pattern.MULTILINE);
    }
    
	public void appendIfSet(StringBuilder str, ObjectMapper mapper, String name, String value) {
		if (value!=null && value.length()>0) {
			str.append(name);
			str.append(":");
			str.append(mapper.writeValueAsString(value));
			str.append(",");
		}
	}
    
    public String recipeToJson(Recipe recipe) {
		ObjectMapper mapper = JsonFactory.create();
		StringBuilder json = new StringBuilder("{");
		appendIfSet(json, mapper, "author", recipe.getAuthor());
		if (recipe.getCookTime()>0) {
			json.append("cooktime:"+mapper.writeValueAsString(recipe.getCookTime()+" min")+",");
		}
		appendIfSet(json, mapper, "copyright", recipe.getCopyright());
		appendIfSet(json, mapper, "cuisine", recipe.getCuisine());
		appendIfSet(json, mapper, "description", recipe.getDescription());
		appendIfSet(json, mapper, "directions", recipe.getDirectionsAsString());
		if (recipe.getImages().size()>0) {
			json.append("image:{url:"+mapper.writeValueAsString(recipe.getImages().get(0).getUrl())+",");
			json.append("image:"+mapper.writeValueAsString(recipe.getImages().get(0).encodeAsBase64())+"},");
		}
		appendIfSet(json, mapper, "ingredients", recipeIngredientToJson(recipe.getIngredients()));
		appendIfSet(json, mapper, "note", recipe.getNote());
		if (recipe.getPreparationTime()>0) {
			json.append("preptime:"+mapper.writeValueAsString(recipe.getPreparationTime()+" min")+",");
		}
		if (recipe.getServings()>0) {
			json.append("servings:"+mapper.writeValueAsString(recipe.getServings())+",");
		}
		appendIfSet(json, mapper, "servingideas", recipe.getServingIdeas());
		appendIfSet(json, mapper, "source", recipe.getSource());
		appendIfSet(json, mapper, "title", recipe.getTitle());
		appendIfSet(json, mapper, "url", recipe.getUrl());
		appendIfSet(json, mapper, "yield", recipe.getYield());
		appendIfSet(json, mapper, "wine", recipe.getWine());
		
		json.append("}");
		return json.toString();
    }
    
    public String recipeToJson(List<Recipe> recipes) {
    	StringBuilder json = new StringBuilder();
    	json.append("[");
    	for (Recipe r : recipes) {
    		json.append(recipeToJson(r));
    		json.append(",");
    	}
    	json.deleteCharAt(json.length()-1);
    	json.append("]");
    	return json.toString();
    }
    
    public List<Recipe> recipeFromJson(String json) {
    	ArrayList<Recipe> result = new ArrayList<Recipe>();
		ObjectMapper mapper =  JsonFactory.create();
		List<Map<String, Object>> recipeList = (List<Map<String, Object>>) mapper.fromJson(json, List.class);
		for (Map<String, Object> r: recipeList) {
			Recipe recipe = new Recipe();
			result.add(recipe);
			for (String key : r.keySet()) {
				if ("image".equals(key)) {
					Map<String, String> imageMap = (Map<String, String>) r.get(key);
					Image img = new Image();
					img.setUrl(imageMap.get("url"));
					img.setImageFromBase64(imageMap.get("image"));
					recipe.addImage(img);
				} else {
					Object value = r.get(key);
					if (value instanceof String) {
						Recipe.setJsonField(recipe, key, (String) r.get(key));
					} else if (value instanceof Integer) {
						Recipe.setJsonField(recipe, key, ((Integer) r.get(key)).toString());
					}
				}
			}
		}

    	return result;
    }

	public String recipeIngredientToJson(RecipeIngredient ri) {
		ObjectMapper mapper = JsonFactory.create();
		return mapper.writeValueAsString(ri.toString());
	}
	
	public String recipeIngredientToJson(List<RecipeIngredient> ingredients) {
		if (ingredients.size()==0) return "";
		StringBuilder str = new StringBuilder();
		for (RecipeIngredient ri : ingredients) {
			str.append(ri.toString());
			str.append("\n");
		}
		if (str.length()>0) {
			str.deleteCharAt(str.length()-1);
		}
		ObjectMapper mapper = JsonFactory.create();
		return mapper.writeValueAsString(str.toString());
	}

    
    
    
}
