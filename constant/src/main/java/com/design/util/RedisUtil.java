package com.design.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author huangming
 * @Date 2021/4/16
 **/
public class RedisUtil {
    private Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    private static JedisPool pool;

    private static final String RETURN_OK = "OK";

    private static final Long REDIS_RECONNECT_TIME_SLOT = 5000L;

    private static final Long REDIS_EXPIRE_DELETE_TIME = 5L;

    static {
        InputStream is = RedisUtil.class.getClassLoader().getResourceAsStream("redis.properties");
        Properties properties = new Properties();

        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JedisPoolConfig config = new JedisPoolConfig();
        pool = new JedisPool(config, (String) properties.get("host"),Integer.parseInt(properties.get("port").toString()));
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }

    /**
     * 封装redis的hmset
     *
     * @param key  key
     * @param hash value-map
     * @return true 设置成功, false 设置失败
     */
    public boolean hmset(String key, Map<String, String> hash) {


        try (Jedis jedis = pool.getResource()) {
            if (RETURN_OK.equals(jedis.hmset(key, hash))) {
                return true;
            }
        } catch (Exception e) {
            logger.error("Redis hmset failed for {}.", e.getMessage());
        }

        return false;
    }

    /**
     * 封装redis的hmget
     *
     * @param key    key
     * @param fields hash-key
     * @return 按fields顺序返回的value列表
     */
    public List<String> hmget(String key, String... fields) {
        List<String> values = new ArrayList<>();
        try (Jedis jedis = pool.getResource()) {
            values = jedis.hmget(key, fields);
        } catch (Exception e) {
            logger.warn("Redis hmget failed for {}.", e.getMessage());
        }
        return values;
    }

    /**
     * 用redis新版set封装的setNx功能 当key存在的时候,不允许再次设置
     *
     * @param key      key
     * @param value    value
     * @param time     超时时间
     * @param timeUnit 超时时间的单位
     * @return true 设置成功, false 设置失败
     */
    public boolean setNx(String key, String value, long time, TimeUnit timeUnit) {
        String redisTimeUnit = "EX";
        if (timeUnit.equals(TimeUnit.MILLISECONDS)) {
            redisTimeUnit = "PX";
        }

        try (Jedis jedis = pool.getResource()) {
            String response = jedis.set(key, value, "NX", redisTimeUnit, time);
            if (response == null) {
                return false;
            } else if (RETURN_OK.equals(response)) {
                return true;
            }
        } catch (Exception e) {
            logger.error("Redis setNx failed for {}.", e.getMessage());
        }
        return false;
    }

    /**
     * 设置key setex
     *
     * @param key    key
     * @param value  值
     * @param expire 超时时间 second
     */
    public void set(final String key, final String value, final int expire) {

        try (Jedis jedis = pool.getResource()) {
            jedis.setex(key, expire, value);
        } catch (Exception e) {
            logger.error("Redis set failed for {}.", e.getMessage());
        }
    }

    public String get(final String key) {
        String result = null;
        try (Jedis jedis = pool.getResource()) {
            result = jedis.get(key);
        } catch (Exception e) {
            logger.error("Redis get failed for {}.", e.getMessage());
        }
        return result;
    }

    /**
     * 设置key超时时间
     * @param key
     * @param time 秒
     * @return
     */
    public long expire(final String key, int time){
        long ret = 0;
        try (Jedis jedis = pool.getResource()) {
            ret = jedis.expire(key, time);
        } catch (Exception e) {
            logger.error("Redis expire failed for {}.", e.getMessage());
        }
        return ret;
    }

