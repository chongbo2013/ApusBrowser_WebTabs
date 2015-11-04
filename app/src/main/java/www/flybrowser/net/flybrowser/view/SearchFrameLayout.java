package www.flybrowser.net.flybrowser.view;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;

import www.flybrowser.net.flybrowser.BrowserActivity;
import www.flybrowser.net.flybrowser.MainActivity;
import www.flybrowser.net.flybrowser.R;
import www.flybrowser.net.flybrowser.constant.Constants;

/**
 * Created by 搜索View on 2015/10/26.
 */
public class SearchFrameLayout extends FrameLayout{
    private AutoCompleteTextView tv_input;
    private TextView tv_go;
    public SearchFrameLayout(Context context) {
        super(context);
    }

    public SearchFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public static SearchFrameLayout getXml(Context mContext){
        return (SearchFrameLayout) LayoutInflater.from(mContext).inflate(R.layout.search_view,null);
    }

    private BrowserActivity mActivity;
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mActivity=(BrowserActivity)getContext();
        tv_input=(AutoCompleteTextView)findViewById(R.id.tv_input);
        tv_go=(TextView)findViewById(R.id.tv_go);
        tv_go.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getContext()).search(tv_input.getText().toString());
            }
        });

        SearchListenerClass searchListenerClass=new SearchListenerClass();
        tv_input.setOnKeyListener(searchListenerClass);
        tv_input.setOnFocusChangeListener(searchListenerClass);
        tv_input.setOnEditorActionListener(searchListenerClass);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
    //打开搜索后
    public void open(final String msg) {
        if(!TextUtils.isEmpty(msg)){
            tv_input.setText(msg);
        }
        post(new Runnable() {
            @Override
            public void run() {

                tv_input.requestFocus();
                InputMethodManager imm = (InputMethodManager)getContext(). getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(tv_input, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    private class SearchListenerClass implements OnKeyListener, TextView.OnEditorActionListener, OnFocusChangeListener {

        @Override
        public boolean onKey(View arg0, int arg1, KeyEvent arg2) {

            switch (arg1) {
                case KeyEvent.KEYCODE_ENTER:
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tv_input.getWindowToken(), 0);
                    mActivity.search(tv_input.getText().toString());
                    return true;
                default:
                    break;
            }
            return false;
        }

        @Override
        public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
            // hide the keyboard and search the web when the enter key
            // button is pressed
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_SEARCH
                    || (arg2.getAction() == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tv_input.getWindowToken(), 0);
                mActivity.search(tv_input.getText().toString());
                return true;
            }
            return false;
        }

        @Override
        public void onFocusChange(View v, final boolean hasFocus) {

            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager)getContext(). getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tv_input.getWindowToken(), 0);
            }
        }


    }
}
