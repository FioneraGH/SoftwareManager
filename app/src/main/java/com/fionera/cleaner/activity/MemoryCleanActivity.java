package com.fionera.cleaner.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.adapter.ClearMemoryAdapter;
import com.fionera.cleaner.base.BaseSwipeBackActivity;
import com.fionera.cleaner.bean.AppProcessInfo;
import com.fionera.cleaner.bean.StorageSize;
import com.fionera.cleaner.service.CoreService;
import com.fionera.cleaner.utils.ShowToast;
import com.fionera.cleaner.utils.StorageUtil;
import com.fionera.cleaner.widget.textcounter.CounterView;
import com.fionera.cleaner.widget.textcounter.formatters.DecimalFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class MemoryCleanActivity extends BaseSwipeBackActivity
        implements CoreService.OnProcessActionListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.rl_header)
    RelativeLayout header;
    @Bind(R.id.tc_counter)
    CounterView textCounter;
    @Bind(R.id.tv_postfix)
    TextView tvPostFix;

    @Bind(R.id.listview)
    ListView mListView;
    List<AppProcessInfo> mAppProcessInfos = new ArrayList<>();
    ClearMemoryAdapter mClearMemoryAdapter;

    @Bind(R.id.btn_clear)
    Button btnClear;

    @Bind(R.id.ll_loading_progress)
    LinearLayout mProgressBar;
    @Bind(R.id.tv_progress_text)
    TextView mProgressBarText;

    public long allMemory;

    private CoreService mCoreService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(MemoryCleanActivity.this);
            mCoreService.scanRunProcess();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService.setOnActionListener(null);
            mCoreService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_clean);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mClearMemoryAdapter = new ClearMemoryAdapter(mContext, mAppProcessInfos);
        mListView.setAdapter(mClearMemoryAdapter);

        bindService(new Intent(mContext, CoreService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);

        textCounter.setAutoFormat(false);
        textCounter.setAutoStart(false);
        textCounter.setFormatter(new DecimalFormatter());
        textCounter.setIncrement(5f);
        textCounter.setTimeInterval(50);
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
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {
        mAppProcessInfos.clear();

        allMemory = 0;
        for (AppProcessInfo appInfo : apps) {
            if (!appInfo.isSystem) {
                mAppProcessInfos.add(appInfo);
                allMemory += appInfo.memory;
            }
        }

        refreshTextCounter();

        mClearMemoryAdapter.notifyDataSetChanged();
        showProgressBar(false);


        if (apps.size() > 0) {
            header.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);
        } else {
            header.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {

    }

    @OnClick(R.id.btn_clear)
    public void onClickClear() {
        long killAppMemory = 0;

        for (int i = mAppProcessInfos.size() - 1; i >= 0; i--) {
            if (mAppProcessInfos.get(i).checked) {
                killAppMemory += mAppProcessInfos.get(i).memory;
                mCoreService.killBackgroundProcesses(mAppProcessInfos.get(i).processName);
                mAppProcessInfos.remove(mAppProcessInfos.get(i));
                mClearMemoryAdapter.notifyDataSetChanged();
            }
        }
        allMemory = allMemory - killAppMemory;
        ShowToast.show("共清理" + StorageUtil.convertStorage(killAppMemory) + "内存");
        if (allMemory > 0) {
            refreshTextCounter();
        }
    }

    private void refreshTextCounter() {
        StorageSize mStorageSize = StorageUtil.convertStorageSize(allMemory);
        textCounter.setStartValue(0f);
        textCounter.setEndValue(mStorageSize.value);
        tvPostFix.setText(mStorageSize.suffix);
        textCounter.start();
    }

    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
