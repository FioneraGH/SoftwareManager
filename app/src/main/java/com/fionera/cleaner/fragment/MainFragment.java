package com.fionera.cleaner.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fionera.cleaner.base.BaseFragment;
import com.fionera.cleaner.model.SDCardInfo;
import com.fionera.cleaner.activity.AutoStartManageActivity;
import com.fionera.cleaner.activity.MemoryCleanActivity;
import com.fionera.cleaner.activity.RubbishCleanActivity;
import com.fionera.cleaner.utils.AppUtil;
import com.fionera.cleaner.utils.StorageUtil;
import com.fionera.cleaner.widget.circleprogress.ArcProgress;
import com.fionera.cleaner.R;
import com.fionera.cleaner.activity.SoftwareManageActivity;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

public class MainFragment extends BaseFragment {

    @Bind(R.id.arc_home_storage)
    ArcProgress arcStore;
    @Bind(R.id.capacity)
    TextView capacity;
    @Bind(R.id.arc_home_process)
    ArcProgress arcProcess;

    Context mContext;

    private Timer timer;
    private Timer timer2;

    @OnClick(R.id.card1)
    void speedUp() {
        startActivity(MemoryCleanActivity.class);
    }

    @OnClick(R.id.card2)
    void rubbishClean() {
        startActivity(RubbishCleanActivity.class);
    }

    @OnClick(R.id.card3)
    void AutoStartManage() {
        startActivity(AutoStartManageActivity.class);
    }

    @OnClick(R.id.card4)
    void SoftwareManage() {
        startActivity(SoftwareManageActivity.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        mContext = getActivity();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        timer = new Timer();
        timer2 = new Timer();

        long availMemory = AppUtil.getAvailMemory(mContext);
        long totalMemory = AppUtil.getTotalMemory(mContext);
        final double offset = (((totalMemory - availMemory) / (double) totalMemory) * 100);

        arcProcess.setProgress(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (arcProcess.getProgress() >= (int) offset) {
                            timer.cancel();
                        } else {
                            arcProcess.setProgress(arcProcess.getProgress() + 1);
                        }
                    }
                });
            }
        }, 50, 20);

        SDCardInfo mSDCardInfo = StorageUtil.getSDCardInfo();
        SDCardInfo mSystemInfo = StorageUtil.getSystemSpaceInfo(mContext);

        long availBlock;
        long TotalBlocks;
        if (mSDCardInfo != null) {
            availBlock = mSDCardInfo.free + mSystemInfo.free;
            TotalBlocks = mSDCardInfo.total + mSystemInfo.total;
        } else {
            availBlock = mSystemInfo.free;
            TotalBlocks = mSystemInfo.total;
        }

        final double percentStore = (((TotalBlocks - availBlock) / (double) TotalBlocks) * 100);

        capacity.setText(StorageUtil.convertStorage(TotalBlocks - availBlock) + "/" + StorageUtil.convertStorage(TotalBlocks));
        arcStore.setProgress(0);

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (arcStore.getProgress() >= (int) percentStore) {
                            timer2.cancel();
                        } else {
                            arcStore.setProgress(arcStore.getProgress() + 1);
                        }
                    }
                });
            }
        }, 50, 20);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer2.cancel();
    }
}
