package org.snapscript.ui.chrome;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromeLogListener implements ChromeFrameListener {
	
	@Override
	public void onAddressChange(CefBrowser browser, CefFrame frame, String location) {
		log.info("onAddressChange({})", location);		
	}

	@Override
	public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
		log.info("onLoadingStateChange(isLoading={})", isLoading);
	}

	@Override
	public void onStatusMessage(CefBrowser browser, String message) {
		log.info("onStatusMessage({})", message);
	}

}
