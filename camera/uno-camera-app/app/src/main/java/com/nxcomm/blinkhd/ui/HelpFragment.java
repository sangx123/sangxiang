package com.nxcomm.blinkhd.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


/**
 * Created by CVision on 6/30/2016.
 */
public class HelpFragment extends Fragment implements View.OnClickListener {

    private TextView tvGuide931;
    private TextView tvGuide921;
    private TextView tvManual931;
    private TextView tvManual921;
    private TextView tvFaq931;
    private TextView tvFaq921;
    private TextView tvContactUs;
    private Dialog dialogShowing;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_layout, container, false);

        tvGuide931 = (TextView) view.findViewById(R.id.tv_guid_931);
        tvGuide921 = (TextView) view.findViewById(R.id.tv_guid_921);
        tvManual931 = (TextView) view.findViewById(R.id.tv_manual_931);
        tvManual921 = (TextView) view.findViewById(R.id.tv_manual_921);
        tvFaq931 = (TextView) view.findViewById(R.id.tv_faq_931);
        tvFaq921 = (TextView) view.findViewById(R.id.tv_faq_921);
        tvContactUs = (TextView) view.findViewById(R.id.tv_contact_us);

        tvGuide931.setOnClickListener(this);
        tvGuide921.setOnClickListener(this);
        tvManual931.setOnClickListener(this);
        tvManual921.setOnClickListener(this);
        tvFaq931.setOnClickListener(this);
        tvFaq921.setOnClickListener(this);
        tvContactUs.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_guid_931:
                showDialog(true);
                break;

            case R.id.tv_guid_921:
                showDialog(false);
                break;

            case R.id.tv_manual_931:
                startView(getString(R.string.manual_931));
                break;

            case R.id.tv_manual_921:
                startView(getString(R.string.manual_921));
                break;

            case R.id.tv_faq_931:
                startView(getString(R.string.faq_931));
                break;

            case R.id.tv_faq_921:
                startView(getString(R.string.faq_921));
                break;

            case R.id.tv_contact_us:
                break;
        }
    }

    public void showDialog(final boolean is931) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_lang);
        dialog.setCancelable(true);
        String title = "";
        if (is931) {
            title = String.format(getString(R.string.installation_fro), "V931");
        } else {
            title = String.format(getString(R.string.installation_fro), "V921");
        }

        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
        TextView tvEng = (TextView) dialog.findViewById(R.id.tv_eng);
        TextView tvFrance = (TextView) dialog.findViewById(R.id.tv_france);
        TextView tvDone = (TextView) dialog.findViewById(R.id.tv_done);

        tvTitle.setText(title);
        tvEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is931) {
                    startView(getString(R.string.guide_931_en));
                } else {
                    startView(getString(R.string.guide_921_en));
                }
                dialog.dismiss();
            }
        });


        tvFrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is931) {
                    startView(getString(R.string.guide_931_fr));
                } else {
                    startView(getString(R.string.guide_921_fr));
                }
                dialog.dismiss();
            }
        });

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void startView(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
