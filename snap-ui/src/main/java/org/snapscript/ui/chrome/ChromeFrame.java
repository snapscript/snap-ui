// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package org.snapscript.ui.chrome;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;

import javax.swing.JPanel;

import org.cef.CefApp;
import org.cef.CefApp.CefVersion;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.browser.CefRequestContext;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefRequestContextHandlerAdapter;
import org.cef.network.CefCookieManager;
import org.snapscript.ui.chrome.dialog.DevToolsDialog;
import org.snapscript.ui.chrome.dialog.DownloadDialog;
import org.snapscript.ui.chrome.handler.AppHandler;
import org.snapscript.ui.chrome.handler.ContextMenuHandler;
import org.snapscript.ui.chrome.handler.DragHandler;
import org.snapscript.ui.chrome.handler.JSDialogHandler;
import org.snapscript.ui.chrome.handler.KeyboardHandler;
import org.snapscript.ui.chrome.handler.MessageRouterHandler;
import org.snapscript.ui.chrome.handler.MessageRouterHandlerEx;
import org.snapscript.ui.chrome.handler.RequestHandler;
import org.snapscript.ui.chrome.ui.BrowserFrame;

public class ChromeFrame extends BrowserFrame {
    private static final long serialVersionUID = -2295538706810864538L;
    private final CefClient client_;
    private String errorMsg_ = "";
    private final CefCookieManager cookieManager_;

    public ChromeFrame(ChromeFrameListener listener, URI address, boolean osrEnabled, boolean transparentPaintingEnabled, String cookiePath, String[] args) {
        CefApp myApp;
        if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {
            // 1) CefApp is the entry point for JCEF. You can pass
            //    application arguments to it, if you want to handle any
            //    chromium or CEF related switches/attributes in
            //    the native world.
            CefSettings settings = new CefSettings();
            settings.windowless_rendering_enabled = osrEnabled;
            // try to load URL "about:blank" to see the background color
            settings.background_color = settings.new ColorType(100, 255, 242, 211);
            myApp = CefApp.getInstance(args, settings);

            CefVersion version = myApp.getVersion();
            System.out.println("Using:\n" + version);

            //    We're registering our own AppHandler because we want to
            //    add an own schemes (search:// and client://) and its corresponding
            //    protocol handlers. So if you enter "search:something on the web", your
            //    search request "something on the web" is forwarded to www.google.com
            CefApp.addAppHandler(new AppHandler(args));
        } else {
            myApp = CefApp.getInstance();
        }

        //    By calling the method createClient() the native part
        //    of JCEF/CEF will be initialized and an  instance of
        //    CefClient will be created. You can create one to many
        //    instances of CefClient.
        client_ = myApp.createClient();

        // 2) You have the ability to pass different handlers to your
        //    instance of CefClient. Each handler is responsible to
        //    deal with different informations (e.g. keyboard input).
        //
        //    For each handler (with more than one method) adapter
        //    classes exists. So you don't need to override methods
        //    you're not interested in.
        DownloadDialog downloadDialog = new DownloadDialog(this);
        client_.addContextMenuHandler(new ContextMenuHandler(this));
        client_.addDownloadHandler(downloadDialog);
        client_.addDragHandler(new DragHandler());
        client_.addJSDialogHandler(new JSDialogHandler());
        client_.addKeyboardHandler(new KeyboardHandler());
        client_.addRequestHandler(new RequestHandler(this));

        //    Beside the normal handler instances, we're registering a MessageRouter
        //    as well. That gives us the opportunity to reply to JavaScript method
        //    calls (JavaScript binding). We're using the default configuration, so
        //    that the JavaScript binding methods "cefQuery" and "cefQueryCancel"
        //    are used.
        CefMessageRouter msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new MessageRouterHandler(), true);
        msgRouter.addHandler(new MessageRouterHandlerEx(client_), false);
        client_.addMessageRouter(msgRouter);

        // 2.1) We're overriding CefDisplayHandler as nested anonymous class
        //      to update our address-field, the title of the panel as well
        //      as for updating the status-bar on the bottom of the browser
        client_.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
            	listener.onAddressChange(browser, frame, url);
            }
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                setTitle(title);
            }
            @Override
            public void onStatusMessage(CefBrowser browser, String value) {
            	listener.onStatusMessage(browser, value);
            }
        });

        // 2.2) To disable/enable navigation buttons and to display a prgress bar
        //      which indicates the load state of our website, we're overloading
        //      the CefLoadHandler as nested anonymous class. Beside this, the
        //      load handler is responsible to deal with (load) errors as well.
        //      For example if you navigate to a URL which does not exist, the
        //      browser will show up an error message.
        client_.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                    boolean canGoBack, boolean canGoForward) {
                listener.onLoadingStateChange(browser, isLoading, canGoBack, canGoForward);

                if (!isLoading && !errorMsg_.isEmpty()) {
                    browser.loadString(errorMsg_, address.toString());
                    errorMsg_ = "";
                }
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode,
                    String errorText, String failedUrl) {
                if (errorCode != ErrorCode.ERR_NONE && errorCode != ErrorCode.ERR_ABORTED) {
                    errorMsg_ = "<html><head>";
                    errorMsg_ += "<title>Error while loading</title>";
                    errorMsg_ += "</head><body>";
                    errorMsg_ += "<h1>" + errorCode + "</h1>";
                    errorMsg_ += "<h3>Failed to load " + failedUrl + "</h3>";
                    errorMsg_ += "<p>" + (errorText == null ? "" : errorText) + "</p>";
                    errorMsg_ += "</body></html>";
                    browser.stopLoad();
                }
            }
        });

        // 3) Before we can display any content, we require an instance of
        //    CefBrowser itself by calling createBrowser() on the CefClient.
        //    You can create one to many browser instances per CefClient.
        //
        //    If the user has specified the application parameter "--cookie-path="
        //    we provide our own cookie manager which persists cookies in a directory.
        CefRequestContext requestContext = null;
        if (cookiePath != null) {
            cookieManager_ = CefCookieManager.createManager(cookiePath, false);
            requestContext = CefRequestContext.createContext(new CefRequestContextHandlerAdapter() {
                @Override
                public CefCookieManager getCookieManager() {
                    return cookieManager_;
                }
            });
        } else {
            cookieManager_ = CefCookieManager.getGlobalManager();
        }
        CefBrowser browser = client_.createBrowser(
                address.toString(), osrEnabled, transparentPaintingEnabled, requestContext);
        setBrowser(browser);

        //    Last but not least we're setting up the UI for this example implementation.
        getContentPane().add(createContentPanel(), BorderLayout.CENTER);
    }
    
    public void showDevTools() {
        DevToolsDialog devToolsDlg = new DevToolsDialog(this, "DEV Tools", getBrowser());
        devToolsDlg.setVisible(true);
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        //contentPanel.add(control_pane_, BorderLayout.NORTH);

        // 4) By calling getUIComponen() on the CefBrowser instance, we receive
        //    an displayable UI component which we can add to our application.
        contentPanel.add(getBrowser().getUIComponent(), BorderLayout.CENTER);
        return contentPanel;
    }
}
