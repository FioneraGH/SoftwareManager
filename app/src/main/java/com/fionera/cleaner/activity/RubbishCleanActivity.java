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
import com.fionera.cleaner.bean.CacheListItem;
import com.fionera.cleaner.bean.StorageSize;
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
    List<CacheListItem> mCacheListItem = new ArrayList<>();
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

    private boolean mAlreadyScanned = false;

    private CleanerService mCleanerService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCleanerService = ((CleanerService.CleanerServiceBinder) service).getService();
            mCleanerService.setOnActionListener(RubbishCleanActivity.this);
            if (!mCleanerService.isScanning() && !mAlreadyScanned) {
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

        rubbishMemoryAdapter = new RubbishMemoryAdapter(mContext, mCacheListItem);
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
    public void onScanCompleted(Context context, List<CacheListItem> apps) {
        showProgressBar(false);
        mCacheListItem.clear();
        mCacheListItem.addAll(apps);
        rubbishMemoryAdapter.notifyDataSetChanged();
        header.setVisibility(View.GONE);
        if (apps.size() > 0) {
            header.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);

            long medMemory = mCleanerService != null ? mCleanerService.getCacheSize() : 0;

            StorageSize mStorageSize = StorageUtil.convertStorageSize(medMemory);
            suffix.setText(mStorageSize.suffix);
            textCounter.setText(String.format(Locale.CHINA, "%.2f", mStorageSize.value));
        } else {
            header.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
        }

        if (!mAlreadyScanned) {
            mAlreadyScanned = true;
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
        header.setVisibility(View.GONE);
        btnClear.setVisibility(View.GONE);
        mCacheListItem.clear();
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
