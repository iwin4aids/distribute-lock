package com.skloda.distributelock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpringDistributeLockTest {

    private static final int POOL_SIZE = 10;

    @Autowired
    RedisLockRegistry redisLockRegistry;

    @Test
    public void testSpringRedisLock() {
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            executorService.submit(() -> {
                // 获取锁对象（没有真正获取到锁)
                Lock lock = redisLockRegistry.obtain("skloda");
                while (true) {
                    try {
                        // 2秒内尝试获取锁，获取到则立即返回true，超时未获取到返回false
                        boolean success = lock.tryLock(2, TimeUnit.SECONDS);
                        if (success) {
                            log.info("acquired lock in thread => " + Thread.currentThread().getName());
                            break;
                        } else {
                            log.warn(Thread.currentThread().getName() + "等待");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    // 模拟业务执行
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 释放锁
                    lock.unlock();
                }

            });

        }

        // block main thread
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
