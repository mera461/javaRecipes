/**
 * 
 */
package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author Frank
 *
 */
public class MastercookReader extends BinaryFileReader {

		int pagesize;
		int itemsPerPart = 0; // value at 0x0044
		int noItems = 0;

		String cookbookTitle = "";
		@SuppressWarnings("unused")
		private Date cookbookDate = null;
		
		Map<String, Long> dbSection = new HashMap<String, Long>();
		Map<Integer, String> keyStrings = new HashMap<Integer, String>();
		
		
		void seekNextPage() {
	        long pos = 0;
			try {
				pos = file.getFilePointer();
		        if (pos % pagesize > 0) {
		        	pos += pagesize - (pos % pagesize);
			        file.seek(pos);
		        }
			} catch (IOException e) {
				throw new RecipeFoxException("Error reading from the file", e);
			}
		}

		void seekNextPageWithMark(int markNo) {
			boolean done = false;
			long fileLength = 0;
			while (! done) {
				seekNextPage();
				try {
					fileLength = file.length(); 
					checkMark(markNo);
					done = true;
				} catch (RuntimeException e) {
					done = (fileLength == getFilePointer());
					skipBytes(1);
				} catch (IOException e) {
					done = true;
				}
			}
			// go back before the mark.
			try {
				file.seek(file.getFilePointer()-8);
			} catch (IOException e) {
				throw new RecipeFoxException("Error reading from the file", e);
			}
		}

		
	/**
	 * @param no
	 */
	boolean checkMark(int no) {
		byte[] mark = new byte[] {-91,90,90,-91,36,87,121, (byte) no};
		byte[] markBuffer = new byte[8];
		
	    try {
			file.read (markBuffer);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		boolean identical = true;
		for (int i=0; i<mark.length; i++) {
			identical &= (mark[i]==markBuffer[i]);
		}
	    if (! identical) {
	    	// check if nothing there
	    	boolean nothingHere = true;
			for (int i=0; i<mark.length; i++) {
				nothingHere &= (0==markBuffer[i]);
			}
			
			if (nothingHere) {
				return false;
			} else {
				// logic error
		       	throw new RecipeFoxException( String.format("Invalid format: didn't find data mark pos=0x%x, no=%c, buffer=%s", getFilePointer(), no, Arrays.toString(markBuffer)));
			}
	    }
	    return true;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		openFile(file);
	    // get pagesize
		readDatabaseStructure();
	}

	void readDatabaseStructure() {
		keyStrings.clear();

		pagesize = readIntAt(0x40);
		itemsPerPart = readIntAt(0x44);
		
		// no recipes
		noItems = readIntAt(pagesize);
		int noDbSections = readIntAt(pagesize+4);
		cookbookDate = readDateAt(pagesize + 0x14);
		cookbookTitle = readShortStringAt(pagesize + 0x1E);
		
		// get the sections.
		seek (2*pagesize);
		dbSection.clear();
		String tag = "";
		for (int i=0; i<noDbSections;i++) {
			long blockAddr = readInt()*pagesize;
			readInt(); // pagesize
			readInt(); // id
			readDate(); // date
			skipBytes(8); // all 0
			tag = readTag();
			int part = readInt();
			if (part==0) { 
				dbSection.put(tag, blockAddr);
			} else {
				dbSection.put(tag+"-"+part, blockAddr);
			}
			
			if ("#cat".equals(tag)
				|| "#ca#".equals(tag)
				|| "#db0".equals(tag)
				|| "#dbi".equals(tag)
				|| "#idx".equals(tag)
				|| "#im#".equals(tag) // used in the ingredients file
				|| "#key".equals(tag)
				|| "#nat".equals(tag)
				|| "#ra#".equals(tag)
				|| "#rat".equals(tag)
				|| "*map".equals(tag)
				|| "CBCv".equals(tag)
				|| "CBDs".equals(tag)
				|| "CBHL".equals(tag)
				|| "DPrp".equals(tag)
				|| "SrtC".equals(tag)
				) {
				skipBytes(4);
			} else if ("#drt".equals(tag)) {
				skipBytes(4); // skips #hdt
			} else if ("\u0000\u0000\u0000\u0000".equals(tag)) {
				break;
			} else {
				throw new RecipeFoxException("Unknown database section: "+tag);
			}
		}
	}

	/**
	 * Get the content of a specific DB section.
	 */
	byte[] readDbSection(String tag) {
		long addr = dbSection.get(tag);
		seek(addr);
		skipBytes(8); // skip '0'-mark
		long startPos = getFilePointer();
		readInt(); 
		int totalLength = readInt(); // after 0-mark to z-mark
		readInt(); // length1
		String packType = readTag();
		int lengthOfBlock0 = readInt()+16;
		if (lengthOfBlock0==totalLength) {
			return new byte[0];
		}
		
		skipBytes(lengthOfBlock0-28);
		checkMark('1');
		byte[] block1 = readByteBuffer((int) (startPos+totalLength-getFilePointer()));
		byte[] decoded = null;
		if ("PKWr".equals(packType)) {
			Blast blast = new Blast();
			decoded = blast.blast(block1);
		} else {
			decoded = block1;
		}
		return decoded;
	}
	
	/**
	 * Read the special string table
	 */
	void readStringTable() {
		keyStrings.clear();
		byte[] dbSection = readDbSection("#key");
		//save(dbSection);
		int addr = 10;
		while (addr<dbSection.length) {
			int strLength = readShortAt(dbSection, addr)-1; // do not include the terminating \0
			String str = stringFromBytes(dbSection, addr+2, strLength, true);
			keyStrings.put(addr+2, str);
			addr += strLength+3;
			if (addr % 2==1) addr++;
		}
	}
	
	static int no=0;
	/**
	 * Save a dump of the given block
	 * @param block1
	 */
	void save(byte[] block1) {
		try {
			FileOutputStream fos = new FileOutputStream("target/block1"+ no++ +".block1");
			fos.write(block1);
			fos.close();
		} catch (FileNotFoundException e) {
			throw new RecipeFoxException("File not found.", e);
		} catch (IOException e) {
			throw new RecipeFoxException("IO error.", e);
		}
	}
}
