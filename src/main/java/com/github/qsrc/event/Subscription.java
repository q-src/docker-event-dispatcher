package com.github.qsrc.event;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
public class Subscription {

    private String containerId;

    private String command;

    private boolean start;

    private int debounceTime;

    private Event event;

    public String getId() {
        return String.format("%s-%s", containerId, event == null ? "null" : event.getId());
    }
}
