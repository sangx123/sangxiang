package com.actor.model;

/**
 * Created by sonikas on 17/08/16.
 */
public class PanTiltSuccess {

    public SendPanTilt message;

    public PanTiltSuccess() {
    }

    public PanTiltSuccess(SendPanTilt message) {
        this.message = message;
    }
}
