package org.snapscript.ui.chrome.load;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class LibraryLoader {

   public static void loadFrom(String folder) {
      int slash = folder.indexOf(File.separatorChar);
      boolean exists = new File(folder).exists();

      if(slash != -1 || exists) {
         File directory = new File(folder);
         loadFromPath(directory);
      } else {
         String home = System.getProperty("user.home");
         File directory = new File(home, folder);
         loadFromPath(directory);
      }
   }

   public static void loadFromPath(File directory) {
      try {
         File location = LibraryExtractor.extractTo(directory);
         String[] path = expandPath(location);
         Field field = ClassLoader.class.getDeclaredField("usr_paths");

         field.setAccessible(true);
         field.set(null, path);
      } catch (Exception e) {
         throw new IllegalStateException("Could not load library from " + directory, e);
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
