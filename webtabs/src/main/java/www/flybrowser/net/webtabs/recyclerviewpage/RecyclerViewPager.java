package www.flybrowser.net.webtabs.recyclerviewpage;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import www.flybrowser.net.webtabs.BuildConfig;
import www.flybrowser.net.webtabs.R;

public class RecyclerViewPager extends RecyclerView {
    public static final boolean DEBUG = BuildConfig.DEBUG;

    private RecyclerViewPagerAdapter<?> mViewPagerAdapter;
    private float mTriggerOffset = 0.25f;
    private float mFlingFactor = 0.15f;
    private float mTouchSpan;
    private List<OnPageChangedListener> mOnPageChangedListeners;
    private int mSmoothScrollTargetPosition = -1;
    private int mPositionBeforeScroll = -1;

    private boolean mSinglePageFling;

    boolean mNeedAdjust;
    int mFisrtLeftWhenDragging;
    int mFirstTopWhenDragging;
    View mCurView;
    int mMaxLeftWhenDragging = Integer.MIN_VALUE;
    int mMinLeftWhenDragging = Integer.MAX_VALUE;
    int mMaxTopWhenDragging = Integer.MIN_VALUE;
    int mMinTopWhenDragging = Integer.MAX_VALUE;
    private int mPositionOnTouchDown = -1;
    private boolean mLoopEnabled;

