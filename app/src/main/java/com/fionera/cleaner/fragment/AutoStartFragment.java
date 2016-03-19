package com.fionera.cleaner.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fionera.cleaner.adapter.AutoStartAdapter;
import com.fionera.cleaner.bean.AutoStartInfo;
import com.fionera.cleaner.utils.BootStartUtils;
import com.fionera.cleaner.utils.ShellUtils;
import com.fionera.cleaner.utils.ShowToast;
import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.RootUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

public class AutoStartFragment
        extends Fragment {

    Context mContext;
    private int position;
    AutoStartAdapter mAutoStartAdapter;

    @Bind(R.id.tv_top_tips)
    TextView topText;
    @Bind(R.id.disable_button)
    Button disableButton;
    @Bind(R.id.listview)
    ListView listview;
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
        mContext = getActivity();
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fillData();
    }

    @OnClick(R.id.disable_button)
    public void onClickDisable() {
        RootUtil.preparezlsu(mContext);
        disableAPP();
    }

    private void disableAPP() {
        List<String> mSring = new ArrayList<>();
        for (AutoStartInfo auto : noSystemAuto) {
            if (auto.isEnable()) {
                String packageReceiverList[] = auto.getPackageReceiver().toString().split(";");
                for (int j = 0; j < packageReceiverList.length; j++) {
                    String cmd = "pm disable " + packageReceiverList[j];
                    //部分receiver包含$符号，需要做进一步处理，用"$"替换掉$
                    cmd = cmd.replace("$", "\"" + "$" + "\"");
                    //执行命令
                    mSring.add(cmd);
                }
            }
        }

        ShellUtils.CommandResult mCommandResult = ShellUtils.execCommand(mSring, true, true);
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


    private void fillData() {

        if (position == 0) {
            topText.setText("禁止用户软件自启,可提升运行速度");
        } else {
            topText.setText("禁止系统核心软件自启,可能影响手机的正常使用");
        }

        List<AutoStartInfo> mAutoStartInfo = BootStartUtils.fetchAutoApps(mContext);

        //   List<AutoStartInfo> mAutoStartInfo = BootStartUtils.fetchInstalledApps(mContext);
        noSystemAuto = new ArrayList<>();
        isSystemAuto = new ArrayList<>();

        for (AutoStartInfo a : mAutoStartInfo) {
            if (a.isSystem()) {
                isSystemAuto.add(a);
            } else {
                noSystemAuto.add(a);
            }
        }

        if (position == 0) {
            mAutoStartAdapter = new AutoStartAdapter(mContext, noSystemAuto, mHandler);
            listview.setAdapter(mAutoStartAdapter);
            refreshClearButton();
        } else {
            mAutoStartAdapter = new AutoStartAdapter(mContext, isSystemAuto, null);
            listview.setAdapter(mAutoStartAdapter);
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
                disableButton.setVisibility(View.VISIBLE);
                disableButton.setText("可优化" + canDisableCom + "款");
            } else {
                disableButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
