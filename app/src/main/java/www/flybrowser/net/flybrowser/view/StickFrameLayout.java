package www.flybrowser.net.flybrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import www.flybrowser.net.flybrowser.MainActivity;
import www.flybrowser.net.flybrowser.R;

/**
 * 459821731@qq.com
 * Created by ferris on 2015/10/24.
 */
public class StickFrameLayout extends FrameLayout{
    private FrameLayout stick_titlebar_one,stick_titlebar_two;

    private float alphaOffset=0f;
    private int maxScrollY=0;
    public void setMaxScroll(int maxScrollY){
        this.maxScrollY=maxScrollY;
        alphaOffset=1f/maxScrollY;
    }

    private StickyScrollView msStickyScrollView;
    public void setScrollView(StickyScrollView mScrollView){
        this.msStickyScrollView=mScrollView;

    }
    public StickFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public StickFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickFrameLayout(Context context) {
        this(context, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        stick_titlebar_one=(FrameLayout)findViewById(R.id.stick_titlebar_one);
        stick_titlebar_two=(FrameLayout)findViewById(R.id.stick_titlebar_two);
        stick_titlebar_one.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(msStickyScrollView!=null){
                    msStickyScrollView.scrolltoUp();
                    long deledy=(Math.max(msStickyScrollView.getScrollY(),maxScrollY)/maxScrollY)*200L;
                    msStickyScrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity)getContext()).openSearch();
                        }
                    },deledy);
                }

            }
        });
    }

    public void scroll(int scrollY){

        float alpha=1f-scrollY*alphaOffset;
        stick_titlebar_two.setAlpha(alpha);
    }
}
