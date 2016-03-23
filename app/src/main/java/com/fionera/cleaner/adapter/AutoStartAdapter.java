package com.fionera.cleaner.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.bean.AutoStartInfo;
import com.fionera.cleaner.utils.ShellUtils;
import com.fionera.cleaner.utils.ShowToast;

import java.util.ArrayList;
import java.util.List;

public class AutoStartAdapter
        extends BaseAdapter {

    private List<AutoStartInfo> mListAppInfo;
    private LayoutInflater inflater = null;
    private Handler mHandler;

    public AutoStartAdapter(Context context, List<AutoStartInfo> apps, Handler mHandler) {
        this.inflater = LayoutInflater.from(context);
        this.mListAppInfo = apps;
        this.mHandler = mHandler;
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
            convertView = inflater.inflate(R.layout.listview_auto_start, parent, false);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.size = (TextView) convertView.findViewById(R.id.app_size);
            holder.switchCompat = (SwitchCompat) convertView.findViewById(R.id.sw_disable);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final AutoStartInfo item = (AutoStartInfo) getItem(position);
        holder.appIcon.setImageDrawable(item.getIcon());
        holder.appName.setText(item.getLabel());
        holder.switchCompat.setChecked(item.isEnable());
        holder.switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShellUtils.checkRootPermission()) {
                    if (item.isEnable()) {
                        disableApp(item);
                    } else {

                        enableApp(item);
                    }
                } else {
                    ShowToast.show("该功能需要获取系统root权限");
                }
            }
        });
        holder.packageName = item.getPackageName();

        return convertView;
    }

    private void disableApp(AutoStartInfo item) {
        String packageReceiverList[] = item.getPackageReceiver().split(";");
        List<String> commandStrings = new ArrayList<>();
        for (String aPackageReceiverList : packageReceiverList) {
            String cmd = "pm disable " + aPackageReceiverList;
            /**
             * 部分receiver包含$符号，需要做进一步处理，用"$"替换掉$
             */
            cmd = cmd.replace("$", "\"" + "$" + "\"");
            commandStrings.add(cmd);
        }
        ShellUtils.CommandResult mCommandResult = ShellUtils.execCommand(commandStrings, true, true);

        if (mCommandResult.result == 0) {
            ShowToast.show(item.getLabel() + "已禁止");
            item.setEnable(false);
            notifyDataSetChanged();
            if (mHandler != null) {
                mHandler.sendEmptyMessage(0);
            }
        } else {
            ShowToast.show(item.getLabel() + "禁止失败");
        }
    }

    private void enableApp(AutoStartInfo item) {
        String packageReceiverList[] = item.getPackageReceiver().split(";");
        List<String> mSring = new ArrayList<>();
        for (String aPackageReceiverList : packageReceiverList) {
            String cmd = "pm enable " + aPackageReceiverList;
            /**
             * 部分receiver包含$符号，需要做进一步处理，用"$"替换掉$
             */
            cmd = cmd.replace("$", "\"" + "$" + "\"");
            mSring.add(cmd);

        }
        ShellUtils.CommandResult mCommandResult = ShellUtils.execCommand(mSring, true, true);

        if (mCommandResult.result == 0) {
            ShowToast.show(item.getLabel() + "已开启");
            item.setEnable(true);
            notifyDataSetChanged();
            if (mHandler != null) {
                mHandler.sendEmptyMessage(0);
            }
        } else {
            ShowToast.show(item.getLabel() + "开启失败");
        }
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView size;
        SwitchCompat switchCompat;
        String packageName;
    }
}
