package com.github.qsrc.event.processor;

import com.github.qsrc.docker.SubscriptionManager;
import com.github.qsrc.event.Event;
import com.github.qsrc.event.Notification;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class NotificationExtractor implements Processor {

    private SubscriptionManager subscriptionManager;

    public NotificationExtractor(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void process(Exchange exchange) {
        var message = exchange.getMessage();
        message.setBody(
                extractNotifications(message.getBody(Event.class)),
                NotificationList.class
        );
    }

    public NotificationList extractNotifications(Event event) {
        return subscriptionManager.findSubscriptions(event).stream()
                .map(subscription -> new Notification(subscription, event))
                .collect(Collectors.toCollection(NotificationList::new));
    }

}
