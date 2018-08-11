package org.snapscript.ui;

import java.io.File;
import java.net.URI;

public class ClientLauncher {

	public static void main(String[] list) throws Exception {
		String temp = System.getProperty("java.io.tmpdir");
		File log = new File(temp, "/cef.log");
		File cache = new File(temp);

      ClientContext context = ClientContext.builder()
         .debug(true)
         .title("Browser")
         .host(URI.create(list[0]).getHost())
         .port(URI.create(list[0]).getPort())
			.logFile(log)
			.cachePath(cache)
         .arguments(list)
         .build();

		ClientProvider.provide(ClientEngine.CEF).show(context);
	}
}
