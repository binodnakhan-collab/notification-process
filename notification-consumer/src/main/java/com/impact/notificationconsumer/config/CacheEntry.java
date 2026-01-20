package com.impact.notificationconsumer.config;

public class CacheEntry<T> {
    private final T value;
    private final long expiryTime;

    public CacheEntry(T value, long ttlMillis) {
        this.value = value;
        this.expiryTime = System.currentTimeMillis() + ttlMillis;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public T getValue() {
        return value;
    }
}
