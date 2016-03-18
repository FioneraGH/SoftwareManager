package com.fionera.cleaner.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fionera.cleaner.R;


public class ItemCardView extends RelativeLayout {

    private ImageView cardImage;
    private TextView cardName;

    public ItemCardView(Context context) {
        super(context);
        initView(context);
    }

    public ItemCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ItemCardView);

        cardName.setText(a.getString(R.styleable.ItemCardView_card_name));
        cardImage.setImageDrawable(a.getDrawable(R.styleable.ItemCardView_card_image));
        a.recycle();
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.item_card_view, this);
        cardImage = (ImageView) view.findViewById(R.id.card_image);
        cardName = (TextView) view.findViewById(R.id.card_name);
    }
}
