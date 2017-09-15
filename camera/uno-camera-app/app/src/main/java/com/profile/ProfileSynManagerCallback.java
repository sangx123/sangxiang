package com.profile;

/**
 * Created by ingkeethol on 6/23/2016.
 */
public interface ProfileSynManagerCallback {

    void onProfileDataChanged(String profileID);
    void onProfileImageChanged(String profileID);
    void onProfileLoadError();
}
