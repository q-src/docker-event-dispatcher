package com.github.qsrc.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.qsrc.event.Event;
import com.github.qsrc.event.Notification;
import com.github.qsrc.event.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class ContainerNotifier {

    private static Logger LOGGER = LoggerFactory.getLogger(ContainerNotifier.class);

    private final static String LOG_PREFIX = "[{}] ";

    private final static String NOT_DISPATCHING_MSG = LOG_PREFIX + "Cannot dispatch event. ";

    private DockerClient docker;

    public ContainerNotifier(DockerClient docker) {
        this.docker = docker;
    }

    public void notify(Notification notification) {
        var subscription = notification.getSubscription();
        try {
            if (subscription.isStart() && !isRunning(subscription.getContainerId())) {
                docker.startContainerCmd(subscription.getContainerId())
                        .exec();
            }
            if (!isRunning(subscription.getContainerId())) {
                LOGGER.warn(
                        NOT_DISPATCHING_MSG + "Container is not running. You may want to set the '{}' label to true.",
                        notification.getId(),
                        ConfigLabel.CONTAINER_START
                );
            } else if (subscription.getCommand().isEmpty()) {
                LOGGER.warn(
                        NOT_DISPATCHING_MSG + "Label '{}' is empty.",
                        notification.getId(),
                        ConfigLabel.CONTAINER_COMMAND
                );
            } else {
                execInContainer(notification);
            }

        } catch (DockerException dockerException) {
            LOGGER.error(
                    LOG_PREFIX, "Event Dispatching failed. Reason: '{}' - '{}'",
                    notification.getId(),
                    dockerException.getClass().getSimpleName(),
                    dockerException.getMessage()
            );
        }
    }

    private void execInContainer(Notification notification) {
        var event = notification.getEvent();
        var subscription = notification.getSubscription();
        var command = createCommand(subscription);
        var execCmd = docker.execCreateCmd(subscription.getContainerId())
                .withCmd(command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        docker.execStartCmd(execCmd.getId())
                .withDetach(true)
                .exec(new ExecStartResultCallback(System.out, System.err));
        LOGGER.info(
                LOG_PREFIX + "Event dispatched (Command: [{}]).",
                subscription.getContainerId(),
                event.getId(),
                String.format("\"%s\"", String.join("\", \"", command))
        );
    }

    private boolean isRunning(String containerId) {
        return Boolean.TRUE.equals(
                docker.inspectContainerCmd(containerId)
                        .exec()
                        .getState().getRunning()
        );
    }

    private String[] createCommand(Subscription subscription) {
        var rawCommand = subscription.getCommand();

        var regex = Pattern.compile("(\"[^\"]*\"|'[^']*'|[\\S]+)");
        var matcher = regex.matcher(rawCommand);
        var command = new ArrayList<String>();
        while (matcher.find()) {
            command.add(matcher.group().replaceAll("^[\"']{1}|[\"']{1}$", ""));
        }
        return command.toArray(new String[]{});
    }


}
