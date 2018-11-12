package com.github.qsrc.eventdispatcher.event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {
                "dispatcher.event.namespace=globalns"
        }
)
public class EventFactoryTest {

    @Autowired
    private EventFactory eventFactory;

    @Test
    public void testCreate() {
        Event event = eventFactory.create(
                String.join(String.valueOf(EventFactory.FILE_SEPARATOR), new String[]{"namespace", "event"}),
                "content"
        );
        assertEquals("globalns.namespace.event", event.getId());
        assertEquals("content", event.getContent());
    }

}
