package com.bifrost.demo.service.monitoring;

public interface LogService {
    void info(String message);

    void error(String message);

    void debug(String message);

    void error(Exception ex);
}
