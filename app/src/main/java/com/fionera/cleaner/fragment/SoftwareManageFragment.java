package com.fionera.cleaner.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.adapter.SoftwareAdapter;
import com.fionera.cleaner.base.BaseFragment;
import com.fionera.cleaner.bean.AppInfo;
import com.fionera.cleaner.utils.DroidWallApi;
import com.fionera.cleaner.utils.RvItemTouchListener;
import com.fionera.cleaner.utils.ShowToast;
import com.fionera.cleaner.utils.StorageUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SoftwareManageFragment
        extends BaseFragment {

    private int position;
    private SoftwareAdapter mAutoStartAdapter;

    @Bind(R.id.tv_top_tips)
    TextView topText;
    @Bind(R.id.listview)
    RecyclerView recyclerView;
    List<AppInfo> userAppInfos = null;
    List<AppInfo> systemAppInfos = null;
    @Bind(R.id.ll_loading_progress)
    View mProgressBar;
    @Bind(R.id.tv_progress_text)
    TextView mProgressBarText;

    private Method mGetPackageSizeInfoMethod;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        position = getArguments().getInt("position");
        View view = inflater.inflate(R.layout.fragment_software, container, false);
        ButterKnife.bind(this, view);
        try {
            mGetPackageSizeInfoMethod = mContext.getPackageManager().getClass()
                    .getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillData();
    }

    private void fillData() {
        if (position == 0) {
            topText.setText("");
        } else {
            topText.setText("卸载下列软件，会影响正常使用");
        }

        new AsyncTask<Void, Integer, List<AppInfo>>() {
            private int mAppCount = 0;

            @Override
            protected void onPreExecute() {
                showProgressBar(true);
                mProgressBarText.setText(R.string.scanning);
                super.onPreExecute();
            }

            @Override
            protected List<AppInfo> doInBackground(Void... params) {
                final List<AppInfo> appInfos = new ArrayList<>();

                PackageManager pm = mContext.getPackageManager();
                final List<PackageInfo> packInfos = pm.getInstalledPackages(0);
                final CountDownLatch countDownLatch = new CountDownLatch(packInfos.size());
                publishProgress(0, packInfos.size());
                try {
                    for (PackageInfo packInfo : packInfos) {
                        final AppInfo appInfo = new AppInfo();
                        appInfo.setAppIcon(packInfo.applicationInfo.loadIcon(pm));
                        appInfo.setUid(packInfo.applicationInfo.uid);
                        appInfo.setAppName(packInfo.applicationInfo.loadLabel(pm).toString());
                        appInfo.setPackname(packInfo.packageName);
                        appInfo.setUserApp(
                                (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) ==
                                        0);
                        appInfo.setInRom(
                                (packInfo.applicationInfo.flags & ApplicationInfo
                                        .FLAG_EXTERNAL_STORAGE) == 0);
                        appInfo.setVersion(packInfo.versionName);
                        mGetPackageSizeInfoMethod
                                .invoke(pm, packInfo.packageName, new IPackageStatsObserver.Stub() {
                                    @Override
                                    public void onGetStatsCompleted(PackageStats pStats,
                                                                    boolean succeeded) throws
                                            RemoteException {
                                        synchronized (appInfos) {
                                            publishProgress(++mAppCount, packInfos.size());
                                            appInfo.setPkgSize(pStats.cacheSize +
                                                                       pStats.codeSize +
                                                                       pStats.dataSize);
                                            appInfos.add(appInfo);
                                        }

                                        synchronized (countDownLatch) {
                                            countDownLatch.countDown();
                                        }
                                    }
                                });
                    }
                    countDownLatch.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return appInfos;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                try {
                    mProgressBarText
                            .setText(getString(R.string.scanning_m_of_n, values[0], values[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onPostExecute(List<AppInfo> result) {
                super.onPostExecute(result);
                showProgressBar(false);
                userAppInfos = new ArrayList<>();
                systemAppInfos = new ArrayList<>();
                long allSize = 0;
                for (AppInfo a : result) {
                    if (a.isUserApp()) {
                        allSize += a.getPkgSize();
                        userAppInfos.add(a);
                    } else {
                        systemAppInfos.add(a);
                    }
                }
                if (position == 0) {
                    topText.setText(getString(R.string.software_top_text, userAppInfos.size(),
                                              StorageUtil.convertStorage(allSize)));
                    mAutoStartAdapter = new SoftwareAdapter(mContext, userAppInfos);
                    recyclerView.setAdapter(mAutoStartAdapter);
                    mAutoStartAdapter.setRvItemTouchListener(new RvItemTouchListener() {
                        @Override
                        public void onItemClick(View v, int pos) {
                            DroidWallApi.alert(mContext, userAppInfos.get(pos).getPackname());
                        }
                    });
                } else {
                    mAutoStartAdapter = new SoftwareAdapter(mContext, systemAppInfos);
                    recyclerView.setAdapter(mAutoStartAdapter);
                    mAutoStartAdapter.setRvItemTouchListener(new RvItemTouchListener() {
                        @Override
                        public void onItemClick(View v, int pos) {
                            DroidWallApi.alert(mContext, systemAppInfos.get(pos).getPackname());
                        }
                    });
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            }
        }.execute();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
