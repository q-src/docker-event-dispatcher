package com.github.qsrc.eventdispatcher.docker;

import com.github.dockerjava.api.model.Container;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static junit.framework.TestCase.*;

@RunWith(SpringRunner.class)
public class ConfigProviderTest {

    @Mock
    private Container subscribedContainer;

    @Mock
    private Container notSubscribedContainer;

    private Map<String, String> labels = Map.of(
            "subscription.event.some-label", "some-value",
            "subscription.event.int-label", "23",
            "subscription.event.bool-label-string-true", "true",
            "subscription.event.bool-label-string-false", "false",
            "subscription.event.bool-label-numeric-true", "1",
            "subscription.event.bool-label-numeric-false", "0"
    );

    private ConfigProvider configProvider;

    @Before
    public void setup() {
        subscribedContainer.labels = labels;
        notSubscribedContainer.labels = Map.of("some-other-label", "some-other-value");

        configProvider = new ConfigProvider("subscription");
    }

    @Test
    public void testHasAny() {
        assertTrue(configProvider.hasAny(subscribedContainer, "event"));
        assertFalse(configProvider.hasAny(subscribedContainer, "another-event"));
        assertFalse(configProvider.hasAny(notSubscribedContainer, "event"));
    }

    @Test
    public void testSupportsStringValues() {
        Config config = Config.of("some-label");
        assertEquals("some-value", configProvider.get(subscribedContainer, config.in("event")));

        config = Config.of("missing-label", "default-value").in("event");
        assertEquals("default-value", configProvider.get(subscribedContainer, config));
    }

    @Test
    public void testSupportsIntValues() {
        assertEquals(23, configProvider.getInt(subscribedContainer, Config.of("int-label").in("event")));
        assertEquals(22, configProvider.getInt(subscribedContainer, Config.of("missing-label", "22").in("event")));
    }

    @Test
    public void testGetBool() {
        assertTrue(configProvider.getBool(subscribedContainer, Config.of("bool-label-string-true").in("event")));
        assertFalse(configProvider.getBool(subscribedContainer, Config.of("bool-label-string-false").in("event")));
        assertTrue(configProvider.getBool(subscribedContainer, Config.of("bool-label-numeric-true").in("event")));
        assertFalse(configProvider.getBool(subscribedContainer, Config.of("bool-label-numeric-false").in("event")));

        assertTrue(configProvider.getBool(subscribedContainer, Config.of("missing-label", "true").in("event")));
        assertTrue(configProvider.getBool(subscribedContainer, Config.of("missing-label", "1").in("event")));
        assertFalse(configProvider.getBool(subscribedContainer, Config.of("missing-label", "false").in("event")));
        assertFalse(configProvider.getBool(subscribedContainer, Config.of("missing-label", "0").in("event")));
    }

}
