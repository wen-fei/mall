package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {
    // sharded jedis连接池
    private static ShardedJedisPool pool;
    // 最大连接池
    private static Integer maxTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.max.total", "20"));
    // 最大空闲数
    private static Integer maxIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.max.idle", "10"));
    // 最小空闲数
    private static Integer minIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.min.idle", "2"));
    // 在borrow一个jedis实例的时候，师傅要进行验证操作。如果为true，则得到的jedis实例一定是可用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    // 在return一个jedis实例的时候，师傅要进行验证操作。如果为true，则放回jedis连接池的实例一定是可用的
    private static Boolean testOnRetuen = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));


    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip", "127.0.0.1");
    private static Integer redis1Port = Integer.valueOf(PropertiesUtil.getProperty("redis1.port", "6379"));

    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip", "127.0.0.1");
    private static Integer redis2Port = Integer.valueOf(PropertiesUtil.getProperty("redis2.port", "6379"));

    private static void initPool() {
        JedisPoolConfig conf = new JedisPoolConfig();
        conf.setMaxTotal(maxTotal);
        conf.setMaxIdle(maxIdle);
        conf.setMinIdle(minIdle);

        conf.setTestOnBorrow(testOnBorrow);
        conf.setTestOnReturn(testOnRetuen);

        // 连接耗尽的时候是否阻塞，false会抛出异常，true阻塞知道超时，默认为true
        conf.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 1000*2);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000*2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(conf, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    // 虚拟机启动的时候初始化
    static {
        initPool();
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    // 失效jedis放回连接池
    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    // jedis放回连接池
    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();

        for(int i = 0; i<10; i++) {
            jedis.set("key" + i, "value" + i);
        }

        returnBrokenResource(jedis);
        // pool.destroy();  // 临时调用，销毁连接池所有连接
        System.out.println("program end");
    }
}
