package com.fionera.cleaner.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.fionera.cleaner.base.BaseSwipeBackActivity;
import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.AppUtil;

import butterknife.Bind;

public class AboutActivity
        extends BaseSwipeBackActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tv_about_version)
    TextView textView;
    @Bind(R.id.tv_about_me)
    TextView textMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        textView.setText(AppUtil.getVersion(this));

        textMe.setText((Html.fromHtml(
                "<p>声明：</p><p>2.1 下载、安装和使用：本软件为免费软件，用户可以非商业性、无限制数量地下载、安装及使用本软件。</p>2.2 " +
                        "复制、分发和传播：用户可以非商业性、无限制数量地复制、分发和传播本软件产品。但必须保证每一份复制、分发和传播都是完整和真实的, " +
                        "包括所有有关本软件产品的软件、电子文档, 版权和商标，亦包括本协议。</p" +
                        "><p>微博：<a href='http://weibo.com/u/2594140111'>@写了一首烂代码</a></p>")));
        textMe.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
