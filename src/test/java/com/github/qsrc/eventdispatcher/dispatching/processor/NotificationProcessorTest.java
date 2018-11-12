package com.github.qsrc.eventdispatcher.dispatching.processor;

import com.github.qsrc.eventdispatcher.notification.ContainerNotifier;
import com.github.qsrc.eventdispatcher.notification.Notification;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationProcessorTest {

    @Autowired
    protected NotificationProcessor notificationProcessor;

    @Mock
    private ContainerNotifier containerNotifier;

    @Mock
    protected Exchange exchange;

    @Mock
    protected Message message;

    @Mock
    protected Notification notification;

    @Before
    public void init() {
        when(exchange.getMessage()).thenReturn(message);
        when(message.getBody(eq(Notification.class))).thenReturn(notification);
        notificationProcessor = new NotificationProcessor(containerNotifier);
    }

    @Test
    public void testNotify() {
        notificationProcessor.process(exchange);
        verify(containerNotifier, times(1)).notify(same(notification));
    }

}