    public RecyclerViewPager(Context context) {
        this(context, null);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context, attrs, defStyle);
		setNestedScrollingEnabled(false);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerViewPager, defStyle,
                0);
        mFlingFactor = a.getFloat(R.styleable.RecyclerViewPager_flingFactor, 0.15f);
        mTriggerOffset = a.getFloat(R.styleable.RecyclerViewPager_triggerOffset, 0.25f);
        mSinglePageFling = a.getBoolean(R.styleable.RecyclerViewPager_singlePageFling, mSinglePageFling);
        mLoopEnabled = a.getBoolean(R.styleable.RecyclerViewPager_loopEnabled, false);
        a.recycle();
    }

    public void setFlingFactor(float flingFactor) {
        mFlingFactor = flingFactor;
    }

    public float getFlingFactor() {
        return mFlingFactor;
    }

    public void setTriggerOffset(float triggerOffset) {
        mTriggerOffset = triggerOffset;
    }

    public float getTriggerOffset() {
        return mTriggerOffset;
    }

    public void setSinglePageFling(boolean singlePageFling) {
        mSinglePageFling = singlePageFling;
    }

    public boolean isSinglePageFling() {
        return mSinglePageFling;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            Field fLayoutState = state.getClass().getDeclaredField("mLayoutState");
            fLayoutState.setAccessible(true);
            Object layoutState = fLayoutState.get(state);
            Field fAnchorOffset = layoutState.getClass().getDeclaredField("mAnchorOffset");
            Field fAnchorPosition = layoutState.getClass().getDeclaredField("mAnchorPosition");
            fAnchorPosition.setAccessible(true);
            fAnchorOffset.setAccessible(true);
            if (fAnchorOffset.getInt(layoutState) > 0) {
                fAnchorPosition.set(layoutState, fAnchorPosition.getInt(layoutState) - 1);
            } else if(fAnchorOffset.getInt(layoutState) < 0){
                fAnchorPosition.set(layoutState, fAnchorPosition.getInt(layoutState) + 1);
            }
            fAnchorOffset.setInt(layoutState, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mViewPagerAdapter = ensureRecyclerViewPagerAdapter(adapter);
        super.setAdapter(mViewPagerAdapter);
        if (isLoopEnabled()) {
            scrollToPosition(getMiddlePosition());
        }
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        mViewPagerAdapter = ensureRecyclerViewPagerAdapter(adapter);
        super.swapAdapter(mViewPagerAdapter, removeAndRecycleExistingViews);
        if (isLoopEnabled()) {
            scrollToPosition(getMiddlePosition());
        }
    }

    @Override
    public Adapter getAdapter() {
        if (mViewPagerAdapter != null) {
            return mViewPagerAdapter.mAdapter;
        }
        return null;
    }

    public RecyclerViewPagerAdapter getWrapperAdapter() {
        return mViewPagerAdapter;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        boolean flinging = super.fling((int) (velocityX * mFlingFactor), (int) (velocityY * mFlingFactor));
        if (flinging) {
            if (getLayoutManager().canScrollHorizontally()) {
                adjustPositionX(velocityX);
            } else {
                adjustPositionY(velocityY);
            }
        }

        if (DEBUG) {
            Log.d("@", "velocityX:" + velocityX);
            Log.d("@", "velocityY:" + velocityY);
        }
        return flinging;
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (DEBUG) {
            Log.d("@", "smoothScrollToPosition:" + position);
        }
        position = transformInnerPositionIfNeed(position);
        mSmoothScrollTargetPosition = position;
        super.smoothScrollToPosition(position);
    }

    @Override
    public void scrollToPosition(int position) {
        if (DEBUG) {
            Log.d("@", "scrollToPosition:" + position);
        }
        position = transformInnerPositionIfNeed(position);
        mPositionBeforeScroll = getCurrentPosition();
        mSmoothScrollTargetPosition = position;
        super.scrollToPosition(position);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                if (mSmoothScrollTargetPosition >= 0 && mSmoothScrollTargetPosition < mViewPagerAdapter.getItemCount()) {
                    if (mOnPageChangedListeners != null) {
                        for (OnPageChangedListener onPageChangedListener : mOnPageChangedListeners) {
                            if (onPageChangedListener != null) {
                                onPageChangedListener.OnPageChanged(mPositionBeforeScroll, getCurrentPosition());
                            }
                        }
                    }
                }
            }
        });
    }

    private int transformInnerPositionIfNeed(int position) {
        if (isLoopEnabled() && position < ((LoopRecyclerViewPagerAdapter) mViewPagerAdapter).getActualItemCount()) {
            final int actualItemCount = ((LoopRecyclerViewPagerAdapter) mViewPagerAdapter).getActualItemCount();
            position = getCurrentPosition() - getCurrentPosition() % actualItemCount + position;
        }
        return position;
    }

    /**
     * get item position in center of viewpager
     */
    public int getCurrentPosition() {
        int curPosition = -1;
        if (getLayoutManager().canScrollHorizontally()) {
            curPosition = ViewUtils.getCenterXChildPosition(this);
        } else {
            curPosition = ViewUtils.getCenterYChildPosition(this);
        }
        if (curPosition < 0) {
            curPosition = mSmoothScrollTargetPosition;
        }
        return curPosition;
    }

    public boolean isLoopEnabled() {
        return mLoopEnabled;
    }

    /***
     * adjust position before Touch event complete and fling action start.
     */
    protected void adjustPositionX(int velocityX) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int curPosition = ViewUtils.getCenterXChildPosition(this);
            int childWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int flingCount = getFlingCount(velocityX, childWidth);
            int targetPosition = curPosition + flingCount;
            if (mSinglePageFling) {
                flingCount = Math.max(-1, Math.min(1, flingCount));
                targetPosition = flingCount == 0 ? curPosition : mPositionOnTouchDown + flingCount;
                if (DEBUG) {
                    Log.d("@", "flingCount:" + flingCount);
                    Log.d("@", "original targetPosition:" + targetPosition);
                }
            }
            targetPosition = Math.max(targetPosition, 0);
            targetPosition = Math.min(targetPosition, mViewPagerAdapter.getItemCount() - 1);
            if (targetPosition == curPosition
                    && ((mSinglePageFling
                    && mPositionOnTouchDown == curPosition)
                    || !mSinglePageFling)) {
                View centerXChild = ViewUtils.getCenterXChild(this);
                if (centerXChild != null) {
                    if (mTouchSpan > centerXChild.getWidth() * mTriggerOffset * mTriggerOffset && targetPosition != 0) {
                        targetPosition--;
                    } else if (mTouchSpan < centerXChild.getWidth() * -mTriggerOffset && targetPosition != mViewPagerAdapter.getItemCount() - 1) {
                        targetPosition++;
                    }
                }
            }
            if (DEBUG) {
                Log.d("@", "mTouchSpan:" + mTouchSpan);
                Log.d("@", "adjustPositionX:" + targetPosition);
            }
            smoothScrollToPosition(safeTargetPosition(targetPosition, mViewPagerAdapter.getItemCount()));
        }
    }

    public void addOnPageChangedListener(OnPageChangedListener listener) {
        if (mOnPageChangedListeners == null) {
            mOnPageChangedListeners = new ArrayList<>();
        }
        mOnPageChangedListeners.add(listener);
    }

    public void removeOnPageChangedListener(OnPageChangedListener listener) {
        if (mOnPageChangedListeners != null) {
            mOnPageChangedListeners.remove(listener);
        }
    }

    public void clearOnPageChangedListeners() {
        if (mOnPageChangedListeners != null) {
            mOnPageChangedListeners.clear();
        }
    }

    /***
     * adjust position before Touch event complete and fling action start.
     */
    protected void adjustPositionY(int velocityY) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int curPosition = ViewUtils.getCenterYChildPosition(this);
            int childHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            int flingCount = getFlingCount(velocityY, childHeight);
            int targetPosition = curPosition + flingCount;
            if (mSinglePageFling) {
                flingCount = Math.max(-1, Math.min(1, flingCount));
                targetPosition = flingCount == 0 ? curPosition : mPositionOnTouchDown + flingCount;
            }

            targetPosition = Math.max(targetPosition, 0);
            targetPosition = Math.min(targetPosition, mViewPagerAdapter.getItemCount() - 1);
            if (targetPosition == curPosition
                    && ((mSinglePageFling
                    && mPositionOnTouchDown == curPosition)
                    || !mSinglePageFling)) {
                View centerYChild = ViewUtils.getCenterYChild(this);
                if (centerYChild != null) {
                    if (mTouchSpan > centerYChild.getHeight() * mTriggerOffset && targetPosition != 0) {
                        targetPosition--;
                    } else if (mTouchSpan < centerYChild.getHeight() * -mTriggerOffset && targetPosition != mViewPagerAdapter.getItemCount() - 1) {
                        targetPosition++;
                    }
                }
            }
            if (DEBUG) {
                Log.d("@", "mTouchSpan:" + mTouchSpan);
                Log.d("@", "adjustPositionY:" + targetPosition);
            }
            smoothScrollToPosition(safeTargetPosition(targetPosition, mViewPagerAdapter.getItemCount()));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mPositionOnTouchDown = getLayoutManager().canScrollHorizontally()
                    ? ViewUtils.getCenterXChildPosition(this)
                    : ViewUtils.getCenterYChildPosition(this);
            if (DEBUG) {
                Log.d("@", "mPositionOnTouchDown:" + mPositionOnTouchDown);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // recording the max/min value in touch track
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            if (mCurView != null) {
                mMaxLeftWhenDragging = Math.max(mCurView.getLeft(), mMaxLeftWhenDragging);
                mMaxTopWhenDragging = Math.max(mCurView.getTop(), mMaxTopWhenDragging);
                mMinLeftWhenDragging = Math.min(mCurView.getLeft(), mMinLeftWhenDragging);
                mMinTopWhenDragging = Math.min(mCurView.getTop(), mMinTopWhenDragging);
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == SCROLL_STATE_DRAGGING) {
            mNeedAdjust = true;
            mCurView = getLayoutManager().canScrollHorizontally() ? ViewUtils.getCenterXChild(this) :
                    ViewUtils.getCenterYChild(this);
            if (mCurView != null) {
                mPositionBeforeScroll = getChildLayoutPosition(mCurView);
                if (DEBUG) {
                    Log.d("@", "mPositionBeforeScroll:" + mPositionBeforeScroll);
                }
                mFisrtLeftWhenDragging = mCurView.getLeft();
                mFirstTopWhenDragging = mCurView.getTop();
            } else {
                mPositionBeforeScroll = -1;
            }
            mTouchSpan = 0;
        } else if (state == SCROLL_STATE_SETTLING) {
            mNeedAdjust = false;
            if (mCurView != null) {
                if (getLayoutManager().canScrollHorizontally()) {
                    mTouchSpan = mCurView.getLeft() - mFisrtLeftWhenDragging;
                } else {
                    mTouchSpan = mCurView.getTop() - mFirstTopWhenDragging;
                }
            } else {
                mTouchSpan = 0;
            }
            mCurView = null;
        } else if (state == SCROLL_STATE_IDLE) {
            if (mNeedAdjust) {
                int targetPosition = getLayoutManager().canScrollHorizontally() ? ViewUtils.getCenterXChildPosition(this) :
                        ViewUtils.getCenterYChildPosition(this);
                if (mCurView != null) {
                    targetPosition = getChildAdapterPosition(mCurView);
                    if (getLayoutManager().canScrollHorizontally()) {
                        int spanX = mCurView.getLeft() - mFisrtLeftWhenDragging;
                        // if user is tending to cancel paging action, don't perform position changing
                        if (spanX > mCurView.getWidth() * mTriggerOffset && mCurView.getLeft() >= mMaxLeftWhenDragging) {
                            targetPosition--;
                        } else if (spanX < mCurView.getWidth() * -mTriggerOffset && mCurView.getLeft() <= mMinLeftWhenDragging) {
                            targetPosition++;
                        }
                    } else {
                        int spanY = mCurView.getTop() - mFirstTopWhenDragging;
                        if (spanY > mCurView.getHeight() * mTriggerOffset && mCurView.getTop() >= mMaxTopWhenDragging) {
                            targetPosition--;
                        } else if (spanY < mCurView.getHeight() * -mTriggerOffset && mCurView.getTop() <= mMinTopWhenDragging) {
                            targetPosition++;
                        }
                    }
                }
                smoothScrollToPosition(safeTargetPosition(targetPosition, mViewPagerAdapter.getItemCount()));
                mCurView = null;
            } else if (mSmoothScrollTargetPosition != mPositionBeforeScroll) {
                if (DEBUG) {
                    Log.d("@", "onPageChanged:" + mSmoothScrollTargetPosition);
                }
                if (mOnPageChangedListeners != null) {
                    for (OnPageChangedListener onPageChangedListener : mOnPageChangedListeners) {
                        if (onPageChangedListener != null) {
                            onPageChangedListener.OnPageChanged(mPositionBeforeScroll, mSmoothScrollTargetPosition);
                        }
                    }
                }
                mPositionBeforeScroll = mSmoothScrollTargetPosition;
            }
            // reset
            mMaxLeftWhenDragging = Integer.MIN_VALUE;
            mMinLeftWhenDragging = Integer.MAX_VALUE;
            mMaxTopWhenDragging = Integer.MIN_VALUE;
            mMinTopWhenDragging = Integer.MAX_VALUE;
        }
    }

    @NonNull
    private RecyclerViewPagerAdapter ensureRecyclerViewPagerAdapter(Adapter adapter) {
        if (mLoopEnabled) {
            return (adapter instanceof LoopRecyclerViewPagerAdapter)
                    ? (LoopRecyclerViewPagerAdapter) adapter
                    : new LoopRecyclerViewPagerAdapter(this, adapter);
        }
        return (adapter instanceof RecyclerViewPagerAdapter)
                ? (RecyclerViewPagerAdapter) adapter
                : new RecyclerViewPagerAdapter(this, adapter);

    }

    private int getMiddlePosition() {
        int middlePosition = Integer.MAX_VALUE / 2;
        final int actualItemCount = ((LoopRecyclerViewPagerAdapter) mViewPagerAdapter).getActualItemCount();
        if (actualItemCount > 0 && middlePosition % actualItemCount != 0) {
            middlePosition = middlePosition - middlePosition % actualItemCount;
        }
        return middlePosition;
    }

    private int getFlingCount(int velocity, int cellSize) {
        if (velocity == 0) {
            return 0;
        }
        int sign = velocity > 0 ? 1 : -1;
        return (int) (sign * Math.ceil((velocity * sign * mFlingFactor / cellSize)
                - mTriggerOffset));
    }

    private int safeTargetPosition(int position, int count) {
        if (position < 0) {
            return 0;
        }
        if (position >= count) {
            return count - 1;
        }
        return position;
    }

    public interface OnPageChangedListener {
        void OnPageChanged(int oldPosition, int newPosition);
    }

}
