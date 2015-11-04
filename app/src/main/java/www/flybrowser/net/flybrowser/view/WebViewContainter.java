package www.flybrowser.net.flybrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

import www.flybrowser.net.flybrowser.R;

/**
 * 459821731@qq.com
 * Created by ferris on 2015/10/24.
 */
public class WebViewContainter extends ScrollView {
    private WebViewScrollCallbacks mCallbacks;

    private GestureDetector mGestureDetector;
    private int titlebar_hight = 0;
    private float xDistance, yDistance, xLast, yLast;

    public WebViewContainter(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(context,
                new CustomGestureListener());
        titlebar_hight = getResources().getDimensionPixelSize(R.dimen.titlebar_hight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int pointerCount = ev.getPointerCount();
        if (ev.getAction() != MotionEvent.ACTION_DOWN && pointerCount >= 2) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;

                if (xDistance > yDistance) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

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
            } else {
                if (distanceY > 0) {
                    mCallbacks.onScrollUp();
                } else if (distanceY < 0) {
                    mCallbacks.onScrollDown();
                }

            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public interface WebViewScrollCallbacks {
        public void onScrollChanged();

        public void onScrollUp();

        public void onScrollDown();
    }
}
