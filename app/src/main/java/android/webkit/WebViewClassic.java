package android.webkit;

/**
 * Created by Administrator on 2015/10/26.
 */
import android.view.View;

/**
 * Trojan class for getting access to a hidden API level 16 interface
 */
public class WebViewClassic {
    public interface TitleBarDelegate {
        int getTitleHeight();

        public void onSetEmbeddedTitleBar(final View title);
    }
}
