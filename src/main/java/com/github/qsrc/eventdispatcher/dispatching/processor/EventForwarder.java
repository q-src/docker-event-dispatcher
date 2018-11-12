package com.github.qsrc.eventdispatcher.dispatching.processor;

import com.github.qsrc.eventdispatcher.dispatching.DispatchingRoute;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventForwarder implements Processor {

    @Value("${dispatcher.forward.destination:direct:null}")
    private String[] forwardDestinations;

    @Value(DispatchingRoute.RECIPIENT_DELIMITER_VALUE)
    private String forwardDelimiter;

    @Override
    public void process(Exchange exchange) {
        var message = exchange.getMessage();
        message.setHeader(
                DispatchingRoute.RECIPIENT_HEADER,
                String.join(forwardDelimiter, forwardDestinations)
        );
    }

}
