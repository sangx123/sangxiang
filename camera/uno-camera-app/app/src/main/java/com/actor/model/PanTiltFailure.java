package com.actor.model;

/**
 * Created by sonikas on 17/08/16.
 */
public class PanTiltFailure {

    public SendPanTilt message;

    public PanTiltFailure() {
    }

    public PanTiltFailure(SendPanTilt message) {
        this.message = message;
    }
}
