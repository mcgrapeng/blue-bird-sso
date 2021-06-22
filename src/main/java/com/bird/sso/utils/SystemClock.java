package com.bird.sso.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 *
 * copy上面的ID生成算法你会发现有个报错找不到SystemClock.now(), 因为它并不是JDK自带的类, 使用这个工具类生成时间戳的原因是经过实测发现 Linux环境中高并发下System.currentTimeMillis()这个API对比Windows环境有近百倍的性能差距;
   最简单直接的是起一个线程定时维护一个毫秒时间戳以覆盖JDK的System.currentTimeMillis(), 虽然这样固然会造成一定的时间精度问题, 但我们的ID生成算法是秒级的Unix时间戳, 也不在乎这几十微秒的误差, 换来的却是百倍的性能提升, 这是完全值得的支出;
 *
 * @date 2020/7/22 10:53
 */
public final class SystemClock {
    private final long period;
    private final AtomicLong now;

    private SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    /**
     * 尝试下枚举单例法
     */
    private enum SystemClockEnum {
        SYSTEM_CLOCK;
        private SystemClock systemClock;
        SystemClockEnum() {
            systemClock = new SystemClock(1);
        }
        public SystemClock getInstance() {
            return systemClock;
        }
    }

    /**
     * 获取单例对象
     * @return com.cmallshop.module.core.commons.util.sequence.SystemClock
     */
    private static SystemClock getInstance() {
        return SystemClockEnum.SYSTEM_CLOCK.getInstance();
    }

    /**
     * 获取当前毫秒时间戳
     * @return long
     */
    public static long now() {
        return getInstance().now.get();
    }

    /**
     * 起一个线程定时刷新时间戳
     */
    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }
}
