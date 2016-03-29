package com.fionera.cleaner.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fionera.cleaner.R;
import com.fionera.cleaner.base.BaseActivity;
import com.fionera.cleaner.bean.AppProcessInfo;
import com.fionera.cleaner.service.CoreService;
import com.fionera.cleaner.utils.ShowToast;
import com.fionera.cleaner.utils.StorageUtil;

import java.util.List;

import butterknife.Bind;

public class ShortCutActivity
        extends BaseActivity
        implements CoreService.OnProcessActionListener {

    @Bind(R.id.iv_short_cut_cleaning)
    ImageView cleanLightImg;

    private ObjectAnimator objectAnimator;

    private CoreService mCoreService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(ShortCutActivity.this);
            mCoreService.cleanAllProcess();
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
        setContentView(R.layout.activity_short_cut);

        bindService(new Intent(mContext, CoreService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        objectAnimator = ObjectAnimator.ofFloat(cleanLightImg,"rotation",0,720);
        objectAnimator.setDuration(1500);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatMode(ValueAnimator.INFINITE);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finish();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    @Override
    public void onScanStarted(Context context) {

    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {

    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {

    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, final long cacheSize) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cacheSize > 0) {
                    ShowToast.show("为您释放" + StorageUtil.convertStorage(cacheSize) + "内存");
                } else {
                    ShowToast.show("您刚刚清理过内存,请稍后再试");
                }
                objectAnimator.cancel();
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        objectAnimator.cancel();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_activity_exit, R.anim.anim_trans_right_out);
    }
}
