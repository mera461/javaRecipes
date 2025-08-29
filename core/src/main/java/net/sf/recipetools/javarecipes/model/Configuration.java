/**
 * 
 */
package net.sf.recipetools.javarecipes.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

/**
 * @author Frank
 *
 */
public class Configuration {

	private static ThreadLocal<Properties> localProperties = new ThreadLocal<Properties>() {
		@Override protected Properties initialValue() {
			return new Properties(); 
		};
	};
	
	static {
	    InputStream is = Configuration.class.getResourceAsStream("/commas.txt");
	    if (is!=null) {
	        loadSafeCommaStructuresFromFile(is);
	    }
	}
	
	/**
	 * Add all the lines from the given file to the safeCommaStructures
	 * Empty lines and lines starting with '#' are ignored
	 * @param filename the file to read.
	 */
	public static void loadSafeCommaStructuresFromFile(String filename) {
		File file = new File(filename);
		if (! file.canRead()) {
			throw new RecipeFoxException("Could not read the file:"+ filename);
		}
		try (InputStream is = new FileInputStream(file)) {
		    loadSafeCommaStructuresFromFile(is);
		} catch (IOException e) {
            throw new RecipeFoxException("Error reading the file: "+filename, e);
        } 
	}
		
    public static void loadSafeCommaStructuresFromFile(InputStream is) {
    	List<String> conf = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line = null;
			while ((line=reader.readLine()) != null) {
				// skip empty lines and lines starting with #
				if (line.trim().length()==0
					|| line.charAt(0) == '#') {
					continue;
				}
				conf.add(line.trim());
			}
			RecipeIngredient.setSafeCommaStructures(conf);
		} catch (IOException e) {
			throw new RecipeFoxException("Error reading the file.", e);
		}
	}
	
	private Configuration() {
	    // to hide the default public constructor
	}
	
	public static void loadFromFile(String name) {
		try {
			FileInputStream fis = new FileInputStream(name);
			localProperties.get().load(fis);
			// Configuration.class.getClassLoader().getResourceAsStream(name)
		} catch (IOException e) {
			throw new RecipeFoxException("Could not read the configuration file: "+name, e);
		}
	}

	public static int getIntProperty(String name) {
		String value = localProperties.get().getProperty(name);
		if (value==null) {
			return 0;
		}
		int result = 0;
		
		try {
			result = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			result = 0;
		}
		return result;
	}

	public static boolean getBooleanProperty(String name) {
		String value = localProperties.get().getProperty(name);
		if (value==null) {
			return false;
		}
		boolean result = false;
		
		try {
			result = Boolean.parseBoolean(value);
		} catch (NumberFormatException e) {
			result = false;
		}
		return result;
	}


	public static String getStringProperty(String name) {
		String value = localProperties.get().getProperty(name);
		if (value==null) {
			value = "";
		}
		return value;
	}
	
	public static void setIntProperty(String name, int value) {
		localProperties.get().setProperty(name, Integer.toString(value));
	}
	
	public static void setBooleanProperty(String name, boolean value) {
		localProperties.get().setProperty(name, Boolean.toString(value));
	}
	
	public static void setStringProperty(String name, String value) {
		localProperties.get().setProperty(name, value);
	}
	
	public static void clear() {
		localProperties.get().clear();
	}
	
	public static void fromJson(String json) {
		ObjectMapper mapper =  JsonFactory.create();
		Map<String, Object> configMap = mapper.readValue(json, Map.class);
		for(String key : configMap.keySet()){
		    Object value = configMap.get(key);
		    if (value.getClass() == Boolean.class) {
		    	Configuration.setBooleanProperty(key, (Boolean)value);
		    } else if (value.getClass() == String.class) {
		    	Configuration.setStringProperty(key, (String)value);
		    } else if (value.getClass() == Integer.class) {
		    	Configuration.setIntProperty(key, (Integer)value);
		    }
		}		
		
	}
	
	public static String dump() {
	    StringBuilder result = new StringBuilder();
	    for (Object s: localProperties.get().keySet()) {
	        String key = (String) s;
	        Object value = localProperties.get().getProperty(key);
	        result.append(key+"="+value+"\n");
	    }
	    return result.toString();
	}
}
