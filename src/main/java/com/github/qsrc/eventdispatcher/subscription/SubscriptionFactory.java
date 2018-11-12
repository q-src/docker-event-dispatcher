package com.github.qsrc.eventdispatcher.subscription;

import com.github.dockerjava.api.model.Container;
import com.github.qsrc.eventdispatcher.docker.ConfigLabel;
import com.github.qsrc.eventdispatcher.docker.LabelProvider;
import com.github.qsrc.eventdispatcher.event.Event;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionFactory {

    private LabelProvider labelProvider;

    public SubscriptionFactory(LabelProvider labelProvider) {
        this.labelProvider = labelProvider;
    }

    public Subscription create(Container container, Event event) {
        return Subscription.builder()
                .containerId(container.getId())
                .command(labelProvider.get(container, event, ConfigLabel.CONTAINER_COMMAND))
                .debounceTime(labelProvider.getInt(container, event, ConfigLabel.DISPATCH_DEBOUNCE))
                .start(labelProvider.getBool(container, event, ConfigLabel.CONTAINER_START))
                .build();
    }

}
