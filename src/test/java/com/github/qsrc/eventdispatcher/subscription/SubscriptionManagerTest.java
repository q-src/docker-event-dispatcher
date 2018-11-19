package com.github.qsrc.eventdispatcher.subscription;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import com.github.qsrc.eventdispatcher.docker.ConfigProvider;
import com.github.qsrc.eventdispatcher.event.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubscriptionManagerTest {

    private static final Container SUBSCRIBED_CONTAINER = mock(Container.class);

    @MockBean
    private DockerClient docker;

    @MockBean
    private ConfigProvider configProvider;

    @MockBean
    private SubscriptionFactory subscriptionFactory;

    @Mock
    private Event event;

    @Mock
    private ListContainersCmd listContainersCmd;

    private List<Container> containers = List.of(
            mock(Container.class),
            SUBSCRIBED_CONTAINER,
            mock(Container.class)
    );

    private SubscriptionManager subscriptionManager;

    @Before
    public void setup() {
        when(event.getId()).thenReturn("the-id");

        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.exec()).thenReturn(containers);

        when(docker.listContainersCmd()).thenReturn(listContainersCmd);

        when(configProvider.hasAny(any(Container.class), anyString())).thenReturn(false);
        when(configProvider.hasAny(same(SUBSCRIBED_CONTAINER), anyString())).thenReturn(true);

        subscriptionManager = new SubscriptionManager(docker, configProvider, subscriptionFactory);
    }

    @Test
    public void testFindSubscription() {
        var subscriptions = subscriptionManager.findSubscriptions(event);
        assertEquals(1, subscriptions.size());
        verify(subscriptionFactory, times(1)).create(any(Container.class), any(Event.class));
        verify(subscriptionFactory, times(1)).create(same(SUBSCRIBED_CONTAINER), same(event));
    }

}
