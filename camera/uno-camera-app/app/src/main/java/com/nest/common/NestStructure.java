package com.nest.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dasari on 30/01/17.
 */

public class NestStructure implements Parcelable {

    private String homeName;
    private String homeID;
    private String homeAwayStatus;

    public String getHomeName() {
        return homeName;
    }

    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    public String getHomeID() {
        return homeID;
    }

    public void setHomeID(String homeID) {
        this.homeID = homeID;
    }

    public String getHomeAwayStatus() {
        return homeAwayStatus;
    }

    public void setHomeAwayStatus(String homeAwayStatus) {
        this.homeAwayStatus = homeAwayStatus;
    }

    public NestStructure(){

    }

    protected NestStructure(Parcel in){
        this.homeName = in.readString();
        this.homeID = in.readString();
        this.homeAwayStatus = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.homeName);
        dest.writeString(this.homeID);
        dest.writeString(this.homeAwayStatus);
    }

    public static final Creator<NestStructure> CREATOR = new Creator<NestStructure>() {
        @Override
        public NestStructure createFromParcel(Parcel in) {
            return new NestStructure(in);
        }

        @Override
        public NestStructure[] newArray(int size) {
            return new NestStructure[size];
        }
    };

}
