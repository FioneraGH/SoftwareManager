package com.fionera.cleaner.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.bean.AutoStartInfo;
import com.fionera.cleaner.utils.RvItemTouchListener;
import com.fionera.cleaner.utils.ShellUtils;
import com.fionera.cleaner.utils.ShowToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AutoStartAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AutoStartInfo> mListAppInfo;
    private LayoutInflater inflater = null;
    private Handler mHandler;

    private RvItemTouchListener rvItemTouchListener;

    public void setRvItemTouchListener(RvItemTouchListener rvItemTouchListener) {
        this.rvItemTouchListener = rvItemTouchListener;
    }

    public AutoStartAdapter(Context context, List<AutoStartInfo> apps, Handler mHandler) {
        this.inflater = LayoutInflater.from(context);
        this.mListAppInfo = apps;
        this.mHandler = mHandler;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.rv_auto_start_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder theHolder = (ViewHolder) holder;
        theHolder.appIcon
                .setImageDrawable(mListAppInfo.get(theHolder.getAdapterPosition()).getIcon());
        theHolder.appName.setText(mListAppInfo.get(theHolder.getAdapterPosition()).getLabel());
        theHolder.switchCompat
                .setChecked(mListAppInfo.get(theHolder.getAdapterPosition()).isEnable());
        theHolder.switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShellUtils.checkRootPermission()) {
                    if (mListAppInfo.get(theHolder.getAdapterPosition()).isEnable()) {
                        disableApp(mListAppInfo.get(theHolder.getAdapterPosition()));
                    } else {

                        enableApp(mListAppInfo.get(theHolder.getAdapterPosition()));
                    }
                } else {
                    ShowToast.show("该功能需要获取系统root权限");
                }
            }
        });
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
        ShellUtils.CommandResult mCommandResult = ShellUtils
                .execCommand(commandStrings, true, true);

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

    class ViewHolder
            extends RecyclerView.ViewHolder {
        @Bind(R.id.app_icon)
        ImageView appIcon;
        @Bind(R.id.app_name)
        TextView appName;
        @Bind(R.id.app_size)
        TextView size;
        @Bind(R.id.sw_disable)
        SwitchCompat switchCompat;

        String packageName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
