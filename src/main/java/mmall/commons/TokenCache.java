package mmall.commons;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
@Slf4j
public class TokenCache {

    public static final String TOKEN_PREFIX="token_";
    //private static Logger logger= LoggerFactory.getLogger(TokenCache.class);
    private static LoadingCache<String,String>localCache= CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
    .build(new CacheLoader<String, String>() {
        @Override
        public String load(String s) throws Exception {
            return "null";
        }
    });
    public static void setkey(String key,String value){
        localCache.put(key,value);
    }
    public static String getKey(String key){
        String value=null;
        try {
            value=localCache.get(key);
            if ("null".equals(value)){
                return null;
            }
            return value;
        } catch (Exception e) {
            log.error("localCache get error",e);
        }
        return null;
    }
}
