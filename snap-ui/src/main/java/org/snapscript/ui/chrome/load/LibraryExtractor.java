package org.snapscript.ui.chrome.load;

import java.io.ByteArrayOutputStream;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.snapscript.ui.OperatingSystem;

@Slf4j
public class LibraryExtractor {

   public static final String CEF_VERSION = "3.3396.1775.g5340bb0";
   public static final String CEF_REQUIRED_LIST = "required.list";

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
      List<String> resources = locateRequiredResources(path);

      return resources.stream()
              .map(resource -> loadDependency(path, resource))
              .filter(dependency -> dependency.isValid())
              .collect(Collectors.toList());
   }

   @SneakyThrows
   private static List<String> locateRequiredResources(String path) {
      URL list = locateResource(path + "/" + CEF_REQUIRED_LIST);

      if (list == null) {
         throw new IllegalArgumentException("No library found at " + path);
      }
      InputStream source = list.openStream();

      try {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         byte[] chunk = new byte[1024];
         int count = 0;

         while ((count = source.read(chunk)) != -1) {
            buffer.write(chunk);
         }
         String[] lines = buffer.toString().split("\\r?\\n");
         return Arrays.asList(lines)
                 .stream()
                 .filter(Objects::nonNull)
                 .map(text -> text.trim())
                 .filter(text -> !text.isEmpty() && !text.startsWith("#"))
                 .collect(Collectors.toList());
      } finally {
         source.close();
      }
   }

   private static URL locateResource(String path) {
      String normal = path.replace("//", "/");
      URL resource = LibraryExtractor.class.getResource(normal);

      if (resource == null) {
         if (normal.startsWith("/")) {
            normal = normal.substring(1);
         } else {
            normal = "/" + normal;
         }
         return LibraryExtractor.class.getResource(normal);
      }
      return resource;
   }


   private static LibraryDependency loadDependency(String prefix, String path) {
      String normal = path.startsWith("/") ? path.substring(1) : path;
      URL resource = locateResource(prefix + "/" + normal);
      return new LibraryDependency(resource, normal);
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