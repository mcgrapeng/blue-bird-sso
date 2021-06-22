package com.bird.sso.lock;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/12 11:09
 */
@Component
@Slf4j
public class RedisLockHelper {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 超时时间 5s
     */
    private static final int TIMEOUT = 5 * 1000;


    /**
     * 加锁
     *
     * @param targetId  targetId - 唯一标志
     * @param timeStamp 当前时间+超时时间 也就是时间戳
     * @return
     */
    public boolean lock(String targetId, long timeStamp) {
        if (stringRedisTemplate.opsForValue().setIfAbsent(targetId, String.valueOf(TIMEOUT + timeStamp))) {
            // 对应setnx命令，可以成功设置,也就是key不存在
            return Boolean.TRUE;
        }

        // 判断锁超时 - 防止原来的操作异常，没有运行解锁操作  防止死锁
        String currentLock = stringRedisTemplate.opsForValue().get(targetId);
        // 如果锁过期 currentLock不为空且小于当前时间
        if (!Strings.isNullOrEmpty(currentLock) && Long.parseLong(currentLock) < System.currentTimeMillis()) {
            // 获取上一个锁的时间value 对应getset，如果lock存在
            String preLock = stringRedisTemplate.opsForValue().getAndSet(targetId, String.valueOf(timeStamp));

            // 假设两个线程同时进来这里，因为key被占用了，而且锁过期了。获取的值currentLock=A(get取的旧的值肯定是一样的),两个线程的timeStamp都是B,key都是K.锁时间已经过期了。
            // 而这里面的getAndSet一次只会一个执行，也就是一个执行之后，上一个的timeStamp已经变成了B。只有一个线程获取的上一个值会是A，另一个线程拿到的值是B。
            if (!Strings.isNullOrEmpty(preLock) && preLock.equals(currentLock)) {
                // preLock不为空且preLock等于currentLock，也就是校验是不是上个对应的商品时间戳，也是防止并发
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }


    /**
     * 解锁
     *
     * @param target
     * @param timeStamp
     */
    public void unlock(String target, long timeStamp) {
        try {
            String currentValue = stringRedisTemplate.opsForValue().get(target);
            if (!Strings.isNullOrEmpty(currentValue) && currentValue.equals(String.valueOf(timeStamp))) {
                // 删除锁状态
                stringRedisTemplate.opsForValue().getOperations().delete(target);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
