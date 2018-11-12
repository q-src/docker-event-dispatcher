package com.github.qsrc.eventdispatcher.docker;

import com.github.dockerjava.api.model.Container;
import com.github.qsrc.eventdispatcher.event.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class LabelProvider {

    private static List<String> TRUE_VALUES = Arrays.asList("true", "1");

    @Value("${event.dispatcher.label.namespace:subscription}")
    private String namespace;

    public boolean hasAny(Container container, Event event) {
        return container.labels.keySet().stream()
                .anyMatch(label -> label.startsWith(ConfigLabel.ROOT.key(namespace, event.getId())));
    }

    public String get(Container container, Event event, ConfigLabel configLabel) {
        return container.labels.getOrDefault(
                configLabel.key(namespace, event.getId()),
                configLabel.defaultValue()
        );
    }

    public int getInt(Container container, Event event, ConfigLabel configLabel) {
        return Integer.parseInt(get(container, event, configLabel));
    }

    public boolean getBool(Container container, Event event, ConfigLabel configLabel) {
        return TRUE_VALUES.contains(get(container, event, configLabel));
    }

}
