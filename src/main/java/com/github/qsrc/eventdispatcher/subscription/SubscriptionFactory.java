package com.github.qsrc.eventdispatcher.subscription;

import com.github.dockerjava.api.model.Container;
import com.github.qsrc.eventdispatcher.docker.Config;
import com.github.qsrc.eventdispatcher.docker.ConfigProvider;
import com.github.qsrc.eventdispatcher.event.Event;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionFactory {

    private ConfigProvider configProvider;

    public SubscriptionFactory(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public Subscription create(Container container, Event event) {
        var namespace = event.getId();
        return Subscription.builder()
                .containerId(String.format("%12.12s", container.getId()))
                .command(configProvider.get(container, Config.Container.COMMAND.in(namespace)))
                .start(configProvider.getBool(container, Config.Container.START.in(namespace)))
                .debounceTime(configProvider.getInt(container, Config.Dispatching.DEBOUNCE.in(namespace)))
                .build();
    }

}
