package com.actor.model;

import com.hubble.devcomm.Device;

/**
 * Created by sonikas on 17/08/16.
 */
public class StartPatrolling {
    public Device device;

    public StartPatrolling() {
    }

    public StartPatrolling(Device device) {
        this.device = device;
    }
}
