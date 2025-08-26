/**
 * 
 */
package net.sf.recipetools.javarecipes.model;

import java.lang.reflect.Method;
import java.util.prefs.Preferences;

/**
 * @author Frank
 * 
 */
public class WindowsRegistry {

	//private static final int HKEY_CURRENT_USER = 0x80000001;
	//private static final int KEY_QUERY_VALUE = 1;
	//private static final int KEY_SET_VALUE = 2;
	private static final int KEY_READ = 0x20019;

	boolean isWindows = false;
	public Preferences userRoot = null;
	public Preferences systemRoot = null;

	// methods to access the registry:
	Method openKey = null;
	Method closeKey = null;
	Method winRegQueryValue = null;
	Method winRegEnumValue = null;
	Method winRegQueryInfo = null;

	public WindowsRegistry() {
		// check if we are on windows
		isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
		if (! isWindows) return;
		
		// get root keys
		userRoot = Preferences.userRoot();
		systemRoot = Preferences.systemRoot();
		Class<? extends Preferences> clz = userRoot.getClass();
		try {
			openKey = clz.getDeclaredMethod("openKey", byte[].class, int.class, int.class);
			openKey.setAccessible(true);

			closeKey = clz.getDeclaredMethod("closeKey", int.class);
			closeKey.setAccessible(true);

			winRegQueryValue = clz.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class);
			winRegQueryValue.setAccessible(true);
			winRegEnumValue = clz.getDeclaredMethod("WindowsRegEnumValue1", int.class, int.class, int.class);
			winRegEnumValue.setAccessible(true);
			winRegQueryInfo = clz.getDeclaredMethod("WindowsRegQueryInfoKey1", int.class);
			winRegQueryInfo.setAccessible(true);
		} catch (SecurityException e) {
			isWindows = false;
			throw new RecipeFoxException("Could not get access to Windows Registry.",e);
		} catch (NoSuchMethodException e) {
			isWindows = false;
			throw new RecipeFoxException("Could not find the method to access Windows Registry.",e);
		}
	}

	public String getKey(Preferences root, String key) {
		if (! isWindows) return "";
		byte[] valb = null;
		Integer handle = -1;
		
		// split into dir and name
		String[] parts=key.split("\\\\", -1);
		String name = parts[parts.length-1];
		String dir = key.replaceAll("\\\\"+name+"$", "");
		

		// Query Internet Settings for Proxy
		String result = ""; 
		try {
			handle = (Integer) openKey.invoke(root, toCstr(dir), KEY_READ, KEY_READ);
			valb = (byte[]) winRegQueryValue.invoke(root, handle.intValue(), toCstr(name));
			result = (valb != null ? new String(valb).trim() : null);
			closeKey.invoke(root, handle);
		} catch (Exception e) {
			if (handle != -1) {
				try {
					closeKey.invoke(root, handle);
				} catch (Exception e1) {
				}
			}
			throw new RecipeFoxException("Could not get key: "+key, e);
		}
		return result;
	}
	
	/**
	 * @return The path to the Living Cookbook exe file.
	 */
	public String getLivingCookbookPath() {
		if (! isWindows) return null;
		String s1 = getKey(systemRoot, "SOFTWARE\\Classes\\.FDX\\");
		String s2 = getKey(systemRoot, "SOFTWARE\\Classes\\"+s1+"\\Shell\\Open\\Command\\");
		String result = s2.replaceAll("\"", "")
						  .replaceAll("\\s*%1\\s*", "");
		return result;
	}
	
	/**
	 * @return The path to the Mastercook exe file.
	 */
	public String getMastercookPath() {
		if (! isWindows) return null;
		String result = getKey(systemRoot, "SOFTWARE\\Classes\\SystemFileAssociations\\.mx2\\DefaultIcon\\");
		
		if (result!=null && result.length()>0) {
			result = result.replaceAll("\"", "")
						   .replaceAll(",\\d+$", "");
		}
		/*
		String prefix = "SOFTWARE\\Valusoft\\Mastercook\\";
		String postfix = ".0\\LaunchFile";
		// or use "HKEY_CLASSES_ROOT\SystemFileAssociations\.mx2\DefaultIcon"
		// or HKEY_LOCAL_MACHINE\SOFTWARE\Classes\SystemFileAssociations
		String result = "";
		for (int i=15; i>0; i--) {
			result = getKey(systemRoot, prefix+ new Integer(i)+postfix);
			if (result != null) break; 
		}
		*/
		return result;
	}

	/**
	 * Convert a java string to a C string (zero terminated byte array)
	 * @param str the string to convert
	 * @return the corresponding C string
	 */
	private static byte[] toCstr(String str) {
		byte[] result = new byte[str.length() + 1];
		for (int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[str.length()] = 0;
		return result;
	}
}
