package com.fionera.cleaner.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.bean.AppInfo;
import com.fionera.cleaner.utils.StorageUtil;

import java.util.List;

public class SoftwareAdapter
        extends BaseAdapter {

    public List<AppInfo> mListAppInfo;
    private LayoutInflater inflater = null;
    private Context mContext;


    public SoftwareAdapter(Context context, List<AppInfo> apps) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mListAppInfo = apps;
    }

    @Override
    public int getCount() {
        return mListAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.rv_software_manage_item, parent, false);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.size = (TextView) convertView.findViewById(R.id.app_size);
            holder.uninstall = (TextView) convertView.findViewById(R.id.app_uninstall);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final AppInfo item = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(item.getAppIcon());
        holder.appName.setText(item.getAppName());
        holder.size.setText(StorageUtil.convertStorage(item.getPkgSize()));
        holder.uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setAction("android.intent.action.DELETE");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse("package:" + item.getPackname()));
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView size;
        TextView uninstall;
    }
}
