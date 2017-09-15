package com.actor.model;

/**
 * Created by sonikas on 17/08/16.
 */
public class SendPanPreset {

    Direction dir;
    String valueV;
    String valueH;

    public SendPanPreset(Direction dir, String valueV, String valueH) {
        this.dir = dir;
        this.valueV = valueV;
        this.valueH = valueH;
    }

    public SendPanPreset() {
    }
}
