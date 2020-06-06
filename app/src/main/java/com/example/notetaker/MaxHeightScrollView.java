package com.example.notetaker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class MaxHeightScrollView extends NestedScrollView {

    private int maxHeight = -1;

    public MaxHeightScrollView(@NonNull Context context) {
        this(context, null, 0); // Modified changes
    }

    public MaxHeightScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0); // Modified changes
    }

    public MaxHeightScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr); // Modified changes
    }

    // Modified changes
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.MaxHeightScrollView, defStyleAttr, 0);
        maxHeight =
                a.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, 0);
        a.recycle();
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

