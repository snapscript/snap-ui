package org.snapscript.ui;

public enum OperatingSystem {
   WINDOWS("win64"),
   MAC("mac"),
   LINUX("linux64");

   private final String code;

   private OperatingSystem(String code) {
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
   
   public static OperatingSystem resolveSystem() {
      OperatingSystem[] values = OperatingSystem.values();
      String system = System.getProperty("os.name");
      String token = system.toLowerCase();
      
      for(OperatingSystem os : values) {
         String name = os.name().toLowerCase();

         if(token.startsWith(name)) {
            return os;
         }
      }
      return WINDOWS;
   }
}
