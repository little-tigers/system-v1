/**
 * Create by gen
 */
package cn.v1.system.security.shiro.cache;

import cn.v1.framework.utils.ServletUtils;
import cn.v1.system.redis.JedisWrapper;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.SafeEncoder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 自定义授权缓存管理类
 */
public class CacheManager implements org.apache.shiro.cache.CacheManager {

	// 外部类持有，供内部类使用。因为内部类不能声明静态变量。
	private static final Logger logger = LoggerFactory.getLogger(JedisCache.class);

	private String cacheKeyPrefix = "shiro_cache:";

	@Override
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		return new JedisCache<K, V>(cacheKeyPrefix + name);
	}

	public String getCacheKeyPrefix() {
		return cacheKeyPrefix;
	}

	public void setCacheKeyPrefix(String cacheKeyPrefix) {
		this.cacheKeyPrefix = cacheKeyPrefix;
	}

	/**
	 * 获取byte[]类型Key
	 * @param key
	 * @return
	 */
	private static Object keyToObject(byte[] key) {
		try {
			// 保持跟 jedis 一致的转换方式
			return SafeEncoder.encode(key);
		} catch (JedisException e) {
			try {
				return JedisWrapper.toObject(key);
			} catch (UnsupportedOperationException uoe2) {
				uoe2.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取byte[]类型Key
	 * @param object
	 * @return
	 */
	private static byte[] keyToBytes(Object object) {
		if (object instanceof String) {
			// String 类型的 key 转换方法跟 Jedis 保持一致
			return SafeEncoder.encode(object.toString());
		} else {
			return JedisWrapper.toBytes(object);
		}
	}

	/**
	 * 自定义授权缓存管理类
	 * @author ThinkGem
	 * @version 2014-7-20
	 */
	public class JedisCache<K, V> implements Cache<K, V> {

		private final String cacheKeyName;
		private final byte[] cacheKeyBytes;

		public JedisCache(String cacheKeyName) {
			this.cacheKeyName = cacheKeyName;
			this.cacheKeyBytes = SafeEncoder.encode(cacheKeyName);
		}

		@SuppressWarnings("unchecked")
		@Override
		public V get(K key) throws CacheException {
			if (key == null) {
				return null;
			}

			V v = null;
			HttpServletRequest request = ServletUtils.getRequest();
			if (request != null) {
				v = (V) request.getAttribute(cacheKeyName + ":" + key.toString());
				if (v != null) {
					return v;
				}
			}

			V value = null;
			Jedis jedis = null;
			try {
				jedis = JedisWrapper.getResource();
				value = (V) JedisWrapper.toObject(jedis.hget(cacheKeyBytes,
						CacheManager.keyToBytes(key)));
				logger.debug("get {} {} {}", cacheKeyName, key,
						request != null ? request.getRequestURI() : "");
			} catch (Exception e) {
				logger.error("get {} {} {}, {}\n{}", cacheKeyName, key,
						request != null ? request.getRequestURI() : "",
						e.getMessage(), ExceptionUtils.getStackTrace(e));
			} finally {
				JedisWrapper.returnResource(jedis);
			}

			if (request != null && value != null) {
				request.setAttribute(cacheKeyName + ":" + key.toString(), value);
			}

			return value;
		}

		@Override
		public V put(K key, V value) throws CacheException {
			if (key == null) {
				return null;
			}

			Jedis jedis = null;
			try {
				jedis = JedisWrapper.getResource();
				jedis.hset(cacheKeyBytes, CacheManager.keyToBytes(key),
						JedisWrapper.toBytes(value));
				logger.debug("put {} {} = {}", cacheKeyName, key, value);
			} catch (Exception e) {
				logger.error("put {} {}, {}\n{}", cacheKeyName, key,
						e.getMessage(), ExceptionUtils.getStackTrace(e));
			} finally {
				JedisWrapper.returnResource(jedis);
			}
			return value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public V remove(K key) throws CacheException {
			V value = null;
			Jedis jedis = null;
			try {
				jedis = JedisWrapper.getResource();
				value = (V) JedisWrapper.toObject(jedis.hget(cacheKeyBytes,
						CacheManager.keyToBytes(key)));
				jedis.hdel(cacheKeyBytes, CacheManager.keyToBytes(key));
				HttpServletRequest request = ServletUtils.getRequest();
				if (request != null) {
					request.removeAttribute(
							cacheKeyName + ":" + key.toString());
				}
				logger.debug("remove {} {}", cacheKeyName, key);
			} catch (Exception e) {
				logger.warn("remove {} {}, {}\n{}", cacheKeyName, key,
						e.getMessage(), ExceptionUtils.getStackTrace(e));
			} finally {
				JedisWrapper.returnResource(jedis);
			}
			return value;
		}

		@Override
		public void clear() throws CacheException {
			Jedis jedis = null;
			try {
				jedis = JedisWrapper.getResource();
				jedis.del(cacheKeyBytes);
				logger.debug("clear {}", cacheKeyName);
			} catch (Exception e) {
				logger.error("clear {}, {}\n{}", cacheKeyName, e.getMessage(),
						ExceptionUtils.getStackTrace(e));
			} finally {
				JedisWrapper.returnResource(jedis);
			}
		}

		@Override
		public int size() {
			int size = 0;
			Jedis jedis = null;
			try {
				jedis = JedisWrapper.getResource();
				size = jedis.hlen(cacheKeyBytes).intValue();
				logger.debug("size {} {} ", cacheKeyName, size);
				return size;
			} catch (Exception e) {
				logger.error("clear {}, {}\n{}", cacheKeyName, e.getMessage(),
						ExceptionUtils.getStackTrace(e));
			} finally {
				JedisWrapper.returnResource(jedis);
			}
			return size;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Set<K> keys() {
			Set<K> keys = Sets.newHashSet();
			Jedis jedis = null;
			try {
				jedis = JedisWrapper.getResource();
				Set<byte[]> set = jedis.hkeys(cacheKeyBytes);
				for (byte[] key : set) {
					Object obj = (K) CacheManager.keyToObject(key);
					if (obj != null) {
						keys.add((K) obj);
					}
				}
				logger.debug("keys {} {} ", cacheKeyName, keys);
				return keys;
			} catch (Exception e) {
				logger.error("keys {}, {}\n{}", cacheKeyName, e.getMessage(),
						ExceptionUtils.getStackTrace(e));
			} finally {
				JedisWrapper.returnResource(jedis);
			}
			return keys;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Collection<V> values() {
			Collection<V> vals = Collections.emptyList();
			;
			Jedis jedis = null;
			try {
				jedis = JedisWrapper.getResource();
				Collection<byte[]> col = jedis.hvals(cacheKeyBytes);
				for (byte[] val : col) {
					Object obj = JedisWrapper.toObject(val);
					if (obj != null) {
						vals.add((V) obj);
					}
				}
				logger.debug("values {} {} ", cacheKeyName, vals);
				return vals;
			} catch (Exception e) {
				logger.error("values {}", cacheKeyName, e);
			} finally {
				JedisWrapper.returnResource(jedis);
			}
			return vals;
		}
	}
}
