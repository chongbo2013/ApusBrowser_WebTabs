package www.flybrowser.net.flybrowser.utils;

import android.os.Environment;

/**
 * 桌面的环境管理类
 * @author liwenxue
 *
 */
public final class FlyEnvironment {

		
		private final static String SDCARD = Environment.getExternalStorageDirectory().getPath();
		
		private final static String FILE_PATH = SDCARD + "/FlyBrowser/";
		
		public final static String LOG = FILE_PATH + "log/";

		public final static String HTTP_CACHE = SDCARD + "/FlyBrowser/http/";
		
	
}
