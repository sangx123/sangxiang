/*
 * Copyright 2016, Google Inc.
 * Copyright 2014, Nest Labs Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nest.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hubble.framework.service.security.SecurityService;
import com.nestlabs.sdk.NestToken;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.IllegalBlockSizeException;

public class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private static final String TOKEN_KEY = "token";
    private static final String EXPIRATION_KEY = "expiration";
    private static final String USER_ID_KEY = "userID";
    private static String mAlias = "Hubble Nest";
    private static String mPlainText = null;

    public static void saveAuthToken(Context context, NestToken token) {
        if (token == null) {
            getPrefs(context).edit().remove(TOKEN_KEY).remove(EXPIRATION_KEY).commit();
            mPlainText = null;
            return;
        }
        SecurityService securityService = new SecurityService();
        securityService.initSecurityContext(context);
        String cipherData = null;
        try {
            cipherData = securityService.encryptData(token.getToken(), SecurityService.CipherMethod.AES_KEY_ALGORITHM, mAlias);
        }catch (NoSuchAlgorithmException | IllegalBlockSizeException | InvalidKeyException e){
            Log.d(TAG, "Exception in encryption " + e.getMessage());
        }
        getPrefs(context).edit()
                .putString(TOKEN_KEY, cipherData)
                .putLong(EXPIRATION_KEY, token.getExpiresIn())
                .putString(USER_ID_KEY, token.getUserID())
                .commit();
    }

    public static NestToken loadAuthToken(Context context) {
        if(context!=null) {
            final SharedPreferences prefs = getPrefs(context);
            final String token = prefs.getString(TOKEN_KEY, null);
            SecurityService securityService = new SecurityService();
            securityService.initSecurityContext(context);
            final long expirationDate = prefs.getLong(EXPIRATION_KEY, -1);
            final String userID = prefs.getString(USER_ID_KEY, null);
            if (token == null || expirationDate == -1) {
                return null;
            }

            if (mPlainText == null) {
                try {
                    mPlainText = securityService.decryptData(token, SecurityService.CipherMethod.AES_KEY_ALGORITHM, mAlias);
                } catch (NoSuchAlgorithmException | IllegalBlockSizeException | InvalidKeyException e) {
                    Log.d(TAG, "Exception in encryption " + e.getMessage());
                }
            }
            return new NestToken(mPlainText, expirationDate, userID);
        }else{
            return  null;
        }
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(NestToken.class.getSimpleName(), 0);
    }

    public static void addDeviceToDashboard(Context context, String deviceID, boolean isEnabled){
        getPrefs(context).edit()
                .putBoolean(deviceID, isEnabled).commit();
    }

    public static boolean isDeviceEnabled(Context context, String deviceID){
        Log.d(TAG,"HAppContext ="+context);
        SharedPreferences prefs = getPrefs(context);
        boolean isEnabled = prefs.getBoolean(deviceID, false);
        return isEnabled;
    }

    public static void addSmokeRule(Context context, String smokeProtectName, String camID){
        getPrefs(context).edit()
                .putString(smokeProtectName, camID).commit();
    }

    public static boolean addSmokeDetection(Context context,String homeSmokeDetection, boolean isEnabled){
        getPrefs(context).edit()
                .putBoolean(homeSmokeDetection, isEnabled).commit();
        return isEnabled;
    }
    public static boolean getSmokeDetection(Context context, String homeSmokeDetection){
        SharedPreferences prefs = getPrefs(context);
        boolean  isSmokeDetect = prefs.getBoolean(homeSmokeDetection, false);
        return isSmokeDetect;

    }

    public static String getCamID(Context context, String smokeProtectName){
        SharedPreferences prefs = getPrefs(context);
        String  camID = prefs.getString(smokeProtectName, null);
        return camID;

    }

}
