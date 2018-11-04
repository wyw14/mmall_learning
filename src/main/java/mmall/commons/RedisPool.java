package mmall.commons;

import mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool;
    private static Integer maxTotal= Integer.valueOf(PropertiesUtil.getProperty("redis.maxTotal","20"));
    private static Integer maxIdle= Integer.valueOf(PropertiesUtil.getProperty("redis.maxIdle","10"));
    private static Integer minIdle= Integer.valueOf(PropertiesUtil.getProperty("redis.minIdle","2"));
    private static Boolean testOnBorrow= Boolean.valueOf(PropertiesUtil.getProperty("redis.testOnBorrow","true"));
    private static Boolean testOnReturn= Boolean.valueOf(PropertiesUtil.getProperty("redis.testOnReturn","true"));
    private static String redisHost=PropertiesUtil.getProperty("redis.Host");
    private static String redisPort=PropertiesUtil.getProperty("redis.Port");

    private static void init(){
        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        pool=new JedisPool(jedisPoolConfig,PropertiesUtil.getProperty("redis.Host"),6379,1000*2);
    }
    static {
        init();
    }
    public static Jedis getJedis(){
        return pool.getResource();
    }
    public static void returnJedis(Jedis jedis){
        pool.returnResource(jedis);
    }
    public static void returnBrokeJedis(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }


}
