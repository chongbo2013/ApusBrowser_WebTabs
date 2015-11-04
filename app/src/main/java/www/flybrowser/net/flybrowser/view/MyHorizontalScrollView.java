package www.flybrowser.net.flybrowser.view;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class MyHorizontalScrollView extends HorizontalScrollView {
    protected static final String TAG = "MyHorizontalScrollView";
    private Handler handler;
    private onScrollListener mOnScrollListener;
    private View child;
    private boolean isScrollDisable = false;

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        // TODO Auto-generated method stub
        if (isScrollDisable) {
            return true;
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public boolean isScrollDisable() {
        return isScrollDisable;
    }

    public void setScrollDisable(boolean isScrollDisable) {
        this.isScrollDisable = isScrollDisable;
    }

    public void initScrollListener() {
        this.setOnTouchListener(onTouchListener);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                if (getScrollX() == 0) {
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onBegin();
                    }
                } else if (child.getMeasuredWidth() <= getScrollX() + getWidth()) {
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onEnd();
                    }
                } else {
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onScroll();
                    }
                }

            }
        };
    }

    public void initScrollView() {
        this.child = getChildAt(0);
        if (child != null) {
            initScrollListener();
        }
    }

    OnTouchListener onTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (child != null && mOnScrollListener != null) {
                        handler.sendMessageDelayed(handler.obtainMessage(), 20);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public interface onScrollListener {
        void onBegin();

        void onEnd();

        void onScroll();
    }

    public void setOnScrollListener(onScrollListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
    }


}
