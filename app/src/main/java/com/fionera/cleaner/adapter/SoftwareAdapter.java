package com.fionera.cleaner.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.bean.AppInfo;
import com.fionera.cleaner.utils.RvItemTouchListener;
import com.fionera.cleaner.utils.StorageUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SoftwareAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<AppInfo> mListAppInfo;
    private LayoutInflater inflater = null;
    private Context mContext;

    private RvItemTouchListener rvItemTouchListener;

    public void setRvItemTouchListener(RvItemTouchListener rvItemTouchListener) {
        this.rvItemTouchListener = rvItemTouchListener;
    }

    public SoftwareAdapter(Context context, List<AppInfo> apps) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mListAppInfo = apps;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.rv_software_manage_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder theHolder = (ViewHolder) holder;
        theHolder.appIcon
                .setImageDrawable(mListAppInfo.get(theHolder.getAdapterPosition()).getAppIcon());
        theHolder.appName.setText(mListAppInfo.get(theHolder.getAdapterPosition()).getAppName());
        theHolder.size.setText(StorageUtil.convertStorage(
                mListAppInfo.get(theHolder.getAdapterPosition()).getPkgSize()));
        theHolder.uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setAction("android.intent.action.DELETE");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(
                        "package:" + mListAppInfo.get(theHolder.getAdapterPosition())
                                .getPackname()));
                mContext.startActivity(intent);
            }
        });
        theHolder.packageName = mListAppInfo.get(theHolder.getAdapterPosition()).getPackname();
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
        @Bind(R.id.app_uninstall)
        TextView uninstall;

        String packageName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
