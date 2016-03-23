package com.fionera.cleaner.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fionera.cleaner.bean.CacheInfo;
import com.fionera.cleaner.R;

import java.util.List;

public class RubbishMemoryAdapter
        extends BaseAdapter
        implements AdapterView.OnItemClickListener {

    private List<CacheInfo> mListAppInfo;
    private LayoutInflater infater = null;
    private Context mContext;

    public RubbishMemoryAdapter(Context context, List<CacheInfo> apps) {
        this.infater = LayoutInflater.from(context);
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
            convertView = infater.inflate(R.layout.listview_rublish_clean, parent, false);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.size = (TextView) convertView.findViewById(R.id.app_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final CacheInfo item = (CacheInfo) getItem(position);
        holder.appIcon.setImageDrawable(item.getApplicationIcon());
        holder.appName.setText(item.getApplicationName());
        holder.size.setText(Formatter.formatShortFileSize(mContext, item.getCacheSize()));
        holder.packageName = item.getPackageName();

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (viewHolder != null && viewHolder.packageName != null) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + viewHolder.packageName));

            mContext.startActivity(intent);
        }
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView size;
        String packageName;
    }
}
