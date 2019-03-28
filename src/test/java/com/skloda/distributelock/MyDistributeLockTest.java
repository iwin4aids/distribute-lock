package com.skloda.distributelock;

import com.skloda.distributelock.utils.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MyDistributeLockTest {

    private static final int POOL_SIZE = 10;
    private static final String LOCK_KEY = "distribute-lock-test";

    @Autowired
    RedisLock redisLock;

    @Test
    public void testMyDistributeLock() {
        CountDownLatch latch = new CountDownLatch(POOL_SIZE);
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            executorService.submit(() -> {
                // 获取锁对象（没有真正获取到锁)
                while (true) {
                    // 以当前线程为名称作为请求id，会写入LOCK_KEY对应的value中，为了“解铃还须系铃人“
                    boolean canDo = redisLock.lock(LOCK_KEY, Thread.currentThread().getName(), 60, TimeUnit.SECONDS);
                    if (canDo) {
                        log.info(Thread.currentThread().getName() + " => 获取到锁,准备执行业务...");
                        break;
                    }

                    // 实际业务这里可以让当前线程等待避免cpu使用过高
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    // 模拟业务执行时间
                    Thread.sleep(2000);
                    latch.countDown();
                    log.info(Thread.currentThread().getName() + " => 执行业务完毕，准备释放锁...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 释放锁
                    // 即当前线程加的锁必须由当前线程释放，或者自动超时
                    redisLock.unlock(LOCK_KEY, Thread.currentThread().getName());
                }
            });

        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("全部任务执行完毕!!!");
    }

}
