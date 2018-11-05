package com.github.qsrc.event.processor;

import com.github.qsrc.event.Event;
import com.github.qsrc.event.EventFactory;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.component.file.GenericFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventExtractorTest {

    private EventExtractor eventExtractor;

    @MockBean
    private EventFactory eventFactory;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @Mock
    private GenericFile file;

    @Before
    public void init() throws InvalidPayloadException {
        when(exchange.getMessage()).thenReturn(message);
        when(message.getMandatoryBody(eq(String.class))).thenReturn("content");
        when(message.getMandatoryBody(eq(GenericFile.class))).thenReturn(file);
        when(file.getFileName()).thenReturn("namespace/event");
        when(eventFactory.create(anyString(), anyString())).thenReturn(Event.builder().id("id").build());

        eventExtractor = new EventExtractor(eventFactory);
    }

    @Test
    public void testProcessMessage() {
        eventExtractor.process(exchange);
        verify(eventFactory, times(1)).create("namespace/event", "content");
        verify(message, times(1)).setHeader(EventExtractor.EVENT_ID_HEADER, "id");
    }
}
