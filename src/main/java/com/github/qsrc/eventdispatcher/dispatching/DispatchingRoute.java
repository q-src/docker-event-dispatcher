package com.github.qsrc.eventdispatcher.dispatching;

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

    public static final String FILE_URI = "file://camel?recursive=true";

    private static class Route {
        private static final String NULL = "null";
        private static final String EVENT = "event";
        private static final String SUBSCRIPTION_EXTRACTOR = "subscriptions";
        private static final String SUBSCRIPTION = "subscription";
        private static final String DEBOUNCED_NOTIFICATION = "notifications:debounced";
        private static final String NOTIFICATION = "notifications";
    }

    private static class Header {
        private static final String SUBSCRIPTION_ID = "id";
        private static final String DISPATCH_TIMEOUT = "timeout";
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
        from("file://camel?recursive=true")
                .routeId(direct("events:extractor"))
                .tracing()
                .process(eventExtractor)
                .multicast()
                .to(direct(Route.EVENT), direct(Route.SUBSCRIPTION_EXTRACTOR));

        from(direct(Route.NULL))
                .routeId(Route.NULL)
                .log("[${headers.breadcrumbId}] Skipping event forwarding of '${headers[event.id]}'");

        from(direct(Route.EVENT))
                .routeId(Route.EVENT)
                .log("[${headers.breadcrumbId}] Detected event '${headers[event.id]}'")
                .process(eventForwarder)
                .log("[${headers.breadcrumbId}] Forwarding event '${headers[event.id]}' to '${headers.destination}'")
                .recipientList(header(RECIPIENT_HEADER), recipientDelimiter)
                .end();


        from(direct(Route.SUBSCRIPTION_EXTRACTOR))
                .routeId(Route.SUBSCRIPTION_EXTRACTOR)
                .process(notificationExtractor)
                .split(bodyAs(NotificationList.class))
                .to(direct(Route.SUBSCRIPTION));

        from(direct(Route.SUBSCRIPTION))
                .routeId(Route.SUBSCRIPTION)
                .setHeader(Header.SUBSCRIPTION_ID).body(Notification.class, Notification::getId)
                .setHeader(Header.DISPATCH_TIMEOUT).body(Notification.class, notification -> notification.getSubscription().getDebounceTime())
                .process(notificationCountEnricher)
                .choice()
                .when(
                        and(
                                header(Header.DISPATCH_TIMEOUT).isGreaterThan(0),
                                header(NotificationCountEnricher.COUNT_HEADER).isGreaterThan(1)
                        )
                )
                .to(direct(Route.DEBOUNCED_NOTIFICATION))
                .otherwise()
                .to(direct(Route.NOTIFICATION));

        from(direct(Route.NOTIFICATION))
                .log("[${headers.id}] Start processing...")
                .process(notificationProcessor);

        from(direct(Route.DEBOUNCED_NOTIFICATION))
                .routeId(Route.DEBOUNCED_NOTIFICATION)
                .log("[${headers.id}] Aggregating with 'dispatch.debounce'='${headers.timeout}ms'.")
                .aggregate(header(Header.SUBSCRIPTION_ID))
                .strategy(new UseLatestAggregationStrategy())
                .completionTimeout(header(Header.DISPATCH_TIMEOUT))
                .to(direct(Route.NOTIFICATION));
    }

    private static String direct(String routeId) {
        return String.format("direct:%s", routeId);
    }
}
