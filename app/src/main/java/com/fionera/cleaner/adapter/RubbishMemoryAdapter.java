package com.fionera.cleaner.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fionera.cleaner.bean.CacheInfo;
import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.RvItemTouchListener;
import com.fionera.cleaner.utils.ShellUtils;
import com.fionera.cleaner.utils.ShowToast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RubbishMemoryAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CacheInfo> mListAppInfo;
    private LayoutInflater inflater = null;
    private Context mContext;

    private RvItemTouchListener rvItemTouchListener;

    public void setRvItemTouchListener(RvItemTouchListener rvItemTouchListener) {
        this.rvItemTouchListener = rvItemTouchListener;
    }

    public RubbishMemoryAdapter(Context context, List<CacheInfo> apps) {
        this.inflater = LayoutInflater.from(context);
        this.mListAppInfo = apps;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.rv_rubbish_clean_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder theHolder = (ViewHolder) holder;
        theHolder.appIcon.setImageDrawable(
                mListAppInfo.get(theHolder.getAdapterPosition()).getApplicationIcon());
        theHolder.appName
                .setText(mListAppInfo.get(theHolder.getAdapterPosition()).getApplicationName());
        theHolder.size.setText(Formatter.formatShortFileSize(mContext, mListAppInfo
                .get(theHolder.getAdapterPosition()).getCacheSize()));
        theHolder.packageName = mListAppInfo.get(theHolder.getAdapterPosition()).getPackageName();
        theHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rvItemTouchListener != null) {
                    rvItemTouchListener.onItemClick(v, theHolder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListAppInfo.size();
    }

    class ViewHolder
            extends RecyclerView.ViewHolder {
        @Bind(R.id.app_icon)
        ImageView appIcon;
        @Bind(R.id.app_name)
        TextView appName;
        @Bind(R.id.app_size)
        TextView size;
        String packageName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
