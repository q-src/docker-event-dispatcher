package com.github.qsrc.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {

    public static char NAMESPACE_DELIMITER = '.';

    private String id;

    private String content;

}
