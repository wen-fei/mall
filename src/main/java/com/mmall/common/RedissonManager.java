package com.mmall.common;


import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class RedissonManager {

    private Config config = new Config();

    private Redisson redisson = null;

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip", "127.0.0.1");
    private static Integer redis1Port = Integer.valueOf(PropertiesUtil.getProperty("redis1.port", "6379"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip", "127.0.0.1");
    private static Integer redis2Port = Integer.valueOf(PropertiesUtil.getProperty("redis2.port", "6379"));

    @PostConstruct
    private void init() {
        try {
            config.useSingleServer().setAddress(redis1Ip + ":" + redis1Port);
            redisson = (Redisson) Redisson.create(config);
            log.info("初始化Redisson结束");
        } catch (Exception e) {
            log.error("redisson init error: ", e);
        }
    }

    public Redisson getRedisson() {
        return redisson;
    }
}
