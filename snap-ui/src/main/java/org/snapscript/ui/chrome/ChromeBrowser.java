package org.snapscript.ui.chrome;

import java.io.File;
import java.net.URI;

import org.cef.OS;
import org.snapscript.ui.chrome.load.LibraryLoader;

public class ChromeBrowser {

	public static void main(String[] list) throws Exception {
		LibraryLoader.load();
		show(list);
	}
	
	public static void show(String[] args) throws Exception {
        // OSR mode is enabled by default on Linux.
        // and disabled by default on Windows and Mac OS X.
        boolean osrEnabledArg = OS.isLinux();
        boolean transparentPaintingEnabledArg = false;
        String cookiePath = null;
        for (String arg : args) {
            arg = arg.toLowerCase();
            if (!OS.isLinux() && arg.equals("--off-screen-rendering-enabled")) {
                osrEnabledArg = true;
            } else if (arg.equals("--transparent-painting-enabled")) {
                transparentPaintingEnabledArg = true;
            } else if (arg.startsWith("--cookie-path=")) {
                cookiePath = arg.substring("--cookie-path=".length());
                File testPath = new File(cookiePath);
                if (!testPath.isDirectory() || !testPath.canWrite()) {
                    System.out.println("Can't use " + cookiePath
                            + " as cookie directory. Check if it exists and if it is writable");
                    cookiePath = null;
                } else {
                    System.out.println("Storing cookies in " + cookiePath);
                }
            }
        }
        // MainFrame keeps all the knowledge to display the embedded browser
        // frame.
        final ChromeFrameListener listener = new ChromeLogListener();
        final ChromeFrame frame = new ChromeFrame(listener, URI.create("http://www.google.com"), osrEnabledArg, transparentPaintingEnabledArg, cookiePath, args);
        frame.setSize(800, 600);
        frame.setVisible(true);
	}
}
