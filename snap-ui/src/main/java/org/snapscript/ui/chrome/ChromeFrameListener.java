package org.snapscript.ui.chrome;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;

public interface ChromeFrameListener {
	void onAddressChange(CefBrowser browser, CefFrame frame, String location);
	void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward);
    void onStatusMessage(CefBrowser browser, String value);
}
