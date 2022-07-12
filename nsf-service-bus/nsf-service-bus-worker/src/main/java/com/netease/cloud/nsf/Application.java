package com.netease.cloud.nsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/14
 **/
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition STOP = LOCK.newCondition();

    public static void main(String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:application-context.xml");
        applicationContext.registerShutdownHook();
        logger.info("worker start success !~");
        addHook(applicationContext);
        try {
            LOCK.lock();
            STOP.await();
        } catch (InterruptedException e) {
            logger.warn("worker stopped, interrupted by other thread!", e);
        } finally {
            LOCK.unlock();
        }
    }

    private static void addHook(AbstractApplicationContext applicationContext) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                applicationContext.stop();
            } catch (Exception e) {
                logger.error("worker stop exception ", e);
            }

            logger.info("jvm exit.");
            try {
                LOCK.lock();
                STOP.signal();
            } finally {
                LOCK.unlock();
            }
        }, "worker-shutdown-hook"));
    }
}
