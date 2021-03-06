package com.github.qsrc.eventdispatcher.notification;

import com.github.qsrc.eventdispatcher.notification.Notification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationCounter {

    private Map<String, Integer> counts;

    public NotificationCounter() {
        counts = new HashMap<>();
    }

    public int count(Notification notification) {
        var count = counts.getOrDefault(notification.getId(), 0);
        counts.put(notification.getId(), ++count);
        return count;
    }

}
