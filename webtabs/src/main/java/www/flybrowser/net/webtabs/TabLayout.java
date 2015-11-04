package www.flybrowser.net.webtabs;

import android.content.Context;


import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import www.flybrowser.net.webtabs.recyclerviewpage.RecyclerViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/3.
 */
public class TabLayout extends FrameLayout {

    private RecyclerViewPager webview_tabs;
    public List<FlyingView> mDataset;
    public TabLayout(Context context) {
        super(context);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public static TabLayout getXml(Context mContext){
        return (TabLayout) LayoutInflater.from(mContext).inflate(R.layout.tab_layout,null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        webview_tabs=(RecyclerViewPager)findViewById(R.id.webview_tabs);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext());
        horizontalLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        webview_tabs.addItemDecoration(new DividerItemDecoration(getContext(), horizontalLayoutManager.getOrientation(), getContext().getResources().getDrawable(R.drawable.divider_bg)));



        webview_tabs.setHasFixedSize(true);
        webview_tabs.setLongClickable(true);
        webview_tabs.setLayoutManager(horizontalLayoutManager);



        initData();
    }






    public void initData(){
        mDataset=new ArrayList<>();
        for(int i=0;i<10;i++){
            mDataset.add(new FlyingView("tabs_"+i));
        }

        final TabAdapter adapter = new TabAdapter(getContext(),mDataset);
        webview_tabs.setAdapter(adapter);


        SwipeDismissRecyclerViewTouchListener verticalListener = new SwipeDismissRecyclerViewTouchListener.Builder(
                webview_tabs,
                new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view) {
                        int id = webview_tabs.getChildPosition(view);
                        adapter.mDataset.remove(id);
                        adapter.notifyDataSetChanged();

                    }
                }).setIsVertical(true).create();

        webview_tabs.setOnTouchListener(verticalListener);

    }
}
