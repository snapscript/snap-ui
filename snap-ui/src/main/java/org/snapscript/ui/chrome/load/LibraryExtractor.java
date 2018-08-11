package org.snapscript.ui.chrome.load;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.snapscript.ui.OperatingSystem;

@Slf4j
public class LibraryExtractor {

   public static final String CEF_VERSION = "3.3396.1775.g5340bb0";

   public static File extractTo(File location) throws Exception {
      File root = new File(location, "cef/" + CEF_VERSION);

      if(root.exists()) {
         root.mkdirs();
      }
      extractToPath(root);
      return root;
   }

   private static void extractToPath(File location) throws Exception {
      OperatingSystem os = OperatingSystem.resolveSystem();

      listFiles(os).forEach(dependency -> {
         File file = dependency.getLocation(location);
         File parent = file.getParentFile();
         URL resource = dependency.getResource();

         try {
            if (dependency.isJar()) {
               String path = file.toString();
               LibraryClassPathExtender.updateClassPath(path);
            }
            if (parent.isFile()) {
               parent.delete();
            }
            log.info("Writing to " + file);
            parent.mkdirs();

            if(file.isDirectory()) {
               log.info("Ignoring directory " + file);
            } else {
               writeTo(resource, file);
            }
         } catch (Exception e) {
            log.error("Error writing to " + file, e);
         }
      });
   }

   private static void writeTo(URL resource, File location) throws Exception {
      OutputStream out = new FileOutputStream(location);

      try {
         InputStream source = resource.openStream();
         byte[] buffer = new byte[1024 * 8];
         int count = 0;

         while ((count = source.read(buffer)) != -1) {
            out.write(buffer, 0, count);
         }
         source.close();
      } finally {
         out.close();
      }
   }

   private static List<LibraryDependency> listFiles(OperatingSystem os) throws Exception {
      String path = "/" + os.name().toLowerCase();
      Path root = locateRoot(path);

      return Files.walk(root, 10).map(dependency -> loadDependency(path, root, dependency))
              .filter(dependency -> dependency.isValid())
              .collect(Collectors.toList());
   }

   @SneakyThrows
   private static Path locateRoot(String path) {
      URL root = locateResource(path);

      if (root == null) {
         throw new IllegalArgumentException("No library found at " + path);
      }
      URI uri = root.toURI();

      if (uri.getScheme().equals("jar")) {
         FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
         return  fileSystem.getPath(path);
      }
      return Paths.get(uri);
   }

   private static URL locateResource(String path) {
      URL resource = LibraryExtractor.class.getResource(path);

      if (resource == null) {
         if (path.startsWith("/")) {
            path = path.substring(1);
         } else {
            path = "/" + path;
         }
         return LibraryExtractor.class.getResource(path);
      }
      return resource;
   }


   private static LibraryDependency loadDependency(String prefix, Path from, Path path) {
      String relative = from.relativize(path).toString().replace(File.separatorChar, '/');
      URL resource = locateResource(prefix + "/" + relative);

      return new LibraryDependency(resource, relative);
   }

   @Data
   @AllArgsConstructor
   private static class LibraryDependency {
      private final URL resource;
      private final String path;

      public boolean isJar() {
         return path.endsWith(".jar");
      }

      public boolean isValid(){
         return resource != null;
      }

      @SneakyThrows
      public File getLocation(File root) {
         return new File(root, path).getCanonicalFile();
      }
   }


}