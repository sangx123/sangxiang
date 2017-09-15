package com.hubble.subscription;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Toast;

import com.hubble.ui.eventsummary.EventSummaryConstant;
import com.hubble.ui.eventsummary.VideoAnalyticsOfferDialog;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;

/**
 * Created by Admin on 31-03-2017.
 */
public class OfferPopUpActivity extends FragmentActivity {

	private VideoAnalyticsOfferDialog mVideoAnalyticsOfferDialog;
	private OfferExecutor mOfferExecutor;
	private Dialog mEnableProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Bundle extras = getIntent().getExtras();
		String offerType = extras.getString(CommonConstants.OFFER_TYPE_FLAG);

		//Dialog for Video analytics offer
		if (offerType.equalsIgnoreCase(CommonConstants.OFFER_TYPE_VA)) {
			mOfferExecutor = new OfferExecutor(this);
			mOfferExecutor.checkUserOfferOptIn(new OfferExecutor.IOfferOptInResponse() {
				@Override
				public void onOfferOptInResponse(boolean isOfferAvailable, boolean isOfferOptedIn) {
					if (isOfferAvailable) {
						mVideoAnalyticsOfferDialog = new VideoAnalyticsOfferDialog(OfferPopUpActivity.this, new VideoAnalyticsOfferDialog.IVAOfferListener() {
							@Override
							public void vaOfferOptIn() {
								dismissVideoAnalyticsDialog();
								mEnableProgressDialog = ProgressDialog.show(OfferPopUpActivity.this, null, getString(R.string.va_opt_in_progress));
								mOfferExecutor.consumeUserOffer(new OfferExecutor.IOfferConsumeResponse() {
									@Override
									public void onOfferConsumeResponse(boolean consumeSuccess) {
										dismissEnableProgressDialog();
										if (consumeSuccess) {
											dismissVideoAnalyticsDialog();
											mVideoAnalyticsOfferDialog = new VideoAnalyticsOfferDialog(OfferPopUpActivity.this, EventSummaryConstant.VA_HINT_DIALOG);
											mVideoAnalyticsOfferDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
												@Override
												public void onCancel(DialogInterface dialog) {
													finish();
												}
											});
											mVideoAnalyticsOfferDialog.show();
										}else {
											Toast.makeText(OfferPopUpActivity.this, getString(R.string.va_opt_in_failure), Toast.LENGTH_LONG).show();
										}
									}
								});
							}
						}, EventSummaryConstant.VA_OFFER_DIALOG);
						mVideoAnalyticsOfferDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								finish();
							}
						});
						mVideoAnalyticsOfferDialog.show();
					}else {
						Toast.makeText(OfferPopUpActivity.this, getString(R.string.va_offer_unavailable), Toast.LENGTH_LONG).show();
						finish();
					}
				}
			});
		}else {
			Toast.makeText(OfferPopUpActivity.this, getString(R.string.va_offer_unavailable), Toast.LENGTH_LONG).show();
			finish();
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		dismissVideoAnalyticsDialog();
		dismissEnableProgressDialog();
	}

	private void dismissVideoAnalyticsDialog() {
		if (mVideoAnalyticsOfferDialog != null && mVideoAnalyticsOfferDialog.isShowing()) {
			mVideoAnalyticsOfferDialog.dismiss();
		}
	}

	private void dismissEnableProgressDialog() {
		if (mEnableProgressDialog != null && mEnableProgressDialog.isShowing()) {
			mEnableProgressDialog.dismiss();
		}
		mEnableProgressDialog = null;
	}
}
