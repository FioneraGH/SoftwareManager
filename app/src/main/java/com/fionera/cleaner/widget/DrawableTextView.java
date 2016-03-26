package com.fionera.cleaner.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.fionera.cleaner.R;

/**
 * Created by fionera on 15-12-7.
 */
public class DrawableTextView extends TextView {

    public static final int DRAWABLE_LEFT = 0;
    public static final int DRAWABLE_TOP = 1;
    public static final int DRAWABLE_RIGHT = 2;
    public static final int DRAWABLE_BOTTOM = 3;
    private int leftHeight = 0;
    private int leftWidth = 0;
    private int rightHeight = 0;
    private int rightWidth = 0;
    private int topHeight = 0;
    private int topWidth = 0;
    private int bottomHeight = 0;
    private int bottomWidth = 0;

    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    /**
     * 初始化读取参数
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView, defStyle,
                                                      0);
        if (a != null) {

            int count = a.getIndexCount();
            int index;
            for (int i = 0; i < count; i++) {
                index = a.getIndex(i);
                switch (index) {
                    case R.styleable.DrawableTextView_left_height:
                        leftHeight = a.getDimensionPixelSize(index, 0);
                        break;
                    case R.styleable.DrawableTextView_left_width:
                        leftWidth = a.getDimensionPixelSize(index, 0);
                        break;
                    case R.styleable.DrawableTextView_top_height:
                        topHeight = a.getDimensionPixelSize(index, 0);
                        break;
                    case R.styleable.DrawableTextView_top_width:
                        topWidth = a.getDimensionPixelSize(index, 0);
                        break;
                    case R.styleable.DrawableTextView_right_height:
                        rightHeight = a.getDimensionPixelSize(index, 0);
                        break;
                    case R.styleable.DrawableTextView_right_width:
                        rightWidth = a.getDimensionPixelSize(index, 0);
                        break;

                    case R.styleable.DrawableTextView_bottom_height:
                        bottomHeight = a.getDimensionPixelSize(index, 0);
                        break;
                    case R.styleable.DrawableTextView_bottom_width:
                        bottomWidth = a.getDimensionPixelSize(index, 0);
                        break;
                }
            }

            /**
             * 取一圈修改大小，大小是读取的
             */
            Drawable[] drawables = getCompoundDrawables();
            setImageSize(drawables[0], DRAWABLE_LEFT);
            setImageSize(drawables[1], DRAWABLE_TOP);
            setImageSize(drawables[2], DRAWABLE_RIGHT);
            setImageSize(drawables[3], DRAWABLE_BOTTOM);
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
            a.recycle();
        }
    }

    /**
     * 设定图片的大小
     */
    private void setImageSize(Drawable d, int dir) {

        int height = 0;
        int width = 0;
        if (d == null) {
            return;
        }

        switch (dir) {
            case DRAWABLE_LEFT:
                height = leftHeight;
                width = leftWidth;
                break;
            case DRAWABLE_TOP:
                height = topHeight;
                width = topWidth;
                break;
            case DRAWABLE_RIGHT:
                height = rightHeight;
                width = rightWidth;
                break;
            case DRAWABLE_BOTTOM:
                height = bottomHeight;
                width = bottomWidth;
                break;
        }

        d.setBounds(0, 0, width, height);
    }
}
