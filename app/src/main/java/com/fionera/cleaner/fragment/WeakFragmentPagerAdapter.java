package com.fionera.cleaner.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class WeakFragmentPagerAdapter extends FragmentPagerAdapter {
	private List<WeakReference<Fragment>> mList = new ArrayList<>();

	protected void saveFragment(Fragment fragment) {
		if (fragment == null) {
			return;
		}
		for (WeakReference<Fragment> item : mList) {
			if (item.get() == fragment) {
				return;
			}
		}
		mList.add(new WeakReference<>(fragment));
	}

	protected WeakFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}
}
