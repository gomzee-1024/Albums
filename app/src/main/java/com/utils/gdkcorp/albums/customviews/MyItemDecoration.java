package com.utils.gdkcorp.albums.customviews;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.utils.gdkcorp.albums.Constants.DIRECTION;

import static com.utils.gdkcorp.albums.Constants.DIRECTION.DOWN;
import static com.utils.gdkcorp.albums.Constants.DIRECTION.LEFT;
import static com.utils.gdkcorp.albums.Constants.DIRECTION.RIGHT;
import static com.utils.gdkcorp.albums.Constants.DIRECTION.UP;

/**
 * Created by Gautam Kakadiya on 05-09-2017.
 */

public class MyItemDecoration extends RecyclerView.ItemDecoration {

    private int mFirstItemOffset;
    private int mDirection;

    public MyItemDecoration(int firstItemOffset,int direction){
        mFirstItemOffset = firstItemOffset;
        mDirection = direction;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(parent.getChildAdapterPosition(view)==0){
            switch (mDirection){
                case UP : outRect.top = mFirstItemOffset;
                    break;
                case RIGHT : outRect.right = mFirstItemOffset;
                    break;
                case DOWN : outRect.bottom = mFirstItemOffset;
                    break;
                case LEFT :  outRect.left = mFirstItemOffset;
                    break;
            }
        }
    }
}
