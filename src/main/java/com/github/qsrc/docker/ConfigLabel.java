package com.github.qsrc.docker;

public enum ConfigLabel {

    ROOT("", ""),

    CONTAINER_COMMAND(Namespace.CONTAINER + "command", ""),

    CONTAINER_START(Namespace.CONTAINER + "start", "false"),

    DISPATCH_DEBOUNCE(Namespace.DISPATCH + "debounce", "0");

    private String key;

    private String defaultValue;

    ConfigLabel(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String key(String... namespace) {
        if (namespace == null || namespace.length == 0) {
            return key;
        }
        return String.join(Namespace.DELIMITER, namespace) + Namespace.DELIMITER + key;
    }

    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return key;
    }

    private static class Namespace {

        private static final String DISPATCH = "dispatch.";

        private static final String CONTAINER = "container.";

        private static String DELIMITER = ".";

    }

}
