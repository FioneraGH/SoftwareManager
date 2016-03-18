package com.fionera.cleaner.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.fionera.cleaner.base.BaseSwipeBackActivity;
import com.fionera.cleaner.utils.FragmentArgs;
import com.fionera.cleaner.R;

import java.lang.reflect.Method;

import butterknife.Bind;

public class FragmentContainerActivity extends BaseSwipeBackActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    public static void launch(Activity activity, Class<? extends Fragment> clazz, FragmentArgs args) {
        Intent intent = new Intent(activity, FragmentContainerActivity.class);
        intent.putExtra("className", clazz.getName());
        if (args != null)
            intent.putExtra("args", args);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String className = getIntent().getStringExtra("className");
        if (TextUtils.isEmpty(className)) {
            finish();
            return;
        }

        FragmentArgs values = (FragmentArgs) getIntent().getSerializableExtra("args");

        Fragment fragment = null;
        if (savedInstanceState == null) {
            try {
                Class clazz = Class.forName(className);
                fragment = (Fragment) clazz.newInstance();
                if (values != null) {
                    try {
                        Method method = clazz.getMethod("setArguments", new Class[]{Bundle.class});
                        method.invoke(fragment, FragmentArgs.transToBundle(values));
                    } catch (Exception e) {
						e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_fragment_container);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (fragment != null) {
            getFragmentManager().beginTransaction().add(R.id.fl_fragment_container, fragment, className).commit();
        }
    }
}
