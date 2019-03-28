package com.skloda.distributelock.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @Author: jiangkun
 * @Description: 基于redis的分布式锁实现
 * @Date: Created in 2019-03-28 16:07
 */
@Component
public class RedisLock {

    private static final String LUA_SCRIPT_STR = "if redis.call('get', KEYS[1]) == ARGV[1] then return (1==redis.call('del', KEYS[1])) else return false end";
    private static final RedisScript<Boolean> RELEASE_SCRIPT = new DefaultRedisScript<>(LUA_SCRIPT_STR, Boolean.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 尝试获取分布式锁
     * 使用redis的setnx命令实现分布式锁，同时加上expire过期时间
     *
     * @param lockKey    锁键，抽象为一类可竞争线程的组标识
     * @param requestId  请求标识，贯穿业务处理前后
     * @param expireTime 超期时间毫秒
     * @return 是否获取成功
     */
    public Boolean lock(String lockKey, String requestId, int expireTime, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, timeUnit);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁键
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public Boolean unlock(String lockKey, String requestId) {
        return redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(lockKey), requestId);
    }

}