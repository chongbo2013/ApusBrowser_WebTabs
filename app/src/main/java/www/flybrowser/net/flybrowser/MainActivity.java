package www.flybrowser.net.flybrowser;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import www.flybrowser.net.flybrowser.preference.PreferenceManager;

public class MainActivity extends BrowserActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public synchronized void initializeTabs() {
        restoreOrNewTab();
        // if incognito mode use newTab(null, true); instead
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void updateCookiePreference() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        cookieManager.setAcceptCookie(PreferenceManager.getInstance().getCookiesEnabled());
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean isIncognito() {
        return false;
    }

    @Override
    public void closeActivity() {
        moveTaskToBack(true);
    }


}
