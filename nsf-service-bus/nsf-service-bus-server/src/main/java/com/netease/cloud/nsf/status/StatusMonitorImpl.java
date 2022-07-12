package com.netease.cloud.nsf.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class StatusMonitorImpl implements StatusMonitor {
    private static final Logger logger = LoggerFactory.getLogger(StatusMonitorImpl.class);

    private AtomicReference<Status> status;
    private final Map<String, List<BiConsumer<Event, Status.Property>>> handlers;


    private StatusProductor statusProductor;
    private ScheduledExecutorService timerTask;
    private ExecutorService notifyQueue;
    private long timerIntervalMs;

    public StatusMonitorImpl(long checkIntervalMs, StatusProductor productor) {
        this.status = new AtomicReference<>(new Status(new Status.Property[0]));
        this.handlers = new HashMap<>();
        this.timerTask = Executors.newScheduledThreadPool(1);
        // 使用单线程执行分发任务，要求是任务不能导致单线程阻塞
        this.notifyQueue = Executors.newSingleThreadExecutor();
        this.timerIntervalMs = checkIntervalMs;
        this.statusProductor = productor;
    }

    // 如果handler的执行耗时总是大于status检测间隔，则有可能导致分发任务持续
    // 堆积导致溢出
    // 处理方式： 1. 调大状态检测间隔周期 2. 避免handler执行耗时过长
    @Override
    public void registerHandler(String key, BiConsumer<Event, Status.Property> handle) {
        if (!handlers.containsKey(key)) {
            handlers.put(key, new ArrayList<>());
        }
        this.handlers.get(key).add(handle);
    }

    @Override
    public void start() {
        // 在上一次任务执行完成后固定时间间隔执行下一个任务,避免任务积压
        // 要求任务不能阻塞
        this.timerTask.scheduleWithFixedDelay(() -> {
            // catch exception 避免定时任务被中止
            try {
                process();
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("an error occurred while performing periodic state checks", e);
            }
        }, 0, this.timerIntervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        this.timerTask.shutdown();
    }

    private void process() {
        Status oldStatus = this.status.get();
        // 涉及db io，可能会阻塞线程，需要配置db超时时间
        Status newStatus = statusProductor.product();
        if (this.status.compareAndSet(oldStatus, newStatus)) {
            Status.Difference diff = oldStatus.compare(newStatus);
            for (Status.Property p : diff.getAdd()) {
                notify(Event.ADD, p);
            }
            for (Status.Property p : diff.getUpdate()) {
                notify(Event.UPDATE, p);
            }
            for (Status.Property p : diff.getDelete()) {
                notify(Event.DELETE, p);
            }
        }
    }

    private void notify(StatusMonitor.Event event, Status.Property property) {
        List<BiConsumer<Event, Status.Property>> handlers = this.handlers.get(property.key);
        if (Objects.nonNull(handlers)) {
            for (BiConsumer<Event, Status.Property> handle : this.handlers.get(property.key)) {
                this.notifyQueue.execute(() -> handle.accept(event, property));
            }
        }
    }
}
