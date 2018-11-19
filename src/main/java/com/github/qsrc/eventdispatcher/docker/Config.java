package com.github.qsrc.eventdispatcher.docker;

public class Config {

    public static final Config ANY = new Config("", "");

    private static final String NAMESPACE_DELIMITER = ".";

    private String key;

    private String defaultValue;

    private Config(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public static Config of(String key) {
        return of(key, "");
    }

    public static Config of (String key, String defaultValue) {
        return new Config(key, defaultValue);
    }

    public String key() {
        return key;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public Config in(String namespace) {
        if (namespace == null || namespace.length() == 0) {
            return this;
        }
        return new Config(namespace + NAMESPACE_DELIMITER + this.key, this.defaultValue);
    }

    public static class Container {

        private static final String NAMESPACE = "container";

        public static final Config COMMAND = Config.of("command", "").in(NAMESPACE);

        public static final Config START = Config.of("start", "false").in(NAMESPACE);

    }

    public static class Dispatching {

        private static final String NAMESPACE = "dispatching";

        public static final Config DEBOUNCE = Config.of("debounce", "0").in(NAMESPACE);

    }

}
