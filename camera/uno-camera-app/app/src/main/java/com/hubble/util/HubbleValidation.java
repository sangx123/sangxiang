package com.hubble.util;

import android.text.TextUtils;

/**
 * Created by QuayChenh on 2/17/2016.
 */
public class HubbleValidation {

    final private static int USERNAME_MIN_LENGTH = 5;
    final private static int USERNAME_MAX_LENGTH = 20;
    final private static int PASSWORD_MIN_LENGTH = 8;
    final private static int PASSWORD_MAX_LENGTH = 30;

    final public static int VALIDATE = 0x0;
    final public static int INVALID_USERNAME_EMPTY = 0x1;
    final public static int INVALID_USER_LENGTH_OUT_OF_RANGE = 0x2;
    final public static int INVALID_PASSWORD_EMPTY = 0x3;
    final public static int INVALID_PASSWORD_TOO_SHORT = 0x4;
    final public static int INVALID_PASSWORD_TOO_LONG = 0x5;
    final public static int INVALID_PASSWORD_MATCH_USERNAME = 0x6;
    final public static int INVALID_PASSWORD = 0x7;
    final public static int INVALID_CONFIRM_PASSWORD_DOES_NOT_MATCH = 0x8;

    public static int isLoginValidate(String username, String password) {
        int result = VALIDATE;
        if (TextUtils.isEmpty(username)) {
            result = INVALID_USERNAME_EMPTY;
        } else if (TextUtils.isEmpty(password)) {
            result = INVALID_PASSWORD_EMPTY;
        }
        return result;
    }
}
