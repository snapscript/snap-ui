package org.snapscript.ui.chrome;

import lombok.SneakyThrows;
import org.snapscript.ui.Client;
import org.snapscript.ui.ClientContext;
import org.snapscript.ui.WindowIcon;
import org.snapscript.ui.WindowIconLoader;
import org.snapscript.ui.chrome.load.LibraryLoader;

import java.awt.*;
import java.net.URI;
import java.net.URL;

public class ChromeClient implements Client {

	@Override
	@SneakyThrows
	public void show(ClientContext context) {
		LibraryLoader.load();
		String address = context.getTarget();	
		String title = context.getTitle();
		String path = context.getIcon();
		WindowIcon icon = WindowIconLoader.loadIcon(path);
		String[] arguments = context.getArguments();
		final ChromeFrameListener listener = new ChromeLogListener();
		final ChromeFrame frame = new ChromeFrame(listener, URI.create(address), false, false, null, arguments);
		frame.setTitle(title);
		frame.setSize(800, 600);
		frame.setVisible(true);

		if (icon != null) {
			URL resource = icon.getResource();
			Image image = Toolkit.getDefaultToolkit().getImage(resource);

			frame.setIconImage(image);
		}
	}

}
