package com.github.qsrc.event.processor;

import com.github.qsrc.FileRouteBuilder;
import com.github.qsrc.event.Event;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventForwarder implements Processor {

    @Value("${dispatcher.forward.destination:direct:null}")
    private String[] forwardDestinations;

    @Value(FileRouteBuilder.RECIPIENT_DELIMITER_VALUE)
    private String forwardDelimiter;

    @Override
    public void process(Exchange exchange) {
        var message = exchange.getMessage();
        message.setHeader(
                FileRouteBuilder.RECIPIENT_HEADER,
                String.join(forwardDelimiter, forwardDestinations)
        );
    }

}
