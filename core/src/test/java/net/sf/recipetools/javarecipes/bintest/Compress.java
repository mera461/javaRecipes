package net.sf.recipetools.javarecipes.bintest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.sf.recipetools.javarecipes.model.RecipeFoxException;


public class Compress {
	
	public static byte[] gzip(byte[] buffer) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			GZIPOutputStream gz = new GZIPOutputStream(os);
			compress(buffer, gz);
			gz.finish();
			gz.flush();
			return os.toByteArray();
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
	}

	public static byte[] gunzip(byte[] buffer) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(buffer);
			GZIPInputStream gz = new GZIPInputStream(is);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transform(gz, os);
			os.flush();
			return os.toByteArray();
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
	}
	
	public static byte[] zip(byte[] buffer) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ZipOutputStream gz = new ZipOutputStream(os);
		try {
			gz.putNextEntry(new ZipEntry("0"));
			compress(buffer, gz);
			gz.finish();
			gz.flush();
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
		return os.toByteArray();
	}

	public static byte[] unzip(byte[] buffer) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(buffer);
			ZipInputStream gz = new ZipInputStream(is);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transform(gz, os);
			os.flush();
			return os.toByteArray();
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
	}
	
	
	
	public static byte[] deflate(byte[] buffer) {
		ByteArrayOutputStream os = null;
		try {
			os = new ByteArrayOutputStream();
			DeflaterOutputStream gz = new DeflaterOutputStream(os);
			compress(buffer, gz);
			gz.finish();
			gz.flush();
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
		return os.toByteArray();
	}

	public static byte[] undeflate(byte[] buffer) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(buffer);
			DeflaterInputStream gz = new DeflaterInputStream(is);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transform(gz, os);
			os.flush();
			return os.toByteArray();
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
	}
	
	
	public static void compress(byte[] input, OutputStream os) {
		InputStream is = new ByteArrayInputStream(input);
		transform(is, os);
	}

	public static void transform(InputStream is, OutputStream os) {
		boolean more = true;
		byte[] buffer = new byte[1024];
		try {
			while (more) {
				int noBytes = is.read(buffer);
				if (noBytes > 0) {
					os.write(buffer, 0, noBytes);
				} else {
					more = false;
				}
			}
			os.flush();
		} catch (IOException e) {
            throw new RecipeFoxException(e);
		}
	}
	
	public static void printBytes(byte[] buf) {
		System.out.print(" length="+buf.length+"\n");
		for (int i = 0; i < buf.length; i++) {
			System.out.printf(" %02x", buf[i]);
		}
		System.out.println();
		System.out.println("As String="+new String(buf));
	}

	public static void compressAll(byte[] input) {
		System.out.print("GZIP:\t");
		printBytes(gzip(input));
		System.out.print("ZIP:\t");
		printBytes(zip(input));
		System.out.print("Deflate:\t");
		printBytes(deflate(input));
	}

	public static void uncompressAll(byte[] input) {
		System.out.print("GZIP:\t");
		printBytes(gunzip(input));
		System.out.print("ZIP:\t");
		printBytes(unzip(input));
		System.out.print("Deflate:\t");
		printBytes(undeflate(input));
	}
	
	public static byte[] toByteArray(int[] input) {
		byte[] output = new byte[input.length];
		System.arraycopy(input, 0, output, 0, input.length);
		return output;
	}
	
	public static byte[] subarray(byte[] x, int offset) {
		byte[] result = new byte[x.length-offset+1];
		for (int i = offset; i < x.length; i++) {
			result[i-offset] = x[offset];
		}
		return result;
	}
	
// =============================================================================
	public static void test1() {
		byte[] x = new byte[] {
				 /*0x00,0x00,0x05,0x00,0x2a,0x74,0x63,0x6b 
				,*/0x00,0x00,0x06,0x00,0x74,0x65,0x73,0x74,0x31,0x00,0x06,0x00,0x74,0x65,0x73,0x74
				,0x32,0x00,0x06,0x00,0x74,0x65,0x73,0x74,0x33,0x00,0x06,0x00,0x74,0x65,0x73,0x74
				,0x34,0x00 				
		};
		compressAll(x);
	}
	
	public static void test2() {
		int[] y = new int[] {
				0x00,0x05, 0x06,0x00, 
				0x00,0x00, 0x00,0xea, 0x0f,0x78, 0x7f,0xe4, 0x83,0x20, 0x53,0x98, 0x88,0xcd, 0x87,0x0a,
				0x3a,0x61, 0xdc,0x10, 0x94,0x27, 0x22,0x65, 0xe6,0x8c, 0xe5,0x27, 0x84,0xf2, 0xe4,0xe4,
				0x0d,0x9d, 0xb2,0xfc, 0x8c,0xe9, 0x2f,0xad, 0xf4,0x23, 0x27,0x0d, 0x1c,0x3a, 0x69,0xde,
				0xb8,0x81, 0x11,0x43, 0xc6,0x0c, 0x1a,0x35, 0x6c,0xdc, 0xc0,0x91, 0x13,0x64, 0xa6,0x25,
				0xcf,0x80, 0x7f
		};
		printBytes(undeflate(toByteArray(y)));
	}

	public static void test3() {
		String t = "Description0123456789";
		System.out.println("1 x Description:");
		printBytes(deflate(t.getBytes()));
		System.out.println("2 x Description:");
		printBytes(deflate((t+t).getBytes()));
		System.out.println("3 x Description:");
		printBytes(deflate((t+t+t).getBytes()));
		System.out.println("4 x Description:");
		printBytes(deflate((t+t+t+t).getBytes()));
		System.out.println("5 x Description:");
		printBytes(deflate((t+t+t+t+t).getBytes()));
	}

	public static void test4() {
		int[] y = new int[] {
				0x00,0x05, 0x06,0x00, 
				0x00,0x00, 0x00,0xea, 0x0f,0x78, 0x7f,0xe4, 0x83,0x20, 0x53,0x98, 0x88,0xcd, 0x87,0x0a,
				0x3a,0x61, 0xdc,0x10, 0x94,0x27, 0x22,0x65, 0xe6,0x8c, 0xe5,0x27, 0x84,0xf2, 0xe4,0xe4,
				0x0d,0x9d, 0xb2,0xfc, 0x8c,0xe9, 0x2f,0xad, 0xf4,0x23, 0x27,0x0d, 0x1c,0x3a, 0x69,0xde,
				0xb8,0x81, 0x11,0x43, 0xc6,0x0c, 0x1a,0x35, 0x6c,0xdc, 0xc0,0x91, 0x13,0x64, 0xa6,0x25,
				0xcf,0x80, 0x7f
		};
		for (int i=0; i<y.length-10; i++) {
			System.out.println("Starting from index="+i);
			printBytes(undeflate(subarray(toByteArray(y), i)));
		}
	}
	
	
	
	public static void main(String[] args) {
		test3();
	}
	
}
