package com.hubble.analytics;

/**
 * Created by QuayChenh on 2/18/2016.
 */
public class GAEventAction {
    String action;
    String label;
    long value;

    public GAEventAction(String action, String label) {
        this(action, label, 1);
    }

    public GAEventAction(String action, String label, long value) {
        this.action = action;
        this.label = label;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public String getLabel() {
        return label;
    }

    public long getValue() {
        return value;
    }
}
