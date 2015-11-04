package www.flybrowser.net.flybrowser.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import www.flybrowser.net.flybrowser.BrowserActivity;

public class TitleBarWebView extends WebView {
    private View mTitleBar;

    private LayoutParams mTitleBarLayoutParams;


    public TitleBarWebView(Context context) {
        super(context);

    }
    public TitleBarWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public TitleBarWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    protected int getVisibleTitleHeightCompat() {
        return Math.max(BrowserActivity.TITLE_BAR_HIGHT - Math.max(0, getScrollY()), 0);
    }



    boolean mTouchMove;
    int mTitleBarOffs;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mTitleBar != null) {
            final float x = event.getX();
            float y = event.getY();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_MOVE:
                    mTouchMove = true;
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mTouchMove = false;
                    break;

                default:
            }


            if (!mTouchMove) {
                mTitleBarOffs = getVisibleTitleHeightCompat();
            }

            y -= mTitleBarOffs;
            if (y < 0) y = 0;
            event.setLocation(x, y);


            return super.dispatchTouchEvent(event);

        } else {
            return super.dispatchTouchEvent(event);
        }
    }

    public void setEmbeddedTitleBar(View v) {
        if (mTitleBar == v) return;
        if (mTitleBar != null) {
            removeView(mTitleBar);
        }
        if (null != v) {
            mTitleBarLayoutParams = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0);
            addView(v, mTitleBarLayoutParams);
        }
        mTitleBar = v;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        if (mTitleBar != null) {
            final int sy = getScrollY();
            Log.d("TitleBarWebView","getScrollY="+sy);
            int titleBarOffs = BrowserActivity.TITLE_BAR_HIGHT - sy;
            if (titleBarOffs < 0) titleBarOffs = 0;
            canvas.translate(0, titleBarOffs);
        }

        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child == mTitleBar) {
            mTitleBar.offsetLeftAndRight((int) (getScrollX() - mTitleBar.getLeft()));
        }
        return super.drawChild(canvas, child, drawingTime);
    }
}

