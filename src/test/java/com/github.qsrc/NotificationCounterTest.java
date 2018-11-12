package com.github.qsrc;

import com.github.qsrc.event.Notification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationCounterTest {

    private static final String NOTIFICATION_ID = "the-id";

    @Mock
    private Notification notification;

    private NotificationCounter notificationCounter;

    @Before
    public void setup() {
        when(notification.getId()).thenReturn(NOTIFICATION_ID);

        notificationCounter = new NotificationCounter();
    }

    @Test
    public void testCount() {
        var count = notificationCounter.count(notification);
        assertEquals(1, count);

        count = notificationCounter.count(notification);
        assertEquals(2, count);
    }

}
