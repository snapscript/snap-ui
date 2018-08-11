package org.snapscript.ui;

import java.io.File;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientContext {
	
	private static final String ICON_PATH = "/resource/img/icon_large.png";

	private final ClientEngine engine;
	private final String title;
	private final String host;
	private final int port;
	private final boolean debug;
	private final String[] arguments;

	public String getTarget() {
		if(port != -1 && port != 80 && port != 0) {
			return String.format("http://%s:%s", host, port);
		}
		return String.format("http://%s", host); 
	}
	
	public String[] getArguments(){
		return arguments != null ? arguments : new String[]{};
	}
}
