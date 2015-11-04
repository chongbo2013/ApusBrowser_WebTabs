package www.flybrowser.net.flybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import www.flybrowser.net.flybrowser.constant.Constants;
import www.flybrowser.net.flybrowser.constant.HistoryPage;
import www.flybrowser.net.flybrowser.controller.BrowserController;
import www.flybrowser.net.flybrowser.database.HistoryDatabase;
import www.flybrowser.net.flybrowser.object.ClickHandler;
import www.flybrowser.net.flybrowser.preference.PreferenceManager;
import www.flybrowser.net.flybrowser.utils.ProxyUtils;
import www.flybrowser.net.flybrowser.utils.UrlUtils;
import www.flybrowser.net.flybrowser.utils.Utils;
import www.flybrowser.net.flybrowser.utils.WebUtils;
import www.flybrowser.net.flybrowser.view.AnimatedProgressBar;
import www.flybrowser.net.flybrowser.view.SearchFrameLayout;
import www.flybrowser.net.flybrowser.view.StickFrameLayout;
import www.flybrowser.net.flybrowser.view.StickyScrollView;
import www.flybrowser.net.flybrowser.view.stickhelp.StickyScrollViewCallbacks;
import www.flybrowser.net.flybrowser.view.stickhelp.StickyScrollViewGlobalLayoutListener;
import www.flybrowser.net.flybrowser.webview.FlyingView;

/**
 * Created by ferris on 2015/10/23.
 */
public abstract class BrowserActivity extends AppCompatActivity implements BrowserController,View.OnClickListener{

    private StickyScrollView browser_scrollview_layout;
    private StickFrameLayout top_titlerbar_layout;
    private FrameLayout top_titlebar_layout_search;
    private View browser_scrollview_head_search_bar;
    private int titlebar_hight=0,head_hight=0,max_scrolly=0;
    private StickyScrollViewCallbacks mCallbacks;
    private FrameLayout main_container,webview_container;

    //search view
    private SearchFrameLayout searchFrameLayout;


    private final List<FlyingView> mWebViewList = new ArrayList<>();
    private FlyingView mCurrentView;


    // Full Screen Video Views
    private FrameLayout mFullscreenContainer;
    private VideoView mVideoView;
    private View mCustomView;

    private final PreferenceManager mPreferences = PreferenceManager.getInstance();

    // Primatives
    private boolean mFullScreen=false, mColorMode=false, mDarkTheme=false,
            mIsNewIntent = false,
            mIsFullScreen = false,
            mIsImmersive = false,
            mShowTabsInDrawer=false;

    private static final FrameLayout.LayoutParams MATCH_PARENT = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);

    private static final int API = android.os.Build.VERSION.SDK_INT;
    private ClickHandler mClickHandler;

    private HistoryDatabase mHistoryDatabase;
    // Proxy
    private ProxyUtils mProxyUtils;

    private int mIdGenerator;

    private String mSearchText;

    private TextView tv_input;

    private AnimatedProgressBar mProgressBar;

    private String mUntitledTitle;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;

    private String mCameraPhotoPath;

    private int mOriginalOrientation;

    private static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

    private WebChromeClient.CustomViewCallback mCustomViewCallback;


    public static int TITLE_BAR_HIGHT=0;
