package com.fionera.cleaner.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.DroidWallApi;
import com.fionera.cleaner.utils.DroidWallApi.DroidApp;
import com.fionera.cleaner.utils.ShowToast;

import java.util.Arrays;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NetworkFragment
        extends Fragment
        implements CompoundButton.OnCheckedChangeListener {

    private Context mContext;
    private ProgressDialog progress = null;
    @Bind(R.id.lv_network_manage)
    ListView listview;


    @OnClick({R.id.fab_network_show_rules, R.id.btn_network_purge_rules, R.id
            .btn_network_apply_rules})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_network_show_rules:
                showRules();
                break;
            case R.id.btn_network_purge_rules:
                purgeRules();
                break;
            case R.id.btn_network_apply_rules:
                applyOrSaveRules();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_network, container, false);
        ButterKnife.bind(this, view);
        DroidWallApi.assertBinaries(mContext, true);
        DroidWallApi.applications = null;
        showOrLoadApplications();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void showOrLoadApplications() {
        if (DroidWallApi.applications == null) {
            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    showApplications();
                }
            };
            new Thread() {
                public void run() {
                    DroidWallApi.getApps(mContext);
                    handler.sendEmptyMessage(0);
                }
            }.start();
        } else {
            showApplications();
        }
    }

    private void showApplications() {
        final DroidApp[] apps = DroidWallApi.getApps(mContext);
        Arrays.sort(apps, new Comparator<DroidApp>() {
            @Override
            public int compare(DroidApp droidApp1, DroidApp droidApp2) {
                if ((droidApp1.selected_wifi | droidApp1.selected_3g) == (droidApp2.selected_wifi
                        | droidApp2.selected_3g)) {
                    return droidApp1.names[0].compareTo(droidApp2.names[0]);
                }
                if (droidApp1.selected_wifi || droidApp1.selected_3g) {
                    return -1;
                }
                return 1;
            }
        });
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final ListAdapter adapter = new ArrayAdapter<DroidApp>(mContext,
                                                               R.layout.listview_network_item,
                                                               R.id.tv_package_info, apps) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder entry;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.listview_network_item, parent, false);
                    entry = new ViewHolder();
                    entry.box_wifi = (CheckBox) convertView.findViewById(R.id.cb_check_wifi);
                    entry.box_wifi.setOnCheckedChangeListener(NetworkFragment.this);
                    entry.box_3g = (CheckBox) convertView.findViewById(R.id.cb_check_3g);
                    entry.box_3g.setOnCheckedChangeListener(NetworkFragment.this);
                    entry.text = (TextView) convertView.findViewById(R.id.tv_package_info);
                    convertView.setTag(entry);
                } else {
                    entry = (ViewHolder) convertView.getTag();
                }
                final DroidApp app = getItem(position);
                entry.text.setText(app.toString());
                entry.box_wifi.setTag(app);
                entry.box_wifi.setChecked(app.selected_wifi);
                entry.box_3g.setTag(app);
                entry.box_3g.setChecked(app.selected_3g);
                return convertView;
            }

            class ViewHolder {
                private CheckBox box_wifi;
                private CheckBox box_3g;
                private TextView text;
            }
        };
        listview.setAdapter(adapter);
    }

    private void applyOrSaveRules() {
        final Handler handler;
        progress = ProgressDialog.show(mContext, "请稍候...", "应用IpTables规则中", true);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (!DroidWallApi.hasRootAccess(mContext, true)) {
                    return;
                }
                if (DroidWallApi.applyIpTablesRules(mContext, true)) {
                    ShowToast.show("规则已应用");
                }
                if (progress != null) {
                    progress.dismiss();
                }
            }
        };
        handler.sendEmptyMessageDelayed(0, 100);
    }

    private void purgeRules() {
        final Handler handler;
        progress = ProgressDialog.show(mContext, "请稍候...", "删除IpTables规则中", true);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (!DroidWallApi.hasRootAccess(mContext, true)) {
                    return;
                }
                if (DroidWallApi.purgeIpTables(mContext, true)) {
                    ShowToast.show("规则已移除");
                }
                if (progress != null) {
                    progress.dismiss();
                }
            }
        };
        handler.sendEmptyMessageDelayed(0, 100);
    }

    private void showRules() {
        final Handler handler;
        progress = ProgressDialog.show(mContext, "请稍候...", "正在获取IpTables规则", true);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (!DroidWallApi.hasRootAccess(mContext, true)) {
                    return;
                }
                DroidWallApi.showIpTablesRules(mContext);
                if (progress != null) {
                    progress.dismiss();
                }
            }
        };
        handler.sendEmptyMessageDelayed(0, 100);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final DroidApp app = (DroidApp) buttonView.getTag();
        if (app != null) {
            switch (buttonView.getId()) {
                case R.id.cb_check_wifi:
                    app.selected_wifi = isChecked;
                    break;
                case R.id.cb_check_3g:
                    app.selected_3g = isChecked;
                    break;
            }
        }
    }
}
