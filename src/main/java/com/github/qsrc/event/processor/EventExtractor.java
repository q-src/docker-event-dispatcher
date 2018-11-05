package com.github.qsrc.event.processor;

import com.github.qsrc.event.Event;
import com.github.qsrc.event.EventFactory;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class EventExtractor implements Processor {

    static final String EVENT_ID_HEADER = "event.id";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventExtractor.class);

    private EventFactory eventFactory;

    public EventExtractor(EventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    @Override
    public void process(Exchange exchange) {
        var message = exchange.getMessage();
        try {
            var event = eventFactory.create(
                    message.getMandatoryBody(GenericFile.class).getFileName(),
                    message.getMandatoryBody(String.class)
            );
            message.setHeader(EVENT_ID_HEADER, event.getId());
            exchange.getMessage().setBody(event, Event.class);
        } catch (InvalidPayloadException e) {
            LOGGER.error("Unable to extract event from message '{}'.", message);
        }
    }
}
