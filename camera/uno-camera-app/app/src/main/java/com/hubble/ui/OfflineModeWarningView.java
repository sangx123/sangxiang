package com.hubble.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


/**
 * Created by QuayChenh on 1/11/2016.
 */
public class OfflineModeWarningView extends LinearLayout {

    private Context context;

    private ImageView icon;
    private TextView warning, desc;
    private Button checkNow;

    private OnCheckNowButtonClickListener listener;

    public OfflineModeWarningView(Context context) {
        this(context, null);
    }

    public OfflineModeWarningView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        this.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.offline_mode_warning_view, this, false);
        icon = (ImageView) view.findViewById(R.id.offline_mode_warning_icon);
        warning = (TextView) view.findViewById(R.id.offline_mode_main_text);
        desc = (TextView) view.findViewById(R.id.offline_mode_desc);
        checkNow = (Button) view.findViewById(R.id.offline_mode_checknow_button);
        checkNow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCheckNowOfflineMode();
                }
            }
        });
        this.addView(view);
    }

    public void setOnCheckNowButtonClickListener(OnCheckNowButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnCheckNowButtonClickListener {
        void onCheckNowOfflineMode();
    }
}
