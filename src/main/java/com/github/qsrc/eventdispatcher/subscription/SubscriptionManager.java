package com.github.qsrc.eventdispatcher.subscription;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.qsrc.eventdispatcher.docker.LabelProvider;
import com.github.qsrc.eventdispatcher.event.Event;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionManager {

    private DockerClient docker;

    private LabelProvider labelProvider;

    private SubscriptionFactory subscriptionFactory;

    public SubscriptionManager(
            DockerClient docker,
            LabelProvider labelProvider,
            SubscriptionFactory subscriptionFactory
    ) {
        this.docker = docker;
        this.labelProvider = labelProvider;
        this.subscriptionFactory = subscriptionFactory;
    }

    public List<Subscription> findSubscriptions(Event event) {
        return docker.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> isSubscribed(container, event))
                .map(container -> subscriptionFactory.create(container, event))
                .collect(Collectors.toList());
    }

    private boolean isSubscribed(Container container, Event event) {
        return labelProvider.hasAny(container, event);
    }

}