//    BookmarksDialogBuilder mBookmarksDialogBuilder;

    private LinearLayout buttom_titlerbar_layout;
    private ImageView menu_forward,menu_back,menu_more,menu_tab,menu_home;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        init();
        initialize();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.menu_forward:
                if(isHomePage){
                    switchView(ViewType.WEB);
                }else if (mCurrentView != null&&mCurrentView.canGoForward()) {
                        mCurrentView.goForward();
                    }

                break;
            case R.id.menu_back:
                if (mCurrentView!=null&&mCurrentView.canGoBack()) {
                    mCurrentView.goBack();
                }else if(!isHomePage){
                    switchView(ViewType.HOME);
                }
                break;
            case R.id.menu_more:
                break;
            case R.id.menu_tab:
                break;
            case R.id.menu_home:
                if(!isHomePage){
                    switchView(ViewType.HOME);
                }
                break;
        }
    }

    private void findView() {
        TITLE_BAR_HIGHT=getResources().getDimensionPixelSize(R.dimen.titlebar_hight);
        browser_scrollview_layout=(StickyScrollView)findViewById(R.id.browser_scrollview_layout);
        top_titlerbar_layout=(StickFrameLayout)findViewById(R.id.top_titlerbar_layout);
        browser_scrollview_head_search_bar=findViewById(R.id.browser_scrollview_head_search_bar);
        mCallbacks = new StickyScrollViewCallbacks(top_titlerbar_layout, browser_scrollview_head_search_bar,
                null, browser_scrollview_layout);
        mCallbacks.setEnableSticky(true);
        browser_scrollview_layout.addCallbacks(mCallbacks);
        browser_scrollview_layout.getViewTreeObserver().addOnGlobalLayoutListener(
                new StickyScrollViewGlobalLayoutListener(mCallbacks));
        top_titlerbar_layout.setScrollView(browser_scrollview_layout);

        //test main containter
        main_container=(FrameLayout)findViewById(R.id.main_container);
        webview_container=(FrameLayout)findViewById(R.id.webview_container);
        top_titlebar_layout_search=(FrameLayout)findViewById(R.id.top_titlebar_layout_search);


        tv_input=(TextView)top_titlebar_layout_search.findViewById(R.id.tv_input);
        tv_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
            }
        });
        mProgressBar=(AnimatedProgressBar)top_titlebar_layout_search.findViewById(R.id.mProgressBar);

        buttom_titlerbar_layout=(LinearLayout)findViewById(R.id.buttom_titlerbar_layout);
        menu_forward=(ImageView)buttom_titlerbar_layout.findViewById(R.id.menu_forward);
        menu_back=(ImageView)buttom_titlerbar_layout.findViewById(R.id.menu_back);
        menu_more=(ImageView)buttom_titlerbar_layout.findViewById(R.id.menu_more);
        menu_tab=(ImageView)buttom_titlerbar_layout.findViewById(R.id.menu_tab);
        menu_home=(ImageView)buttom_titlerbar_layout.findViewById(R.id.menu_home);
        menu_forward.setOnClickListener(this);
        menu_back.setOnClickListener(this);
        menu_more.setOnClickListener(this);
        menu_tab.setOnClickListener(this);
        menu_home.setOnClickListener(this);
    }

    //初始化配置
    private void init() {
        head_hight=getResources().getDimensionPixelSize(R.dimen.browser_scrollview_head_fl_hight);
        titlebar_hight=getResources().getDimensionPixelSize(R.dimen.titlebar_hight);
        max_scrolly=head_hight-titlebar_hight;
        top_titlerbar_layout.setMaxScroll(max_scrolly);
        browser_scrollview_layout.setMaxScroll(max_scrolly);

        //init search view
        searchFrameLayout=SearchFrameLayout.getXml(this);

        mDarkTheme = false;
        mUntitledTitle = getString(R.string.untitled);
    }


    private synchronized void initialize() {
        mProxyUtils = ProxyUtils.getInstance();
        mWebViewList.clear();
        mClickHandler = new ClickHandler(this);
        mHistoryDatabase = HistoryDatabase.getInstance();
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                initializeSearchSuggestions(mSearch);
//            }
//
//        }).run();

        if (API <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
        }

        initializeTabs();
        mProxyUtils.checkForProxy(this);
    }

    public abstract boolean isIncognito();
    public abstract void updateCookiePreference();
    public abstract void closeActivity();
    public abstract void initializeTabs();

    /**
     * 显示搜索
     */
    public void openSearch(){
        if(main_container.indexOfChild(searchFrameLayout)==-1){
            main_container.addView(searchFrameLayout);
            searchFrameLayout.open(mCurrentView!=null?mCurrentView.getUrl():null);
        }
    }

    public void removeSearch(){
        if(main_container.indexOfChild(searchFrameLayout)!=-1){
            main_container.removeView(searchFrameLayout);
        }
    }

    //第一次桌面初始化走这里
    void restoreOrNewTab() {
        mIdGenerator = 0;
        String url = null;
        if (getIntent() != null) {
            url = getIntent().getDataString();
            if (url != null) {
                if (url.startsWith(Constants.FILE)) {
                    Utils.showSnackbar(this, R.string.message_blocked_local);
                    url = null;
                }
            }
        }
        //新建一个tab但是不显示，此时显示的是Home页面
        newTab(url, false);
    }

    boolean isHomePage=true;
    public enum ViewType {
        HOME,WEB
    }
    public synchronized void switchView(ViewType type){
        switch (type){
            case HOME://切换到主页
                isHomePage=true;

                browser_scrollview_layout.setVisibility(View.VISIBLE);
                top_titlerbar_layout.setVisibility(View.VISIBLE);
                webview_container.setVisibility(View.INVISIBLE);
                top_titlebar_layout_search.setVisibility(View.INVISIBLE);
                browser_scrollview_layout.scrolltoDownFast();
                break;
            case WEB://切换到网页
                isHomePage=false;
                webview_container.setVisibility(View.VISIBLE);
                top_titlebar_layout_search.setVisibility(View.VISIBLE);
                browser_scrollview_layout.setVisibility(View.INVISIBLE);
                top_titlerbar_layout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void handleNewIntent(Intent intent) {
        String url = null;
        if (intent != null) {
            url = intent.getDataString();
        }
        int num = 0;
        String source = null;
        if (intent != null && intent.getExtras() != null) {
            num = intent.getExtras().getInt(getPackageName() + ".Origin");
            source = intent.getExtras().getString("SOURCE");
        }
        if (num == 1) {
            loadUrlInCurrentView(url);
        } else if (url != null) {
            if (url.startsWith(Constants.FILE)) {
                Utils.showSnackbar(this, R.string.message_blocked_local);
                url = null;
            }
            newTab(url, false);
            mIsNewIntent = (source == null);
        }
    }

    private void loadUrlInCurrentView(final String url) {
        if (mCurrentView == null) {
            return;
        }
        mCurrentView.loadUrl(url);
    }

    @Override
    public void updateUrl(String url, boolean shortUrl) {
        if (url == null ) {
            return;
        }
        if (shortUrl && !url.startsWith(Constants.FILE)) {
            switch (mPreferences.getUrlBoxContentChoice()) {
                case 0: // Default, show only the domain
                    url = url.replaceFirst(Constants.HTTP, "");
                    url = Utils.getDomainName(url);
                    tv_input.setText(url);
                    break;
                case 1: // URL, show the entire URL
                    tv_input.setText(url);
                    break;
                case 2: // Title, show the page's title
                    if (mCurrentView != null && !mCurrentView.getTitle().isEmpty()) {
                        tv_input.setText(mCurrentView.getTitle());
                    } else {
                        tv_input.setText(mUntitledTitle);
                    }
                    break;
            }
        } else {
            tv_input.setText(url);
        }
    }

    @Override
    public void updateProgress(int n) {
        mProgressBar.setProgress(n);
    }

    @Override
    public void updateHistory(String title, String url) {

    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, getString(R.string.title_file_chooser)), 1);
    }

    //用来更新 tabs
    @Override
    public void updateTabs() {
//        mTabAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLongPress() {
        if (mClickHandler == null) {
            mClickHandler = new ClickHandler(this);
        }
        Message click = mClickHandler.obtainMessage();
        if (click != null) {
            click.setTarget(mClickHandler);
            mCurrentView.getWebView().requestFocusNodeHref(click);
        }
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (view == null) {
            return;
        }
        if (mCustomView != null && callback != null) {
            callback.onCustomViewHidden();
            return;
        }
        try {
            view.setKeepScreenOn(true);
        } catch (SecurityException e) {
            Log.e(Constants.TAG, "WebView is not allowed to keep the screen on");
        }
        mOriginalOrientation = getRequestedOrientation();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        mFullscreenContainer = new FrameLayout(this);
        mFullscreenContainer.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        mCustomView = view;
        mFullscreenContainer.addView(mCustomView, COVER_SCREEN_PARAMS);
        decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
        setFullscreen(true, true);
        mCurrentView.setVisibility(View.GONE);
        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                mVideoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                mVideoView.setOnErrorListener(new VideoCompletionListener());
                mVideoView.setOnCompletionListener(new VideoCompletionListener());
            }
        }
        mCustomViewCallback = callback;
    }

    @Override
    public void onHideCustomView() {
        if (mCustomView == null || mCustomViewCallback == null || mCurrentView == null) {
            return;
        }
        Log.d(Constants.TAG, "onHideCustomView");
        mCurrentView.setVisibility(View.VISIBLE);
        try {
            mCustomView.setKeepScreenOn(false);
        } catch (SecurityException e) {
            Log.e(Constants.TAG, "WebView is not allowed to keep the screen on");
        }
        setFullscreen(mPreferences.getHideStatusBarEnabled(), false);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        if (decor != null) {
            decor.removeView(mFullscreenContainer);
        }

        if (API < Build.VERSION_CODES.KITKAT) {
            try {
                mCustomViewCallback.onCustomViewHidden();
            } catch (Throwable ignored) {

            }
        }
        mFullscreenContainer = null;
        mCustomView = null;
        if (mVideoView != null) {
            mVideoView.setOnErrorListener(null);
            mVideoView.setOnCompletionListener(null);
            mVideoView = null;
        }
        setRequestedOrientation(mOriginalOrientation);
    }

    /**
     * This method sets whether or not the activity will display
     * in full-screen mode (i.e. the ActionBar will be hidden) and
     * whether or not immersive mode should be set. This is used to
     * set both parameters correctly as during a full-screen video,
     * both need to be set, but other-wise we leave it up to user
     * preference.
     *
     * @param enabled   true to enable full-screen, false otherwise
     * @param immersive true to enable immersive mode, false otherwise
     */
    private void setFullscreen(boolean enabled, boolean immersive) {
        mIsFullScreen = enabled;
        mIsImmersive = immersive;
        Window window = getWindow();
        View decor = window.getDecorView();
        if (enabled) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (immersive) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }
    @Override
    public Bitmap getDefaultVideoPoster() {
        return BitmapFactory.decodeResource(getResources(), android.R.drawable.spinner_background);
    }

    @Override
    public View getVideoLoadingProgressView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        return inflater.inflate(R.layout.video_loading_progress, null);
    }

    @Override
    public void onCreateWindow(Message resultMsg) {
        if (resultMsg == null) {
            return;
        }
        if (newTab("", true)) {
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mCurrentView.getWebView());
            resultMsg.sendToTarget();
        }
    }

    private class VideoCompletionListener implements MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            onHideCustomView();
        }

    }

    @Override
    public void onCloseWindow(FlyingView view) {
        deleteTab(mWebViewList.indexOf(view));
    }

    private synchronized void deleteTab(int position) {
        if (position >= mWebViewList.size()) {
            return;
        }
        int current = mWebViewList.indexOf(mCurrentView);
        if (current < 0) {
            return;
        }
        FlyingView reference = mWebViewList.get(position);
        if (reference == null) {
            return;
        }
//        if (!reference.getUrl().startsWith(Constants.FILE) && !isIncognito()) {
//            mPreferences.setSavedUrl(reference.getUrl());
//        }
//        boolean isShown = reference.isShown();
//        if (isShown) {
//            mBrowserFrame.setBackgroundColor(mBackgroundColor);
//        }

            mWebViewList.remove(position);
            updateTabs();
            reference.onDestroy();

//        mTabAdapter.notifyDataSetChanged();
//
//        if (mIsNewIntent && isShown) {
//            mIsNewIntent = false;
//            closeActivity();
//        }
//
//        Log.d(Constants.TAG, "deleted tab");
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    @Override
    public void longClickPage(String url) {
        WebView.HitTestResult result = null;
        String currentUrl = null;
        if (mCurrentView!=null&&mCurrentView.getWebView() != null) {
            result = mCurrentView.getWebView().getHitTestResult();
            currentUrl = mCurrentView.getWebView().getUrl();
        }
        if (currentUrl != null && currentUrl.startsWith(Constants.FILE)) {
            if (currentUrl.endsWith(HistoryPage.FILENAME)) {
                if (url != null) {
                    longPressHistoryLink(url);
                } else if (result != null && result.getExtra() != null) {
                    final String newUrl = result.getExtra();
                    longPressHistoryLink(newUrl);
                }
            } else if (currentUrl.endsWith(Constants.BOOKMARKS_FILENAME)) {
                if (url != null) {
//                    mBookmarksDialogBuilder.showLongPressedDialogForUrl(this, url);
                } else if (result != null && result.getExtra() != null) {
                    final String newUrl = result.getExtra();
//                    mBookmarksDialogBuilder.showLongPressedDialogForUrl(this, newUrl);
                }
            }
        } else {
            if (url != null) {
                if (result != null) {
                    if (result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE || result.getType() == WebView.HitTestResult.IMAGE_TYPE) {
                        longPressImage(url);
                    } else {
                        longPressLink(url);
                    }
                } else {
                    longPressLink(url);
                }
            } else if (result != null && result.getExtra() != null) {
                final String newUrl = result.getExtra();
                if (result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE || result.getType() == WebView.HitTestResult.IMAGE_TYPE) {
                    longPressImage(newUrl);
                } else {
                    longPressLink(newUrl);
                }
            }
        }
    }
    private void longPressLink(final String url) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        newTab(url, false);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        loadUrlInCurrentView(url);
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", url);
                        clipboard.setPrimaryClip(clip);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this); // dialog
        builder.setTitle(url)
                .setCancelable(true)
                .setMessage(R.string.dialog_link)
                .setPositiveButton(R.string.action_new_tab, dialogClickListener)
                .setNegativeButton(R.string.action_open, dialogClickListener)
                .setNeutralButton(R.string.action_copy, dialogClickListener)
                .show();
    }
    private void longPressImage(final String url) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        newTab(url, false);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        loadUrlInCurrentView(url);
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        Utils.downloadFile(BrowserActivity.this, url,
                                mCurrentView.getUserAgent(), "attachment");
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(url.replace(Constants.HTTP, ""))
                .setCancelable(true)
                .setMessage(R.string.dialog_image)
                .setPositiveButton(R.string.action_new_tab, dialogClickListener)
                .setNegativeButton(R.string.action_open, dialogClickListener)
                .setNeutralButton(R.string.action_download, dialogClickListener)
                .show();
    }
    private void longPressHistoryLink(final String url) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        newTab(url, false);
