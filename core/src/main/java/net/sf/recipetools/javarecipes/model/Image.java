/*
 * Created on 24-10-2004
 *
 */
package net.sf.recipetools.javarecipes.model;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.InvalidParameterException;
import java.security.PrivilegedAction;

import javax.imageio.ImageIO;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author ft
 *
 */
//@Entity
public class Image extends NamedEntity {
	public static enum ImageFormat {JPEG, PNG, GIF};
	
	// soften factor used when resizing.
	float softenFactor = 0.05f;
	
	// save jpg with quality
	float jpegQuality = 0.8f;

	
	
	/** Hibernate ID: primary key */
	//@Id
	//@GeneratedValue
	Long id;

	/** the hibernate HBM version */
	//@Version
	int hbmVersion;
	
	//@Basic
	//@org.hibernate.annotations.Index(name = "IDX_UNIT_NAME")
	private String name;

	//@Basic
	private String url;
	
	//@ManyToOne( targetEntity = net.sf.recipetools.javarecipes.model.Recipe.class )
	//@JoinColumn(nullable = false)
	private Recipe		recipe;

	//@Transient
	private byte[] image;
	
	
	/**
	 * 
	 */
	public Image() {
		super();
	}

	public Image(int id, String name) {
		super(id, name);
	}
	public Image(String name) {
		super(name);
	}
	
	public Image(String name, String filename) {
		super(name);
		loadFromFile(filename);
	}

	/**
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
    @Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the hbmVersion
	 */
    @Override
	public int getHbmVersion() {
		return hbmVersion;
	}

	/**
	 * @param hbmVersion the hbmVersion to set
	 */
    @Override
	public void setHbmVersion(int hbmVersion) {
		this.hbmVersion = hbmVersion;
	}

	/**
	 * @return the name
	 */
    @Override
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
    @Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the recipe
	 */
	public Recipe getRecipe() {
		return recipe;
	}

	/**
	 * @param recipe the recipe to set
	 */
	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the image
	 */
	public byte[] getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(byte[] image) {
		this.image = image;
	}
	
	public void setImageFromBinaryString(String image) {
		this.image = image.getBytes();
	}
	
