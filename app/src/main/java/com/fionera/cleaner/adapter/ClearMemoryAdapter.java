package com.fionera.cleaner.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fionera.cleaner.bean.AppProcessInfo;
import com.fionera.cleaner.utils.RvItemTouchListener;
import com.fionera.cleaner.utils.StorageUtil;
import com.fionera.cleaner.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ClearMemoryAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AppProcessInfo> mListAppInfo;
    private LayoutInflater inflater = null;

    private RvItemTouchListener rvItemTouchListener;

    public void setRvItemTouchListener(RvItemTouchListener rvItemTouchListener) {
        this.rvItemTouchListener = rvItemTouchListener;
    }

    public ClearMemoryAdapter(Context context, List<AppProcessInfo> apps) {
        this.inflater = LayoutInflater.from(context);
        this.mListAppInfo = apps;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.rv_memory_clean_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder theHolder = (ViewHolder) holder;
        theHolder.appIcon.setImageDrawable(mListAppInfo.get(theHolder.getAdapterPosition()).icon);
        theHolder.appName.setText(
                mListAppInfo.get(theHolder.getAdapterPosition()).appName + "\n" + mListAppInfo
                        .get(theHolder.getAdapterPosition()).processName);
        theHolder.memory.setText(StorageUtil.convertStorage(
                mListAppInfo.get(theHolder.getAdapterPosition()).memory));
        theHolder.cb.setChecked(mListAppInfo.get(theHolder.getAdapterPosition()).checked);
        theHolder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListAppInfo.get(theHolder.getAdapterPosition()).checked = !mListAppInfo
                        .get(theHolder.getAdapterPosition()).checked;
                notifyItemChanged(theHolder.getAdapterPosition());
            }
        });
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

        @Bind(R.id.iv_splash)
        ImageView appIcon;
        @Bind(R.id.name)
        TextView appName;
        @Bind(R.id.memory)
        TextView memory;
        @Bind(R.id.cb_checked)
        AppCompatCheckBox cb;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
