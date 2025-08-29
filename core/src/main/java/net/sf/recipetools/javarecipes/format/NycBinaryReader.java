/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor;
import net.sf.recipetools.javarecipes.format.MasterCookBinary.BinaryFileReader;
import net.sf.recipetools.javarecipes.model.Cookbook;
import net.sf.recipetools.javarecipes.model.Image;
import net.sf.recipetools.javarecipes.model.Ingredient;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;
import net.sf.recipetools.javarecipes.model.Unit;

/**
 * @author Frank
 *
 * File header (first byte in file):
 * 	File type: 0x01
 *		FoxBASE: 0x02
 *		FoxBASE+/Dbase III plus, no memo: 0x2F
 *		Visual FoxPro: 0x30
 *		Visual FoxPro, autoincrement enabled: 0x31
 *		Visual FoxPro, Varchar, Varbinary, or Blob-enabled: 0x42
 *		dBASE IV SQL table files, no memo: 0x62
 *		dBASE IV SQL system files, no memo: 0x82
 *		FoxBASE+/dBASE III PLUS, with memo: 0x8A
 *		dBASE IV with memo: 0xCA
 *		dBASE IV SQL table files, with memo: 0xF4
 *		FoxPro 2.x (or earlier) with memo: 0xFA
 * 
 *
 * Visual FoxPro database
 * OleDB driver: 
 * ODBCConnection String:
 *      "Provider=MSDASQL;DSN=dsnName;UID=MyUserID;PWD=MyPassword;"
 * 		Driver={Microsoft Visual FoxPro Driver};SourceType=DBC;SourceDB=c:\myvfpdb.dbc;
Exclusive=No;NULL=NO;Collate=Machine;BACKGROUNDFETCH=NO;DELETED=NO;
 * 		Driver={Microsoft Visual FoxPro Driver};SourceType=DBF;SourceDB=c:\myvfpdbfolder;
Exclusive=No;Collate=Machine;NULL=NO;DELETED=NO;BACKGROUNDFETCH=NO;
 * Connection strings:
 * 		http://www.devlist.com/ConnectionStringsPage.aspx
 * 		FoxPro ODBC Connection String
 * 			Driver={Microsoft Visual FoxPro Driver};SourceType=DBC;SourceDB=c:\demo.dbc;Exclusive=No;NULL=NO;Collate=Machine;BACKGROUNDFETCH=NO;DELETED=NO
 * 		FoxPro OLEDB Connection String
 * 			Provider=vfpoledb.1;Data Source=c:\directory\demo.dbc;Collating Sequence=machine
 *
 * 		Note that DELETED=NO will cause the driver to include deleted rows in the resultset. To not retrieve deleted rows specify DELETED=YES. The terminology is a bit confusing here, a more appropriate keyword would have been IGNORE DELETED instead of DELETED.
 *
 * ODBC Driver:
 * ODBC Driver (Microsoft Visual FoxPro ODBC Driver) can be downloaded here
 *            http://download.microsoft.com/download/vfoxodbcdriver/Install/6.1/W9XNT4/EN-US/VFPODBC.msi
 * 
 *           
 * JDBC Connection String
 *           jdbc:odbc:Driver={Microsoft Visual FoxPro Driver};SourceType=DBF;SourceDB=C:\pr\downloads\temp\cb2cb\recipes\t\12cook02.dbc;Exclusive=No;NULL=NO;Collate=Machine;BACKGROUNDFETCH=NO;DELETED=YES
 * 
 * 
 * FORMAT:
 * It doesn't seems to be a single table, since it contains all the info on a recipe
 * 
 * byte 0: 0x42 (Header)
 * 1: int: no of records
 * 5: int: no of records
 * 9: int = name length+1
 * d: Cookbook name
 * 4c: record no
 * 
 * 
 * IDX: 
 * 		string: Recipe title: GIANTS THUMGPRINT BUTTER COOKIES
 *		int: Record No for the R-record
 *		int: Record No for the N-record
 *		int: Record No for the first I-record
 *		int: Record No for the first Q-record
 *		int: Record No for the first D-record
 *		int: Record No for the last  D-record
 * 
 * IMX:
 *      for each recipe/image:
 *      	int: start block (0x400)
 *      	int: length (no blocks of 0x400)
 * IMA:
 * 		for each image
 * 			'A'
 * 			string: title (length=0x3c)
 * 			string: image type (length=3)
 * 			next block = start+0x400 .... image start     
 * 
 *
 */
/**
 * @author Frank
 *
 */
public class NycBinaryReader implements AllFileInOneGo, RecipeFormatter, BinaryInputProcessor {
	private static final Logger log = LoggerFactory.getLogger(NycBinaryReader.class);

	public static final int RECORD_LENGTH = 80;
	
