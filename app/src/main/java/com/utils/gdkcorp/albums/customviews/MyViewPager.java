package com.utils.gdkcorp.albums.customviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Gautam Kakadiya on 02-08-2017.
 */

public class MyViewPager extends ViewPager  {

    private GestureDetector gestureDetector;
    private MyClickListener myClickListener;
    public MyViewPager(Context context) {
        super(context);
        assignGestuureDetector(context);
    }


    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        assignGestuureDetector(context);
    }

    public void setMyClickListener(MyClickListener myClickListener){
        this.myClickListener = myClickListener;
    }

    private void assignGestuureDetector(Context context) {
        gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                myClickListener.onClick();
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    public interface MyClickListener{
        public void onClick();
    }
}
