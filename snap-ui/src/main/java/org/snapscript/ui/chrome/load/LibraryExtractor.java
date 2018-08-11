package org.snapscript.ui.chrome.load;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LibraryExtractor {
	
	public static void main(String s[])  throws Exception{
		LibraryExtractor.extractToHome();
	}
	
	public static File extractToHome() throws Exception {
		File f = new File(System.getProperty("user.home"), ".cef");
		extractTo(f);
		return f;
	}
	
	public static File extractTo(File location) throws Exception {
		LibraryType os = LibraryType.resolveSystem();
		listFiles(os).forEach(e -> {
			try{
				File file = new File(location, e.path);
				log.info("Writing to "+file.getCanonicalPath());
				writeTo(e.resource, file);
			} catch(Exception ex){
				log.error("Error writing ", ex);
			}
		});		
		return location;
	}
	
	public static void writeTo(URL resource, File location) throws Exception {
		File parent = location.getParentFile();
		if(parent.isFile()){
			parent.delete();
		} 
		parent.mkdirs();
		FileOutputStream out = new FileOutputStream(location);
		try{
			InputStream in = resource.openStream();
			byte[] b = new byte[1024*8];
			int count = 0;
			while((count = in.read(b)) !=-1) {
				out.write(b, 0, count);
			}
			in.close();
		}finally{		
			out.close();
		}
		
	}
	
	public static List<Entry> listFiles(LibraryType os) throws Exception {
        URL root = locateRoot(os);
        if(root == null) {
        	throw new IllegalArgumentException("No library for " + os);
        }
		String path = "/" + os.name().toLowerCase();
        URI uri = root.toURI();
        final Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            myPath = fileSystem.getPath(path);
        } else {
            myPath = Paths.get(uri);
        }
        return Files.walk(myPath, 2).map(x -> {
	        	return relative(path, myPath, x);
	        })
	        .filter(e -> e.resource != null)
	        .collect(Collectors.toList());
  
	}
	
	public static URL locateRoot(LibraryType os) {
		String name = os.name().toLowerCase();
        URL uri = LibraryExtractor.class.getResource(name);
        if(uri == null) {
        	return LibraryExtractor.class.getResource("/" + name);
        }
        return uri;
	}
	
	
	public static Entry relative(String prefix, Path from, Path path) {
		String x = from.relativize(path).toString().replace(File.separatorChar, '/');
		return new Entry(LibraryExtractor.class.getResource(prefix + "/"+x), x);
	}
	
	@AllArgsConstructor
	private static class Entry {
		private final URL resource;
		private final String path;
	}
	

}