    /**
     * expireDelete 用1ms超时的机制来删除key 如果key不存在，不会设置成功，也不会抛异常，直接返回
     *
     * @param key key
     */
    public void expireDelete(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.pexpire(key, REDIS_EXPIRE_DELETE_TIME);
        } catch (Exception e) {
            logger.error("Redis expireDelete failed for {}.", e.getMessage());
        }
    }

    public void directDel(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            logger.error("Redis expireDelete failed for {}.", e.getMessage());
        }
    }

    /**
     * 向redis订阅事件
     *
     * @param jedisPubSub 事件触发的回调
     * @param channels    订阅的监听事件列表
     */
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        while (true) {
            try (Jedis jedis = pool.getResource()) {
                // 这里调用会阻塞住,除非碰到异常,如redis无法连接
                jedis.subscribe(jedisPubSub, channels);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Redis subscribe interrupted for {}.", e.getMessage());
                }

                try {
                    Thread.sleep(REDIS_RECONNECT_TIME_SLOT);
                } catch (InterruptedException e1) {
                    logger.error("Redis subscribe reconnect sleeping is interrupted.");
                }
            }
        }
    }

    /**
     * 添加到set
     *
     * @param key     key
     * @param members 待添加的成员
     * @return true 操作成功
     */
    public boolean sadd(String key, String... members) {
        boolean result = false;
        try (Jedis jedis = pool.getResource()) {
            Long code = jedis.sadd(key, members);

            if (code == 1) {
                result = true;
            }
        } catch (Exception e) {
            logger.error("Redis sadd failed for {}.", e.getMessage());
        }
        return result;
    }

    /**
     * set中是否含有member
     *
     * @param key    key
     * @param member 待检查的成员
     * @return true 包含该成员
     */
    public boolean sismember(String key, String member) {
        boolean result = false;
        try (Jedis jedis = pool.getResource()) {
            result = jedis.sismember(key, member);
        } catch (Exception e) {
            logger.error("Redis sadd failed for {}.", e.getMessage());
        }
        return result;
    }

    /**
     * 获取集合成员
     *
     * @param key key
     * @return 集合的成员Set
     */
    public Set<String> smembers(String key) {
        Set<String> result = new HashSet<>(0);
        try (Jedis jedis = pool.getResource()) {
            result = jedis.smembers(key);
        } catch (Exception e) {
            logger.error("Redis sadd failed for {}.", e.getMessage());
        }
        return result;
    }

    public Transaction getTransection() {
        Transaction transaction = null;
        try (Jedis jedis = pool.getResource()) {
            transaction = jedis.multi();
        } catch (Exception e) {
            logger.error("Redis get transaction failed for {}.", e.getMessage());
        }
        return transaction;
    }

    /**
     * 加到双端队列，从队尾加入，FIFO模式
     *
     * @param key     key
     * @param members 待加入的成员
     */
    public void rpush(String key, String... members) {
        try (Jedis jedis = pool.getResource()) {
            jedis.rpush(key, members);
        } catch (Exception e) {
            logger.error("Redis get transaction failed for {}.", e.getMessage());
        }
    }

    /**
     * 获取队列中所有的value，用lrange模式
     *
     * @param key key
     * @return 列表
     */
    public List<String> getALlListMember(String key) {
        List<String> result = new ArrayList<>();
        try (Jedis jedis = pool.getResource()) {
            result = jedis.lrange(key, 0, -1);
        } catch (Exception e) {
            logger.error("Redis get getALlListMember failed for {}.", e.getMessage());
        }
        return result;
    }

    /**
     * 返回加1后的值
     * @param key
     * @return
     */
    public long incr(final String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.incr(key);
        } catch (Exception e) {
            logger.error("Redis get incr failed for {}.", e.getMessage());
        }
        return 0;
    }

    /**
     * 设置在固定时刻超时
     * @param key
     * @return
     */
    public long expireAt(final String key, final long time) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.expireAt(key, time);
        } catch (Exception e) {
            logger.error("Redis get incr failed for {}.", e.getMessage());
        }
        return 0;
    }

    /**
     * key的有效期余额
     * @param key
     * @return
     * [-3: 异常]
     * [-2: 不存在Key]
     * [-1: key未设置expire]
     * [>=0:剩余的有效时间]
     */
    public long pttl(final String key){
        try (Jedis jedis = pool.getResource()) {
            return jedis.pttl(key);
        } catch (Exception e) {
            logger.error("Redis pttl failed for {}.", e.getMessage());
        }
        return -3;
    }

    /**
     * redis执行脚本
     * @param script
     * @param keys
     * @param values
     * @return
     */
    public Object eval(String script, List<String> keys, List<String> values){
        try (Jedis jedis = pool.getResource()) {
            return jedis.eval(script, keys, values);
        } catch (Exception e) {
            logger.error("Redis eval failed for {}.", e.getMessage());
        }
        return null;
    }
}
