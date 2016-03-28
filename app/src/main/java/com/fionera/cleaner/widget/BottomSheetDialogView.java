package com.fionera.cleaner.widget;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.ShowToast;

/**
 * Created by fionera on 16-3-1.
 */
public class BottomSheetDialogView {

    public BottomSheetDialogView(Context context, String text) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_dialog_recycler_view, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_bottom_sheet);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SimpleAdapter());
        TextView textView = (TextView) view.findViewById(R.id.tv_bottom_sheet_title);
        textView.setText(text);

        dialog.setContentView(view);
        dialog.show();
    }

    public static void show(Context context) {
        new BottomSheetDialogView(context, "");
    }

    public static void show(Context context, String text) {
        new BottomSheetDialogView(context, text);
    }

    private static class SimpleAdapter
            extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.rv_bottom_sheet_recycler_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowToast.show(holder.getAdapterPosition() + "");
                }
            });
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    private static class ViewHolder
            extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}