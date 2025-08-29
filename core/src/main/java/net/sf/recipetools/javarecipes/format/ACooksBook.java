/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.format.MasterCookBinary.BinaryFileReader;
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
public class ACooksBook extends BinaryFileReader implements RecipeFormatter, BinaryInputProcessor {
	
	File directory;
	
	public ACooksBook() {
		super();
		setCharSet("UTF-8");
	}
	
	public ACooksBook(File file) {
		setCharSet("UTF-8");
		initializeWithFile(file);
	}
	
	public void initializeWithFile(File f) {
		if (! f.exists()) {
			throw new RecipeFoxException("The file does not exists: file:\nfile="+f.getAbsolutePath());
		}
		
		if (! f.getName().toLowerCase().endsWith("acbk")) {
			throw new RecipeFoxException("The reader should point to a ACooksBook file (like food.acbk) file="+f.getAbsolutePath());
		}
		openFile(f);
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
	 * Return all recipes in the file
	 */
	List<Recipe> readAllRecipes() {
		List<Recipe> recipes = new ArrayList<Recipe>();
		int noRecipes = readByteAt(0x41);
		seek(0x4f);
		for (int i=0; i<noRecipes; i++) {
			Recipe r = readRecipe();
			recipes.add(r);
		}
		
		return recipes;
	}
	
	Recipe readRecipe() {
		Recipe r = new Recipe();
		r.setTitle(readStringElement());
		r.setCategories(readStringElement()); // region/categories
		r.addCategory(readStringElement()); // course
		readStringElement(); // TODO: ignore Cooking method ??????
		readStringElement(); // ignore dummy string: e2 98 85 e2 98 85
		String source = readStringElement(); // publication
		String date = readStringElement(); // date of publication
		if (date != null && date.length()>0) {
			source = source + " ("+date+")";
		}
		String page = readStringElement(); // page
		if (page != null && page.length()>0) {
			source = source + ", page "+page;
		}
		r.setTextAttribute("Publication", source); 
		r.setAuthor(readStringElement());
		r.setPreparationTime(readStringElement());
		r.setCookTime(readStringElement());
		r.setServings(readStringElement());
		readStringElement(); // TODO: ignore Serving size ??????
		r.setTextAttribute("Difficulty", readStringElement()); // as a rating???
		r.setDirections(readStringElement());
		skipUntil(0x86,0x86,0x86); // formatting....
		skipBytes(3);
		r.setNote(readStringElement());

		skipUntil(0x86);
		skipBytes(4);
		readIngredients(r);
		
		// get photo
		byte[] bytes = readBinaryElement();
		if (bytes != null) {
			Image image = new Image();
			image.setImage(bytes);
			r.addImage(image);
		}
		
		r.setServingIdeas(readStringElement());
		r.setWine(readStringElement());
		
		// skip nutrition
		skipUntil(0x86, 0x86);
		skipUntil(0x86, 0x86);
		skipUntil(0x86);
		
		r.setYield(readStringElement());
		r.setTextAttribute("Equipment", readStringElement());
		
		// skip GUID
		skipUntil(0x86);
		skipUntil(0x86);
		
		// skip URL
		skipUntil(0x86, 0x86);
		
		// skip value
		skipUntil(0x86);
		
		// skip date
		skipUntil(0x86);
		skipUntil(0x86);
		
		// Source
		r.setSource(readStringElement());
		
		// Description
		skipBytes(3);  // 92 84 a8
		r.setDescription(readStringElement());
		// skip formatting
		skipUntil(0x86, 0x86);
		
		// Total Time
		r.setTotalTime(readStringElement());
		
		return r;
	}
	
	void readIngredients(Recipe r) {
		int noIngredients = readByte();
		System.out.println(String.format("No ingredients=%d at %05x", noIngredients, (getFilePointer()-1)));

		for (int i=0; i<noIngredients; i++) {
			RecipeIngredient ingr = new RecipeIngredient();
			r.addIngredient(ingr);
			
			ingr.setIngredient(new Ingredient(readStringElement()));
			ingr.setAmount(RecipeIngredient.getNumber(readStringElement()));
			ingr.setUnit(new Unit(readStringElement()));
			ingr.setProcessing(readStringElement());
			readStringElement(); // empty string
			skipUntil(0x86);
			if (i!=(noIngredients-1)) {
				skipBytes(3); // 92 84 c4
			}
		}
	}
	
	int readIntElement() {
		return (int) readElement();
	}
	
	String readStringElement() {
		return (String) readElement();
	}
	
	byte[] readBinaryElement() {
		Object o = readElement();
		if (o instanceof String) {
			return null;
		} else {
			return (byte[]) o;
		}
	}
	
	Object readElement() {
		String result = "";
		boolean noTail = false;
		boolean tailPossible = false;
		long pos = getFilePointer();
		int head = readByte();
		// skip all end-of-attribute 
		while (head == 0x86) {
			head = readByte();
		}
		if (head != 0x92) {
			throw new RecipeFoxException(String.format("Invalid head, expected 0x92, but was %02x at %05x", head, (getFilePointer()-1)));
		}
		int t1 = readByte();
		if (t1 == 0x81) { // 2 byte int
			int l = readShort();
			return l;
		} else if (t1 == 0x82) { // 4 byte int
			int l = readInt();
			return l;
		} else if (t1 == 0x84) {
			int t2 = readByte();
			if (t2 == 0x81) { 
				int t3 = readByte();
				if (t3 == 0x95) {// image?
					skipBytes(2);
					int t4 = readByte();
					int length = 0;
					if (t4 == 0x81) {
						length = readShort();
					} else if (t4 == 0x82) {
						length = readInt();
					}
					// skip string
					skipBytes(1);
					int intStringLength = readByte();
					skipBytes(intStringLength);
					byte[] image = readByteBuffer(length);
					return image;
				}
				
			} else if (t2 == 0x84) {
				int t3 = readByte();
				if (t3 == 0x84) { // 92 84 84 84 ----> Type string followed by def
					String type = readByteString();
					if (type.equals("NSMutableData")) {
						skipBytes(13); // skip nsData untill length element
						int t4 = readByte();
						int length = 0;
						if (t4 == 0x81) {
							length = readShort();
						} else if (t4 == 0x82) {
							length = readInt();
						}
						// skip string
						skipBytes(1);
						int intStringLength = readByte();
						skipBytes(intStringLength);
						byte[] image = readByteBuffer(length);
						return image;
					} else {
						@SuppressWarnings("unused")
						int t4 = readByte();
						int t5 = readByte();
						assert (t5 == 0x95);
						int t6 = readByte();
						if (t6==0x92) {
							seek(getFilePointer()-1);
							result = readStringElement();
							noTail = true;
						} else {
							skipBytes(2); // 84 01 2b @ just before title
							result = readByteString();
						}
					}
				} else if (t3 == 0xc4) {
					// ???
					noTail = true;
				}
			} else if (t2 == 0x93) {
				int t3 = readByte();
				if (t3==0x96) {
					// array length, next byte is the length
					noTail = true;
				}
			} else if (t2 == 0x99) {
				int t3 = readByte();
				if (t3 == 0x99) { // 92 84 99 99 --> String
					int typeOrLength = readByte();
					if (typeOrLength == 0x81) { // long string 
						result = readShortString();
					} else { // short string
						seek(getFilePointer()-1);
						result = readByteString();
					}
				}
			} else if (t2 == 0xc4) {
				noTail = true;
			}
		} else if (t1==0x85) {
			// empty ???
			// no tail if empty photo
			tailPossible = true;
		} else if (t1==0x9b || t1==0xc9 || t1==0xb6 || t1==0xbd || t1==0xcf || t1==0xd0) {
			// not defined.
			noTail = true;
		} else if (t1==0xa9 || t1==0xaa) {
			//ignore
		} else if (t1==0xc6 || t1==0xb1) {
			result = "1"; // in ingredients -- or same as prev ???
			noTail = true;
		}
		if (! noTail) {
			int tail = readByte();
			if (tailPossible) {
				if (tail != 0x86) {
					seek(getFilePointer()-1);
				}
			} else if (tail != 0x86) {
				throw new RecipeFoxException(String.format("Invalid tail, expected 0x86, but was %02x at %05x", tail, (getFilePointer()-1)));
			}
		}
		System.out.println(String.format("%05x : %s", pos, result));
		
		return result;
	}
	
	@Override
	public void write(List<Recipe> recipe) {
		throw new RecipeFoxException("CookenPro formatter does not support writing.");
	}

	@Override
	public void startFile(String name) {
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
