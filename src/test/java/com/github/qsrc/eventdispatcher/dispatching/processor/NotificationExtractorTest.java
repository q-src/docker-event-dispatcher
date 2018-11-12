package com.github.qsrc.eventdispatcher.dispatching.processor;

import com.github.qsrc.eventdispatcher.subscription.SubscriptionManager;
import com.github.qsrc.eventdispatcher.event.Event;
import com.github.qsrc.eventdispatcher.subscription.Subscription;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationExtractorTest {

    private NotificationExtractor notificationExtractor;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @MockBean
    private SubscriptionManager subscriptionManager;

    @Mock
    private Subscription subscription;

    @Mock
    private Event event;

    @Before
    public void init() {
        when(exchange.getMessage()).thenReturn(message);
        when(message.getBody(eq(Event.class))).thenReturn(event);
        when(subscriptionManager.findSubscriptions(event)).thenReturn(Collections.singletonList(subscription));
        notificationExtractor = new NotificationExtractor(subscriptionManager);
    }

    @Test
    public void testProcessMessage() {
        notificationExtractor.process(exchange);

        ArgumentCaptor<NotificationList> notificationListCaptor = ArgumentCaptor.forClass(NotificationList.class);
        verify(message, times(1)).setBody(notificationListCaptor.capture(), eq(NotificationList.class));
        var notificationList = notificationListCaptor.getValue();
        assertNotNull(notificationList);
        var notification = notificationList.get(0);
        assertNotNull(notification);
        assertSame(subscription, notification.getSubscription());
        assertSame(event, notification.getEvent());
    }
}
