package com.github.qsrc.eventdispatcher.dispatching.processor;

import com.github.qsrc.eventdispatcher.notification.NotificationCounter;
import com.github.qsrc.eventdispatcher.notification.Notification;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

@Service
public class NotificationCountEnricher implements Processor {

    public static final String COUNT_HEADER = "stats.notification.count";

    private NotificationCounter notificationCounter;

    public NotificationCountEnricher(
            NotificationCounter notificationCounter
    ) {
        this.notificationCounter = notificationCounter;
    }

    @Override
    public void process(Exchange exchange) throws InvalidPayloadException {
        var message = exchange.getMessage();
        var count = notificationCounter.count(message.getMandatoryBody(Notification.class));
        message.setHeader(COUNT_HEADER, count);
    }

}
