package www.flybrowser.net.webtabs;

/**
 * Created by Administrator on 2015/11/3.
 */
public class FlyingView {
    private String webview_title;
    private int webview_image=R.mipmap.tabpre;

    public FlyingView(String webview_title){
        this.webview_title=webview_title;
    }
    public int getWebview_image() {
        return webview_image;
    }

    public void setWebview_image(int webview_image) {
        this.webview_image = webview_image;
    }

    public String getWebview_title() {
        return webview_title;
    }

    public void setWebview_title(String webview_title) {
        this.webview_title = webview_title;
    }
}
