package com.github.qsrc.eventdispatcher.dispatching;

import com.github.qsrc.eventdispatcher.docker.Config;
import com.github.qsrc.eventdispatcher.event.Event;
import com.github.qsrc.eventdispatcher.notification.Notification;
import com.github.qsrc.eventdispatcher.dispatching.processor.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.apache.camel.builder.PredicateBuilder.and;

@Component
public class DispatchingRoute extends RouteBuilder {

    public static final String RECIPIENT_DELIMITER_VALUE = "${dispatcher.forward.delimiter:,}";

    public static final String RECIPIENT_HEADER = "destination";

    private static final String FILE_URI = "file://events?recursive=true";

    private static final String LOG_PREFIX = "[${headers.id}] ";

    static class Route {
        static final String NULL = "null";
        static final String EVENT_EXTRACTOR = "file";
        static final String EVENT = "event";
        static final String SUBSCRIPTION_EXTRACTOR = "subscriptions";
        static final String SUBSCRIPTION = "subscription";
        static final String NOTIFICATION = "notification";
        static final String NOTIFICATIONS_DEBOUNCED = "notifications:debounced";
    }

    static class Header {
        static final String MESSAGE_ID = "id";
        static final String DISPATCH_TIMEOUT = "timeout";
    }

    private EventExtractor eventExtractor;

    private NotificationExtractor notificationExtractor;

    private EventForwarder eventForwarder;

    private NotificationProcessor notificationProcessor;

    private NotificationCountEnricher notificationCountEnricher;

    @Value(RECIPIENT_DELIMITER_VALUE)
    private String recipientDelimiter;

    public DispatchingRoute(
            EventExtractor eventExtractor,
            EventForwarder eventForwarder,
            NotificationExtractor notificationExtractor,
            NotificationProcessor notificationProcessor,
            NotificationCountEnricher notificationCountEnricher
    ) {
        super();
        this.eventExtractor = eventExtractor;
        this.eventForwarder = eventForwarder;
        this.notificationExtractor = notificationExtractor;
        this.notificationProcessor = notificationProcessor;
        this.notificationCountEnricher = notificationCountEnricher;
    }

    @Override
    public void configure() {
        from(FILE_URI)
                .routeId(Route.EVENT_EXTRACTOR)
                .tracing()
                .process(eventExtractor)
                .setHeader(Header.MESSAGE_ID).body(Event.class, Event::getId)
                .multicast()
                .to(direct(Route.EVENT), direct(Route.SUBSCRIPTION_EXTRACTOR));

        from(direct(Route.NULL))
                .routeId(Route.NULL)
                .log(message("Skipping event forwarding."));

        from(direct(Route.EVENT))
                .routeId(Route.EVENT)
                .log(message("Event detected."))
                .process(eventForwarder)
                .log(message("Forwarding event to '${headers.destination}'."))
                .recipientList(header(RECIPIENT_HEADER), recipientDelimiter)
                .end();


        from(direct(Route.SUBSCRIPTION_EXTRACTOR))
                .routeId(Route.SUBSCRIPTION_EXTRACTOR)
                .process(notificationExtractor)
                .log(message("Number of subscribed containers found: '${body.size()}'"))
                .split(bodyAs(NotificationList.class))
                .to(direct(Route.SUBSCRIPTION));

        from(direct(Route.SUBSCRIPTION))
                .routeId(Route.SUBSCRIPTION)
                .setHeader(Header.MESSAGE_ID).body(Notification.class, Notification::getId)
                .setHeader(Header.DISPATCH_TIMEOUT).body(Notification.class, notification -> notification.getSubscription().getDebounceTime())
                .process(notificationCountEnricher)
                .choice()
                .when(
                        and(
                                header(Header.DISPATCH_TIMEOUT).isGreaterThan(0),
                                header(NotificationCountEnricher.COUNT_HEADER).isGreaterThan(1)
                        )
                )
                .to(direct(Route.NOTIFICATIONS_DEBOUNCED))
                .otherwise()
                .to(direct(Route.NOTIFICATION));

        from(direct(Route.NOTIFICATION))
                .routeId(Route.NOTIFICATION)
                .log(message("Start processing..."))
                .process(notificationProcessor);

        from(direct(Route.NOTIFICATIONS_DEBOUNCED))
                .routeId(Route.NOTIFICATIONS_DEBOUNCED)
                .log(message("Aggregating for ${headers.timeout}ms (see also %s).", Config.Dispatching.DEBOUNCE.key()))
                .aggregate(header(Header.MESSAGE_ID))
                .strategy(new UseLatestAggregationStrategy())
                .completionTimeout(header(Header.DISPATCH_TIMEOUT))
                .to(direct(Route.NOTIFICATION));
    }

    private static String direct(String routeId) {
        return String.format("direct:%s", routeId);
    }

    private static String message(String message, Object... params) {
        return LOG_PREFIX + String.format(message, params);
    }
}
