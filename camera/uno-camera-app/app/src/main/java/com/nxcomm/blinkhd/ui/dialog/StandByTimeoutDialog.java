package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hubble.registration.ui.CommonDialog;
import com.hubbleconnected.camera.R;


public class StandByTimeoutDialog extends CommonDialog
{
    private static final String TAG = StandByTimeoutDialog.class.getSimpleName();
    private static final String WAKE_UP = "wakeup";
    private static final String REMAINING_TIME = "remaining_time";
    private boolean showWakeupButton = false;
    private int mSecond;


    public static StandByTimeoutDialog newInstance(boolean wakeupButton,int second)
    {
        StandByTimeoutDialog timeoutDialog = new StandByTimeoutDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(WAKE_UP,wakeupButton);
        bundle.putInt(REMAINING_TIME,second);
        timeoutDialog.setArguments(bundle);

        return timeoutDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mSecond = getArguments().getInt(REMAINING_TIME);
        showWakeupButton = getArguments().getBoolean(WAKE_UP,false);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        contentView = inflater.inflate(R.layout.dialog_stand_by_timeout, null);

        Button btnCancel = (Button) contentView.findViewById(R.id.dialog_timeout_btn_cancel);
        Button btnOk = (Button) contentView.findViewById(R.id.dialog_timeout_btn_ok);


        TextView tvMessage = (TextView) contentView.findViewById(R.id.dialog_stand_by_tv);

        builder.setView(contentView);

        if(!showWakeupButton)
        {
            tvMessage.setText(String.format(getResources().getString(R.string.camera_entering_stand_by_mode),String.valueOf(mSecond)));
            btnCancel.setVisibility(View.GONE);
        }
        else
        {

        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commonDialogListener != null) {
                    commonDialogListener.onDialogNegativeClick(StandByTimeoutDialog.this);
                }
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (commonDialogListener != null)
                {
                    commonDialogListener.onDialogPositiveClick(StandByTimeoutDialog.this);

                }
            }
        });

        return builder.create();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try
        {
            super.show(manager,tag);
        }
        catch (Exception e)
        {
            Log.d(TAG,"failed to show dialog " + e.getMessage());
        }
    }

}
