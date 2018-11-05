package com.github.qsrc;

import com.github.qsrc.event.Subscription;
import com.github.qsrc.event.processor.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileRouteBuilder extends RouteBuilder {

    public static final String RECIPIENT_DELIMITER_VALUE = "${dispatcher.forward.delimiter:,}";

    public static final String RECIPIENT_HEADER = "destination";

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

    private SubscriptionsExtractor subscriptionsExtractor;

    private EventForwarder eventForwarder;

    private ContainerNotifier containerNotifier;

    @Value(RECIPIENT_DELIMITER_VALUE)
    private String recipientDelimiter;

    public FileRouteBuilder(
            EventExtractor eventExtractor,
            EventForwarder eventForwarder,
            SubscriptionsExtractor subscriptionsExtractor,
            ContainerNotifier containerNotifier
    ) {
        super();
        this.eventExtractor = eventExtractor;
        this.eventForwarder = eventForwarder;
        this.subscriptionsExtractor = subscriptionsExtractor;
        this.containerNotifier = containerNotifier;
    }

    @Override
    public void configure() {
        from("file://camel?recursive=true")
                .routeId("events:extractor")
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
                .recipientList(header(RECIPIENT_HEADER), recipientDelimiter);


        from(direct(Route.SUBSCRIPTION_EXTRACTOR))
                .routeId(Route.SUBSCRIPTION_EXTRACTOR)
                .process(subscriptionsExtractor)
                .split(bodyAs(SubscriptionList.class))
                .to(direct(Route.SUBSCRIPTION));

        from(direct(Route.SUBSCRIPTION))
                .routeId(Route.SUBSCRIPTION)
                .setHeader(Header.SUBSCRIPTION_ID).body(Subscription.class, Subscription::getId)
                .setHeader(Header.DISPATCH_TIMEOUT).body(Subscription.class, Subscription::getDebounceTime)
                .choice().when(header(Header.DISPATCH_TIMEOUT).isGreaterThan(0))
                    .to(direct(Route.DEBOUNCED_NOTIFICATION))
                .otherwise()
                    .to(direct(Route.NOTIFICATION));

        from(direct(Route.NOTIFICATION))
                .log("[${headers.id}] Start processing...")
                .process(containerNotifier);

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
