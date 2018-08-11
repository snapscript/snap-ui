package org.snapscript.ui;

import java.net.URI;

public class ClientLauncher {

	public static void main(String[] list) throws Exception {
      ClientContext context = ClientContext.builder()
         .debug(true)
         .title("Browser")
         .host(URI.create(list[0]).getHost())
         .port(URI.create(list[0]).getPort())
         .arguments(list)
         .build();
		ClientProvider.provide(ClientEngine.JAVAFX).show(context);
	}
}
