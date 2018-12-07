package cn.v1.system.redis.aspect;

import cn.v1.framework.utils.StringUtils;
import cn.v1.system.redis.JedisWrapper;
import cn.v1.system.redis.aspect.annotation.RedisCache;
import cn.v1.system.redis.aspect.annotation.RedisEvict;
import cn.v1.system.redis.aspect.annotation.StorageType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.SafeEncoder;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wr
 * Date: 2018/4/11
 *
 * @usage 定义redis缓存切面
 */
@Aspect
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class RedisCacheAspect {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAspect.class);

    private static final String DEFAULT_CACHE_CATEGORY = "CACHE";

    private static final String CACHE_ENTITY_MAP_PREFIX = "CACHE_ENTITY_MAP:";

    private static final String CACHE_ENTITY_LIST_PREFIX = "CACHE_ENTITY_LIST:";

    private JedisPool jedisPool;

    public RedisCacheAspect (JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    private String createEntityKey(String category, Class<?> entityClass) {
        return StringUtils.defaultString(category, DEFAULT_CACHE_CATEGORY)
                + ":" + CACHE_ENTITY_MAP_PREFIX + entityClass.getSimpleName();
    }

    private String createListKey(String category, Class<?> entityClass) {
        return StringUtils.defaultString(category, DEFAULT_CACHE_CATEGORY)
                + ":" + CACHE_ENTITY_LIST_PREFIX + entityClass.getSimpleName();
    }

    /**
     * 从redis中获取指定实体的缓存数据
     * @param mapKey
     * @param field
     * @param clazz 指定类型
     * @return
     */
    private Object getEntityFromRedis(String mapKey, String field, Class clazz){
        Object result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] mapKeyByte = SafeEncoder.encode(mapKey);
            byte[] fieldBate = SafeEncoder.encode(field);
            //判断redis中是否有缓的值
            boolean hasCache = jedis.hexists(mapKeyByte, fieldBate);
            if (hasCache) {
                // 缓存命中
                logger.debug("缓存已命中，mapKey={} field={}", mapKey, field);

                byte[] bytes = jedis.hget(mapKeyByte, fieldBate);
                if (!JedisWrapper.checkNil(bytes)) {
                    result = JedisWrapper.toObject(bytes);
                }
                if (result == null || !clazz.equals(result.getClass())) {
                    // 缓存值为空或类型不匹配，删除缓存
                    jedis.hdel(mapKeyByte, fieldBate);
                    logger.info("entity cache class un match ({}->{}), delete cache mapKey={} field={}",
                            clazz.getSimpleName(), result.getClass().getSimpleName(),
                            mapKey, field);
                }
            }
        } catch (Exception e) {
            logger.warn("get entity cache fail mapKey=" + mapKey + ", field=" + field, e);
        } finally {
            JedisWrapper.returnResource(jedis);
        }
        return result;
    }

    /**
     * 缓存实体到redis
     * @param mapKey
     * @param field
     * @param entity
     * @param expireSconds
     */
    private void cacheEntityIntoRedis(String mapKey, String field, Object entity, int expireSconds) {
        if(entity != null) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                //保存redis
                jedis.hset(SafeEncoder.encode(mapKey), SafeEncoder.encode(field),
                        JedisWrapper.toBytes(entity));
                if (expireSconds > 0) {
                    jedis.expire(mapKey, expireSconds);
                }
            }  catch (Exception e) {
                logger.warn("put entity into cache fail mapKey=" + mapKey + ", field=" + field, e);
            } finally {
                JedisWrapper.returnResource(jedis);
            }
        }
    }

    /**
     * 从redis中获取指定实体列表的缓存数据
     * @param key
     * @param clazz
     * @return
     */
    private Object getEntityListFromRedis(String key, Class clazz){
        Object result = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] keyByte = SafeEncoder.encode(key);
            boolean hasCache = jedis.exists(keyByte);
            if (hasCache) {
                logger.debug("缓存已命中，key={}", key);
                byte[] bytes = jedis.get(keyByte);
                List values = (List) JedisWrapper.toObject(bytes);
                if (values == null || !clazz.equals(values.get(0).getClass())) {
                    // 缓存列表值为空或类型不匹配，删除缓存
                    jedis.del(keyByte);
                    logger.info("get list cache class un match({}->{}), delete cache key={}",
                            clazz.getSimpleName(),
                            values.get(0).getClass().getSimpleName(), key);
                } else {
                    result = values;
                }
            }
        } catch (Exception e) {
            logger.warn("get list cache fail key=" + key, e);
        } finally {
            JedisWrapper.returnResource(jedis);
        }
        return result;
    }

    /**
     * 缓存实体列表到redis
     * @param key
     * @param list
     * @param expireSconds
     */
    private void cacheEntityListIntoRedis(String key, Object list, int expireSconds){
        if (list != null) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                //保存redis
                byte[] keyByte = SafeEncoder.encode(key);
                jedis.set(keyByte, JedisWrapper.toBytes(list));
                if (expireSconds > 0) {
                    jedis.expire(keyByte, expireSconds);
                }
            }  catch (Exception e) {
                logger.warn("put entity list into cache fail key=" + key, e);
            } finally {
                JedisWrapper.returnResource(jedis);
            }
        }
    }

    /**
     * 定义环绕通知方法
     */
    @Around("@annotation(redisCache)")
    public Object getCache(ProceedingJoinPoint joinPoint, RedisCache redisCache) throws Throwable {
        //类名
        Class entityClass = redisCache.entityClass();
        //key 前缀
        String category = redisCache.category();
        //存储内型（ENTITY,LIST）
        StorageType type = redisCache.storage();
        //方法参数
        Method method = getMethod(joinPoint);
        //缓存失效时间
        int expire = redisCache.expire();
        // 得到类名、方法名和参数
        Object[] args = joinPoint.getArgs();

        Jedis jedis = null;

        Object result = null;

        if(entityClass == null){
            throw new NullPointerException("RedisCache entityClass is not specified!");
        }
        if(type == null){
            throw new NullPointerException("RedisCache storageType is not specified!");
        }

        switch (type) {
            case ENTITY: //实体对象缓存
                //存储实体集合KEY
                String mapKey = createEntityKey(category, entityClass);
                final String field = getField(redisCache.fieldKey(), method, args);
                if (StringUtils.isBlank(field)) {
                    throw new NullPointerException("parseKey does not assign a value!");
                }

                //从redis中获取缓存值
                result = getEntityFromRedis(mapKey, field, entityClass);

                if(result == null) {
                    // 调用数据库查询方法
                    result = joinPoint.proceed(args);
                    // 缓存到redis
                    cacheEntityIntoRedis(mapKey, field, result, expire);

                }
                break;
            case LIST: //列表数据缓存
                //存储列表数据的KEY
                String listKey = createListKey(category, entityClass);
                //从redis中获取缓存值
                result = getEntityListFromRedis(listKey, entityClass);

                if (result == null) {
                    // 调用数据库查询方法
                    result = joinPoint.proceed(args);

                    // 缓存到redis
                    cacheEntityListIntoRedis(listKey, result, expire);
                }
                break;
        }
        return result;
    }

    @Around("@annotation(redisEvict)")
    public void redisEvict(ProceedingJoinPoint joinPoint, RedisEvict redisEvict) throws Throwable {
        //类名
        Class entityClass = redisEvict.entityClass();
        //key 前缀
        String category = redisEvict.category();
        //方法参数
        Method method = getMethod(joinPoint);
        // 得到类名、方法名和参数
        Object[] args = joinPoint.getArgs();

        if(entityClass == null){
            throw new NullPointerException("RedisEvict entityClass is not specified!");
        }

        //继续调用后续操作
        joinPoint.proceed(args);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //ENTITY key
            String mapKey = createEntityKey(category, entityClass);
            String fieldKey = redisEvict.fieldKey();
            if (StringUtils.isNotBlank(fieldKey)) {
                final String filed = getField(fieldKey, method, args);
                if (StringUtils.isNotBlank(filed)) {
                    jedis.hdel(SafeEncoder.encode(mapKey), SafeEncoder.encode(filed));
                    logger.debug("delete entity cache mapKey={}, hkey={}", filed, filed);
                }
            }
            //同时删除缓存的list数据
            String listKey = createListKey(category, entityClass);
            jedis.del(SafeEncoder.encode(listKey));
            logger.debug("delete list cache key={}", listKey);
        } catch (Exception e) {
            logger.warn("delete entity cache fail", e);
        } finally {
            JedisWrapper.returnResource(jedis);
        }
    }

    /**
     * 获取被拦截方法对象
     * <p>
     * MethodSignature.getMethod() 获取的是顶层接口或者父类的方法对象
     * 而缓存的注解在实现类的方法上
     * 所以应该使用反射获取当前对象的方法对象
     */
    public Method getMethod(ProceedingJoinPoint joinPoint) {
        Method method = null;
        try {
            //拦截的实体类
            Object target = joinPoint.getTarget();
            //拦截的方法名称
            String methodName = joinPoint.getSignature().getName();
            //拦截的放参数类型
            Class[] parameterTypes = ((MethodSignature)joinPoint.getSignature()).getMethod().getParameterTypes();
            method = target.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return method;
    }

    /**
     * 获取缓存的key
     * fieldKey 定义在注解上，支持SPEL表达式
     *
     * @return
     */
    private String getField(String fieldKey, Method method, Object[] args) {
        //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);
        // 使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        //SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        //把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(fieldKey).getValue(context, String.class);
    }
}
