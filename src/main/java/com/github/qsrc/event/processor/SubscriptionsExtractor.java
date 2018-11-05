package com.github.qsrc.event.processor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.qsrc.docker.LabelProvider;
import com.github.qsrc.docker.SubscriptionFactory;
import com.github.qsrc.event.Event;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class SubscriptionsExtractor implements Processor {

    private DockerClient docker;

    private LabelProvider labelProvider;

    private SubscriptionFactory subscriptionFactory;

    public SubscriptionsExtractor(
            DockerClient docker,
            LabelProvider labelProvider,
            SubscriptionFactory subscriptionFactory
    ) {
        this.docker = docker;
        this.labelProvider = labelProvider;
        this.subscriptionFactory = subscriptionFactory;
    }


    @Override
    public void process(Exchange exchange) throws Exception {
        var message = exchange.getMessage();
        message.setBody(
                extractNotifications(message.getBody(Event.class)),
                SubscriptionList.class
        );
    }

    public SubscriptionList extractNotifications(Event event) {
        return docker.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> isSubscribed(container, event))
                .map(container -> subscriptionFactory.create(container, event))
                .collect(Collectors.toCollection(SubscriptionList::new));
    }

    private boolean isSubscribed(Container container, Event event) {
        return labelProvider.hasAny(container, event);
    }


}
