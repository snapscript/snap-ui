package org.snapscript.ui;

public enum ClientEngine {
   CEF("cef"),
   JAVAFX("javafx");
   
   public final String name;
   
   private ClientEngine(String name) {
      this.name = name;
   }
   
   public boolean isChromium() {
      return this == CEF;
   }
   
   public boolean isJavaFX() {
      return this == JAVAFX;
   }
   
   public static ClientEngine resolveEngine(String token) {
      if(token != null) {
    	  ClientEngine[] engines = ClientEngine.values();
         
         for(ClientEngine engine : engines) {
            if(engine.name.equalsIgnoreCase(token)) {
               return engine;
            }
         }
      }
      return JAVAFX;
   }
}

