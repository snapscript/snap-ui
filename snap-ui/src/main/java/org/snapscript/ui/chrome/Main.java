package org.snapscript.ui.chrome;

import org.snapscript.ui.chrome.load.LibraryLoader;

public class Main {

	public static void main(String[] list) throws Exception {
		LibraryLoader.load();
		MainFrame.main(list);
	}
}
