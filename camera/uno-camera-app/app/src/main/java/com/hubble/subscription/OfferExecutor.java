package com.hubble.subscription;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.framework.service.cloudclient.offer.pojo.request.UserOffer;
import com.hubble.framework.service.cloudclient.offer.pojo.response.UserOfferOptInDetail;
import com.hubble.framework.service.subscription.OfferService;
import com.hubble.ui.eventsummary.EventSummaryConstant;

import base.hubble.PublicDefineGlob;

/**
 * Created by Admin on 30-03-2017.
 */
public class OfferExecutor {

	private Context mContext;
	private String mAccessToken;

	public OfferExecutor(Context context) {
		mContext = context;
		mAccessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
	}

	public interface IOfferOptInResponse {
		public void onOfferOptInResponse(boolean isOfferAvailable, boolean isOfferOptedIn);
	}

	public interface IOfferConsumeResponse {
		public void onOfferConsumeResponse(boolean consumeSuccess);
	}

	public void checkUserOfferOptIn(final IOfferOptInResponse offerOptInResponse) {
		OfferService offerService = OfferService.getInstance(mContext);
		UserOffer userOffer = new UserOffer(mAccessToken, EventSummaryConstant.VA_OFFER_TYPE);
		offerService.getUserOfferOptInfo(userOffer, new Response.Listener<UserOfferOptInDetail>() {
			@Override
			public void onResponse(UserOfferOptInDetail response) {
				boolean isOfferAvailable = false;
				boolean isOfferOpted = false;
				if (response != null) {
					if (response.getCapability().equalsIgnoreCase(EventSummaryConstant.VA_OFFER_TYPE)) {
						isOfferAvailable = true;
						isOfferOpted = response.getOptIn();
					}
				}
				offerOptInResponse.onOfferOptInResponse(isOfferAvailable, isOfferOpted);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				offerOptInResponse.onOfferOptInResponse(false, false);
			}
		});
	}

	public void consumeUserOffer(final IOfferConsumeResponse OfferConsumeResponse) {
		OfferService offerService = OfferService.getInstance(mContext);
		UserOffer userOffer = new UserOffer(mAccessToken, EventSummaryConstant.VA_OFFER_TYPE);
		offerService.consumeVideoAnalyticsUserOffer(userOffer, new Response.Listener<UserOfferOptInDetail>() {
			@Override
			public void onResponse(UserOfferOptInDetail response) {
				boolean isSummaryOptIn = false;
				if (response != null) {
					if (response.getCapability().equalsIgnoreCase(EventSummaryConstant.VA_OFFER_TYPE)) {
						isSummaryOptIn = response.getOptIn();
					}
				}
				OfferConsumeResponse.onOfferConsumeResponse(isSummaryOptIn);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				OfferConsumeResponse.onOfferConsumeResponse(false);
			}
		});
	}

}