	/**
	 * Load the image from the given file
	 * @param file image file.
	 */
	public void setImageFromFile(File file) {
		if (file==null || ! file.canRead()) {
			throw new InvalidParameterException("The file was not found! file="+file);
		}
		
		long size = file.length();
		byte[] bytes = new byte[(int) size];
		
		FileInputStream fi = null;
		BufferedInputStream bis = null;
		try {
			fi = new FileInputStream(file);
			bis = new BufferedInputStream(fi);
			bis.read(bytes);
			setImage(bytes);
			setName(file.getName());
			
		} catch (FileNotFoundException e) {
			throw new InvalidParameterException("The file could not be read. file="+file);
		} catch (IOException e) {
			throw new InvalidParameterException("Could not read the complete file. File="+file);
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
				if (fi != null) {
					fi.close();
				}
			} catch (IOException e) {
				throw new InvalidParameterException("Could not close the file. File="+file);
			}
		}
	}

	/**
	 * Load the image from the given file
	 * @param file image file.
	 */
	public void setImageFromFile(String filename) {
		setImageFromFile(new File(filename));
	}	
	
	
	/**
	 * Set the image from a base64 coded string
	 */
	public void setImageFromBase64(String coded) {
		if (coded==null || coded.length()==0) {
			return;
		}
		
		image = Base64.decodeBase64(coded.getBytes());
	}
	
	/**
	 * Set the image from a hex coded string
	 */
	public void setImageFromHex(String coded) {
		if (coded==null || coded.length()==0) {
			return;
		}
		
		try {
			image = Hex.decodeHex(coded.toCharArray());
		} catch (DecoderException e) {
			throw new RecipeFoxException("Could not decode the image", e);
		}
	}

	/**
	 * Load the image from the given filename
	 * @param filename filename of the image
	 */
	public String loadFromFile(final String filename) {
		final Logger log = LoggerFactory.getLogger(Image.class);
		String result = AccessController.doPrivileged(new PrivilegedAction<String>() {
		    @Override
			public String run() {
				String msg = "";
				File file;
				try {
					file = new File(filename);
					setImageFromFile(file);
				} catch (RuntimeException e) {
					log.error("Could not find photo file: "+filename, e);
					msg = "Could not find photo file: "+filename+"\n"+e.getMessage();
				}
				return msg;
			}
		});
		return result;
	}
	
	/**
	 * Unload the image, and eventually free the memory 
	 */
	public void unload() {
		image = null;
	}
	
	public boolean isCorrupted() {
		if (! isValid()) return false;
		String type = getImageType();
		int last = image.length-1;
		//System.out.println("last5: "+image[last-4]+","+image[last-3]+","+image[last-2]+","+image[last-1]+","+image[last]);
		if ("JPG".equals(type)) {
			//  check for FF D9 end mark
			return ! (image[last-1]==-1 && image[last]==-39);
		} else if ("GIF".equals(type)) {
			return false;
		} else if ("PNG".equals(type)) {
			// verify the last chunk is IEND
			// see spec: http://www.libpng.org/pub/png/spec/1.2/PNG-Structure.html#Chunk-layout
			// http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html#C.IEND
			return ("IEND".equals(new String(image,last-7,4)));
		} else {
			return false;
		}
	}
	

	/**
	 * Save the the image as a temporary file  
	 * @return
	 */
	public File saveToTemp() {
		return saveToTemp(null);
	}
	
	/**
	 * Save the the image as a temporary file in the given directory  
	 * @return
	 */
	public File saveToTemp(File directory) {
		File tempFile = null; 
		
		// 
		String suffix = "image";
		if (name!=null && name.length()>0) {
			suffix = name.replaceAll(".*\\.", ".");
		}
		
		// create a temporary file with the image
		try {
			tempFile = File.createTempFile("image-", suffix, directory);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not save image to temp directory", e);
		}
		tempFile.deleteOnExit();
		saveAs(tempFile);
		return tempFile;
	}
	
	public void saveAs(String filename) {
		saveAs(new File(filename));
	}
	public void saveAs(File f) {
		if (image==null || image.length==0) {
			return;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			fos.write(image);
			fos.close();
		} catch (IOException e) {
			throw new RecipeFoxException("Could not save image as file: "+f, e);
		} finally {
			if (fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Scale the image with the given factor
	 * @param factor
	 */
	public void scale(float factor) {
		if (image==null || image.length==0) {
			return;
		}
		BufferedImage in;
		try {
			in = ImageIO.read(new ByteArrayInputStream(image));
		} catch (IOException e) {
			throw new RecipeFoxException("Could not read the image",e);
		}
		BufferedImage bdest = new BufferedImage(Math.round(in.getWidth()*factor), Math.round(in.getHeight()*factor), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bdest.createGraphics();
		// scale it 
		AffineTransform at = AffineTransform.getScaleInstance(factor,factor);
		g.drawRenderedImage(in,at);
		// soften it
		float[] softenArray = {0, softenFactor, 0, softenFactor, 1-(softenFactor*4), softenFactor, 0, softenFactor, 0};
		applyFilter(bdest, 3,3, softenArray);
		// encode as jpg
		image = encodeAsJpg(bdest);
	}
	
	/**
	 * Apply the given filter to the image
	 * @param bdest the buffered image
	 * @param width 
	 * @param height
	 * @param filterArray the filter
	 */
	public void applyFilter(BufferedImage bdest, int width, int height, float[] filterArray) {
		Kernel kernel = new Kernel(width, height, filterArray);
		ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		bdest = cOp.filter(bdest, null);
	}

	private byte[] encodeAsJpg(BufferedImage bdest) {
		return encodeAs(ImageFormat.JPEG, bdest);
	}
	/**
	 * @param bdest
	 * @return
	 */
	private byte[] encodeAs(ImageFormat format, BufferedImage bdest) {
		/* encodes image as a JPEG data stream */
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(bdest, format.name(), os);
		} catch (IOException e) {
			throw new RecipeFoxException("Could not encode image as "+format.name(), e);
		}
		return os.toByteArray();
	}

	/**
	 * Convert the image to the given format.
	 * @param format
	 */
	public void convertTo(ImageFormat format) {
		if (image==null || image.length==0) {
			return;
		}
		BufferedImage in = null;
		try {
			in = ImageIO.read(new ByteArrayInputStream(image));
		} catch (IOException e) {
			throw new RecipeFoxException("Could not read the image",e);
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(in, format.name(), os);
		} catch (IOException e1) {
			throw new RecipeFoxException("Could not convert the image to "+format.name(),e1);
		}
		image = os.toByteArray();
	}

	/**
	 * @return a string with a base64 encoded image.
	 */
	public String encodeAsBase64() {
		if (image==null || image.length==0) {
			return "";
		}
		
		byte[] encoded = Base64.encodeBase64(image);
		return new String(encoded);
	}

	/**
	 * Get the file type based on the file header
	 * @return
	 */
	public String getImageType() {
		if (image==null || image.length < 4) return "dummy";
		String header = new String(image,0,4);
		if ("PNG".equals(header.substring(1,4))) {
			return "PNG";
		} else if ("LEAD".equals(header)) {
			return "CMP";
		} else if ("GIF8".equals(header)) {
			return "GIF";
		} else if ("BM".equals(header.substring(0, 2))) {
			return "BMP";
		} else {
			return "JPG";
		}
	}
	
	/**
	 * Test if it is a valid image
	 * @return
	 */
	public boolean isValid() {
		return image != null
			   && image.length > 4;
	}
	
	
}
