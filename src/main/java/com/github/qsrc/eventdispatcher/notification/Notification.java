package com.github.qsrc.eventdispatcher.notification;

import com.github.qsrc.eventdispatcher.event.Event;
import com.github.qsrc.eventdispatcher.subscription.Subscription;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notification {

    private Subscription subscription;

    private Event event;

    public String getId() {
        return String.format(
                "%s-%s",
                event == null ? "null" : event.getId(),
                subscription == null ? "null" : subscription.getContainerId()
        );
    }
}
