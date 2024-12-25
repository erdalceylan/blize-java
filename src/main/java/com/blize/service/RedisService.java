package com.blize.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value); // String bir değer ekler
    }

    public String getValue(String key) {
        return (String) redisTemplate.opsForValue().get(key); // Değeri alır
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key); // Anahtarı siler
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
}
