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
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author Frank
 *
 */
public class McIngrReader extends MastercookReader implements BinaryInputProcessor {
	
	public Map<Integer, String> titleMap = new HashMap<Integer, String>();
	
	// to monitor that the part-tag always is ascending.
	String lastPartTag = "";
	int lastPartNo = 0;


	public McIngrReader() {
	}
	
	public McIngrReader(File file) {
		setFile(file);
		initializeForRead();
	}
	
	void initializeForRead() {
		lastPartTag = "";
		lastPartNo = 0;
		titleMap.clear();
		
		readDatabaseSections();
	}

	void readDatabaseSections() {
		readStringTable();
	}
	
	/**
	 * Return all recipes in the file
	 * @see net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor#read(java.io.File)
	 */
    @Override
	public List<Recipe> read(File f) {
		// ignore anything else than mc2 files.
		if (! f.getName().toLowerCase().endsWith("ing")) {
			return new ArrayList<Recipe>();
		}
		
		setFile(f);
		initializeForRead();
		return new ArrayList<Recipe>(); // readAllRecipes(); ????
	}
	
	
	/**
	 * Return all recipes in the file
	 */
	List<HashMap<String, Object>> readAllRecipes() {
		List<HashMap<String, Object>> recipes = new ArrayList<HashMap<String, Object>>();

		exportHeader();
		
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

		int index = 0;
		int noInThisPart = 0;
		while (index < noItems) {
			long dataBlockAddr = readInt();
			@SuppressWarnings("unused")
			int no = readInt();
			int id = readInt();
			Date changeDate = readDate();
			int x1 = readInt();
			int x2 = readInt();
			int titleStringId = readInt();
			int originalId = readInt();
			int x5 = readInt();
			int x6 = readInt();

			HashMap<String, Object> r = null;
			if (dataBlockAddr==0) {
				r = new HashMap<String, Object>();
				
			} else {
				r = readRecipeAt(dataBlockAddr * pagesize);
					//recipes.add(r);
					
					/*
					if (titleStringAddr != 0) {
						r.setTitle(keyStrings.get(titleStringAddr));
					}
					*/
			}
			if (titleStringId != 0) {
				String titleString = keyStrings.get(titleStringId);
				r.put("title", titleString); // to fix special chars like TM, CopyRight, etc
			}

			r.put("id", id);
			r.put("changeDate", changeDate);
			r.put("x1", x1);
			r.put("x2", x2);
			r.put("synonymForId", originalId);
			r.put("x5", x5);
			r.put("x6", x6);
			
			//System.out.println(r.get("title")+": "+r);
			
			export(r);
			
			index++;
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
	
	HashMap<String, Object> readRecipeAt(long fileAddr) {
		long currentAddr = getFilePointer();
		seek(fileAddr);
		HashMap<String, Object> r = readRecipe();
		seek(currentAddr);
		return r;
	}
	
	HashMap<String, Object> readRecipe() {
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
		skipBytes(16);
		// here comes the title
		String title = readZeroTerminatedString();
		titleMap.put(recipeNo, title);
		seek(startPos + lengthOfBlock0);
		
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
		//save(decoded);
		
		// set fields.
		HashMap<String, Object> values = new HashMap<String, Object>();
		values.put("title", title.trim());
		lastPartNo = 0;
		lastPartTag = "";
		extractFromBlock1(values, decoded);
		checkMark('z');
		
		return values;
	}

	private void extractFromBlock1(HashMap<String, Object> values, byte[] block1) {
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
			setAttrValue(values, block1, tag, type, startOfValues+tagDataPos-4);
			pos += 20;
		}
		
	}
	
	private void setAttrValue(HashMap<String, Object> values, byte[] block1, String tag, String type, int dataPosition) {
		if ("ASLb".equals(tag)) {
			return;
		}
		
		if ("*tan".equals(type)) {
			values.put(tag, readStringAt(block1, dataPosition).trim());
		} else if ("#2_i".equals(type)) {
			values.put(tag, readShortAt(block1, dataPosition+4));
		} else if ("#4_i".equals(type)) {
			values.put(tag,  readIntAt(block1, dataPosition+4));
		} else if ("#pID".equals(type)) { // photo id
			values.put(tag,  readIntAt(block1, dataPosition+4));
		} else if ("#4_r".equals(type)) {
			values.put(tag,  readFloatAt(block1, dataPosition+4));
		} else if ("#4[r".equals(type)) { // array of floats
			int length = readIntAt(block1, dataPosition);
			List<Float> array = new ArrayList<Float>();
			for (int i=0; i<length/4; i++) {
				array.add(readFloatAt(block1, dataPosition+4+4*i));
			}
			values.put(tag, array);
		} else if ("###4".equals(type)) {
			// embedded tag.
			@SuppressWarnings("unused")
			int length = readIntAt(block1, dataPosition);
			byte[] embeddedBlock = Arrays.copyOfRange(block1, dataPosition+4, block1.length);
			if ("IngR".equals(tag)) {
				/*
				RecipeIngredient ingr = new RecipeIngredient();
				ingr.setType(RecipeIngredient.TYPE_INGREDIENT);
				recipe.addIngredient(ingr);
				*/
			}
			extractFromBlock1(values, embeddedBlock);
			return;
		} else if ("*tag".equals(type)) {
			// embedded tag.
			String tagName = readStringAt(block1, dataPosition);
			values.put(tag, readStringAt(block1, dataPosition+4+tagName.length()));
		} else {
			throw new RecipeFoxException("Unknown tag type. Tag="+tag+", type="+type);
		}
	}
	
	void exportHeader() {
		System.out.println("id,name,synonym for id,Description,Plural,Purchase as,Store location,Serving Size Amount,Serving Size Unit,is fluid,Weight Amount,Weight Unit,Volume Amount,Volume Unit,%Refuse,0:????,1:Calories,2:Total fat,3:Saturated fat,4:Monounsaturated fat,5:Polyunsaturated fat,6:Cholesterol,7:Carbohydrates,8:Dietary fiber,9:Protein,10:Sodium,11:Potassium,12:Calcium,13:Iron,14:Zinc,15:Vitamin C,16:Vitamin A (i.u),17:Vitamin B6,18:Vitamin B12,19:Thiamine B1,20:Riboflavin,21:Folacin,22:Niacin,23:Caffeine,24:????,25:Alcohol,26:Exchange/Grain,27:Exchange/Lean Meat,28:Exchange/Vegetable,29:Exchange/Fruit,30:Exchange/Non-fat Milk,31:Exchange/Fat,32:Exchange/Other Carbohydrates,33:Vitamin A (r.e)");
	}
	
	void export(HashMap<String, Object> values) {
		exportAttr(values, "id");
		exportAttr(values, "title");
		exportAttr(values, "synonymForId");
		exportAttr(values, "iDes");
		exportAttr(values, "iPrl");
		exportAttr(values, "iPur");
		exportAttr(values, "iLoc");
		exportAttr(values, "iWlA");
		exportAttr(values, "iWlU");
		exportAttr(values, "iFOz");
		exportAttr(values, "iWtA");
		exportAttr(values, "iWtU");
		exportAttr(values, "iVlA");
		exportAttr(values, "iVlU");
		exportAttr(values, "iPrf");
		// TODO: Cost?
		// Nutrition:
		List<Float> nut = (List<Float>) values.get("iNut");
		if (nut == null) {
			for (int i=0; i<33; i++) {
				System.out.print(',');
			}
		} else {
			for (Float x : nut) {
				System.out.print(x);
				System.out.print(',');
			}
		}
		System.out.println();
		
	}
	
	void exportAttr(HashMap<String, Object> values, String attr) {
		if (!values.containsKey(attr)) {
			System.out.print("");
		} else {
			Object v = values.get(attr);
			if (v instanceof String) {
				String str = (String) v;
				if (str.contains(",")) {
					System.out.print("\""+str+"\"");
				} else {
					System.out.print(str);
				}
			} else {
				System.out.print(v.toString());
			}
		}
		System.out.print(',');
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

}
