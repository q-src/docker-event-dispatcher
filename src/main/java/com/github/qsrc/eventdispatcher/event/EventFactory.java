package com.github.qsrc.eventdispatcher.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventFactory {

    static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);

    @Value("${dispatcher.event.namespace:}")
    private String namespace;

    public Event create(String path, String content) {
        return Event.builder()
                .content(content)
                .id(extractId(path))
                .build();
    }

    private String extractId(String path) {
        return namespace +
                (namespace.length() > 0 ? Event.NAMESPACE_DELIMITER : "") +
                path.replace(FILE_SEPARATOR, Event.NAMESPACE_DELIMITER);
    }

}
