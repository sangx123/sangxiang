package com.actor.model;

/**
 * Created by sonikas on 17/08/16.
 */
public class CheckMovePreset {

    public String moveV;
    public String moveH;
    public Direction direct;

    public CheckMovePreset(String moveV, Direction direct, String moveH) {
        this.moveV = moveV;
        this.direct = direct;
        this.moveH = moveH;
    }

    public CheckMovePreset() {
    }
}
