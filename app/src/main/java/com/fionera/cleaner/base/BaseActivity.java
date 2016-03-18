package com.fionera.cleaner.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.fionera.cleaner.dialogs.ProgressDialogFragment;

import butterknife.ButterKnife;

public abstract class BaseActivity
        extends AppCompatActivity {

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected float mDensity;
    protected Context mContext;

    private static String mDialogTag = "BaseDialog";

    ProgressDialogFragment mProgressDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;
        mDensity = metric.density;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    protected void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    protected void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(mContext, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    protected void startActivity(String action) {
        startActivity(action, null);
    }

    protected void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void showDialogLoading() {
        showDialogLoading(null);
    }

    public void showDialogLoading(String msg) {
        if (mProgressDialogFragment == null) {
            mProgressDialogFragment = ProgressDialogFragment.newInstance(0, null);
        }
        if (msg != null) {
            mProgressDialogFragment.setMessage(msg);
        }
        mProgressDialogFragment.show(getFragmentManager(), mDialogTag);

    }

    public void dismissDialogLoading() {
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
        }
    }
}
