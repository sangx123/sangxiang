package com.hubble.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubbleconnected.camera.R;

/**
 * Created by hoang on 9/13/16.
 */
public class BtaTipDialogFragment extends DialogFragment {

    private Button mSkipBtn;
    private TextView mDistanceMinText;
    private TextView mDistanceMaxText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.fragment_bta_tip_dialog, container, false);

        mSkipBtn = (Button) dialogView.findViewById(R.id.btn_bta_tip_skip);

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(HubbleApplication.TAG, "BTA tips skip button clicked");
                dismissAllowingStateLoss();
            }
        });

        return dialogView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
