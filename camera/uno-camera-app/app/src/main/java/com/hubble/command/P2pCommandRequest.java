package com.hubble.command;

import com.hubble.devcomm.impl.hubble.IP2pCommunicationHandler;

import base.hubble.command.BaseCommandRequest;


/**
 * Created by hoang on 4/13/17.
 */

public class P2pCommandRequest extends BaseCommandRequest {
    private IP2pCommunicationHandler p2pCommunicationHandler;

    public IP2pCommunicationHandler getP2pCommunicationHandler() {
        return p2pCommunicationHandler;
    }

    public void setP2pCommunicationHandler(IP2pCommunicationHandler p2pCommunicationHandler) {
        this.p2pCommunicationHandler = p2pCommunicationHandler;
    }
}
