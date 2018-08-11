package org.snapscript.ui;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.net.URI;

@Data
@Builder
public class ClientContext {

	public static final String ICON_PATH = "/icon/icon_large.png";
	public static final String HOME_FOLDER = ".cef";

	private final File cachePath;
	private final File logFile;
	private final String folder; // home folder
	private final String title;
	private final String host;
	private final String icon;
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

	public String getIcon() {
		return icon != null ? icon : ICON_PATH;
	}

	public String getFolder() {
		return folder != null ? folder : HOME_FOLDER;
	}

	public void validate() {
		String address = getTarget();
		String title = getTitle();
		File logFile = getLogFile();
		File cachePath = getCachePath();

		if(title == null) {
			throw new IllegalStateException("Title is required");
		}
		if(address == null) {
			throw new IllegalStateException("Address is required");
		}
		try {
			URI.create(address);
		} catch(Exception e) {
			throw new IllegalStateException("Address " + address + " is invalid");
		}
		if(cachePath != null) {
			cachePath.mkdirs();
		}
		if(logFile != null) {
			logFile.getParentFile().mkdirs();
		}
	}
}
