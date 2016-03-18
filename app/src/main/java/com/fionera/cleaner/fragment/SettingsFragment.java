package com.fionera.cleaner.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.fionera.cleaner.R;
import com.fionera.cleaner.activity.AboutActivity;
import com.fionera.cleaner.utils.AppUtil;
import com.fionera.cleaner.utils.ShowToast;

public class SettingsFragment
        extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.ui_settings);

        findPreference("createShortCut").setOnPreferenceClickListener(this);
        findPreference("pVersion").setOnPreferenceClickListener(this);
        findPreference("pVersionDetail").setSummary("当前版本：" + AppUtil.getVersion(getActivity()));
        findPreference("pAbout").setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("createShortCut".equals(preference.getKey())) {
            createShortCut();
        } else if ("pVersion".equals(preference.getKey())) {
            ShowToast.show("暂无新版本");
        } else if ("pAbout".equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), AboutActivity.class));
        }
        return false;
    }

    private void createShortCut() {
        Intent intent = new Intent();
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "一键加速");
        intent.putExtra("duplicate", false);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        BitmapFactory.decodeResource(getResources(), R.drawable.short_cut_icon));
        Intent aimIntent = new Intent();
        aimIntent.setAction("com.fionera.shortcut");
        aimIntent.addCategory("android.intent.category.DEFAULT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, aimIntent);
        getActivity().sendBroadcast(intent);
        ShowToast.show("\"一键加速\"快捷图标已创建");
    }
}
