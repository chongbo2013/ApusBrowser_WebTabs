package www.flybrowser.net.flybrowser.view;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * 459821731@qq.com
 * Created by ferris on 2015/10/24.
 */
public class StickyScrollView extends ScrollView {
    Callbacks mCallbacks;
    private int mTouchSlop = 0;
    // 滑动距离及坐标
    private float xDistance, yDistance, xLast, yLast;
    private int maxScrollY = 0;//一半
    private int halfScrollY = 0;

    public void setMaxScroll(int maxScrollY) {
        this.maxScrollY = maxScrollY;
        halfScrollY = maxScrollY / 2;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

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
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                int currentY = (int) ev.getY();
                int scrollY = getScrollY();
                if (scrollY < maxScrollY) {
                    if (yLast != 0 && currentY != 0) {
                        if (yLast - currentY > mTouchSlop) {
                            scrolltoUp();
                        }
                        if (currentY - yLast > mTouchSlop) {
                            scrolltoDown();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }


    public StickyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void scrolltoUp() {
        post(new Runnable() {
            @Override
            public void run() {
                smoothScrollBy(0, maxScrollY - getScrollY());
            }
        });

    }

    public void scrolltoDown() {
        post(new Runnable() {
            @Override
            public void run() {
                smoothScrollBy(0, -getScrollY());
            }
        });

    }

    public void scrolltoDownFast() {
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo(0, -getScrollY());
            }
        });

    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 滚动时回调
        if (mCallbacks != null) {
            mCallbacks.onScrollChanged();
        }
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    public void addCallbacks(Callbacks listener) {
        this.mCallbacks = listener;
    }

    public static interface Callbacks {
        public void onScrollChanged();
    }
}
