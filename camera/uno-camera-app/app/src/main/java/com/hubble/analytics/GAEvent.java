package com.hubble.analytics;

import java.util.List;

/**
 * Created by QuayChenh on 2/18/2016.
 */
public class GAEvent {
    String category;
    List<GAEventAction> actions;

    public GAEvent(String category, List<GAEventAction> actions) {
        this.category = category;
        this.actions = actions;
    }

    public String getCategory() {
        return category;
    }

    public List<GAEventAction> getActions() {
        return actions;
    }
}
