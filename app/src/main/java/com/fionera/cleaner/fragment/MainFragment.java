package com.fionera.cleaner.fragment;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fionera.cleaner.bean.SDCardInfo;
import com.fionera.cleaner.activity.AutoStartManageActivity;
import com.fionera.cleaner.activity.MemoryCleanActivity;
import com.fionera.cleaner.activity.RubbishCleanActivity;
import com.fionera.cleaner.utils.AppUtil;
import com.fionera.cleaner.utils.StorageUtil;
import com.fionera.cleaner.widget.ArcProgress;
import com.fionera.cleaner.R;
import com.fionera.cleaner.activity.SoftwareManageActivity;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

public class MainFragment
        extends Fragment {

    private Context mContext;

    @Bind(R.id.arc_home_storage)
    ArcProgress arcStore;
    @Bind(R.id.capacity)
    TextView capacity;

    @Bind(R.id.arc_home_process)
    ArcProgress arcProcess;

    private ValueAnimator valueAnimator1;
    private ValueAnimator valueAnimator2;

    @OnClick(R.id.tv_main_clean)
    void speedUp() {
        startActivity(new Intent(mContext, MemoryCleanActivity.class));
    }

    @OnClick(R.id.tv_main_rubbish)
    void rubbishClean() {
        startActivity(new Intent(mContext, RubbishCleanActivity.class));
    }

    @OnClick(R.id.tv_main_auto)
    void AutoStartManage() {
        startActivity(new Intent(mContext, AutoStartManageActivity.class));
    }

    @OnClick(R.id.tv_main_soft)
    void SoftwareManage() {
        startActivity(new Intent(mContext, SoftwareManageActivity.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
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
        final int offset = (int) Math
                .round(((TotalBlocks - availBlock) / (double) TotalBlocks) * 100);

        arcStore.setProgress(1);
        valueAnimator1 = ValueAnimator.ofFloat(0, offset);
        valueAnimator1.setDuration(2000);
        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (arcStore.getProgress() >= offset) {
                    arcStore.setProgress(offset);
                } else {
                    arcStore.setProgress(Math.round(
                            arcStore.getProgress() + offset * ((float) animation
                                    .getAnimatedValue()) / animation.getDuration()));
                }
            }
        });
        valueAnimator1.start();
        capacity.setText(StorageUtil.convertStorage(TotalBlocks - availBlock) + "/" + StorageUtil
                .convertStorage(TotalBlocks));

        long availMemory = AppUtil.getAvailMemory(mContext);
        long totalMemory = AppUtil.getTotalMemory(mContext);
        final int offset2 = (int) Math
                .round(((totalMemory - availMemory) / (double) totalMemory) * 100);

        arcProcess.setProgress(1);
        valueAnimator2 = ValueAnimator.ofFloat(0, offset);
        valueAnimator2.setDuration(2500);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (arcProcess.getProgress() >= offset2) {
                    arcProcess.setProgress(offset2);
                } else {
                    arcProcess.setProgress(Math.round(
                            arcProcess.getProgress() + offset2 * ((float) animation
                                    .getAnimatedValue()) / animation.getDuration()));
                }
            }
        });
        valueAnimator2.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        valueAnimator1.cancel();
        valueAnimator2.cancel();
        valueAnimator1 = null;
        valueAnimator2 = null;
    }
}
