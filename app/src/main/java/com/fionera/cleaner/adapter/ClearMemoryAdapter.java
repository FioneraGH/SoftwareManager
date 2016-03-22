package com.fionera.cleaner.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fionera.cleaner.bean.AppProcessInfo;
import com.fionera.cleaner.utils.StorageUtil;
import com.fionera.cleaner.R;

import java.util.List;

public class ClearMemoryAdapter
        extends BaseAdapter {

    private List<AppProcessInfo> mListAppInfo;
    private LayoutInflater inflater = null;

    public ClearMemoryAdapter(Context context, List<AppProcessInfo> apps) {
        this.inflater = LayoutInflater.from(context);
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
            convertView = inflater.inflate(R.layout.listview_memory_clean, parent, false);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.iv_splash);
            holder.appName = (TextView) convertView.findViewById(R.id.name);
            holder.memory = (TextView) convertView.findViewById(R.id.memory);

            holder.cb = (AppCompatCheckBox) convertView.findViewById(R.id.cb_checked);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final AppProcessInfo appInfo = (AppProcessInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.icon);
        holder.appName.setText(appInfo.appName + "\n" + appInfo.processName);
        holder.memory.setText(StorageUtil.convertStorage(appInfo.memory));
        if (appInfo.checked) {
            holder.cb.setChecked(true);
        } else {
            holder.cb.setChecked(false);
        }
        holder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appInfo.checked = !appInfo.checked;
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView memory;
        AppCompatCheckBox cb;
    }

}
