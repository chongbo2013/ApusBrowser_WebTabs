package www.flybrowser.net.flybrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import www.flybrowser.net.flybrowser.R;

/**
 * Created by ferris on 2015/10/26.
 */
public class TouchWebView extends WebView{
    private GestureDetector mGestureDetector;
    private static final String LOG_TAG = "WebViewWithTitle";

    private WebViewScrollCallbacks mCallbacks;

    public TouchWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TouchWebView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public TouchWebView(Context context) {
        super(context);
        init();
    }

    public void init(){
        mGestureDetector = new GestureDetector(getContext(),
                new CustomGestureListener());
        titlebar_hight=getResources().getDimensionPixelSize(R.dimen.titlebar_hight);
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }
    private int titlebar_hight=0;

    public void addCallbacks(WebViewScrollCallbacks listener) {
        this.mCallbacks = listener;
    }



    // 长按事件监听
    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int scrollY = getScrollY();
            if (scrollY <= titlebar_hight) {
                mCallbacks.onScrollChanged();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(velocityY<0){
                mCallbacks.onScrollUp();
            }else if(velocityY>0){
                mCallbacks.onScrollDown();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public interface WebViewScrollCallbacks {
        public void onScrollChanged();

        public void onScrollUp();

        public void onScrollDown();
    }
}
