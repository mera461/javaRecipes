/**
 * 
 */
package net.sf.recipetools.javarecipes.format.cookenpro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.xBaseJ.DBF;
import org.xBaseJ.Util;
import org.xBaseJ.xBaseJException;
import org.xBaseJ.fields.Field;
import org.xBaseJ.fields.LogicalField;

import net.sf.recipetools.javarecipes.format.FormattingUtils;
import net.sf.recipetools.javarecipes.model.Chapter;
import net.sf.recipetools.javarecipes.model.Cookbook;
import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Image.ImageFormat;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;
import net.sf.recipetools.javarecipes.model.Unit;

/**
 * @author Frank
 * 
 * NOTICE: the uncompressor program ARQ is a 16-bit program and therefore only runs 
 * on a 32-bit windows.
 *
 */
public class CookenProV8BinaryReader extends CookenProBinaryReader {
	
	private static final String TEMP_DIR_PREFIX = "RecipeFox-TMP-DVO-";
	private static final long COOKBOOK_ID_MULTIPLIER = 10000l;
	File directory;
	File originalSourceFile;
	int version; // version no multiplied by 10
	
	Map<String, Map<String, String>> food = null;
	Map<String, Map<String, String>> unit = null;
	
	
	public void initalizeXBaseJ() {
		//16-bit ARQ doesn't work on 64-bit windows
		String architecture = System.getProperty("os.arch");
		if (architecture.equals("64")) {
			throw new RecipeFoxException("Old DVO files (until version 8) uses a 16-bit program to uncompress the files, and that is not working on a 64-bit windows");
		}
		try {
			Util.setxBaseJProperty("fieldFilledWithSpaces", "False");
			Util.setxBaseJProperty("ignoreMissingMDX", "True");
			Util.setxBaseJProperty("trimFields", "True");
		} catch (IOException e) {
			throw new RecipeFoxException("Could not access the xBaseJ property file.", e);
		}
	}
	
	public CookenProV8BinaryReader() {
		initalizeXBaseJ();
	}
	
	public CookenProV8BinaryReader(File file) {
		initalizeXBaseJ();
		initializeWithFile(file);
	}
	
    @Override
	public void initializeWithFile(File f) {
		if (! f.exists()) {
			throw new RecipeFoxException("The file does not exists: file:\nfile="+f.getAbsolutePath());
		}
		
		// if it is a directory then it is ok.
		if (f.isDirectory()) {
			directory = f;
			return;
		}
		
		if (! f.getName().toLowerCase().endsWith("dvo")) {
			throw new RecipeFoxException("The reader should point to either a directory with the\nCookenPro database files (like food.dbf)\nor a .dvo export file:\nfile="+f.getAbsolutePath());
		}
		
		// create a temporary directory to unpack the DVO file.
		File tempDir = null;
		try {
			tempDir = File.createTempFile(TEMP_DIR_PREFIX, "TMP");
		} catch (IOException e) {
			throw new RecipeFoxException("Could not create a temporary directory to unpack a DVO file");
		}
		tempDir.delete();
		if (!tempDir.mkdir()) {
			throw new RecipeFoxException("Could not create a temporary directory at "+tempDir.getAbsolutePath());
		}
		tempDir.deleteOnExit();
		
		unpackDvo(f, tempDir);
		directory = tempDir;
		originalSourceFile = f;
	}
	
