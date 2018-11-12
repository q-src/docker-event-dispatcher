package com.github.qsrc.eventdispatcher.subscription;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Subscription {

    private String containerId;

    private String command;

    private boolean start;

    private int debounceTime;

}
