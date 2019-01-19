package org.snapscript.ui;

public interface ClientControl {
   void registerListener(ClientCloseListener listener);
   void showDebugger();
}
