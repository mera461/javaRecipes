/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
public class MacGourmetXmlParser extends DefaultHandler  {
	static final int MAX_DEPTH = 20;
	/**
	 * 
	 */
	public MacGourmetXmlParser() {
		super();
	}

	private StringBuilder elementValue = new StringBuilder();
	private List<Recipe> allRecipes = new ArrayList<Recipe>();
	
	int level = 0;
	String[] lastKey = new String[MAX_DEPTH];
	Object[] element = new Object[MAX_DEPTH];
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
			elementValue.append(ch, start, length);
	}
	
	public void addElement(Object o) {
		Object e = element[level];
		if (e instanceof ArrayList) {
			((ArrayList<Object>)element[level]).add(o);
		} else if (e instanceof HashMap) {
			((HashMap<String, Object>)element[level]).put(lastKey[level], o);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if ("plist".equals(name)) {
		} else if (name.equals("array")) {
			level--;
			if (level>0) {
				((HashMap<String, Object>)element[level]).put(lastKey[level], element[level+1]);
				lastKey[level]=null;
			}
		} else if ("dict".equals(name)) {
			level--;
			//((ArrayList<Object>)element[level]).add(element[level+1]);
			addElement(element[level+1]);
			if (level==1) {
				extractRecipe();
			}
		} else if ("key".equals(name)) {
			lastKey[level] = elementValue.toString();
		} else if ("data".equals(name)) {
			if (lastKey[level]==null) throw new RecipeFoxException("No Key defined before data.");
			addElement(elementValue.toString());
			//((HashMap<String, Object>)element[level]).put(lastKey[level], elementValue.toString());
			lastKey[level] = null;
		} else if ("integer".equals(name)) {
			if (lastKey[level]==null) throw new RecipeFoxException("No Key defined before integer.");
			addElement( Integer.parseInt(elementValue.toString()));
			//((HashMap<String, Object>)element[level]).put(lastKey[level], Integer.parseInt(elementValue.toString()));
			lastKey[level] = null;
		} else if ("string".equals(name)) {
			if (lastKey[level]==null) throw new RecipeFoxException("No Key defined before String.");
			addElement(elementValue.toString());
			//((HashMap<String, Object>)element[level]).put(lastKey[level], elementValue.toString());
			lastKey[level] = null;
		} else if ("false".equals(name)
				  || "true".equals(name)) {
			if (lastKey[level]==null) throw new RecipeFoxException("No Key defined before Boolean.");
			addElement(Boolean.parseBoolean(name));
			//((HashMap<String, Object>)element[level]).put(lastKey[level], Boolean.parseBoolean(name));
			lastKey[level] = null;
		} 
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if ("plist".equals(name)) {
			// reset everything
			level=0;
			lastKey = new String[MAX_DEPTH];
			element = new Object[MAX_DEPTH];
			allRecipes = new ArrayList<Recipe>();
		} else if ("array".equals(name)) {
			level++;
			element[level] = new ArrayList<Object>();
		} else if ("dict".equals(name)) {
			level++;
			element[level] = new HashMap<String, Object>();
		} else if ("key".equals(name)) {
		} else if ("data".equals(name)) {
		} else if ("integer".equals(name)) {
		} else if ("string".equals(name)) {
		} else if ("false".equals(name)) {
		} else if ("true".equals(name)) {
		}
		
		// empty the element value
		elementValue.delete(0, elementValue.length());
	}
	
	@SuppressWarnings("unused")
	static private String prepTimes[] = {
		"", //
		"Active",
		"Bake",
		"??",
		"Chill",
		"Cook", // 5
		"Cool",
		"Grill",
		"Marinate",
		"Prep",
		"Rise", // 10
		"Stand",
		"Start To Finish",
		"Poach",
		"Fry",
		"Roast", // 15
		"Saute",
		"Steam",
		"Boil",
		"Ready In",
		"Simmer", // 20
		"Refrigerate",
		"Brown",
		"Macerate",
		"Pressure Cook",
		"Freeze",
		"Soak", // 26
		"Stir",
		"Inactive",
		"Beat",
		"Total Time", // 30
		"Microwave",
		"Sit",
		"Rest",
		"Mix", 
		"Whip", // 35 
		"Beat"
		};
	
	static private String[] units = {
		"day", // 0
		"hour", // 1
		"minute", // 2
		"second", // 3
		"week", // 4
		"\u176C", // 5
		"\u176F" // 6
	};

	public boolean hasKey(Map<String, Object> map, String key) {
		return map.containsKey(key)
			&& map.get(key) != null
			&& ((map.get(key) instanceof String) ? ! ((String) map.get(key)).isEmpty() : true);
	}
	
	@SuppressWarnings("unchecked")
	void extractRecipe() {
		Recipe r = new Recipe();
		Map<String, Object> map = (Map<String, Object>)element[2];
		// CATEGORIES
		if (map.containsKey("CATEGORIES")) {
			for (Map<String, Object> catMap : (ArrayList<Map<String, Object>>)map.get("CATEGORIES")) {
				String category = (String) catMap.get("NAME");
				r.addCategory(category);
			}
		}
		if (hasKey(map, "COURSE_NAME") && ! ((String) map.get("COURSE_NAME")).equals("--")) {
			r.addCategory((String) map.get("COURSE_NAME"));
		}
		// TODO: COURSE_ID
		// TODO: COURSE_NAME
		// TODO: CUISINE_ID
		// 2->Asian
		// TODO: DIFFICULTY
		// DIRECTIONS and DIRECTION_LIST
		if (map.containsKey("DIRECTIONS_LIST")) {
			ArrayList<Object> l = (ArrayList<Object>) map.get("DIRECTIONS_LIST");
			l.stream().forEach(o -> r.addDirections(((HashMap<String, Object>) o).get("DIRECTION_TEXT").toString()));
		} else if (hasKey(map, "DIRECTIONS")) {
			r.addDirections((String) map.get("DIRECTIONS"));
		}
		// IMAGE
		if (map.containsKey("IMAGE")) {
			Image image = new Image();
			if (((String)map.get("EXPORT_TYPE")).contains("BINARY")) {
				image.setImageFromBase64((String) map.get("IMAGE"));
			} else {
				image.setImageFromHex((String) map.get("IMAGE"));
			}
			r.addImage(image);
		}
		// INGREDIENTS, INGRDIENTS_TREE
		setIngredients(r, map);
		// TODO: KEYWORDS
		// TODO: MEASUREMENT_SYSTEM
		// NAME
		if (map.containsKey("NAME")) {
			r.setTitle((String) map.get("NAME"));
		}
		// NOTE
		if (hasKey(map, "NOTE")) {
			r.setNote((String) map.get("NOTE"));
		}
		// TODO: NOTES_LIST
		// NUTRITION
		if (hasKey(map, "NUTRITION")) {
			r.addDirections("Nutrition: "+(String) map.get("NUTRITION"));
		}

		// PREP_TIMES
		if (hasKey(map, "PREP_TIMES")) {
			setPrepTimes(r, ((ArrayList<Object>)map.get("PREP_TIMES")));
		}
		
		// PUBLICATION_PAGE
		if (hasKey(map, "PUBLICATION_PAGE")) {
			// TODO: URL? 
			// contains date, urls, cusine (Turkey, India)
			//r.setUrl((String) map.get("PUBLICATION_PAGE"));
		}
		// SERVINGS
		if (hasKey(map, "SERVINGS")) {
			r.setServings((Integer) map.get("SERVINGS"));
		}
		// SOURCE
		if (hasKey(map, "SOURCE")) {
			r.setSource((String) map.get("SOURCE"));
		}
		// SUMMARY
		if (hasKey(map, "SUMMARY")) {
			r.setDescription((String) map.get("SUMMARY"));
		}		
		// TODO: TYPE is always 102
		// URL
		if (hasKey(map, "URL")) {
			r.setUrl((String) map.get("URL"));
		}
		if (hasKey(map, "YIELD")) {
			// A string like: Makes enough for a 12-14 lb (5.5-6.5 kg) turkey
			r.addDirections("Yield: "+(String) map.get("YIELD"));
		}
		
		allRecipes.add(r);
		
		// delete the recipe data
		element[2] = null;
	}
	
	void setIngredients(Recipe r, Map<String, Object> map) {
		// INGREDIENTS_TREE
		if (map.containsKey("INGREDIENTS_TREE")) {
			Object o = map.get("INGREDIENTS_TREE");
			if (o instanceof ArrayList) {
				((ArrayList<Map<String, Object>>) map.get("INGREDIENTS_TREE")).forEach(ingrMap -> setIngredients(r, ingrMap));
			} else {
				
			}
		} else if (map.containsKey("INGREDIENTS")) {
			((ArrayList<Map<String, Object>>) map.get("INGREDIENTS")).forEach(ingrMap -> setIngredients(r, ingrMap));
		} else if (map.containsKey("QUANTITY")) {
			RecipeIngredient ingr = new RecipeIngredient();
			ingr.setAmount(RecipeIngredient.getNumber((String) map.get("QUANTITY")));
			ingr.setUnit(new Unit((String) map.get("MEASUREMENT")));
			ingr.setIngredient(new Ingredient((String) map.get("DESCRIPTION")));
			if (hasKey(map, "DIRECTION")) {
				ingr.setProcessing((String) map.get("DIRECTION"));
			}
			if (map.containsKey("IS_DIVIDER") && (Boolean) map.get("IS_DIVIDER")) {
				ingr.setType(RecipeIngredient.TYPE_SUBTITLE);
			}
			// TODO: IS_MAIN

			r.addIngredient(ingr);
		}
		
		
	}
	
	void setPrepTimes(Recipe r, ArrayList<Object> l) {
		if (l.isEmpty()) {
			return;
		}
		
		StringBuffer sb = new StringBuffer("Prep times: ");
		l.forEach( obj -> {
			Map<String, Integer> o1 = (Map<String, Integer>) obj;
			int typeId = o1.get("TIME_TYPE_ID");
			sb.append("\n    "+prepTimes[typeId]+": ");
			sb.append(toPeriod(o1));
			switch (typeId) {
				case 5: // cook
					r.setCookTime(toPeriod(o1));
					break;
				case 9: // prep
					r.setPreparationTime(toPeriod(o1));
					break;
				case 30: // total
					r.setTotalTime(toPeriod(o1));
					break;
			}
		});
		r.addDirections(sb.toString());
	}
	
	String toPeriod(Map<String, Integer> o) {
		StringBuffer sb = new StringBuffer();
		if (o.containsKey("AMOUNT") && o.containsKey("TIME_UNIT_ID")) {
			sb.append(toUnit(o.get("AMOUNT"), o.get("TIME_UNIT_ID")));
		}
		if (o.containsKey("AMOUNT_2") && o.containsKey("TIME_UNIT_2_ID")) {
			sb.append(" ");
			sb.append(toUnit(o.get("AMOUNT_2"), o.get("TIME_UNIT_2_ID")));
		}
		return sb.toString();
	}

	String toUnit(int amount, int unitId) {
		if (amount == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(amount);
		sb.append(" ");
		sb.append(units[unitId]);
		if (amount > 1) {
			sb.append("s");
		}
		return sb.toString();
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
}