	File tempDir = null;
	File file  = null;
	Cookbook cookbook = null;
	
	public NycBinaryReader() {
	}
	
	public NycBinaryReader(File file) {
		initializeWithFile(file);
	}

	public void initializeWithFile(File file) {
		if (! file.exists()) {
			throw new RecipeFoxException("The file does not exists: file="+file.getAbsolutePath());
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
			List<File> files = RecipeTextFormatter.findFilesWithExtension(tempDir, ".gcf");
			if (files.size() == 0) {
				throw new RecipeFoxException("Found no files with gcf extension in the zip file: "+file.getAbsolutePath());
			}
			file = files.get(0);
		}
		
		if (! file.getName().toLowerCase().endsWith("gcf")) {
			throw new RecipeFoxException("The reader should point to a .gcf export file:\nfile="+file.getAbsolutePath());
		}
		
		/*
		// clean up if any old file
		if (zip != null) {
			try {
				zip.close();
			} catch (IOException e) {
				throw new RecipeFoxException("Could not close the zip file.", e);
			}
			files = null;
		}
		*/
		
		this.file = file;
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
	
	List<Recipe> readAllRecipes() {
		List<Recipe> recipes = new ArrayList<Recipe>();
		File idxFile = getAltFile("idx");
		// ignore if files don't exists
		if (! idxFile.exists() && ! file.exists()) return recipes;
		if (! idxFile.exists() && file.exists()) return readAllRecipes2();
		
		long noRecipes = idxFile.length() / 0x54;
		// read the recipes in the order given in the idx file
		try (BinaryFileReader imxReader = new BinaryFileReader(idxFile);
			 BinaryFileReader in = new BinaryFileReader(file)) {
			readCookbookDetails(in);
			
			for (int j=0; j<noRecipes; j++) {
				String title = imxReader.readFixedSizeTrimmedString(0x3c);
				Recipe r = new Recipe(title);
				r.setFolder(cookbook);
				recipes.add(r);
				readRecord(in, r, imxReader.readInt()); // R-record
				readRecord(in, r, imxReader.readInt()); // N-record
				int iRecordStart = imxReader.readInt(); // I-record 
				int qRecordStart = imxReader.readInt(); // Q-record
				int dRecordStart = imxReader.readInt(); // D-record start
				int dRecordEnd = imxReader.readInt();   // D-record end
				int noIngredients = r.getIngredients().size();

				for (int i=iRecordStart; i<iRecordStart+noIngredients; i++) {
					readRecord(in, r, i, i-iRecordStart); // I-record
				}
				for (int i=qRecordStart; i<qRecordStart+noIngredients; i++) {
					readRecord(in, r, i, i-qRecordStart); // Q-record
				}
				for (int i=dRecordStart; i<=dRecordEnd; i++) {
					readRecord(in, r, i); // D-record
				}
			}
			
			// read the category files
			readCategories(recipes);
			
			// read the image files
			readImages(recipes);
		}
		return recipes;
	}
	
	void readCookbookDetails(BinaryFileReader in) {
		// read cookbook details
		in.seek(0x0c);
		String name = in.readFixedSizeTrimmedString(0x3f);
		cookbook = new Cookbook(name);
	}

	void readRecord(BinaryFileReader in, Recipe r, long recordNo) {
		readRecord(in, r, recordNo, 0);
	}
	
	void readRecord(BinaryFileReader in, Recipe r, long recordNo, int index) {
		if (recordNo < 1) {
			log.error("Invalid recordNo="+recordNo);
			return;
		}
		in.seek(0x50 * recordNo - 0x54);
		int recNo = in.readInt();
		if (recNo != recordNo) {
			// it seems only be on R-records.
			//System.out.println("Invalid recordNo, recNo="+recNo+", recordNo="+recordNo);
		}
		
		char recType = in.readFixedSizeTrimmedString(1).charAt(0);
		// debug
		//System.out.println("Record="+recordNo+", index="+index+", type="+recType);
		
		switch (recType) {
		case 'R': // recipe
			r.setTitle(in.readFixedSizeTrimmedString(0x3b));
			in.skipBytes(0x0c);
			int noIngredients = in.readShort();
			@SuppressWarnings("unused")
			int noDirRecords = in.readShort();
			for (int i=0; i<noIngredients; i++) {
				r.addIngredient(new RecipeIngredient());
			}
			//System.out.println("\ttitle="+r.getTitle());
			break;
		case 'X': // deleted recipe
			// don't add it to the result set
			// TODO: HOw do the indexing matches???
			// Otherwise add it also to a ToDeleteList and delete afterwards.
			//r.setTitle(readString(in, 0x3b));
			@SuppressWarnings("unused")
			String str = in.readFixedSizeTrimmedString(0x4b);
			r.setTitle("DELETED ????");
			//System.out.println("*** FOUND a Deleted recipe: "+str);
			//in.skipBytes(0x4b);
			break;
		case 'N': // Notes + Yield
			r.setNote(in.readFixedSizeTrimmedString(0x37));
			r.setYield(in.readFixedSizeTrimmedString(15));
			r.setPreparationTime(in.readFixedSizeTrimmedString(5));
			break;
		case 'I': // ingredient - name + unit
			RecipeIngredient ri = r.getIngredients().get(index);
			ri.setIngredient(new Ingredient(in.readFixedSizeTrimmedString(0x23)));
			ri.setUnit(new Unit(in.readFixedSizeTrimmedString(0x12)));
			in.skipBytes(0x16);
			//System.out.println("\ting="+ri.getIngredient().getName()+", unit="+ri.getUnit().getName());
			break;
		case 'Q': // ingredient - quantity + preparation
			ri = r.getIngredients().get(index);
			ri.setAmount(RecipeIngredient.getNumber(in.readFixedSizeTrimmedString(8)));
			ri.setProcessing(in.readFixedSizeTrimmedString(0x37));
			in.skipBytes(0x0c);
			//System.out.println("\ta="+ri.getAmount()+", proc="+ri.getProcessing()+", ri="+ri.toString());
			break;
		case 'D': // directions
			r.setDirections(r.getDirectionsAsString()+in.readFixedSizeTrimmedString(0x4b));
			break;
		default:
			byte[] buf = in.readByteBuffer(0x4b);
			System.out.println("*** UNKNOWN RECTYPE:"+recType+" at recNo="+recNo+", string="+new String(buf));
		}
		
	}
	
	/**
	 * Return all recipes in the file
	 */
	List<Recipe> readAllRecipes2() {
		List<Recipe> recipes = new ArrayList<Recipe>();
		// iterate the recipes
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			@SuppressWarnings("unused")
			byte b = in.readByte();
			@SuppressWarnings("unused")
			int noRecords0 = readInt(in)-2;
			//ignoreBytes(in, 8);
			int noRecords = readInt(in)-2;
			@SuppressWarnings("unused")
			int noRecipes = readInt(in);
			String name = readString(in, 0x3f);
			Cookbook cookbook = new Cookbook(name);
			Recipe r = null;
			int ingrNo = 0;
			boolean deletedRecipe = false;
			
			for (int i=0; i<noRecords; i++) {
				int recNo = readInt(in);
				//System.out.println("i:"+i+", recNo="+(recNo-2));
				char recType = readString(in, 1).charAt(0);
				switch (recType) {
				case 'R': // recipe
					r = new Recipe();
					r.setFolder(cookbook);
					recipes.add(r);
					r.setTitle(readString(in, 0x3b));
					ignoreBytes(in, 16);
					deletedRecipe = false;
					break;
				case 'X': // deleted recipe
					r = new Recipe();
					// don't add it to the result set
					// TODO: HOw do the indexing matches???
					// Otherwise add it also to a ToDeleteList and delete afterwards.
					r.setTitle(readString(in, 0x3b));
					ignoreBytes(in, 16);
					deletedRecipe = true;
					break;
				case 'N': // Notes + Yield
					if (! deletedRecipe) {
						r.setNote(readString(in, 0x37));
						r.setYield(readString(in, 15));
						r.setPreparationTime(readString(in,5));
					} else {
						ignoreBytes(in, 0x4b);
					}
					break;
				case 'I': // ingredient - name + unit
					if (! deletedRecipe) {
						RecipeIngredient ri = new RecipeIngredient();
						r.addIngredient(ri);
						ri.setIngredient(new Ingredient(readString(in, 0x23)));
						ri.setUnit(new Unit(readString(in, 0x12)));
						ignoreBytes(in, 0x16);
					} else {
						ignoreBytes(in, 0x4b);
					}
					ingrNo = 0;
					break;
				case 'Q': // ingredient - quantity + preparation
					if (! deletedRecipe && ingrNo<r.getIngredients().size()) {
						RecipeIngredient ri = r.getIngredients().get(ingrNo);
						ri.setAmount(RecipeIngredient.getNumber(readString(in, 8)));
						ri.setProcessing(readString(in, 0x37));
						ignoreBytes(in, 0x0c);
						ingrNo++;
					} else {
						ignoreBytes(in, 0x4b);
					}
					break;
				case 'D': // directions
					if (deletedRecipe) {
						r.setDirections(r.getDirectionsAsString()+readString(in, 0x4b));
					} else {
						ignoreBytes(in, 0x4b);
					}
					break;
				default:
					byte[] buf = new byte[0x4b];
					in.readFully(buf);
					System.out.println("*** UNKNOWN RECTYPE:"+recType+" at recNo="+recNo+", string="+new String(buf));
				}
			}
			
			// read the category files
			readCategories(recipes);
			
			// read the image files
			readImages(recipes);

		} catch (FileNotFoundException e) {
			log.error("Could not find the file:"+file.getAbsolutePath(), e);
		} catch (IOException e) {
			log.error("Could not read from the file:"+file.getAbsolutePath(), e);
		}
		
		
		
		// if zip file then delete temp dir
		if (tempDir != null) {
			RecipeTextFormatter.deleteDirectory(tempDir);
			tempDir = null;
		}
		
		return recipes;
	}
	
