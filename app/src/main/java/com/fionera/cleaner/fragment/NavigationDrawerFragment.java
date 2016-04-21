package com.fionera.cleaner.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.ShowToast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NavigationDrawerFragment
        extends Fragment
        implements View.OnClickListener {

    private final int textViewIds[] = {R.id.tv_home_tag, R.id.tv_network_tag, R.id
            .tv_setting_tag, R.id.tv_setting_quit};
    private TextView textViews[] = new TextView[textViewIds.length];

    private NavigationDrawerCallbacks mCallbacks;

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.bind(this, view);

        for (int i = 0; i < textViewIds.length; ++i) {
            textViews[i] = (TextView) view.findViewById(textViewIds[i]);
            textViews[i].setOnClickListener(this);
        }
        textViews[0].setSelected(true);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (NavigationDrawerCallbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("必须实现接口");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_setting_quit) {
            new AlertDialog.Builder(getContext()).setTitle("完全退出软件").setMessage("你确定要完全退出么？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ShowToast.show("软件坚持工作～");
                }
            }).show();
            return;
        }
        for (int i = 0; i < textViews.length; ++i) {
            if (v.equals(textViews[i])) {
                if (mCallbacks != null) {
                    mCallbacks.onNavigationDrawerItemSelected(i);
                }
                textViews[i].setSelected(true);
            } else {
                textViews[i].setSelected(false);
            }
        }
    }
}
