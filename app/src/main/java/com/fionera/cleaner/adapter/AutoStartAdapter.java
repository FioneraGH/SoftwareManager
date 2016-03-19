package com.fionera.cleaner.adapter;

import android.content.Context;
import android.os.Handler;
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

public class AutoStartAdapter extends BaseAdapter {

    public List<AutoStartInfo> mlistAppInfo;
    LayoutInflater infater = null;
    private Context mContext;
    public static List<Integer> clearIds;
    private Handler mHandler;

    public AutoStartAdapter(Context context, List<AutoStartInfo> apps, Handler mHandler) {
        infater = LayoutInflater.from(context);
        mContext = context;
        clearIds = new ArrayList<Integer>();
        this.mlistAppInfo = apps;
        this.mHandler = mHandler;
    }

    @Override
    public int getCount() {
        return mlistAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = infater.inflate(R.layout.listview_auto_start,
                    null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView
                    .findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView
                    .findViewById(R.id.app_name);
            holder.size = (TextView) convertView
                    .findViewById(R.id.app_size);
            holder.disable_switch = (TextView) convertView
                    .findViewById(R.id.disable_switch);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final AutoStartInfo item = (AutoStartInfo) getItem(position);
        if (item != null) {
            holder.appIcon.setImageDrawable(item.getIcon());
            holder.appName.setText(item.getLabel());
            if (item.isEnable()) {
                holder.disable_switch.setBackgroundResource(R.drawable.ic_switch_on);
                holder.disable_switch.setText("已开启");
            } else {
                holder.disable_switch.setBackgroundResource(R.drawable.ic_switch_off);
                holder.disable_switch.setText("已禁止");
            }
            // holder.size.setText(Formatter.formatShortFileSize(mContext, item.getCacheSize()));

            holder.disable_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ShellUtils.checkRootPermission()) {
                        if (item.isEnable()) {
                            diasableApp(item);
                        } else {

                            enableApp(item);
                        }
                    } else {
                        ShowToast.show("该功能需要获取系统root权限，点击允许获取root权限");
                    }
                }
            });
            holder.packageName = item.getPackageName();
        }


        return convertView;
    }

    private void diasableApp(AutoStartInfo item) {
        String packageReceiverList[] = item.getPackageReceiver().toString().split(";");

        List<String> mSring = new ArrayList<>();
        for (int j = 0; j < packageReceiverList.length; j++) {
            String cmd = "pm disable " + packageReceiverList[j];
            //部分receiver包含$符号，需要做进一步处理，用"$"替换掉$
            cmd = cmd.replace("$", "\"" + "$" + "\"");
            //执行命令
            mSring.add(cmd);
        }
        ShellUtils.CommandResult mCommandResult = ShellUtils.execCommand(mSring, true, true);

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
        String packageReceiverList[] = item.getPackageReceiver().toString().split(";");

        List<String> mSring = new ArrayList<>();
        for (int j = 0; j < packageReceiverList.length; j++) {
            String cmd = "pm enable " + packageReceiverList[j];
            //部分receiver包含$符号，需要做进一步处理，用"$"替换掉$
            cmd = cmd.replace("$", "\"" + "$" + "\"");
            //执行命令
            mSring.add(cmd);

        }
        ShellUtils.CommandResult mCommandResult = ShellUtils.execCommand(mSring, true, true);

        if (mCommandResult.result == 0) {
            ShowToast.show( item.getLabel() + "已开启");
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
        TextView disable_switch;

        String packageName;
    }

}
