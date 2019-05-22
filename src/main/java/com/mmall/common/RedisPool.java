package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    // jedis连接池
    private static JedisPool pool;
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


    private static String redisIp = PropertiesUtil.getProperty("redis.ip", "127.0.0.1");
    private static Integer redisPort = Integer.valueOf(PropertiesUtil.getProperty("redis.port", "6379"));

    private static void initPool() {
        JedisPoolConfig conf = new JedisPoolConfig();
        conf.setMaxTotal(maxTotal);
        conf.setMaxIdle(maxIdle);
        conf.setMinIdle(minIdle);

        conf.setTestOnBorrow(testOnBorrow);
        conf.setTestOnReturn(testOnRetuen);

        // 连接耗尽的时候是否阻塞，false会抛出异常，true阻塞知道超时，默认为true
        conf.setBlockWhenExhausted(true);

        pool = new JedisPool(conf, redisIp, redisPort, 1000 * 2);
    }

    // 虚拟机启动的时候初始化
    static {
        initPool();
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }

    // 失效jedis放回连接池
    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    // jedis放回连接池
    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("cq", "shabi");
        returnBrokenResource(jedis);
        pool.destroy();  // 临时调用，销毁连接池所有连接
        System.out.println("program end");
    }
}
