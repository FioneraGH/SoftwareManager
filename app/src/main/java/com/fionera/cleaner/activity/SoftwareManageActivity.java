package com.fionera.cleaner.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.fionera.cleaner.R;
import com.fionera.cleaner.base.BaseSwipeBackActivity;
import com.fionera.cleaner.fragment.SoftwareManageFragment;
import com.fionera.cleaner.fragment.WeakFragmentPagerAdapter;

import butterknife.Bind;


public class SoftwareManageActivity extends BaseSwipeBackActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tl_software_manage)
    TabLayout tabLayout;
    @Bind(R.id.vp_software_manage)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_software_manage);

        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyPagerAdapter extends WeakFragmentPagerAdapter {

        private final String[] TITLES = {"用户软件", "预装软件"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            SoftwareManageFragment fragment = new SoftwareManageFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            fragment.setArguments(bundle);
            saveFragment(fragment);
            return fragment;
        }
    }
}
