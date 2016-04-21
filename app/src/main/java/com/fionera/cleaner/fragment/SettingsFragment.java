package com.fionera.cleaner.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;

import com.fionera.cleaner.R;
import com.fionera.cleaner.activity.AboutActivity;
import com.fionera.cleaner.utils.AppUtil;
import com.fionera.cleaner.utils.ShowToast;

public class SettingsFragment
        extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    public static final String SHORTCUT_NAME = "一键加速";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.ui_settings);

        checkInstallShortCut();

        findPreference("createShortCut").setOnPreferenceClickListener(this);
        findPreference("pVersion").setOnPreferenceClickListener(this);
        findPreference("pVersionDetail").setSummary("当前版本：" + AppUtil.getVersion(getActivity()));
        findPreference("pAbout").setOnPreferenceClickListener(this);
    }

    private void checkInstallShortCut() {
        findPreference("createShortCut").setTitle((hasShortcut() ? "删除" : "创建") + "\"一键加速\"快捷方式");
    }

    private boolean hasShortcut() {
        final ContentResolver cr = getActivity().getContentResolver();
        final Uri CONTENT_URI = Uri
                .parse("content://com.android.launcher3.settings/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?",
                            new String[]{"一键加速"}, null);
        if (c != null && c.getCount() > 0) {
            c.close();
            return true;
        }
        return false;
    }

    private void createShortCut(boolean isCreated) {
        Intent aimIntent = new Intent();
        aimIntent.setAction("com.fionera.shortcut");
        aimIntent.addCategory("android.intent.category.DEFAULT");

        Intent intent = new Intent();
        intent.setAction(
                "com.android.launcher.action." + (isCreated ? "UNINSTALL_SHORTCUT" :
                        "INSTALL_SHORTCUT"));
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, SHORTCUT_NAME);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory
                .decodeResource(getResources(), R.drawable.iv_short_cut_center_light));
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, aimIntent);
        intent.putExtra("duplicate", false);

        getActivity().sendBroadcast(intent);
        ShowToast.show("\"一键加速\"快捷图标已" + (isCreated ? "删除" : "创建"));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            checkInstallShortCut();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("createShortCut".equals(preference.getKey())) {
            createShortCut(hasShortcut());
        } else if ("pVersion".equals(preference.getKey())) {
            ShowToast.show("暂无新版本");
        } else if ("pAbout".equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), AboutActivity.class));
        }
        return false;
    }
}
