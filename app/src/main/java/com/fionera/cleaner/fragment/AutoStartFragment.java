package com.fionera.cleaner.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.adapter.AutoStartAdapter;
import com.fionera.cleaner.base.BaseFragment;
import com.fionera.cleaner.bean.AutoStartInfo;
import com.fionera.cleaner.utils.BootStartUtils;
import com.fionera.cleaner.utils.ShellUtils;
import com.fionera.cleaner.utils.ShowToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AutoStartFragment
        extends BaseFragment {

    private int position;
    private AutoStartAdapter mAutoStartAdapter;

    @Bind(R.id.tv_top_tips)
    TextView tvTips;
    @Bind(R.id.btn_disable_all)
    Button btnDisable;
    @Bind(R.id.listview)
    ListView listView;
    List<AutoStartInfo> isSystemAuto = null;
    List<AutoStartInfo> noSystemAuto = null;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            refreshClearButton();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        position = getArguments().getInt("position");
        View view = inflater.inflate(R.layout.fragment_auto_start, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillData();
    }

    private void fillData() {

        if (position == 0) {
            tvTips.setText("禁止用户软件自启,可提升运行速度");
        } else {
            tvTips.setText("禁止系统核心软件自启,可能影响手机的正常使用");
        }

        List<AutoStartInfo> autoStartInfoList = BootStartUtils.fetchAutoApps(mContext);

        noSystemAuto = new ArrayList<>();
        isSystemAuto = new ArrayList<>();

        for (AutoStartInfo autoStartInfo : autoStartInfoList) {
            if (autoStartInfo.isSystem()) {
                isSystemAuto.add(autoStartInfo);
            } else {
                noSystemAuto.add(autoStartInfo);
            }
        }

        if (position == 0) {
            mAutoStartAdapter = new AutoStartAdapter(mContext, noSystemAuto, mHandler);
            listView.setAdapter(mAutoStartAdapter);
            refreshClearButton();
        } else {
            mAutoStartAdapter = new AutoStartAdapter(mContext, isSystemAuto, null);
            listView.setAdapter(mAutoStartAdapter);
        }
    }

    @OnClick(R.id.btn_disable_all)
    public void onClickDisable() {
        disableAPP();
    }

    private void disableAPP() {
        List<String> commandStrings = new ArrayList<>();
        for (AutoStartInfo auto : noSystemAuto) {
            if (auto.isEnable()) {
                String packageReceiverList[] = auto.getPackageReceiver().split(";");
                for (String packageReceiver : packageReceiverList) {
                    String cmd = "pm disable " + packageReceiver;
                    /**
                     * 部分receiver包含$符号，需要做进一步处理，用"$"替换掉$
                     */
                    cmd = cmd.replace("$", "\"$\"");
                    commandStrings.add(cmd);
                }
            }
        }

        ShellUtils.CommandResult mCommandResult = ShellUtils.execCommand(commandStrings, true, true);
        if (mCommandResult.result == 0) {
            ShowToast.show("应用已禁止");
            for (AutoStartInfo auto : noSystemAuto) {
                if (auto.isEnable()) {
                    auto.setEnable(false);
                }
            }
            mAutoStartAdapter.notifyDataSetChanged();
            refreshClearButton();
        } else {
            ShowToast.show("该功能需要获取系统root权限，请允许获取root权限");
        }
    }

    private void refreshClearButton() {
        if (position == 0) {
            int canDisableCom = 0;
            for (AutoStartInfo autoS : noSystemAuto) {
                if (autoS.isEnable()) {
                    canDisableCom++;
                }
            }
            if (canDisableCom > 0) {
                btnDisable.setVisibility(View.VISIBLE);
                btnDisable.setText("可优化" + canDisableCom + "款");
            } else {
                btnDisable.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
