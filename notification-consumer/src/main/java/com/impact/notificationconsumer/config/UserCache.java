package com.impact.notificationconsumer.config;

import com.impact.notificationconsumer.entity.UserEntity;
import com.impact.notificationconsumer.payload.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserCache {

    private final Map<String, CacheEntry<List<UserResponse>>> cache = new ConcurrentHashMap<>();

    private static final long TTL = 5 * 60 * 1000; // 5 minutes

    public void put(String key, List<UserResponse> users) {
        cache.put(key, new CacheEntry<>(users, TTL));
    }

    public List<UserResponse> get(String key) {
        CacheEntry<List<UserResponse>> entry = cache.get(key);

        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return Collections.emptyList();
        }
        return entry.getValue();
    }

    public void evict(String key) {
        cache.remove(key);
    }
}
