package com.skloda.distributelock.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

/**
 * @Author: jiangkun
 * @Description:
 * @Date: Created in 2019-03-26 16:43
 */
@Configuration
public class RedisLockConfiguration {
    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        // registryKey对应redis中key的前缀
        return new RedisLockRegistry(redisConnectionFactory, "distribute-lock");
    }
}
