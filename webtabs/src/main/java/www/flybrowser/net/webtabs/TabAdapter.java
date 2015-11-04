package www.flybrowser.net.webtabs;

import android.content.Context;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ferris on 2015/11/3.
 */
public class TabAdapter extends RecyclerView.Adapter<TabAdapter.ViewHolder> {
    private Context mContext;
    public List<FlyingView> mDataset;

    public TabAdapter(Context mContext,List<FlyingView> dataset) {
        super();
        this.mContext=mContext;
        this.mDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate( R.layout.tab_items,viewGroup, false);

//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        params.width = UIUtils.getDisplayWidth(mContext);
//        view.setLayoutParams(params);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.mTextView.setText(mDataset.get(i).getWebview_title());
    }

    @Override
    public int getItemCount() {
        return mDataset==null?0:mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView mTextView;
        public ImageView mImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
            mImageView= (ImageView) itemView.findViewById(R.id.mImageView);


        }
    }

}
