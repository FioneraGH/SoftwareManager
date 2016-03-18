package com.fionera.cleaner.activity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.base.BaseActivity;
import com.fionera.cleaner.service.CleanerService;
import com.fionera.cleaner.service.CoreService;

import butterknife.Bind;

public class SplashActivity
        extends BaseActivity {

    @Bind(R.id.iv_splash)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startService(new Intent(this, CoreService.class));
        startService(new Intent(this, CleanerService.class));

        imageView.animate().withLayer().scaleX(1.1f).scaleY(1.1f).setDuration(2000)
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(MainActivity.class);
                        finish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }
}
