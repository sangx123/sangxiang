package com.profile;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.framework.common.BaseContext;
import com.hubble.framework.service.cloudclient.profile.pojo.response.ProfileData;
import com.hubble.framework.service.cloudclient.profile.pojo.response.ProfileDetails;
import com.hubble.framework.service.database.DeviceDatabaseHelper;
import com.hubble.framework.service.device.model.Event;
import com.hubble.framework.service.device.model.ScaleProfile;
import com.hubble.framework.service.profile.ProfileManagerService;
import com.hubble.util.CommonConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ingkeethol on 6/23/2016.
 */
public class ProfileSynManager {

    private static final String TAG = ProfileSynManager.class.getSimpleName();
    private String mApiKey = null, emailID = null;
    private Context mContext;
    private ProfileSynManagerCallback mProfileSynManagerCallback;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static ProfileSynManager mInstance;
    private static final String keyAlgorithm = "AES";
    private static final String cipherAlgorithm = "AES/CBC/NoPadding";
    private boolean isSyncProgress = false;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    public ProfileSynManager(String apiKey, String emailID, ProfileSynManagerCallback profileSynManagerCallback) {
        mApiKey = apiKey;
        this.emailID = emailID;
        mContext = BaseContext.getBaseContext();
        mProfileSynManagerCallback = profileSynManagerCallback;
        sharedpreferences = mContext.getSharedPreferences(CommonConstants.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    public boolean isSyncProgress() {
        return isSyncProgress;
    }

    public void startProfileSyn() {
        isSyncProgress = true;
        ProfileSyncTask downloadEventsAsyncTask = new ProfileSyncTask();
        downloadEventsAsyncTask.execute();
    }

    public class ProfileSyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            Log.d(TAG, "ProfileSyncTask");

            fetchProfileFromServer();
            return null;
        }

        private void fetchProfileFromServer() {

            ProfileManagerService.getInstance(mContext).getProfileData(mApiKey, new Response.Listener<ProfileDetails>() {
                @Override
                public void onResponse(ProfileDetails response) {

                    for (ProfileData profileData : response.getEventDetails()) {
                        System.out.println("fetch profile From Server " + profileData.toString());
                        HashMap<String, String> profileSettings = new HashMap<>();
                        profileSettings = profileData.getProfileSettings();

                        if (profileSettings != null && profileSettings.get("IS_ACCOUNT_PROFILE") != null) {
                            if (Integer.valueOf(profileSettings.get("IS_ACCOUNT_PROFILE")) == 1) {
                                downloadAccountProfileImage(profileData.getProfileID(), profileData.getPicturePath());
                            }
                        }
                    }
                    if (mProfileSynManagerCallback != null)
                        mProfileSynManagerCallback.onProfileDataChanged(null);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    error.printStackTrace();
                    mProfileSynManagerCallback.onProfileLoadError();
                }
            });
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            isSyncProgress = false;
        }
    }

    private void downloadAccountProfileImage(final String profileID, final String profileImageUrl) {

        if (profileImageUrl != null) {
            ProfileManagerService.getInstance(mContext).downloadProfileImage(profileImageUrl, 180, 180,
                    ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ALPHA_8, new Response.Listener<Bitmap>() {

                        @Override
                        public void onResponse(Bitmap response) {
                            Log.d(TAG, "downloadAccountProfileImage Done ");
                            try {
                                ContextWrapper cw = new ContextWrapper(mContext);
                                // path to /data/data/yourapp/app_data/imageDir
                                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                                // Create imageDir
                                File mypath = new File(directory, profileID + ".jpg");

                                FileOutputStream fos = null;
                                try {
                                    fos = new FileOutputStream(mypath);
                                    // Use the compress method on the BitMap object to write image to the OutputStream
                                    response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    fos.close();
                                }
                                editor.putString(CommonConstants.ACCOUNT_PROFILE_ID, profileID).commit();
                                editor.putString(CommonConstants.ACCOUNT_PROFILE_IMAGE, mypath.getAbsolutePath()).commit();
                                Log.d(TAG, "downloadAccountProfileImage Done " + mypath.getAbsolutePath());
                                if (mProfileSynManagerCallback != null)
                                    mProfileSynManagerCallback.onProfileImageChanged(profileID);

                            } catch (Exception e) {
                                Log.e(TAG, "saveToInternalStorage()" + e.getMessage());

                            }
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "downloadAccountProfileImage Failed");
                            error.printStackTrace();

                        }
                    });
        }
    }

}
