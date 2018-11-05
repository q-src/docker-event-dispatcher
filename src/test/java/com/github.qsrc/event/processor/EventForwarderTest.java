package com.github.qsrc.event.processor;

import com.github.qsrc.FileRouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EventForwarderTest {

    @Autowired
    protected EventForwarder eventForwarder;

    @Mock
    protected Exchange exchange;

    @Mock
    protected Message message;

    @Before
    public void init() {
        when(exchange.getMessage()).thenReturn(message);
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(
            properties = {
                    "dispatcher.forward.destination=forward.dst"
            }
    )
    public static class SingleTest extends EventForwarderTest {

        @Test
        public void testForwardSingle() {
            eventForwarder.process(exchange);
            verify(message, times(1)).setHeader(FileRouteBuilder.RECIPIENT_HEADER, "forward.dst");
        }

    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(
            properties = {
                    "dispatcher.forward.destination=forward.dst,forward.second.dst"
            }
    )
    public static class MultipleTest extends EventForwarderTest {

        @Test
        public void testForwardMultiple() {
            eventForwarder.process(exchange);
            verify(message, times(1)).setHeader(
                    FileRouteBuilder.RECIPIENT_HEADER,
                    "forward.dst,forward.second.dst"
            );
        }

    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(
            properties = {
                    "dispatcher.forward.destination=forward.dst,forward.second.dst",
                    "dispatcher.forward.delimiter=$"
            }
    )
    public static class MultipleCustomDelimiterTest extends EventForwarderTest {

        @Test
        public void testForwardMultipleWithCustomDelimiter() {
            eventForwarder.process(exchange);
            verify(message, times(1)).setHeader(
                    FileRouteBuilder.RECIPIENT_HEADER,
                    "forward.dst$forward.second.dst"
            );
        }

    }
}
