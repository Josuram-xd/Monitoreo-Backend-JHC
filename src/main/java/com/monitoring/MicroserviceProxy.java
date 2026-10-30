package com.monitoring;

public interface MicroserviceProxy<T> {

    T execute(String operation, Object... params);
}
