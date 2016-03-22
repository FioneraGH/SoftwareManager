package com.fionera.cleaner.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.adapter.RubbishMemoryAdapter;
import com.fionera.cleaner.base.BaseSwipeBackActivity;
import com.fionera.cleaner.bean.CacheInfo;
import com.fionera.cleaner.bean.StorageSizeInfo;
import com.fionera.cleaner.service.CleanerService;
import com.fionera.cleaner.utils.ShowToast;
import com.fionera.cleaner.utils.StorageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;


public class RubbishCleanActivity
        extends BaseSwipeBackActivity
        implements CleanerService.OnActionListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.listview)
    ListView mListView;
    List<CacheInfo> mCacheInfo = new ArrayList<>();
    RubbishMemoryAdapter rubbishMemoryAdapter;

    @Bind(R.id.rl_header)
    RelativeLayout header;
    @Bind(R.id.tc_counter)
    TextView textCounter;
    @Bind(R.id.tv_postfix)
    TextView suffix;

    @Bind(R.id.ll_loading_progress)
    LinearLayout mProgressBar;
    @Bind(R.id.tv_progress_text)
    TextView mProgressBarText;

    @Bind(R.id.btn_clear)
    Button btnClear;

    private boolean mReadyScanned = false;

    private CleanerService mCleanerService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCleanerService = ((CleanerService.CleanerServiceBinder) service).getService();
            mCleanerService.setOnActionListener(RubbishCleanActivity.this);
            if (!mCleanerService.isScanning() && !mReadyScanned) {
                mCleanerService.scanCache();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCleanerService.setOnActionListener(null);
            mCleanerService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rublish_clean);

        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        rubbishMemoryAdapter = new RubbishMemoryAdapter(mContext, mCacheInfo);
        mListView.setAdapter(rubbishMemoryAdapter);
        mListView.setOnItemClickListener(rubbishMemoryAdapter);
        bindService(new Intent(mContext, CleanerService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onScanStarted(Context context) {
        mProgressBarText.setText(R.string.scanning);
        showProgressBar(true);
    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {
        mProgressBarText.setText(getString(R.string.scanning_m_of_n, current, max));
    }

    @Override
    public void onScanCompleted(Context context, List<CacheInfo> apps) {
        mCacheInfo.clear();
        mCacheInfo.addAll(apps);

        ShowToast.show(mCacheInfo.size());

        StorageSizeInfo mStorageSizeInfo = StorageUtil
                .convertStorageSize(mCleanerService != null ? mCleanerService.getCacheSize() : 0);
        suffix.setText(mStorageSizeInfo.suffix);
        textCounter.setText(String.format(Locale.CHINA, "%.2f", mStorageSizeInfo.value));
        rubbishMemoryAdapter.notifyDataSetChanged();
        showProgressBar(false);

        if (apps.size() > 0) {
            header.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);
        } else {
            header.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
        }

        if (!mReadyScanned) {
            mReadyScanned = true;
        }
    }

    @Override
    public void onCleanStarted(Context context) {
        if (mProgressBar.isShown()) {
            showProgressBar(false);
        }

        if (!RubbishCleanActivity.this.isFinishing()) {
            showDialogLoading();
        }
    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {
        dismissDialogLoading();
        ShowToast.show(context.getString(R.string.cleaned,
                                         Formatter.formatShortFileSize(mContext, cacheSize)));
        suffix.setText("B");
        textCounter.setText(String.format(Locale.CHINA, "%.2f", 0f));
        btnClear.setVisibility(View.GONE);
        mCacheInfo.clear();
        rubbishMemoryAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.btn_clear)
    public void onClickClear() {
        if (mCleanerService != null && !mCleanerService.isScanning() &&
                !mCleanerService.isCleaning() && mCleanerService.getCacheSize() > 0) {
            mCleanerService.cleanCache();
        }
    }

    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(
                    AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

}
