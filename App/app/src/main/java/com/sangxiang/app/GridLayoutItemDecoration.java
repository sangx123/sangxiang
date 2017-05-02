package com.sangxiang.app;

/**
 * Created by sangxiang on 26/4/17.
 */

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;


/**
 * Author: lishang
 * Data: 16/5/23 下午6:45
 * Des:
 * version:
 */
public class GridLayoutItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpanCount;
    private int mHorizonSpan;
    private int mVerticalSpan;
    private int mOrientation;

    public GridLayoutItemDecoration(int count) {
        mSpanCount = count;
        mOrientation = GridLayoutManager.VERTICAL;
    }

    public GridLayoutItemDecoration(int count, int orientation) {
        mSpanCount = count;
        setOrientation(orientation);
    }

    public GridLayoutItemDecoration(int count, int orientation, int horizonSpan, int verticalSpan) {
        this(count);
        setDivideParams(horizonSpan, verticalSpan);
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != LinearLayoutManager.HORIZONTAL && orientation != LinearLayoutManager.VERTICAL) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //获取dapter中的位置
        final int position = parent.getChildAdapterPosition(view);
        //获取总共的数量
        final int totalCount = parent.getAdapter().getItemCount();
        //如果是第一列的话left为0，否则为定义的大小
        int left = (position % mSpanCount == 0) ? 0 : mHorizonSpan;
        //如果不是最后一列的话bottom为mVerticalSpan
        int bottom = ((position + 1) % mSpanCount == 0) ? 0 : mVerticalSpan;
        if (isVertical(parent)) {
            //如果不是最后一行的话
            if (!isLastRaw(parent, position, mSpanCount, totalCount)) {
                outRect.set(left/2, 0, mHorizonSpan/2, mVerticalSpan);
            } else {
                outRect.set(left/2, 0, mHorizonSpan/2, 0);
            }
        } else {
            if (!isLastColumn(parent, position, mSpanCount, totalCount)) {
                outRect.set(0, 0, mHorizonSpan, bottom);
            } else {
                outRect.set(0, 0, mHorizonSpan, bottom);
            }
        }
    }

    private boolean isVertical(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int orientation = ((GridLayoutManager) layoutManager)
                    .getOrientation();
            return orientation == StaggeredGridLayoutManager.VERTICAL;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            return orientation == StaggeredGridLayoutManager.VERTICAL;
        }
        return false;
    }

    public void setDivideParams(int horizon, int vertical) {
        mHorizonSpan = horizon;
        mVerticalSpan = vertical;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                              int childCount) {
        if (isVertical(parent)) {
            if ((pos - pos % spanCount) + spanCount >= childCount)
                return true;
        } else {
            if ((pos + 1) % spanCount == 0) {
                return true;
            }
        }
        return false;
    }


    private boolean isLastColumn(RecyclerView parent, int pos, int spanCount,
                                 int childCount) {
        if (isVertical(parent)) {
            if ((pos + 1) % spanCount == 0) {
                return true;
            }
        } else {
            childCount = childCount - childCount % spanCount;
            if (pos >= childCount)
                return true;
        }
        return false;
    }
}