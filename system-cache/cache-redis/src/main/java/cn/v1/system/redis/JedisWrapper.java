/**
 * Create by gen
 */
package cn.v1.system.redis;

import cn.v1.framework.utils.ExceptionUtils;
import cn.v1.framework.utils.ObjectUtils;
import cn.v1.framework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.exceptions.JedisNoScriptException;
import redis.clients.util.SafeEncoder;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Jedis Cache 工具类<br/>
 * <br/>
 * 方法命名规则：<br/>
 * <br/>
 * 由于 动词 set 和 名词 set 会造成歧义，所以指定方法命名规则，避免歧义。<br/>
 * 方法由 op[Type][PrepContainer] 组成<br/>
 * <br/>
 * op: 操作， get, set, add, put, remove 等，必选<br/>
 * type: 类型，如果参数表已有类型，可以重载，省略；两种情况不能省略：get 一类的操作需要区分返回类型；容器类型参数是泛型，类型被抹除.<br/>
 * prep: 介词，in, into, of, from 等，如果 目标容器 不为空，必选。（注：setStringList 里，StringList 是类型，不是容器）<br/>
 * container: 容器，对容器的操作必选<br/>
 * <br/>
 * @author ThinkGem
 * @version 2014-6-29
 */
public class JedisWrapper {

    private static final boolean DATA_TYPE_ERROR_CLEAN = true;

    private static final String NIL = "nil";
    private static final String OK = "OK";

    private static Logger logger = LoggerFactory.getLogger(JedisWrapper.class);

    private static volatile JedisWrapper sInstance = null;

    private JedisPool jedisPool;

    private String keyPrefix;

