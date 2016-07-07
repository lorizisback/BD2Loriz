package com.loriz.bd2loriz.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Marco on 06/07/2016.
 */
public class CustomViewPager extends ViewPager {

    private boolean isPagingEnabled = true;
    private int QUERY_PAGE = 1;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCurrentItem() == QUERY_PAGE) {
            return super.onTouchEvent(event);
        }else {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (getCurrentItem() == QUERY_PAGE) {
            return this.isPagingEnabled && super.onInterceptTouchEvent(event);
        } else {
            return false;
        }
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }


}