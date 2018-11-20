package com.github.qsrc.eventdispatcher.subscription;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.qsrc.eventdispatcher.docker.ConfigProvider;
import com.github.qsrc.eventdispatcher.event.Event;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionManager {

    private DockerClient docker;

    private ConfigProvider configProvider;

    private SubscriptionFactory subscriptionFactory;

    public SubscriptionManager(
            DockerClient docker,
            ConfigProvider configProvider,
            SubscriptionFactory subscriptionFactory
    ) {
        this.docker = docker;
        this.configProvider = configProvider;
        this.subscriptionFactory = subscriptionFactory;
    }

    public List<Subscription> findSubscriptions(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        return docker.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> isSubscribed(container, event))
                .map(container -> subscriptionFactory.create(container, event))
                .collect(Collectors.toList());
    }

    private boolean isSubscribed(Container container, Event event) {
        return configProvider.hasAny(container, event.getId());
    }

}
