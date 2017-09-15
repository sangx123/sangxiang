package com.actor.model;

/**
 * Created by sonikas on 17/08/16.
 */
public class QueuePanTilt {

    public SendPanTilt message;

    public QueuePanTilt() {
    }

    public QueuePanTilt(SendPanTilt message) {
        this.message = message;
    }

    public SendPanTilt getMessage() {
        return message;
    }
}
