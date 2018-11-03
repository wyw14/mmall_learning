package mmall.util;

import lombok.extern.slf4j.Slf4j;
import mmall.commons.RedisPool;
import redis.clients.jedis.Jedis;

@Slf4j
public class RedisPoolUtils {
    
    public static String set(String key,String value){
        String result=null;
        Jedis jedis=RedisPool.getJedis();
        try {
            jedis=RedisPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            RedisPool.returnBrokeJedis(jedis);
            log.error("key:{},value:{}errormsg",key,value,e);
            return result;
        }
        RedisPool.returnJedis(jedis);
        return result;
    }

    public static String get(String key){
        String result=null;
        Jedis jedis=RedisPool.getJedis();
        try {
            result = jedis.get(key);
        } catch (Exception e) {
            RedisPool.returnBrokeJedis(jedis);
            log.error("key:{},errormsg",key,e);
            return result;
        }
        RedisPool.returnJedis(jedis);
        return result;
    }
    public static String setex(String key,String value,int extime){
        String result=null;
        Jedis jedis=null;
        try {
            jedis=RedisPool.getJedis();
            result = jedis.setex(key,extime,value);
        } catch (Exception e) {
            RedisPool.returnBrokeJedis(jedis);
            log.error("key:{},extimr:{},value:{},error",key,extime,value);
            return result;
        }
        RedisPool.returnJedis(jedis);
        return result;
    }
    public static Long expireExTime(String key,Integer time){
        Long result=null;
        Jedis jedis=null;
        try {
            jedis=RedisPool.getJedis();
            result = jedis.expire(key,time);
        } catch (Exception e) {
            RedisPool.returnBrokeJedis(jedis);
            log.error("key:{},extimr:{},value:{},error",key,time);
            return result;
        }
        RedisPool.returnJedis(jedis);
        return result;
    }
    public static Long del(String key){
        Long result=null;
        Jedis jedis=null;
        try {
            jedis=RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            RedisPool.returnBrokeJedis(jedis);
            log.error("key:{},error",key);
            return result;
        }
        RedisPool.returnJedis(jedis);
        return result;
    }

    public static void main(String[] args) {
        RedisPoolUtils.set("test","test");
        RedisPoolUtils.get("test");
        RedisPoolUtils.setex("new","newone",60*10);
        RedisPoolUtils.expireExTime("test",60*10);
        RedisPoolUtils.del("test");
        System.out.println("end");
    }


}
