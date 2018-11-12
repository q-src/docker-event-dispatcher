package com.github.qsrc.event;

import com.github.qsrc.docker.ContainerNotifier;
import com.github.qsrc.docker.SubscriptionManager;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileMessage;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(CamelSpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
public class RouteIT {

    private static final String TEST_FILE_ROUTE = "direct:file";

    private static Event EXPECTED_EVENT = Event.builder().id("directory.file").content("content").build();

    private static List<Subscription> SUBSCRIPTIONS = List.of(
            Subscription.builder()
                    .containerId("container-0")
                    .build(),
            Subscription.builder()
                    .containerId("container-1")
                    .debounceTime(100)
                    .build()
    );

    @Autowired
    private CamelContext camelContext;

    @MockBean
    private ContainerNotifier containerNotifier;

    @MockBean
    private SubscriptionManager subscriptionManager;

    @Produce(uri = TEST_FILE_ROUTE)
    private ProducerTemplate template;

    private Exchange exchange;

    @Before
    public void setupTestRoute() throws Exception {
        camelContext.getRouteDefinition("direct:events:extractor").adviceWith(camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                replaceFromWith(TEST_FILE_ROUTE);
            }
        });
    }

    @Before
    public void setupMocks() {
        when(subscriptionManager.findSubscriptions(any(Event.class))).thenReturn(SUBSCRIPTIONS);
    }

    @Before
    public void setupExchange() {
        var file = new GenericFile<String>();
        file.setBody(EXPECTED_EVENT.getContent());
        file.setFileName("directory/file");
        var message = new GenericFileMessage<>(camelContext);
        message.setBody(file, GenericFile.class);
        exchange = new DefaultExchange(camelContext);
        exchange.setMessage(message);
    }

    @Test
    public void testSimple() {
        template.send(exchange.copy());

        verify(containerNotifier, times(SUBSCRIPTIONS.size())).notify(any(Notification.class));
        verify(containerNotifier, times(1))
                .notify(eq(new Notification(SUBSCRIPTIONS.get(0), EXPECTED_EVENT)));
        verify(containerNotifier, times(1))
                .notify(eq(new Notification(SUBSCRIPTIONS.get(1), EXPECTED_EVENT)));
    }

    @Test
    public void testDebounced() {
        template.send(exchange.copy()); // handled by: container-0 and container-1
        template.send(exchange.copy()); // handled by: container-0 only
        template.send(exchange.copy()); // handled by: container-0 and container-1

        // container-0 has handled all events, container-1 only the first one
        verify(containerNotifier, times(4)).notify(any(Notification.class));

        // container-1 has handled the second event, too
        verify(containerNotifier, timeout(1000).times(5)).notify(any(Notification.class));

        verify(containerNotifier, times(3))
                .notify(eq(new Notification(SUBSCRIPTIONS.get(0), EXPECTED_EVENT)));
        verify(containerNotifier, times(2))
                .notify(eq(new Notification(SUBSCRIPTIONS.get(1), EXPECTED_EVENT)));
    }

}
