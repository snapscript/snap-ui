package org.snapscript.ui.chrome;

import java.net.URI;

import org.snapscript.ui.Client;
import org.snapscript.ui.ClientContext;
import org.snapscript.ui.chrome.load.LibraryLoader;

public class ChromeClient implements Client {

	@Override
	public void show(ClientContext context) {
		LibraryLoader.load();
		String address = context.getTarget();	
		String title = context.getTitle();
		String[] arguments = context.getArguments();
        final ChromeFrameListener listener = new ChromeLogListener();
        final ChromeFrame frame = new ChromeFrame(listener, URI.create(address), false, false, null, arguments);
        frame.setTitle(title);
        frame.setSize(800, 600);
        frame.setVisible(true);
	}

}
