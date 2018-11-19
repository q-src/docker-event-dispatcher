package com.github.qsrc.eventdispatcher.docker;

import com.github.dockerjava.api.model.Container;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ConfigProvider {

    private static List<String> TRUE_VALUES = Arrays.asList("true", "1");

    @Value("${event.dispatcher.label.namespace:subscription}")
    private String namespace;

    public ConfigProvider() {}

    ConfigProvider(String namespace) {
        this.namespace = namespace;
    }

    public boolean hasAny(Container container, String subNamespace) {
        return container.labels.keySet().stream()
                .anyMatch(l -> l.startsWith(Config.ANY.in(subNamespace).in(namespace).key()));
    }

    public String get(Container container, Config config) {
        config = config.in(namespace);
        return container.labels.getOrDefault(
                config.key(),
                config.defaultValue()
        );
    }

    public int getInt(Container container, Config config) {
        return Integer.parseInt(get(container, config));
    }

    public boolean getBool(Container container, Config config) {
        return TRUE_VALUES.contains(get(container, config));
    }

}
