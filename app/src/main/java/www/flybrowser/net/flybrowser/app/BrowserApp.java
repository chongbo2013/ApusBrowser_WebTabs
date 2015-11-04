package www.flybrowser.net.flybrowser.app;

import android.app.Application;
import android.content.Context;

import www.flybrowser.net.flybrowser.utils.CrashHandler;

/**
 * Created by Administrator on 2015/10/23.
 */
public class BrowserApp extends Application {
    private static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        CrashHandler.getInstance().init(this);

    }

    public static Context getAppContext() {
        return context;
    }
}
