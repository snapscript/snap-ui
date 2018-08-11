package org.snapscript.ui.chrome.load;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class LibraryLoader {

	public static void load()  {
		try {
			File location = LibraryExtractor.extractToHome();
			String[] path = expandPath(location);
	        Field field = ClassLoader.class.getDeclaredField("usr_paths");
	        
	        field.setAccessible(true);
	        field.set(null, path);
		}catch(Exception e){
			throw new IllegalStateException(e);
		}
	}
	
	private static String[] expandPath(File location) throws Exception {
		String path = location.getCanonicalPath();
		String current = System.getProperty("java.library.path");
		String expanded = current + File.pathSeparator + path;
		
		System.setProperty("java.library.path", expanded);
		String[] parts = expanded.split(File.pathSeparator);
		return Arrays.asList(parts)
				.stream()
				.filter(Objects::nonNull)
				.filter(entry -> !entry.isEmpty())
				.toArray(String[]::new);
	}
}
