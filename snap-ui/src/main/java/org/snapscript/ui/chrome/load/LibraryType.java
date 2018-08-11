package org.snapscript.ui.chrome.load;

public enum LibraryType {
   WINDOWS("win64"),
   MAC("mac"),
   LINUX("linux64");

   private final String code;

   private LibraryType(String code) {
      this.code = code;
   }
   
   public String getCode() {
      return code;
   }
   
   public boolean isWindows() {
      return this == WINDOWS;
   }
   
   public boolean isLinux() {
      return this == LINUX;
   }
   
   public boolean isMac() {
      return this == MAC;
   }
   
   public static LibraryType resolveSystem() {
      LibraryType[] values = LibraryType.values();
      String system = System.getProperty("os.name");
      String token = system.toLowerCase();
      
      for(LibraryType os : values) {
         if(token.startsWith(os.name().toLowerCase())) {
            return os;
         }
      }
      return WINDOWS;
   }
}
