package com.fionera.cleaner.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.fionera.cleaner.base.BaseSwipeBackActivity;
import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.AppUtil;

import butterknife.Bind;

public class AboutActivity
        extends BaseSwipeBackActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tv_about_version)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        textView.setText(AppUtil.getVersion(this));
    }
}
