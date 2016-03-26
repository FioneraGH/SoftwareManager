package com.fionera.cleaner.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import com.fionera.cleaner.R;
import com.fionera.cleaner.base.BaseActivity;
import com.fionera.cleaner.fragment.MainFragment;
import com.fionera.cleaner.fragment.NavigationDrawerFragment;
import com.fionera.cleaner.fragment.NetworkFragment;
import com.fionera.cleaner.fragment.SettingsFragment;
import com.fionera.cleaner.utils.ShowToast;

import butterknife.Bind;

public class MainActivity
        extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private MainFragment mMainFragment;
    private NetworkFragment mNetworkFragment;
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(toolbar);
        ViewCompat.setElevation(toolbar, 8);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                                                                        toolbar, 0, 0);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        onNavigationDrawerItemSelected(0);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        ActionBar ab = getSupportActionBar();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mMainFragment != null) {
            transaction.hide(mMainFragment);
        }
        if (mNetworkFragment != null) {
            transaction.hide(mNetworkFragment);
        }
        if (mSettingsFragment != null) {
            transaction.hide(mSettingsFragment);
        }

        switch (position) {
            case 0:
                if (ab != null) {
                    ab.setTitle("软件管理");
                }
                if (mMainFragment == null) {
                    mMainFragment = new MainFragment();
                    transaction.add(R.id.fl_home_container, mMainFragment);
                } else {
                    transaction.show(mMainFragment);
                }
                transaction.commit();
                mDrawerLayout.closeDrawers();
                break;
            case 1:
                if (ab != null) {
                    ab.setTitle("网络控制");
                }
                if (mNetworkFragment == null) {
                    mNetworkFragment = new NetworkFragment();
                    transaction.add(R.id.fl_home_container, mNetworkFragment);
                } else {
                    transaction.show(mNetworkFragment);
                }
                transaction.commit();
                mDrawerLayout.closeDrawers();
                break;
            case 2:
                if (ab != null) {
                    ab.setTitle("设置");
                }
                if (mSettingsFragment == null) {
                    mSettingsFragment = new SettingsFragment();
                    transaction.add(R.id.fl_home_container, mSettingsFragment);
                } else {
                    transaction.show(mSettingsFragment);
                }
                transaction.commit();
                mDrawerLayout.closeDrawers();
                break;
        }
    }

    /**
     * 设置双击退出
     */
    private long exitTime = 0L;

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2500) {
            ShowToast.show("再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
