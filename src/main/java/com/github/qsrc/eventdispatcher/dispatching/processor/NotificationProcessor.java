package com.github.qsrc.eventdispatcher.dispatching.processor;

import com.github.qsrc.eventdispatcher.notification.ContainerNotifier;
import com.github.qsrc.eventdispatcher.notification.Notification;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

@Service
public class NotificationProcessor implements Processor {

    private ContainerNotifier containerNotifier;

    public NotificationProcessor(ContainerNotifier containerNotifier) {
        this.containerNotifier = containerNotifier;
    }

    @Override
    public void process(Exchange exchange) {
        containerNotifier.notify(exchange.getMessage().getBody(Notification.class));
    }
}
