package com.hubble.ui;

/**
 * Created by sonikas on 29/08/16.
 */
public interface IEndlessScrollCallback {

    void loadMoreCallback (int nextPage, int totalItems);
}
