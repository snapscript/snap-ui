package org.snapscript.ui;

import org.snapscript.ui.chrome.ChromeClient;

public class ClientProvider {

	public static Client provide(){
		return new ChromeClient();
	}
}
