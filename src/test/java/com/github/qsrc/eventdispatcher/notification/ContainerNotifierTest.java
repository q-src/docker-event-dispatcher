package com.github.qsrc.eventdispatcher.notification;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.qsrc.eventdispatcher.event.Event;
import com.github.qsrc.eventdispatcher.subscription.Subscription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContainerNotifierTest {

    private static final String EXEC_CMD_ID = "exec-id";

    private boolean containerIsRunning = true;

    private Notification notification = new Notification(
            Subscription.builder()
                    .containerId("container-id")
                    .command("touch someFile")
                    .build(),
            Event.builder()
                    .id("event-id")
                    .content("event-content")
                    .build()
    );

    @MockBean
    private DockerClient docker;

    @Mock
    private StartContainerCmd startContainerCmd;

    @Mock
    private ExecStartCmd execStartCmd;

    @Mock
    private ExecCreateCmd execCreateCmd;

    @Mock
    private ExecCreateCmdResponse execCreateCmdResponse;

    private ContainerNotifier containerNotifier;

    @Before
    public void setup() {
        when(docker.startContainerCmd(notification.getSubscription().getContainerId())).thenReturn(startContainerCmd);
        when(startContainerCmd.exec()).then(mock -> {
            containerIsRunning = true;
            return null;
        });

        when(docker.execCreateCmd(notification.getSubscription().getContainerId())).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(any())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);

        when(execCreateCmdResponse.getId()).thenReturn(EXEC_CMD_ID);

        when(docker.execStartCmd(EXEC_CMD_ID)).thenReturn(execStartCmd);
        when(execStartCmd.withDetach(true)).thenReturn(execStartCmd);

        mockIsRunning(notification.getSubscription().getContainerId(), true);

        containerNotifier = new ContainerNotifier(docker);
    }

    @Test
    public void testNotify() {
        containerNotifier.notify(notification);
        verify(execStartCmd, times(1)).exec(any());
    }

    @Test
    public void testStartContainer() {
        notification.getSubscription().setStart(true);
        mockIsRunning(notification.getSubscription().getContainerId(), false);

        containerNotifier.notify(notification);

        verify(startContainerCmd, times(1)).exec();
        verify(execStartCmd, times(1)).exec(any());
    }

    @Test
    public void testCommandWithSingleQuotes() {
        notification.getSubscription().setCommand("echo 'some content' > someFile");
        containerNotifier.notify(notification);
        verify(execCreateCmd, times(1)).withCmd("echo", "some content", ">", "someFile");
    }

    @Test
    public void testCommandWithDoubleQuotes() {
        notification.getSubscription().setCommand("echo \"some content\" > someFile");
        containerNotifier.notify(notification);
        verify(execCreateCmd, times(1)).withCmd("echo", "some content", ">", "someFile");
    }

    private void mockIsRunning(String containerId, Boolean isRunning) {
        containerIsRunning = isRunning;
        var containerState = mock(InspectContainerResponse.ContainerState.class);
        when(containerState.getRunning()).then(mock -> containerIsRunning);
        var inspectContainerResponse = mock(InspectContainerResponse.class);
        when(inspectContainerResponse.getState()).thenReturn(containerState);
        var inspectContainerCmd = mock(InspectContainerCmd.class);
        when(inspectContainerCmd.exec()).thenReturn(inspectContainerResponse);
        when(docker.inspectContainerCmd(containerId)).thenReturn(inspectContainerCmd);
    }

}
