package ui;

import java.util.List;

import base.hubble.Models;
import base.hubble.database.TimelineEvent;

/**
 * Created by sonikas on 19/08/16.
 */
public interface ITimeLineEvent {

    void receiveEvent (List<TimelineEvent> events);
}
