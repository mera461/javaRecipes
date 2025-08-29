/**
 * 
 */
package net.sf.recipetools.javarecipes.format.MasterCookBinary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author Frank
 * 
 */
public class BinaryFileReader implements AutoCloseable {

	static Calendar CAL1990 = Calendar.getInstance();
	static {
		CAL1990.clear();
		CAL1990.set(1990, 0, 1); // new Date(90,0,1);
	}

	public static final Date DATE1990 = CAL1990.getTime();

	File filename;
	RandomAccessFile file;
	String charSet = "windows-1252";

	public BinaryFileReader() {
	}
	
	public BinaryFileReader(File filename) {
		openFile(filename);
	}
	
	/**
	 * @return the charSet
	 */
	public String getCharSet() {
		return charSet;
	}

	/**
	 * @param charSet the charSet to set
	 */
	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	
	
	public int readIntBigEndian() {
		int i = 0;
		try {
			i = file.readInt();
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return i;
	}

	public int readIntLittleEndian() {
		int i = 0;
		try {
			i = file.readInt();
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return Integer.reverseBytes(i);
	}

	public int readInt() {
		return readIntLittleEndian();
	}

	public Date readDate() {
		long date = readInt();
		return new Date(DATE1990.getTime() + date * 1000);
	}

	public Date readDateAt(long pos) {
		try {
			file.seek(pos);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return readDate();
	}

	public int readShort() {
		short i = 0;
		try {
			i = file.readShort();
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return Short.reverseBytes(i);
	}

	public int readIntAt(long pos) {
		try {
			file.seek(pos);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return readIntLittleEndian();
	}

	public int readUnsignedByteAt(byte[] buffer, int pos) {
		int value = buffer[pos];
		return value < 0 ? 256 + value : value;
	}

	public int readIntAt(byte[] buffer, int pos) {
		return readUnsignedByteAt(buffer, pos) + (readUnsignedByteAt(buffer, pos + 1) << 8)
				+ (readUnsignedByteAt(buffer, pos + 2) << 16) + (readUnsignedByteAt(buffer, pos + 3) << 24);
	}

	public int readShortAt(byte[] buffer, int pos) {
		return readUnsignedByteAt(buffer, pos) + (readUnsignedByteAt(buffer, pos + 1) << 8);
	}

	public float readFloatAt(byte[] buffer, int pos) {
		int x1 = readIntAt(buffer, pos);
		Float f1 = Float.intBitsToFloat(x1);
		return f1;
	}
	
	public int readByte() {
		int i = 0;
		try {
			i = file.readByte();
			if (i<0) i+=256;
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return i;
	}

	public int readByteAt(long pos) {
		try {
			file.seek(pos);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return readByte();
		
	}
	// -------------------------- String ------------------------------------------
	
	
	/**
	 * @param buffer
	 * @param pos
	 * @return Read a string from the given position. Assumed to start with
	 *         string length.
	 */
	public String readStringAt(byte[] buffer, int pos) {
		int length = readIntAt(buffer, pos);
		return stringFromBytes(buffer, pos + 4, length);
	}
	
	public String readByteString() {
		byte[] str = null;
		try {
			int length = readByte();
			if (length == 0) return "";
			str = new byte[length];
			file.readFully(str);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return stringFromBytes(str);
	}

	public String readShortString() {
		byte[] str = null;
		try {
			short x = file.readShort();
			short length = Short.reverseBytes(x);
			str = new byte[length];
			file.readFully(str);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return stringFromBytes(str);
	}

	/**
	 * @param bytes
	 * @return
	 */
	private String stringFromBytes(byte[] bytes) {
		return stringFromBytes(bytes, 0, bytes.length);
	}

	// negated value
	short[] translationTable = new short[] { 32, 164, 254, 253, 184, 222, 158, 157, 175, 152, // -
																								// 0
			136, 221, 217, 219, 218, 210, 240, 212, 211, 204, // -10
			207, 206, 205, 200, 203, 193, 202, 194, 137, 132, // -20
			130, 183, 135, 154, 138, 155, 139, 128, 190, 159, // -30
			255, 215, 247, 146, 145, 148, 147, 151, 150, 156, // -40
			140, 213, 195, 192, 32, 133, 187, 171, 32, 32, // -50
			131, 208, 172, 161, 191, 248, 230, 189, 186, 170, // -60
			166, 185, 142, 141, 188, 181, 165, 179, 178, 177, // -70
			32, 216, 198, 173, 168, 180, 153, 169, 174, 223, // -80
			182, 149, 167, 163, 162, 176, 134, 252, 251, 249, // -90
			250, 245, 246, 244, 242, 243, 241, 239, 238, 236, // -100
			237, 235, 234, 232, 233, 231, 229, 227, 228, 226, // -110
			224, 225, 220, 214, 209, 201, 199, 197, 196, // -120
	};

	/**
	 * @param bytes
	 * @return
	 */
	public String stringFromBytes(byte[] bytes, int start, int length) {
		return stringFromBytes(bytes, start, length, false);
	}

	public String stringFromBytes(byte[] bytes, int start, int length, boolean translateChars) {
		String result = null;
		if (charSet.equals("windows-1252")) {
			// translate special chars for MasterCook
			for (int i = start; i < start + length; i++) {
				if (bytes[i] < 0 && translateChars) {
					bytes[i] = (byte) translationTable[-bytes[i]];
				} else if (bytes[i] >= 0 && bytes[i] < 32 && bytes[i] != 9 // tab
						&& bytes[i] != 10 // LF
						&& bytes[i] != 13 // CR
						) {
					bytes[i] = 32;
				}
			}
		}

		try {
			result = new String(bytes, start, length, charSet);
		} catch (UnsupportedEncodingException e) {
			throw new RecipeFoxException("Could not read the string as "+charSet+" character set", e);
		}
		return result;
	}

	public String readShortStringAt(long pos) {
		try {
			file.seek(pos);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return readShortString();
	}

	public String readZeroTerminatedString() {
		byte[] buffer = new byte[30000];
		int bufIndex = 0;
		try {
			byte x = file.readByte();
			while (x != 0) {
				buffer[bufIndex++] = x;
				x = file.readByte();
			}
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return stringFromBytes(buffer, 0, bufIndex);
	}
	
	public String translateCharset(String s, String fromCharset, String toCharset) {
		try {
			return new String(s.getBytes(fromCharset), toCharset);
		} catch (UnsupportedEncodingException e) {
			throw new RecipeFoxException("Invalid charset, from="+fromCharset+", to="+toCharset, e);
		}
	}

	public String readFixedSizeTrimmedString(int length) {
		byte[] bytes = readByteBuffer(length);
		return stringFromBytes(bytes).trim();
	}

	public String readFixedSizeString(int length) {
		byte[] bytes = readByteBuffer(length);
		return stringFromBytes(bytes);
	}

	String readFixedSizeStringAt(byte[] buffer, int pos, int length) {
		return stringFromBytes(buffer, pos, length);
	}

	// -------------------------- Tags ------------------------------------------
	
	public String readTagAt(byte[] buffer, int pos) {
		return readFixedSizeStringAt(buffer, pos, 4);
	}

	public String readTag() {
		return readFixedSizeString(4);
	}

	public byte[] readByteBuffer(int size) {
		byte[] out = new byte[size];
		try {
			file.readFully(out);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading from the file", e);
		}
		return out;
	}

	public void seek(long pos) {
		try {
			file.seek(pos);
		} catch (IOException e) {
			throw new RecipeFoxException("Error to seek in the file", e);
		}
	}

	public long getFilePointer() {
		try {
			return file.getFilePointer();
		} catch (IOException e) {
			throw new RecipeFoxException("Error getting the file position", e);
		}
	}

	public void skipBytes(int no) {
		try {
			file.skipBytes(no);
		} catch (IOException e) {
			throw new RecipeFoxException("Error skipping bytes.", e);
		}
	}
	
	public void skipUntil(int... bytes) {
		if (bytes==null || bytes.length == 0) return;
		int match = 0;
		while (match!=bytes.length) {
			int b = readByte();
			if (b==bytes[match]) {
				match++;
			} else {
				match = 0;
			}
		}
	}

	/**
	 * @return the file
	 */
	public RandomAccessFile getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void openFile(File file) {
		try {
			filename = file;
			if (this.file != null) {
				this.file.close();
			}
			this.file = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			throw new RecipeFoxException("Could not open the file:" + file.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not close the old file", e);
		}
	}

    @Override
	public void close() {
		try {
			this.file.close();
		} catch (IOException e) {
			throw new RecipeFoxException("Could not close the old file", e);
		}
		this.file = null;
	}

	public void setConfig(String property, String value) {}
	public String getConfig(String property) {
		return "";
	}

	
}
