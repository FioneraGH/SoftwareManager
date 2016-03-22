package com.fionera.cleaner.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.fionera.cleaner.R;
import com.fionera.cleaner.base.BaseFragment;

import butterknife.ButterKnife;

public class NavigationDrawerFragment
        extends BaseFragment
        implements View.OnClickListener {

    private NavigationDrawerCallbacks mCallbacks;
    private final int radioIds[] = {R.id.rb_home_tag, R.id.rb_setting_tag};
    private RadioButton radios[] = new RadioButton[radioIds.length];

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.bind(this, view);

        for (int i = 0; i < radioIds.length; ++i) {
            radios[i] = (RadioButton) view.findViewById(radioIds[i]);
            radios[i].setOnClickListener(this);
        }
        radios[0].setChecked(true);
        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("必须实现接口");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < radios.length; ++i) {
            if (v.equals(radios[i])) {
                mCallbacks.onNavigationDrawerItemSelected(i);
            } else {
                radios[i].setChecked(false);
            }
        }
    }
}