    public JedisWrapper(JedisPool jedisPool) {
        if(jedisPool != null) {
            this.jedisPool = jedisPool;
            this.keyPrefix = "";
            if(sInstance == null) {
                sInstance = this;
                logger.info("init JedisWrapper with jedisPool = {}", jedisPool);
            }
        }
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /**
     * 向List缓存中添加值
     * @param key 键
     * @param cacheSeconds 超时时间，0为不超时
     * @param values 值
     * @return
     */
    public static long addIntoList(String key, int cacheSeconds,
                                   Object... values) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            byte[][] many = new byte[values.length][];
            for (int i = 0; i < values.length; i++) {
                many[i] = toBytes(values[i]);
            }
            jedis = getResource();
            result = jedis.rpush(SafeEncoder.encode(key), many);
            if (result > 0 && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("listObjectAdd {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("listObjectAdd {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 向List缓存中添加值
     * @param key 键
     * @param values 值
     * @return
     */
    public static long addIntoList(String key, int cacheSeconds,
                                   String... values) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.rpush(key, values);
            if (result > 0 && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("listAdd {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("listAdd {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 向Set缓存中添加值
     * @param key 键
     * @param values 值
     * @return
     */
    public static long addIntoSet(String key, int cacheSeconds,
                                  Object... values) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            byte[][] many = new byte[values.length][];
            for (int i = 0; i < values.length; i++) {
                many[i] = toBytes(values[i]);
            }
            jedis = getResource();
            result = jedis.sadd(SafeEncoder.encode(key), many);
            if (result > 0 && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("setSetObjectAdd {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("setSetObjectAdd {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 向Set缓存中添加值
     * @param key 键
     * @param values 值
     * @return
     */
    public static long addIntoSet(String key, int cacheSeconds,
                                  String... values) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.sadd(key, values);
            if (result > 0 && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("setSetAdd {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("setSetAdd {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    public static long bitCount(String key) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.bitcount(key);
        } catch (Exception e) {
            logger.warn("bitCount {}, {}\n{}", key, e.getMessage(),
                    ExceptionUtils.getStackTraceAsString(e));
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    public static long bitCount(String key, int start, int end) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.bitcount(key, start, end);
        } catch (Exception e) {
            logger.warn("bitCount {} [{}, {}), {}\n{}", key, start, end,
                    e.getMessage(), ExceptionUtils.getStackTraceAsString(e));
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    public static boolean checkNil(byte[] reply) {
        try {
            return checkNil(SafeEncoder.encode(reply));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkNil(String reply) {
        return NIL.equalsIgnoreCase(reply);
    }

    public static boolean checkOK(String reply) {
        if (OK.equalsIgnoreCase(reply)) {
            return true;
        } else {
            // new 一个 Exception 来输出调用栈
            logger.warn("jedis reply error: " + reply, new Exception(
                    "[checkOK] fail. Please check stack trace if frequent."));
            return false;
        }
    }

    /**
     * Atomically sets the value to the given updated value
     *
     * @param key 键
     * @param expect 期望旧值
     * @param update 更新值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static boolean compareAndSet(String key, Object expect,
                                        Object update, int cacheSeconds) {
        key = getPrefixedKey(key);
        Jedis jedis = null;
        boolean match = false;
        try {
            byte[] keyBytes = SafeEncoder.encode(key);
            jedis = getResource();
            jedis.watch(key);
            Object current = toObject(jedis.get(keyBytes));

            match = Objects.equals(current, expect);

            if (logger.isDebugEnabled()) {
                logger.debug("compareAndSet {} = {}, expect match ? {}", key,
                        update, match);
            }

            if (!match) {
                jedis.unwatch();
                return false;
            }
            Transaction trans = jedis.multi();
            Response<String> response = trans.set(keyBytes, toBytes(update));
            trans.exec();

            String result = null;
            try {
                result = response.get();
            } catch (JedisDataException e) {
                // response.set() 没有执行，说明 exec 被 watch 拦截，没有返回结果
                return false;
            }

            if (!checkOK(result)) {
                return false;
            }
            if (cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            return true;
        } catch (Exception e) {
            logger.warn("compareAndSet {} = {}, expect match ? {}", key, update,
                    match);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return false;
    }

    /**
     * Atomically sets the value to the given updated value
     *
     * @param key 键
     * @param expect 期望旧值
     * @param update 更新值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static boolean compareAndSet(String key, String expect,
                                        String update, int cacheSeconds) {
        key = getPrefixedKey(key);
        Jedis jedis = null;
        String current = null;
        try {
            jedis = getResource();
            jedis.watch(key);
            current = jedis.get(key);

            if (logger.isDebugEnabled()) {
                logger.debug("compareAndSet {} = {}, expect = {}, current = {}",
                        key, update, expect, current);
            }

            if (!StringUtils.equals(current, expect)) {
                jedis.unwatch();
                return false;
            }
            Transaction trans = jedis.multi();
            Response<String> response = trans.set(key, update);
            trans.exec();

            String result = null;
            try {
                result = response.get();
            } catch (JedisDataException e) {
                // response.set() 没有执行，说明 exec 被 watch 拦截，没有返回结果
                return false;
            }

            if (!checkOK(result)) {
                return false;
            }
            if (cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            return true;
        } catch (Exception e) {
            logger.warn("compareAndSet {} = {}, expect = {}, current = {}", key,
                    update, expect, current);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return false;
    }

    /**
     * 递减计数
     * @param key 键
     * @return
     */
    public static long decr(String key, int cacheSeconds) {
        key = getPrefixedKey(key);
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.decr(key);
            if (cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("decr {}", key);
        } catch (Exception e) {
            logger.warn("decr {}", key);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 递减计数
     * @param key 键
     * @param decrement 增加的值
     * @return
     */
    public static long decrBy(String key, long decrement, int cacheSeconds) {
        key = getPrefixedKey(key);
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.decrBy(key, decrement);
            if (cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("decrBy {}, {}", key, decrement);
        } catch (Exception e) {
            logger.warn("decrBy {}, {}", key, decrement);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 删除缓存
     * @param key 键
     * @return
     */
    public static long del(String key) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.del(key);
            if (result > 0L) {
                logger.debug("del {}", key);
            } else {
                logger.debug("del {} not exists", key);
            }
        } catch (Exception e) {
            logger.warn("del {}", key);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * @param script 要执行的 lua 脚本
     * @param params key1, key2... arg1, arg2... key 和 arg 务必一一对应
     * @return
     */
    public static Object eval(String script, int keyCount, String... params) {
        return eval(script, null, keyCount, params);
    }

    /**
     * @param script 要执行的 lua 脚本
     * @param scriptSha1 脚本的 sha1 值，允许为 null; 为空 或者 该sha1 值未缓存 时执行 script 的内容
     * @param params key1, key2... arg1, arg2... key 和 arg 务必一一对应
     * @return
     */
    public static Object eval(String script, String scriptSha1, int keyCount,
                              String... params) {
        Jedis jedis = null;
        try {
            for (int i = 0; i < keyCount; i++) {
                params[i] = getPrefixedKey(params[i]);
            }
            jedis = getResource();
            if (StringUtils.isNotBlank(scriptSha1)) {
                try {
                    // 优先使用 sha1 执行
                    return jedis.evalsha(scriptSha1, keyCount, params);
                } catch (JedisNoScriptException e) {
                    logger.warn(e.getMessage(), scriptSha1, script);
                }
            }
            // sha1 为空或者 NoScript，再用脚本执行
            return jedis.eval(script, keyCount, params);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            returnResource(jedis);
        }
        return null;
    }

    /**
     * 缓存是否存在
     * @param key 键
     * @return
     */
    public static boolean exists(String key) {
        key = getPrefixedKey(key);
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.exists(key);
            logger.debug("exists {}", key);
        } catch (Exception e) {
            logger.warn("exists {}", key);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 判断Map缓存中的Key是否存在
     * @param key 键
     * @param mapKey 值
     * @return
     */
    public static boolean existsInMap(String key, String mapKey) {
        key = getPrefixedKey(key);
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.hexists(key, mapKey);
            logger.debug("mapExists {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapExists {}  {}", key, mapKey);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 设置缓存时长
     * @param key 键
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static long expire(String key, int cacheSeconds) {
        if (cacheSeconds <= 0) {
            return 0L;
        }

        key = getPrefixedKey(key);
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.expire(key, cacheSeconds);
            logger.debug("expireObject {} = {}", key, cacheSeconds);
        } catch (Exception e) {
            logger.warn("expireObject {} = {}", key, cacheSeconds);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 在一个原子操作内返回旧值并设置新值，新旧值类型必须一致
     * @param key
     * @param value
     * @param cacheSeconds
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAndSet(String key, T value, int cacheSeconds) {
        key = getPrefixedKey(key);
        T result = null;
        boolean success = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            if (value instanceof String) {
                String str = jedis.getSet(key, value.toString());
                success = !checkNil(str);
                result = (T) str;
            } else {
                Object o = toObject(
                        jedis.getSet(SafeEncoder.encode(key), toBytes(value)));
                if (o != null) {
                    result = (T) o;
                    success = true;
                }
            }
            if (success && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("set {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("set {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 获取集合 key中元素的数量
     * @param key 键
     * @return 值
     */
    public static long getCardinalityOfSet(String key) {
        key = getPrefixedKey(key);
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.scard(key);
            logger.debug("getSetCard {} = {}", key, result);
        } catch (Exception e) {
            logger.warn("getSetCard {} = {}", key, result);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 获取缓存<br/>
     * 指定返回类型（注：类型转换错误则返回null）<br/>
     * Redis 对 String 有自己的编码方式。<br/>
     * 获取 String 类型请使用 {@link #getString(String)} <br/>
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromRedis(String key) {
        try {
            Object obj = getObject(key);
            if (obj != null) {
                return (T) obj;
            }
        } catch (Exception e) {
            logger.warn("获取redis缓存对象失败：" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public static Object getObject(String key) {
        key = getPrefixedKey(key);
        Object value = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            if (DATA_TYPE_ERROR_CLEAN)
                jedis.watch(key); // watch 避免后面误删别处已更新的内容

            byte[] bytes = jedis.get(SafeEncoder.encode(key));
            if (checkNil(bytes)) {
                return null;
            }

            value = toObject(bytes);
            if (value != null) {
                logger.debug("getObject {} = {}", key, value);
                return value;
            }

            if (DATA_TYPE_ERROR_CLEAN && jedis.ttl(key) == -1) {
                // key 存在但取得 null 说明反序列化失败，没有 TTL 说明不是 锁值，不会自己超时，清理避免反复报错
                Transaction trans = jedis.multi(); // 放在事务里配置 watch 避免删除其他地方更新的值
                Response<Long> response = trans.del(key);
                trans.exec();

                try {
                    long result = response.get();
                    logger.warn(
                            "getObject from persist key {} fail, del success = {}!",
                            key, result);
                } catch (JedisDataException e) {
                    // response.set() 没有执行，说明 exec 被 watch 拦截，没有返回结果
                }
            }
        } catch (Exception e) {
            logger.warn("getObject {} = {}", key, value);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            try {
                if (DATA_TYPE_ERROR_CLEAN && jedis != null)
                    jedis.unwatch();
            } catch (Exception e) {
                // nothing
            }
            returnResource(jedis);
        }
        return null;
    }

    /**
     * 获取缓存map中指定元素的值
     * @param key
     * @param mapKey
     * @return
     * @author zzm
     */
    public static Object getObjectFromMap(String key, String mapKey) {
        key = getPrefixedKey(key);
        Object value = null;
        Jedis jedis = null;
        try {
            byte[] keyBytes = SafeEncoder.encode(key);
            jedis = getResource();
            byte[] bytes = jedis.hget(keyBytes, SafeEncoder.encode(mapKey));
            if (checkNil(bytes)) {
                return null;
            }
            value = toObject(bytes);
        } catch (Exception e) {
            logger.warn("getObjectMapValues {} = {}", key, mapKey);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 获取List缓存<br/>
     * ！O(N) 时间复杂度，慎用。<br/>
     * 如果必须使用，请尽量控制每个 key 下的数据量，必要时分多个 key 存储。
     * @param key 键
     * @return 值
     */
    public static <T> List<T> getObjectList(String key) {
        return getObjectList(key, 0, -1);
    }

    /**
     * 获取List缓存<br/>
     * 时间复杂度:O(S+N)， S 为偏移量 start ， N 为指定区间内元素的数量。慎用。<br/>
     * 下标以 0 表示列表的第一个元素，以 -1 表示列表的最后一个元素<br/>
     * 超出范围的下标值不会引起错误。
     * 如果 start 下标比列表的最大下标 end还要大，那么 LRANGE 返回一个空列表。
     * 如果 stop 下标比最大下标 end下标还要大，Redis将 stop 的值设置为 end 。<br/>
     * 如果必须使用，请尽量控制每个 key下的数据量，必要时分多个 key 存储。<br/>
     * @param key 键
     * @param start 起始下标（包含）
     * @param stop	结束下标（包含）
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getObjectList(String key, long start, long stop) {
        key = getPrefixedKey(key);
        List<T> values = null;
        Jedis jedis = null;
        try {
            byte[] keyBytes = SafeEncoder.encode(key);
            jedis = getResource();
            List<byte[]> list = jedis.lrange(keyBytes, start, stop);
            if (list == null) {
                return null;
            }
            values = new ArrayList<T>(list.size());
            for (byte[] bs : list) {
                values.add((T) toObject(bs));
            }
            logger.debug("getObjectList {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("getObjectList {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return values;
    }

    /**
     * 获取Map缓存<br/>
     * ！O(N) 时间复杂度，慎用。<br/>
     * 如果必须使用，请尽量控制每个 key 下的数据量，必要时分多个 key 存储。
     * @param key 键
     * @return 值
     */
    public static Map<String, Object> getObjectMap(String key) {
        key = getPrefixedKey(key);
        Map<String, Object> values = null;
        Jedis jedis = null;
        try {
            byte[] keyBytes = SafeEncoder.encode(key);
            jedis = getResource();
            Map<byte[], byte[]> map = jedis.hgetAll(keyBytes);
            if (map == null) {
                return null;
            }
            values = new HashMap<String, Object>(map.size());
            for (Map.Entry<byte[], byte[]> e : map.entrySet()) {
                values.put(SafeEncoder.encode(e.getKey()),
                        toObject(e.getValue()));
            }
            logger.debug("getObjectMap {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("getObjectMap {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return values;
    }

    /**
     * 获取缓存<br/>
     * ！O(N) 时间复杂度，慎用。<br/>
     * 如果必须使用，请尽量控制每个 key 下的数据量，必要时分多个 key 存储。
     * @param key 键
     * @return 值
     */
    public static Set<Object> getObjectSet(String key) {
        key = getPrefixedKey(key);
        Set<Object> values = null;
        Jedis jedis = null;
        try {
            byte[] keyBytes = SafeEncoder.encode(key);
            jedis = getResource();
            Set<byte[]> set = jedis.smembers(keyBytes);
            if (set == null) {
                return null;
            }
            values = new HashSet<Object>(set.size());
            for (byte[] bs : set) {
                values.add(toObject(bs));
            }
            logger.debug("getObjectSet {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("getObjectSet {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return values;
    }

    /**
     * 获取缓存map中指定元素的值
     * @param key
     * @param mapKeys
     * @return
     * @author zzm
     */
    public static List<Object> getObjectsFromMap(String key,
                                                 String... mapKeys) {
        key = getPrefixedKey(key);
        List<Object> values = null;
        Jedis jedis = null;
        try {
            byte[] keyBytes = SafeEncoder.encode(key);
            byte[][] fields = SafeEncoder.encodeMany(mapKeys);
            jedis = getResource();
            List<byte[]> list = jedis.hmget(keyBytes, fields);
            if (list == null) {
                return null;
            }
            values = new ArrayList<Object>(mapKeys.length);
            for (byte[] v : list) {
                if (checkNil(v)) {
                    values.add(null);
                } else {
                    values.add(toObject(v));
                }
            }
        } catch (Exception e) {
            logger.warn("getObjectMapValues {} = {}", key, mapKeys);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return values;
    }

    /**
     * 获取资源
     * @return
     * @throws JedisException
     */
    public static Jedis getResource() throws JedisException {
        Jedis jedis = null;
        try {
            jedis = getResourceImpl();
        } catch (JedisException e) {
            if (!ExceptionUtils.isCausedBy(e, SocketTimeoutException.class)) {
                throw e;
            }
            // 第一次 因 SocketTimeoutException 失败的异常内部打印，不往上抛
            logger.warn(e.getMessage(), (Throwable) e);
        }

        if (jedis == null) {
            // FIXME 临时措施，需要寻找 SocketTimeoutException 的根本原因
            // 较低概率因为网络问题获取失败，重试一次，重试的 异常不再捕获，直接上抛
            jedis = getResourceImpl();
        }
        return jedis;
    }

    private static Jedis getResourceImpl() {
        Jedis jedis = null;
        try {
            jedis = sInstance.jedisPool.getResource();
        } catch (JedisException e) {
            returnResource(jedis);
            logger.warn(
                    "getResource failed. Active {}, Waiters {}, Idle {}, Exception {}",
                    sInstance.jedisPool.getNumActive(), sInstance.jedisPool.getNumWaiters(),
                    sInstance.jedisPool.getNumIdle(), e.getMessage());
            // 上抛交由上层决定怎么打印，此处不再重复打印异常内容
            throw e;
        } catch (Exception e) {
            returnResource(jedis);
            throw new JedisException(e);
        }
        return jedis;
    }

    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public static String getString(String key) {
        key = getPrefixedKey(key);
        String value = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            value = jedis.get(key);
            value = StringUtils.isNotBlank(value) && !checkNil(value) ? value
                    : null;
            logger.debug("get {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("get {} = {}", key, value);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 获取List缓存<br/>
     * ！O(N) 时间复杂度，慎用。<br/>
     * 如果必须使用，请尽量控制每个 key 下的数据量，必要时分多个 key 存储。
     * @param key 键
     * @return 值
     */
    public static List<String> getStringList(String key) {
        key = getPrefixedKey(key);
        List<String> values = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            values = jedis.lrange(key, 0, -1);
            logger.debug("getList {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("getList {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return values;
    }

    /**
     * 获取Map缓存<br/>
     * ！O(N) 时间复杂度，慎用。<br/>
     * 如果必须使用，请尽量控制每个 key 下的数据量，必要时分多个 key 存储。
     * @param key 键
     * @return 值
     */
    public static Map<String, String> getStringMap(String key) {
        key = getPrefixedKey(key);
        Map<String, String> values = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            values = jedis.hgetAll(key);
            logger.debug("getMap {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("getMap {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return values;
    }

    /**
     * 获取缓存<br/>
     * ！O(N) 时间复杂度，慎用。<br/>
     * 如果必须使用，请尽量控制每个 key 下的数据量，必要时分多个 key 存储。
     * @param key 键
     * @return 值
     */
    public static Set<String> getStringSet(String key) {
        key = getPrefixedKey(key);
        Set<String> values = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            values = jedis.smembers(key);
            logger.debug("getSet {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("getSet {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return values;
    }

    /**
     * 递增计数
     * @param key 键
     * @return
     */
    public static long incr(String key, int cacheSeconds) {
        key = getPrefixedKey(key);
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.incr(key);
            if (cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("incr {}", key);
        } catch (Exception e) {
            logger.warn("incr {}", key);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 递增计数
     * @param key 键
     * @param increment 增加的值
     * @return
     */
    public static long incrBy(String key, long increment, int cacheSeconds) {
        key = getPrefixedKey(key);
        long result = 0L;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.incrBy(key, increment);
            if (cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("incrBy {}, {}", key, increment);
        } catch (Exception e) {
            logger.warn("incrBy {}, {}", key, increment);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    public static boolean putObjectIntoMap(String key, String mapKey,
                                           Object value, int cacheSeconds) {
        Map<String, Object> map = new HashMap<>(1);
        map.put(mapKey, value);
        return putObjectsIntoMap(key, map, cacheSeconds);
    }

    /**
     * 向Map缓存中添加值
     * @param key 键
     * @param map 值
     * @return
     */
    public static boolean putObjectsIntoMap(String key, Map<String, Object> map,
                                            int cacheSeconds) {
        key = getPrefixedKey(key);
        boolean result = false;
        Jedis jedis = null;
        try {
            final Map<byte[], byte[]> bmap = new HashMap<byte[], byte[]>(
                    map.size());
            for (final Map.Entry<String, Object> e : map.entrySet()) {
                bmap.put(SafeEncoder.encode(e.getKey()), toBytes(e.getValue()));
            }
            jedis = getResource();
            result = checkOK(jedis.hmset(SafeEncoder.encode(key), bmap));
            if (result && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("mapObjectPut {} = {}", key, map);
        } catch (Exception e) {
            logger.warn("mapObjectPut {} = {}", key, map);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 向Map缓存中添加值
     * @param key 键
     * @param map 值
     * @return
     */
    public static boolean putStringsIntoMap(String key, Map<String, String> map,
                                            int cacheSeconds) {
        key = getPrefixedKey(key);
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = checkOK(jedis.hmset(key, map));
            if (result && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("mapPut {} = {}", key, map);
        } catch (Exception e) {
            logger.warn("mapPut {} = {}", key, map);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 移除Map缓存中的值
     * @param key 键
     * @param mapKey 值
     * @return
     */
    public static long removeFromMap(String key, String mapKey) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.hdel(key, mapKey);
            logger.debug("mapRemove {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapRemove {}  {}", key, mapKey);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 向Set缓存中移除指定值
     * @param key 键
     * @param values 值
     * @return
     */
    public static long removeFromSet(String key, Object... values) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            byte[][] many = new byte[values.length][];
            for (int i = 0; i < values.length; i++) {
                many[i] = toBytes(values[i]);
            }
            jedis = getResource();
            result = jedis.srem(SafeEncoder.encode(key), many);
            logger.debug("setObjectRemove {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("setObjectRemove {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 向Set缓存中移除指定值
     * @param key 键
     * @param values 值
     * @return
     */
    public static long removeFromSet(String key, String... values) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.srem(key, values);
            logger.debug("setSetRemove {} = {}", key, values);
        } catch (Exception e) {
            logger.warn("setSetRemove {} = {}", key, values);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 释放资源
     * @param jedis
     */
    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * 设置缓存<br/>
     * @see #set(String, Object, int)
     * @param key 键
     * @param value 值
     * @return
     */
    public static boolean set(String key, Object value) {
        return set(key, value, 0);
    }


    /**
     * 设置缓存<br/>
     * <br/>
     * Redis 只能存储 byte[]，所有对象都要转换成 byte[] 才能放进 Redis。<br/>
     * 不过 Redis 自己有一套编码 String 的方式，跟默认的序列化方式不同。<br/>
     * 通过 Redis 编码存储的 String 无法通过反序列化的方式获得。<br/>
     * 保存使用统一的 set 方法，做了判断， String 类型会走 Redis 自己的编码。<br/>
     * 其他类型则走 Java 序列化。<br/>
     * 所以 取出的时候需要选择对应的方法。<br/>
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static boolean set(String key, Object value, int cacheSeconds) {
        key = getPrefixedKey(key);
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            if (value instanceof String) {
                result = checkOK(jedis.set(key, value.toString()));
            } else {
                result = checkOK(jedis.set(SafeEncoder.encode(key), toBytes(value)));
            }
            if (result && cacheSeconds > 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("set {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("set {} = {}", key, value);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 设置缓存，当且仅当 key 不存在时执行
     * <br/>
     * Redis 只能存储 byte[]，所有对象都要转换成 byte[] 才能放进 Redis。<br/>
     * 不过 Redis 自己有一套编码 String 的方式，跟默认的序列化方式不同。<br/>
     * 通过 Redis 编码存储的 String 无法通过反序列化的方式获得。<br/>
     * 保存使用统一的 set 方法，做了判断， String 类型会走 Redis 自己的编码。<br/>
     * 其他类型则走 Java 序列化。<br/>
     * 所以 取出的时候需要选择对应的方法。<br/>
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return 如果执行成功返回1，否则 0
     */
    public static long setIfNotExists(String key, Object value,
                                      int cacheSeconds) {
        key = getPrefixedKey(key);
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getResource();
            if (value instanceof String) {
                result = jedis.setnx(key, value.toString());
            } else {
                result = jedis.setnx(SafeEncoder.encode(key), toBytes(value));
            }
            if (result == 1L) {
                if (cacheSeconds > 0) {
                    jedis.expire(key, cacheSeconds);
                }
                logger.debug("set {} = {}", key, value);
            } else {
                logger.debug("set: {} already exists.", key);
            }
        } catch (Exception e) {
            logger.warn("set {} = {}", key, value);
            logger.warn(e.getMessage(), (Throwable) e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 设置List缓存
     * TODO 删除和新建不是事务操作，中间可能插入其他操作导致结果不可预期
     * @param key 键
     * @param list 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static long setObjectList(String key, List<Object> list,
                                     int cacheSeconds) {
        del(key);
        return addIntoList(key, cacheSeconds, list.toArray());
    }

    /**
     * 设置Map缓存
     * TODO 删除和新建不是事务操作，中间可能插入其他操作导致结果不可预期
     * @param key 键
     * @param map 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static boolean setObjectMap(String key, Map<String, Object> map,
                                       int cacheSeconds) {
        del(key);
        return putObjectsIntoMap(key, map, cacheSeconds);
    }

    /**
     * 设置Set缓存
     * TODO 删除和新建不是事务操作，中间可能插入其他操作导致结果不可预期
     * @param key 键
     * @param set 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static long setObjectSet(String key, Set<Object> set,
                                    int cacheSeconds) {
        del(key);
        return addIntoSet(key, cacheSeconds, set.toArray());
    }

    /**
     * 设置List缓存
     * TODO 删除和新建不是事务操作，中间可能插入其他操作导致结果不可预期
     * @param key 键
     * @param list 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static long setStringList(String key, List<String> list,
                                     int cacheSeconds) {
        del(key);
        return addIntoList(key, cacheSeconds, list.toArray(new String[0]));
    }

    /**
     * 设置Map缓存
     * TODO 删除和新建不是事务操作，中间可能插入其他操作导致结果不可预期
     * @param key 键
     * @param map 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static boolean setStringMap(String key, Map<String, String> map,
                                       int cacheSeconds) {
        del(key);
        return putStringsIntoMap(key, map, cacheSeconds);
    }

    /**
     * 设置Set缓存
     * TODO 删除和新建不是事务操作，中间可能插入其他操作导致结果不可预期
     * @param key 键
     * @param set 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static long setStringSet(String key, Set<String> set,
                                    int cacheSeconds) {
        del(key);
        return addIntoSet(key, cacheSeconds, set.toArray(new String[0]));
    }

    /**
     * Object转换byte[]类型
     * @param object
     * @return
     */
    public static byte[] toBytes(Object object) {
        try {
            return ObjectUtils.serialize(object);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * byte[]型转换Object
     * @param bytes
     * @return
     */
    public static Object toObject(byte[] bytes) {
        try {
            return ObjectUtils.deserialize(bytes);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @Description 给key加上前缀     key = KEY_PREFIX+":"+key;
     * @param key
     * @return 加上前缀后的key
     * @author zzm
     * @date 2016年9月9日 下午2:01:11
     */
    public static String getPrefixedKey(String key) {
        if(sInstance == null) {
            throw new NullPointerException("jedisWrapper has not load, or load delay");
        }
        if (key != null)
            key = sInstance.keyPrefix + ":" + key;
        return key;
    }
}
