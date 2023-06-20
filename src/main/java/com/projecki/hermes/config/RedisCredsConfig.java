package com.projecki.hermes.config;

public class RedisCredsConfig {

    private String host;
    private String password;
    private String port;

    public RedisCredsConfig() {}

    public RedisCredsConfig(String host, String password, String port) {
        this.host = host;
        this.password = password;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getPort() {
        return port;
    }
}
