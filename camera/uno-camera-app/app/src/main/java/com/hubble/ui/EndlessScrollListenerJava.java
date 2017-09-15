package com.hubble.ui;

import android.widget.AbsListView;

/**
 * Created by sonikas on 18/08/16.
 */
public class EndlessScrollListenerJava implements AbsListView.OnScrollListener {

    int visibleThreshold;
    private int previousTotalItems = 0;
    public int currentPage = -1;
    private boolean loading = false;
    private static final String TAG = "EndlessScrollListener";
    IEndlessScrollCallback callback;

    public EndlessScrollListenerJava(int visibleThreshold, IEndlessScrollCallback callback) {
        this.visibleThreshold = visibleThreshold;
        this.callback = callback;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)  {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItems, int totalItems) {
        if ( previousTotalItems > totalItems ) {
            currentPage = 0;
            previousTotalItems = totalItems;
            if (totalItems == 0) {
                loading = true;
            }
        }

        if ( loading && (totalItems > previousTotalItems) ) {
            loading = false;
            previousTotalItems = totalItems;
            currentPage += 1;
        }

        if ( !loading && (totalItems - visibleItems) <= (firstVisibleItem + visibleThreshold) ) {
            // v2 v1 endpoint: loadMoreCallback(currentPage + 1, totalItems)
            callback.loadMoreCallback(currentPage + 1
                    , totalItems);
            loading = true;
        }
    }


}
