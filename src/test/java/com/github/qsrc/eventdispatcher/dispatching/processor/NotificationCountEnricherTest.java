package com.github.qsrc.eventdispatcher.dispatching.processor;

import com.github.qsrc.eventdispatcher.notification.NotificationCounter;
import com.github.qsrc.eventdispatcher.notification.Notification;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationCountEnricherTest {

    private static final int EXPECTED_COUNT = 20;

    private NotificationCountEnricher notificationCountEnricher;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @Mock
    private Notification notification;

    @MockBean
    private NotificationCounter notificationCounter;

    @Before
    public void init() throws InvalidPayloadException {
        when(exchange.getMessage()).thenReturn(message);
        when(message.getMandatoryBody(eq(Notification.class))).thenReturn(notification);
        when(notificationCounter.count(any())).thenReturn(EXPECTED_COUNT);
        notificationCountEnricher = new NotificationCountEnricher(notificationCounter);
    }

    @Test
    public void testProcessMessage() throws InvalidPayloadException {
        notificationCountEnricher.process(exchange);

        verify(notificationCounter, times(1)).count(same(notification));
        verify(message, times(1)).setHeader(NotificationCountEnricher.COUNT_HEADER, EXPECTED_COUNT);
    }
}
