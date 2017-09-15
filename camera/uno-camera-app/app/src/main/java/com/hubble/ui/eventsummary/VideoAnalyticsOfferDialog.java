package com.hubble.ui.eventsummary;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hubbleconnected.camera.R;

/**
 * Created by Admin on 27-03-2017.
 */
public class VideoAnalyticsOfferDialog extends Dialog {

	private Context mContext;
	private IVAOfferListener miVAOfferListener;

	private LinearLayout mVAOfferLayout;
	private LinearLayout mVAHintLayout;
	private TextView mVAOfferPrivacyPolicy;
	private TextView mVAOfferKnowMore;
	private TextView mVAOfferOptIn;
	private ImageView mVAOfferClose;
	private Button mHintGotIt;

	private int mDialogType;


	public interface IVAOfferListener{
		public void vaOfferOptIn();
	}

	public VideoAnalyticsOfferDialog(Context context, IVAOfferListener vaOfferListener, int typeOfDialog) {
		super(context);
		mContext = context;
		miVAOfferListener = vaOfferListener;
		mDialogType = typeOfDialog;
	}

	public VideoAnalyticsOfferDialog(Context context, int typeOfDialog) {
		super(context);
		mContext = context;
		mDialogType = typeOfDialog;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.video_analytics_offer_dialog);
		setCanceledOnTouchOutside(false);
		mVAOfferLayout = (LinearLayout)findViewById(R.id.va_offer_dialog);
		mVAHintLayout = (LinearLayout)findViewById(R.id.va_hint_dialog);
		mVAOfferClose = (ImageView) findViewById(R.id.va_offer_close);

		if(mDialogType == EventSummaryConstant.VA_OFFER_DIALOG) {
			mVAOfferLayout.setVisibility(View.VISIBLE);
			mVAHintLayout.setVisibility(View.GONE);
			mVAOfferPrivacyPolicy = (TextView) findViewById(R.id.va_offer_text2);
			mVAOfferKnowMore = (TextView) findViewById(R.id.va_offer_text3);
			mVAOfferOptIn = (Button) findViewById(R.id.va_offer_opt_in);

			mVAOfferPrivacyPolicy.setText(Html.fromHtml(mContext.getString(R.string.va_offer_policy)));
			mVAOfferPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intentPlan = new Intent(Intent.ACTION_VIEW);
					intentPlan.setData(Uri.parse("https://hubbleconnected.com/app-policy"));
					mContext.startActivity(intentPlan);
				}
			});
			mVAOfferKnowMore.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intentPlan = new Intent(Intent.ACTION_VIEW);
					intentPlan.setData(Uri.parse("https://hubbleconnected.com/plans"));
					mContext.startActivity(intentPlan);
				}
			});
			mVAOfferOptIn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					if(miVAOfferListener != null) {
						miVAOfferListener.vaOfferOptIn();
					}
				}
			});
		}else if (mDialogType == EventSummaryConstant.VA_HINT_DIALOG){
			mVAOfferLayout.setVisibility(View.GONE);
			mVAHintLayout.setVisibility(View.VISIBLE);
			mHintGotIt = (Button)findViewById(R.id.va_hint_got_it);
			mHintGotIt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cancel();
				}
			});
		}

		mVAOfferClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		cancel();
	}
}
