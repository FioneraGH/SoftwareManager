package com.fionera.cleaner.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PermissionInfo;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fionera.cleaner.R;
import com.fionera.cleaner.bean.AppInfo;
import com.fionera.cleaner.utils.ShowToast;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by fionera on 16-3-1.
 */
public class BottomSheetDialogView {

    public BottomSheetDialogView(Context context, AppInfo appInfo) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_dialog_recycler_view, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_bottom_sheet);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SimpleAdapter(context, appInfo));

        dialog.setContentView(view);
        dialog.show();
    }

    public static void show(Context context, AppInfo appInfo) {
        new BottomSheetDialogView(context, appInfo);
    }

    private static class SimpleAdapter
            extends RecyclerView.Adapter<InnerViewHolder> {

        private static final int TYPE_BRIEF = 1000;
        private static final int TYPE_TAG = 1001;
        private static final int TYPE_SERVICE_OR_PERMISSION = 1002;

        private AppInfo appInfo;
        private Context context;

        public SimpleAdapter(Context context, AppInfo appInfo) {
            this.context = context;
            this.appInfo = appInfo;
        }

        @Override
        public int getItemViewType(int position) {
            if (0 == position) {
                return TYPE_BRIEF;
            } else if (isTag(position)) {
                return TYPE_TAG;
            } else {
                return TYPE_SERVICE_OR_PERMISSION;
            }
        }

        @Override
        public InnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            if (viewType == TYPE_BRIEF) {
                view = inflater.inflate(R.layout.rv_bottom_sheet_recycler_brief, parent, false);
            } else if (viewType == TYPE_TAG) {
                view = inflater.inflate(R.layout.rv_bottom_sheet_recycler_tag, parent, false);
            } else {
                view = inflater.inflate(R.layout.rv_bottom_sheet_recycler_item, parent, false);
            }
            return new InnerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final InnerViewHolder holder, int position) {
            if (0 == holder.getAdapterPosition()) {
                holder.package_name.setText("包名：" + appInfo.getPackageName());
                holder.version.setText("版本：" + appInfo.getVersion());
                holder.uid.setText("UID：" + appInfo.getUid());
            } else if (isServiceTag(holder.getAdapterPosition())) {
                holder.tag.setText("应用服务：");
            } else if (isService(holder.getAdapterPosition())) {
                String service = (appInfo.getServiceInfos().get(holder.getAdapterPosition() - 2));
                String prefix = "";

                if (service.toLowerCase().contains("download")) {
                    prefix = "下载服务：(可能后台下载)\n";
                    holder.textView.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else if (service.toLowerCase().contains("record")) {
                    prefix = "录音服务：(可能后台录音)\n";
                    holder.textView.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else {
                    holder.textView
                            .setTextColor(ContextCompat.getColor(context, R.color.text_default));
                }
                holder.textView.setText(prefix + service);
            } else if (isPermissionTag(holder.getAdapterPosition())) {
                holder.tag.setText("应用权限：");
            } else {
                String permission = appInfo.getPermissionInfos()
                        .get(holder.getAdapterPosition() - appInfo.getServiceInfos().size() - 3);
                String prefix = "";
                switch (permission) {
                    case Manifest.permission.CAMERA:
                        prefix = "敏感权限：(相机)\n";
                        holder.textView.setTextColor(ContextCompat.getColor(context, R.color.red));
                        break;
                    case Manifest.permission.READ_CONTACTS:
                        prefix = "敏感权限：(读取联系人)\n";
                        holder.textView.setTextColor(ContextCompat.getColor(context, R.color.red));
                        break;
                    case Manifest.permission.RECEIVE_BOOT_COMPLETED:
                        prefix = "风险权限：(开机自启动)\n";
                        holder.textView
                                .setTextColor(ContextCompat.getColor(context, R.color.blue2));
                        break;
                    default:
                        holder.textView.setTextColor(
                                ContextCompat.getColor(context, R.color.text_default));
                        break;
                }
                holder.textView.setText(prefix + permission);
            }
        }

        @Override
        public int getItemCount() {
            return appInfo.getServiceInfos().size() + appInfo.getPermissionInfos().size() + 3;
        }

        private boolean isTag(int pos) {
            return isServiceTag(pos) || isPermissionTag(pos);
        }

        private boolean isServiceTag(int pos) {
            return 1 == pos;
        }

        private boolean isPermissionTag(int pos) {
            return pos == appInfo.getServiceInfos().size() + 2;
        }

        private boolean isService(int pos) {
            return pos < appInfo.getServiceInfos().size() + 2;
        }

    }

    static class InnerViewHolder
            extends RecyclerView.ViewHolder {

        TextView package_name;
        TextView version;
        TextView uid;

        TextView tag;

        TextView textView;

        public InnerViewHolder(View itemView) {
            super(itemView);

            package_name = (TextView) itemView.findViewById(R.id.tv_bottom_sheet_package_name);
            version = (TextView) itemView.findViewById(R.id.tv_bottom_sheet_version);
            uid = (TextView) itemView.findViewById(R.id.tv_bottom_sheet_uid);

            tag = (TextView) itemView.findViewById(R.id.tv_bottom_sheet_tag);

            textView = (TextView) itemView.findViewById(R.id.tv_bottom_sheet_name);
        }
    }
}