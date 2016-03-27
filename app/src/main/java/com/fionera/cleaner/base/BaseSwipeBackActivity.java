package com.fionera.cleaner.base;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.ShowToast;
import com.fionera.cleaner.widget.SwipeBackLayout;

public abstract class BaseSwipeBackActivity
        extends BaseActivity {
    private SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(this)
                .inflate(R.layout.ui_swipeback_layout, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSwipeBackLayout.attachToActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_activity_exit, R.anim.anim_trans_right_out);
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null) {
            return mSwipeBackLayout.findViewById(id);
        }
        return v;
    }
}