//                        mDrawerLayout.closeDrawers();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        mHistoryDatabase.deleteHistoryItem(url);
//                        openHistory();
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        if (mCurrentView != null) {
                            loadUrlInCurrentView(url);
                        }
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.action_history)
                .setMessage(R.string.dialog_history_long_press)
                .setCancelable(true)
                .setPositiveButton(R.string.action_new_tab, dialogClickListener)
                .setNegativeButton(R.string.action_delete, dialogClickListener)
                .setNeutralButton(R.string.action_open, dialogClickListener)
                .show();
    }

    @Override
    public void openBookmarkPage(WebView view) {

    }



    @Override
    public void closeEmptyTab() {

    }

    @Override
    public boolean proxyIsNotReady() {
        return !mProxyUtils.isProxyReady(this);
    }
    synchronized boolean newTab(String url, boolean show) {
        // Limit number of tabs for limited version of app
        mIsNewIntent = false;
        FlyingView startingTab = new FlyingView(BrowserActivity.this, url, mDarkTheme, isIncognito(), BrowserActivity.this);
        startingTab.setTitleBar(top_titlebar_layout_search);

        if (mIdGenerator == 0) {
            startingTab.resumeTimers();
        }
        mIdGenerator++;
        mWebViewList.add(startingTab);

        if (show) {
            showTab(startingTab);
            switchView(ViewType.WEB);
        }else{
            switchView(ViewType.HOME);
        }
        updateTabs();
        return true;
    }



    private synchronized void showTab(FlyingView view) {
        // Set the background color so the color mode color doesn't show through
        if (view == null || (view == mCurrentView && !mCurrentView.isShown())) {
            return;
        }
        webview_container.removeAllViews();
        if (mCurrentView != null) {
            mCurrentView.setForegroundTab(false);
            mCurrentView.onPause();
        }
        mCurrentView = view;
        mCurrentView.setForegroundTab(true);
        if (view.getWebView() != null) {
            updateUrl(mCurrentView.getUrl(), true);
            updateProgress(mCurrentView.getProgress());
        } else {
            updateUrl("", true);
            updateProgress(0);
        }
        webview_container.addView(mCurrentView.getWebView(), MATCH_PARENT);
        mCurrentView.requestFocus();
        mCurrentView.onResume();

    }

    public void search(String query){

        removeSearch();

        if (query.isEmpty()) {
            return;
        }
        String searchUrl = mSearchText + UrlUtils.QUERY_PLACE_HOLDER;
        query = query.trim();
        if (mCurrentView != null) {
            switchView(ViewType.WEB);
            if(!isWebViewShow(mCurrentView.getWebView())){
                showView(mCurrentView.getWebView());
            }

            mCurrentView.stopLoading();
            loadUrlInCurrentView(UrlUtils.smartUrlFilter(query, true, searchUrl));
            if (mCurrentView != null) {
                mCurrentView.requestFocus();
            }
        }else{
            newTab(UrlUtils.smartUrlFilter(query, true, searchUrl),true);
        }


    }

    public void showView(View v){
        if(v==null)
            return;
         if(webview_container.getChildCount()>0){
             webview_container.removeAllViews();
         }
         webview_container.addView(v, MATCH_PARENT);
         webview_container.setVisibility(View.VISIBLE);
    }

    public Boolean isWebViewShow(View v){
        if(v==null) return false;
        if(webview_container.indexOfChild(v)!=-1){
            return true;
        }
        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onBackPressed() {
        if(main_container.indexOfChild(searchFrameLayout)!=-1){
            main_container.removeView(searchFrameLayout);
            return;
        }

        if (mCurrentView != null) {
             if (mCurrentView.canGoBack()) {
                if (!mCurrentView.isShown()) {
                    onHideCustomView();
                } else {
                    mCurrentView.goBack();
                }
            }else if(!isHomePage){
                 switchView(ViewType.HOME);
             }else{
                 super.onBackPressed();
             }
        } else {
            super.onBackPressed();
        }

    }


    private void initializePreferences() {
        switch (mPreferences.getSearchChoice()) {
            case 0:
                mSearchText = mPreferences.getSearchUrl();
                if (!mSearchText.startsWith(Constants.HTTP)
                        && !mSearchText.startsWith(Constants.HTTPS)) {
                    mSearchText = Constants.GOOGLE_SEARCH;
                }
                break;
            case 1:
                mSearchText = Constants.GOOGLE_SEARCH;
                break;
            case 2:
                mSearchText = Constants.ASK_SEARCH;
                break;
            case 3:
                mSearchText = Constants.BING_SEARCH;
                break;
            case 4:
                mSearchText = Constants.YAHOO_SEARCH;
                break;
            case 5:
                mSearchText = Constants.STARTPAGE_SEARCH;
                break;
            case 6:
                mSearchText = Constants.STARTPAGE_MOBILE_SEARCH;
                break;
            case 7:
                mSearchText = Constants.DUCK_SEARCH;
                break;
            case 8:
                mSearchText = Constants.DUCK_LITE_SEARCH;
                break;
            case 9:
                mSearchText = Constants.BAIDU_SEARCH;
                break;
            case 10:
                mSearchText = Constants.YANDEX_SEARCH;
                break;
        }

        updateCookiePreference();
        mProxyUtils.updateProxySettings(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentView != null) {
            mCurrentView.resumeTimers();
            mCurrentView.onResume();
        }
        mHistoryDatabase = HistoryDatabase.getInstance();
        initializePreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCurrentView != null) {
            mCurrentView.pauseTimers();
            mCurrentView.onPause();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        mProxyUtils.onStop();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mProxyUtils.onStart(this);
    }


    /**
     * used to allow uploading into the browser
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (API < Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == 1) {
                if (null == mUploadMessage) {
                    return;
                }
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;

            }
        }

        if (requestCode != 1 || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, intent);
            return;
        }

        Uri[] results = null;

        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (intent == null) {
                // If there is not data, then we may have taken a photo
                if (mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = intent.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }

    @Override
    public void showFileChooser(ValueCallback<Uri[]> filePathCallback) {
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePathCallback;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Utils.createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(Constants.TAG, "Unable to create Image File", ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");

        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        this.startActivityForResult(chooserIntent, 1);
    }

    @Override
    public void onTrimMemory(int level) {
        if (level > TRIM_MEMORY_MODERATE && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Log.d(Constants.TAG, "Low Memory, Free Memory");
            for (int n = 0, size = mWebViewList.size(); n < size; n++) {
                mWebViewList.get(n).freeMemory();
            }
        }
    }

    private void closeBrowser() {
        performExitCleanUp();
        mCurrentView = null;
        for (int n = 0, size = mWebViewList.size(); n < size; n++) {
            if (mWebViewList.get(n) != null) {
                mWebViewList.get(n).onDestroy();
            }
        }
        mWebViewList.clear();
        finish();
    }

    private void performExitCleanUp() {
        if (mPreferences.getClearCacheExit() && mCurrentView != null && !isIncognito()) {
            WebUtils.clearCache(mCurrentView.getWebView());
            Log.d(Constants.TAG, "Cache Cleared");
        }
        if (mPreferences.getClearHistoryExitEnabled() && !isIncognito()) {
            WebUtils.clearHistory(this);
            Log.d(Constants.TAG, "History Cleared");
        }
        if (mPreferences.getClearCookiesExitEnabled() && !isIncognito()) {
            WebUtils.clearCookies(this);
            Log.d(Constants.TAG, "Cookies Cleared");
        }
        if (mPreferences.getClearWebStorageExitEnabled() && !isIncognito()) {
            WebUtils.clearWebStorage();
            Log.d(Constants.TAG, "WebStorage Cleared");
        } else if (isIncognito()) {
            WebUtils.clearWebStorage();     // We want to make sure incognito mode is secure
        }
    }
}
