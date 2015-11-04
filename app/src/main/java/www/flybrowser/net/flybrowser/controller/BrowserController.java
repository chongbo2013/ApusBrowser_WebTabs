/*
 * Copyright 2014 A.C.R. Development
 */
package www.flybrowser.net.flybrowser.controller;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebView;

import www.flybrowser.net.flybrowser.webview.FlyingView;

public interface BrowserController {

    void updateUrl(String title, boolean shortUrl);

    void updateProgress(int n);

    void updateHistory(String title, String url);

    void openFileChooser(ValueCallback<Uri> uploadMsg);

    void updateTabs();

    void onLongPress();

    void onShowCustomView(View view, CustomViewCallback callback);

    void onHideCustomView();

    Bitmap getDefaultVideoPoster();

    View getVideoLoadingProgressView();

    void onCreateWindow(Message resultMsg);

    void onCloseWindow(FlyingView view);

    void hideActionBar();

    void showActionBar();

    void longClickPage(String url);

    void openBookmarkPage(WebView view);

    void showFileChooser(ValueCallback<Uri[]> filePathCallback);

    void closeEmptyTab();

    boolean proxyIsNotReady();

   //  void updateBookmarkIndicator(String url);

}