	public void readCategories(List<Recipe> recipes) {
		List<String> categories = new ArrayList<String>(); 
		File cliFile = getAltFile("cli");
		File cdxFile = getAltFile("cdx");
		// ignore if files dont exists
		if (! cliFile.exists() || ! cdxFile.exists()) return;
		
		// read the names from the CLI file
		try (BufferedReader buf  = new BufferedReader(new FileReader(cliFile))) {
			// read the count
			int count = Integer.parseInt(buf.readLine());
			for (int i=0; i<count; i++) {
				categories.add(buf.readLine().replace('"',' ').trim());
			}
		} catch (IOException e) {
			log.error("File error reading the category files: cli="+cliFile.getAbsolutePath()+", cdx="+cdxFile.getAbsolutePath());
		}
		
		// read the recipe categories from the CDX file
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(cdxFile))) {
			// read the count
			byte[] categoryBits= new byte[0xc8];
			for (Recipe r: recipes) {
				Arrays.fill(categoryBits, (byte) 0);
				in.read(categoryBits);
				//System.out.println("cat="+new String(categoryBits));
				for (int i=0; i<categoryBits.length; i++) {
					if (i>=categories.size()) break;
					if (categoryBits[i] == '1') {
						r.addCategory(categories.get(i));
					}
				}
			}
		} catch (IOException e) {
			log.error("File error reading the category files: cli="+cliFile.getAbsolutePath()+", cdx="+cdxFile.getAbsolutePath());
		}
	}

	public void readImages(List<Recipe> recipes) {
		File imaFile = getAltFile("ima");
		File imxFile = getAltFile("imx");
		
		// ignore if files don't exists
		if (! imaFile.exists() || ! imxFile.exists()) return;
		
		// read the names from the CLI file
		try (BinaryFileReader imaReader = new BinaryFileReader(imaFile);
			 BinaryFileReader imxReader = new BinaryFileReader(imxFile))
		{
			for (Recipe r: recipes) {
				int start = imxReader.readInt();
				int length = imxReader.readInt();
				
				imaReader.seek(0x400l * start);
				
				// read the image name (and skip the leading 'A')
				String name = imaReader.readFixedSizeString(0x3d).substring(1).trim();
				String filetype = imaReader.readFixedSizeString(3).trim();
				//System.out.println("start="+start+", length="+length+", name="+name);
				Image image = new Image();
				image.setName(name+"."+filetype);
				
				imaReader.seek(0x400l * start+ 0x400);
				byte[] bytes = imaReader.readByteBuffer(length*0x400);
				image.setImage(bytes);
				r.addImage(image);
			}
		}
	}
	
	
	public File getAltFile(String extension) {
		return new File(file.getAbsolutePath().replaceAll("\\.\\w+$", "."+extension));
	}
	
	
	public String readString(DataInputStream in, int length) {
		byte[] buffer = new byte[1024];
		String s = "";
		try {
			in.readFully(buffer, 0, length);
			s = new String(buffer, "windows-1252"); 
		} catch (IOException e) {
			log.error("Could not read from file: "+file.getAbsolutePath(), e);
		}
		return s.trim();
	}
	
	public int readInt(DataInputStream in) throws IOException {
		return Integer.reverseBytes(in.readInt());
	}

	/**
	 * Ignore a number of bytes
	 * @param in
	 * @param length
	 */
	public void ignoreBytes(DataInputStream in, int length) {
		byte[] buffer = new byte[1024];
		try {
			in.readFully(buffer, 0, length);
		} catch (IOException e) {
			log.error("Could not read from file: "+file.getAbsolutePath(), e);
		}
	}
	
	@Override
	public void write(List<Recipe> recipe) {
		throw new RecipeFoxException("NycBinary formatter does not support writing.");
	}

	@Override
	public void startFile(String name) {
		throw new RecipeFoxException("NycBinary formatter does not support writing.");
	}

	@Override
	public void startFile(File f) {
		throw new RecipeFoxException("NycBinary formatter does not support writing.");
	}

	@Override
	public void endFile() {
		throw new RecipeFoxException("NycBinary formatter does not support writing.");
	}
	
    @Override
	public void setConfig(String property, String value) {}
    @Override
	public String getConfig(String property) {
		return "";
	}
	
	
}
