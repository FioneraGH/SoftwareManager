package com.fionera.cleaner.base;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.fionera.cleaner.widget.ProgressDialogFragment;

public class BaseFragment
        extends Fragment {

    protected Context mContext;

    private static String DIALOG_TAG = "BaseDialog";

    private ProgressDialogFragment mProgressDialogFragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public void showDialogLoading() {
        if (mProgressDialogFragment == null) {
            mProgressDialogFragment = ProgressDialogFragment.newInstance(0);
        }
        mProgressDialogFragment.show(getFragmentManager(), DIALOG_TAG);
    }

    public void dismissDialogLoading() {
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
        }
    }
}
