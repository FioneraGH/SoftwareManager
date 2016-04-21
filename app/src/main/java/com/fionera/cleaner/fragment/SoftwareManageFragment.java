package com.fionera.cleaner.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ServiceInfo;
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
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.adapter.SoftwareAdapter;
import com.fionera.cleaner.base.BaseFragment;
import com.fionera.cleaner.bean.AppInfo;
import com.fionera.cleaner.utils.LogCat;
import com.fionera.cleaner.utils.RvItemTouchListener;
import com.fionera.cleaner.utils.StorageUtil;
import com.fionera.cleaner.widget.BottomSheetDialogView;

import java.lang.reflect.Method;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
    @Bind(R.id.rv_software_manage)
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
                final List<PackageInfo> packInfos = pm.getInstalledPackages(
                        PackageManager.GET_SERVICES | PackageManager.GET_PERMISSIONS);
                final CountDownLatch countDownLatch = new CountDownLatch(packInfos.size());
                publishProgress(0, packInfos.size());
                try {
                    for (PackageInfo packInfo : packInfos) {
                        final AppInfo appInfo = new AppInfo();
                        appInfo.setAppIcon(packInfo.applicationInfo.loadIcon(pm));
                        appInfo.setAppName(packInfo.applicationInfo.loadLabel(pm).toString());
                        appInfo.setPackageName(packInfo.packageName);
                        appInfo.setVersion(packInfo.versionName);
                        List<String> services = new ArrayList<>();

                        if (packInfo.services != null) {
                            for (ServiceInfo serviceInfo : packInfo.services) {
                                services.add(serviceInfo.name);
                            }
                        } else {
                            services.add("未检测到服务");
                        }
                        appInfo.setServiceInfos(services);
                        List<String> permissions = new ArrayList<>();
                        if (packInfo.requestedPermissions != null) {
                            permissions.addAll(Arrays.asList(packInfo.requestedPermissions));

                        } else {
                            permissions.add("未检测到权限");
                        }
                        appInfo.setPermissionInfos(permissions);
                        appInfo.setUid(packInfo.applicationInfo.uid);
                        appInfo.setUserApp(
                                (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) ==
                                        0);
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
                    mAutoStartAdapter = new SoftwareAdapter(mContext, userAppInfos, position);
                    recyclerView.setAdapter(mAutoStartAdapter);
                    mAutoStartAdapter.setRvItemTouchListener(new RvItemTouchListener() {
                        @Override
                        public void onItemClick(View v, int pos) {
                            BottomSheetDialogView.show(mContext, userAppInfos.get(pos));
                        }
                    });
                } else {
                    mAutoStartAdapter = new SoftwareAdapter(mContext, systemAppInfos, position);
                    recyclerView.setAdapter(mAutoStartAdapter);
                    mAutoStartAdapter.setRvItemTouchListener(new RvItemTouchListener() {
                        @Override
                        public void onItemClick(View v, int pos) {
                            BottomSheetDialogView.show(mContext, systemAppInfos.get(pos));
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
