package com.actor.model;

/**
 * Created by sonikas on 17/08/16.
 */
public class SendPanTilt {

    public Direction direction;

    public SendPanTilt(Direction direction) {
        this.direction = direction;
    }

    public SendPanTilt() {
    }

    public Direction getDirection() {
        return direction;
    }
}
