package com.mmall.task;


import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;


    /**
     * 不希望分布式集群下所有服务器都执行此函数，浪费资源、造成数据错乱
     * 用分布式锁来解决，一个服务器来执行即可
     */
    //@Scheduled(cron = "0 */1 * * * ?")  // 每一分钟
    public void closeOrderTaskV1(){
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

//    @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis()) + lockTimeout);
        if (setnxResult != null && setnxResult.intValue() == 1) {
            // 如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }


//    @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis()) + lockTimeout);
        if (setnxResult != null && setnxResult.intValue() == 1) {
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

            // 锁的时间已过期
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                // getset具有原子性
                String getsetResult = RedisShardedPoolUtil.getset(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                        String.valueOf(System.currentTimeMillis()) + lockTimeout);
                // 再次用当前时间戳getset
                // 返回给定的旧值，对旧值判断，看是否可以获取锁，当key没有旧值，即key不存在时，返回nil -> 获取锁
                // 这里set了一个新的value值，获取旧的值
                if (getsetResult == null || (getsetResult != null && StringUtils.equals(lockValueStr, getsetResult))) {
                    // 真正获取到锁
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取到分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                log.info("没有获取到分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }

    @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟
    public void closeOrderTaskV4() {
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            // 等待获取锁时间1秒，过期时间5秒
            // if里面的代码很短，执行时间远不到1秒，可能发生两个进程都拿到锁的情况
//            if (getLock = lock.tryLock(1, 5, TimeUnit.SECONDS)) {
            // 所以最好是0
            if (getLock = lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                log.info("Redisson获取分布式锁:{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                        Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                iOrderService.closeOrder(hour);
            } else {
                log.info("Redisson没有获取到分布式锁:{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson获取分布式锁出错", e);
        } finally {
            if (!getLock) {
                return;
            }
            lock.unlock();
            log.info("Redisson分布式锁释放");
        }
    }

    private void closeOrder(String lockName) {
        RedisShardedPoolUtil.expire(lockName, 50);
        log.info("获取{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        // iOrderService.closeOrder(hour);
        // 及时释放锁
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("=========================");
    }
}
