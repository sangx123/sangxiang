package com.hubble.util;

/**
 * Created by aruna on 23/02/17.
 */

public class ListChild {

        public int intValue = -1;
        public int secondaryIntValue = -1;
        public boolean booleanValue = false;
        public boolean secondaryBooleanValue = false;
        public boolean isClickable;
        public String title;
        public String value;
        public String modeVda;
        public int motionSource;

        // Old copy
        public int oldIntValue = -1;
        public int oldSecondaryIntValue = -1;
        public boolean oldBooleanValue = false;
        public boolean oldSecondaryBooleanValue = false;
        public boolean oldIsClickable;
        public String oldTitle;
        public String oldValue;
        public String oldModeVda;
        public int oldMotionSource;

        public ListChild(String title, String value, boolean isClickable) {
            this.title = title;
            this.value = value;
            this.isClickable = isClickable;
        }

        public void setOldCopy() {
            oldIntValue = intValue;
            oldSecondaryIntValue = secondaryIntValue;
            oldBooleanValue = booleanValue;
            oldSecondaryBooleanValue = secondaryBooleanValue;
            oldIsClickable = isClickable;
            oldTitle = title;
            oldValue = value;
            oldModeVda = modeVda;
        }

        public void revertToOldCopy() {
            intValue = oldIntValue;
            secondaryIntValue = oldSecondaryIntValue;
            booleanValue = oldBooleanValue;
            secondaryBooleanValue = oldSecondaryBooleanValue;
            isClickable = oldIsClickable;
            title = oldTitle;
            value = oldValue;
            modeVda = oldModeVda;
        }


}
