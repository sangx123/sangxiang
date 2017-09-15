package com.nxcomm.blinkhd.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.hubble.ui.OfflineModeWarningView;
import com.hubbleconnected.camera.R;


/**
 * Created by QuayChenh on 1/11/2016.
 */
public abstract class BaseFragment extends Fragment implements OfflineModeWarningView.OnCheckNowButtonClickListener {

    protected OfflineModeWarningView mOfflineModeWarningView;
    protected boolean isOfflineMode;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof  MainActivity) {
            isOfflineMode = ((MainActivity)getActivity()).isOfflineMode();
        }

        //AA-920: Support Offline Feature on V4.2
        mOfflineModeWarningView = (OfflineModeWarningView) getView().findViewById(R.id.offline_mode_warning_view);
        if (mOfflineModeWarningView != null) {
//            mOfflineModeWarningView.setVisibility(isOfflineMode ? View.VISIBLE : View.GONE);
            if (isOfflineMode) {
                mOfflineModeWarningView.setOnCheckNowButtonClickListener(BaseFragment.this);
            }
        }
    }

    protected void showOfflineModeWarningView() {
        if (mOfflineModeWarningView != null) {
            mOfflineModeWarningView.setVisibility(View.VISIBLE);
        }
    }

    protected void hideOfflineModeWarningView() {
        if (mOfflineModeWarningView != null) {
            mOfflineModeWarningView.setVisibility(View.GONE);
        }
    }
}
