package com.fionera.cleaner.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class ProgressDialogFragment
        extends DialogFragment {

    int mIndeterminateDrawable;
    String mMessage;

    /**
     * Create a new instance of AbProgressDialogFragment.
     */
    public static ProgressDialogFragment newInstance(int indeterminateDrawable) {
        ProgressDialogFragment f = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putInt("indeterminateDrawable", indeterminateDrawable);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIndeterminateDrawable = getArguments().getInt("indeterminateDrawable");
        mMessage = "请稍候";

        ProgressDialog mProgressDialog = new ProgressDialog(getActivity(),
                                                            android.R.style
                                                                    .Theme_Material_Light_Dialog_Alert);
        if (mIndeterminateDrawable > 0) {
            mProgressDialog.setIndeterminateDrawable(
                    ContextCompat.getDrawable(getContext(), mIndeterminateDrawable));
        }

        if (mMessage != null) {
            mProgressDialog.setMessage(mMessage);
        }

        return mProgressDialog;
    }
}
