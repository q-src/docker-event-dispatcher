package com.github.qsrc.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class Notification {

    private Subscription subscription;

    private Event event;

    public String getId() {
        return String.format(
                "%s-%s",
                subscription == null ? "null" : subscription.getContainerId(),
                event == null ? "null" : event.getId()
        );
    }
}