	public void unpackDvo(File f, File dir) {
		// copy the file to the directory
		File tmp = new File(dir, "t.dvo");
		copy (f, tmp);
		// copy ARQ.exe to the directory
		InputStream inputStream = CookenProV8BinaryReader.class.getResourceAsStream("/ARQ.EXE");
		File arq = new File(dir, "arq.exe");
		copy (inputStream, arq);
		
		// unpack the file to the directory
		String[] cmdarray = new String[]{"cmd.exe", "/C", arq.getAbsolutePath(), "-x", "t.dvo"};
		String[] env = null;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmdarray, env, dir);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not unpack the DVO file in dir="+dir.getAbsolutePath(),e);
		} 
		int returnValue = 0;
		try {
			returnValue = process.waitFor();
		} catch (InterruptedException e) {
			returnValue = -1;
		} finally {
			// delete the temporary files again.
			arq.delete();
			tmp.delete();
		}
		String out = "";
		//Scanner scanner = null;
		try (InputStream is = process.getInputStream(); Scanner scanner = new Scanner(is)) {
			scanner.useDelimiter("\\A");
			out = scanner.hasNext() ? scanner.next() : "";
		} catch (IOException e) {
			throw new RecipeFoxException("Could not close the ARQ process output stream.", e);
		} 
		if (returnValue != 0 || out.contains("error")) {
			throw new RecipeFoxException("ARQ returned "+returnValue+". Msg="+out);
		}
	}

	
	
    public void copy(InputStream s, File t) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(t);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = s.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            throw new RecipeFoxException(e);
        } finally {
            try {
				if (fos != null) fos.close();
			} catch (Exception e) {
			}
        }
    }
    
    public void copy(File s, File t) {
        try (FileInputStream fis = new FileInputStream(s);
        	 FileChannel in = fis.getChannel();
        	 FileOutputStream fos = new FileOutputStream(t); 	
        	 FileChannel out = fos.getChannel()) {
			in.transferTo(0, s.length(), out);
		} catch (Exception e) {
			throw new RecipeFoxException("Copy not copy from file "+s.getAbsolutePath()+" to "+t.getAbsolutePath(),e);
		}
    }
    
    /**
     * Delete a file or directory (with files)
     * @param f the file or directory to delete
     * @return
     */
    public boolean delete(File f) {
		if (f.isDirectory()) {
			String[] children = f.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = delete(new File(f, children[i]));
				if (!success) {
					return false;
				}
			}
		} // The directory is now empty so delete it return
		return f.delete();
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
	
	public DBF getDbf(String tableName) {
		if (tableName.equals("RECIPE")) {
			if (new File(directory, "RECIPE8X.DBF").exists()) {
				tableName = "RECIPE8X";
				version = 80;
			} else {
				tableName = "RECIP65X";
				version = 65;
			}
		}
		String name = directory.getAbsolutePath()+File.separator+tableName+".DBF";
		DBF dbf = null;
		try {
			dbf = new DBF(name);
		} catch (xBaseJException e) {
			throw new RecipeFoxException("Could not open file: "+name, e);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not open file: "+name, e);
		}
		return dbf;
	}
	
	public void describeTable(String tableName) {
		DBF dbf = getDbf(tableName);
		System.out.println("Table "+tableName);
		for (int i=1; i<=dbf.getFieldCount(); i++) {
			try {
				System.out.println(""+i+": "+dbf.getField(i).Name+":\t"+dbf.getField(i).getType()+" --> "+dbf.getField(i).getLength());
			} catch (Exception e) {
				System.out.println("Error reading the description: "+e.getMessage());
			}
		}
	}
	
	public Map<String, Map<String, String>> readTable(String tableName) {
		HashMap<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		DBF dbf = getDbf(tableName);
		for (int i=1; i<=dbf.getRecordCount(); i++) {
			Map<String, String> record = readRecord(dbf);
			result.put(record.get("UID").trim(), record);
		}
		try {
			dbf.close();
		} catch (IOException e) {
			throw new RecipeFoxException("Could not close table: "+tableName, e);
		}
		return result;
	}
	
	public Map<String, String> readRecord(DBF dbf) {
		HashMap<String, String> o = new HashMap<String, String>();
		try {
			dbf.read();
		} catch (Exception e) {
			throw new RecipeFoxException("Error reading more records fron record#="+dbf.getCurrentRecordNumber()+", table="+dbf.getName(), e);
		}
		for (int i=1; i<=dbf.getFieldCount(); i++) {
			Field f;
			try {
				f = dbf.getField(i);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new RecipeFoxException("Invalid field i="+i+" in record#="+dbf.getCurrentRecordNumber()+", table="+dbf.getName(), e);
			} catch (xBaseJException e) {
				throw new RecipeFoxException("Invalid xBaseJ error in field i="+i+" in record#="+dbf.getCurrentRecordNumber()+", table="+dbf.getName(), e);
			}
			String name = f.getName();
			switch (f.getType()) {
			case 'L' :
				o.put(name, Boolean.toString(((LogicalField)f).getBoolean()));
				break;
			default:
				o.put(name, f.get().trim());
			}
		}
		return o;
	}
	
	public void listTable(String tableName) {
		DBF dbf = getDbf(tableName);
		System.out.println("****** Table "+tableName);
		for (int i=1; i<=dbf.getRecordCount(); i++) {
			Map<String, String> record = readRecord(dbf);
			
			System.out.print(""+record.get("UID")+": ");
			for (String name : record.keySet()) {
				if (name.equals("UID")) continue;
				System.out.print(""+name+"="+record.get(name).toString().trim()+", ");
			}
			System.out.println();
		}
	}
	
	/**
	 * Return all recipes in the file
	 */
    @Override
	List<Recipe> readAllRecipes() {
		List<Recipe> recipes = new ArrayList<Recipe>();
		
		// read the ingredients and units
		readFoods();
		readUnits();
		readCookbooks();
		readChapters();
		
		DBF dbf = getDbf("RECIPE");
		for (int i=1; i<=dbf.getRecordCount(); i++) {
			Map<String, String> record = readRecord(dbf);
			Recipe recipe = new Recipe();
			recipes.add(recipe);
			// set all the fields
			// Chapter
			long chapterId = Long.parseLong(record.get("CHAP_UID"));
			if (chapterId != 0) {
				recipe.setFolder(Chapter.get(chapterId));
			}
			
			recipe.setTitle(record.get("NAME"));
			recipe.setNote(record.get("DESCRIP"));
			recipe.setDirections(record.get("INSTRUC"));
			recipe.setServings(record.get("SERVE"));
			if (version >= 80) {
				recipe.setYield(record.get("YIELD"));
				// TODO: Rank
				recipe.setPreparationTime(record.get("PREPTIME"));
				// TODO: Cooktime
				// TODO: misc1
				// TODO: misc2
				// TODO: misc3
				// TODO: misc4
			}
			// media
			String media = record.get("MEDIA"); 
			if (media.length()>2) {
				// format is "C:\light\Beeffajitas||"
				// remove trailing ||
				media = media.replaceAll("\\|\\|$", "");
				// remove path
				media = media.replaceAll("^.*[\\\\]", "");
				Image image = new Image(media, originalSourceFile.getParent()+"/pictures/"+media+".BMP");
				if (image.isValid()) {
					// convert it to jpg
					image.convertTo(ImageFormat.JPEG);
					recipe.addImage(image);
				}
			}

			// ingredients
			int maxIngredients = (version == 80) ? 50 : 23;
			for (int j=1; j<=maxIngredients; j++) {
				// empty line?
				String line = record.get("QUAN"+j)+record.get("UNIT"+j)+record.get("INGR"+j)+record.get("1INFO"+j)+record.get("2INFO"+j); 
				if (line.equals("00")) {
					continue;
				}
				
				RecipeIngredient ingr = new RecipeIngredient();
				recipe.addIngredient(ingr);
				// amount
				ingr.setAmount(RecipeIngredient.getNumber(record.get("QUAN"+j)));
				
				// unit
				Map<String, String> unitRecord = unit.get(record.get("UNIT"+j));
				if (unitRecord == null) {
					ingr.setUnit(null);
				} else {
					ingr.setUnit(new Unit(unitRecord.get("NAME")));
				}
				
				// ingredient
				Map<String, String> foodRecord = food.get(record.get("INGR"+j));
				String ingredient = "";
				if (foodRecord != null) {
					ingredient = foodRecord.get("NAME");
					if (ingr.hasAmount()
							&& ingr.getAmount() > 1.0f
							&& foodRecord.get("PLURAL").length() > 0) {
						ingredient = foodRecord.get("PLURAL");
					}
				}
				String name = FormattingUtils.join(record.get("1INFO"+j), " ", ingredient);
				ingr.setIngredient(new Ingredient(name));
				
				// processing
				String processing = record.get("2INFO"+j);
				processing = processing.replaceAll("^\\s*,\\s*", "");
				ingr.setProcessing(processing);
				
				// type ????
				//System.out.println("TYPE="+record.get("INTYPE"+j)+" --> "+ingr);
			}
		}

		try {
			dbf.close();
		} catch (IOException e) {
			throw new RecipeFoxException("Could not close Recipe table.", e);
		}
		
		
		// delete directory if it is a temp
		if (directory.getAbsolutePath().contains(TEMP_DIR_PREFIX)) {
			delete(directory);
		}

		return recipes;
	}
	
	/**
	 * Read the FOOD table
	 */
	void readFoods() {
		food = readTable("FOODX");
	}
	
	/**
	 * Read the UNIT table
	 */
	void readUnits() {
		unit = readTable("UNITX");
	}
	
	void readCookbooks() {
		DBF dbf = getDbf("BOOKX");
		for (int i=1; i<=dbf.getRecordCount(); i++) {
			Map<String, String> record = readRecord(dbf);
			Cookbook cookbook = new Cookbook();
			cookbook.setName(record.get("NAME"));
			cookbook.setId(Long.parseLong(record.get("UID"))*COOKBOOK_ID_MULTIPLIER);
			cookbook.setDescription(record.get("INFO"));
		}
	}

	void readChapters() {
		DBF dbf = getDbf("CHAPTERX");
		for (int i=1; i<=dbf.getRecordCount(); i++) {
			Map<String, String> record = readRecord(dbf);
			Chapter chapter = new Chapter();
			chapter.setName(record.get("NAME"));
			chapter.setId(Long.parseLong(record.get("UID")));
			chapter.setDescription(record.get("INFO"));
			chapter.setParent(Folder.get(Long.parseLong(record.get("BOOK_UID"))*COOKBOOK_ID_MULTIPLIER));
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
