package cn.v1.system.redis.atomic;


import cn.v1.system.redis.JedisWrapper;

/**
 * long类型数据原子性更新<br/>
 * 同一category对应的long值更新是原子性的，底层使用的是redis，具备跨服务器操作原子性<br/>
 * 注意：category是有过期时间的默认过期时间是30天(AtomicLong.DEFAULT_EXPIRE_SECONDS)
 * @author zzm
 * @date 2017年10月11日 下午5:17:57
 */
public class AtomicLong {

	private static final long MAX_COUNT = Long.MAX_VALUE >> 1; // 最大计数值
	private static final int DEFAULT_EXPIRE_SECONDS = 30 * 24 * 60 * 60; // 默认过期时间 30 天
	private static final String PREFIX = "ATOMIC_LONG:";

	/**
	 * @param category
	 * @param expect
	 * @param value
	 * @return
	 * @see #compareAndSet(String, long, long, int)
	 */
	public static boolean compareAndSet(String category, long expect,
			long value) {
		return compareAndSet(category, expect, value, DEFAULT_EXPIRE_SECONDS);
	}

	/**
	 * @param category
	 * @param expect
	 * @param value
	 * @param expireSeconds 过期时间，0则取默认过期时间{@value #DEFAULT_EXPIRE_SECONDS}秒
	 * @return
	 */
	public static boolean compareAndSet(String category, long expect,
			long value, int expireSeconds) {
		if (0L == expect) {
			// AtomicLong 中 空 与 "0" 等价。为了让后面的 CAS 成功，尝试把 空 初始化成 0
			JedisWrapper.setIfNotExists(getPrefixedKey(category), "0",
					expireSeconds > 0 ? expireSeconds : DEFAULT_EXPIRE_SECONDS);
		}
		return JedisWrapper.compareAndSet(getPrefixedKey(category),
				String.valueOf(expect), String.valueOf(value),
				expireSeconds > 0 ? expireSeconds : DEFAULT_EXPIRE_SECONDS);
	}

	public static long get(String category) {
		long count = 0L;
		try {
			String value = JedisWrapper.getString(getPrefixedKey(category));
			if(value != null) {
				count = Long.parseLong(value);
			}
		} catch (NumberFormatException e) {
			// not a num, do nothing;
		}
		return count;
	}

	/**
	 * @param category
	 * @return
	 * @see #getAndIncr(String, int)
	 */
	public static long getAndIncr(String category) {
		return getAndIncr(category, DEFAULT_EXPIRE_SECONDS);
	}

	/**
	 * @param category
	 * @param expireSeconds  过期时间，0则取默认过期时间{@value #DEFAULT_EXPIRE_SECONDS}秒
	 * @return
	 */
	public static long getAndIncr(String category, int expireSeconds) {
		return incrAndGet(category, expireSeconds) - 1L;
	}

	/**
	 * @param category
	 * @return
	 * @see #incrAndGet(String, int)
	 */
	public static long incrAndGet(String category) {
		return incrAndGet(category, DEFAULT_EXPIRE_SECONDS);
	}

	/**
	 * @param category
	 * @param expireSeconds 过期时间，0则取默认过期时间{@value #DEFAULT_EXPIRE_SECONDS}秒
	 * @return
	 */
	public static long incrAndGet(String category, int expireSeconds) {
		String key = getPrefixedKey(category);
		long count = 0;
		for (int i = 0; i < 3; i++) {
			count = JedisWrapper.incr(key,
					expireSeconds > 0 ? expireSeconds : DEFAULT_EXPIRE_SECONDS);
			if (count <= MAX_COUNT) {
				// 正常每秒 incr 一次，需要 34年才能达到最大值，绝大多数情况首次尝试就会返回，不会有效率问题
				return count;
			}
			// 超出范围，尝试重置
			if (compareAndSet(category, count, 0, expireSeconds)) {
				// 重置成功，返回 0
				return 0L;
			}
			// 重置失败，说明值有更新，有  decr 或 其他线程 incr 也踩中了重置，值可能已回到有效范围，循环重新尝试 incr
		}
		// 连续 3 次超出范围且重置失败，说明两个线程重置冲突，放弃重试直接返回最大值
		return MAX_COUNT;
	}

	/**
	 * 自减并返回自减后的值，最小值为 0
	 * @param category
	 * @return
	 * @see #getAndDecr(String, int)
	 */
	public static long getAndDecr(String category) {
		return getAndDecr(category, DEFAULT_EXPIRE_SECONDS);
	}

	/**
	 * 自减并返回自减后的值，最小值为 0
	 * @param category
	 * @param expireSeconds  过期时间，0则取默认过期时间{@value #DEFAULT_EXPIRE_SECONDS}秒
	 * @return
	 */
	public static long getAndDecr(String category, int expireSeconds) {
		return decrAndGet(category, expireSeconds) - 1L;
	}

	/**
	 * 自减并返回自减后的值，最小值为 0
	 * @param category
	 * @return
	 * @see #decrAndGet(String, int)
	 */
	public static long decrAndGet(String category) {
		return decrAndGet(category, DEFAULT_EXPIRE_SECONDS);
	}

	/**
	 * 自减并返回自减后的值，最小值为 0
	 * @param category
	 * @param expireSeconds 过期时间，0则取默认过期时间{@value #DEFAULT_EXPIRE_SECONDS}秒
	 * @return
	 */
	public static long decrAndGet(String category, int expireSeconds) {
		String key = getPrefixedKey(category);
		long count = 0;
		for (int i = 0; i < 2; i++) {
			count = JedisWrapper.decr(key,
					expireSeconds > 0 ? expireSeconds : DEFAULT_EXPIRE_SECONDS);
			if (count >= 0) {
				return count;
			}
			// 超出范围，尝试重置
			if (compareAndSet(category, count, 0, expireSeconds)) {
				// 重置成功，返回 0
				return 0L;
			}
			// 重置失败，说明值有更新，有  decr 或 其他线程 incr 也踩中了重置，值可能已回到有效范围，循环重新尝试 incr
		}
		// 连续 2 次超出范围且重置失败，说明两个线程重置冲突，放弃重试直接返回0 (值范围下穿比上穿概率高，只重试一次)
		return 0;
	}

	/**
	 * 将计数器重置为 0<br/>
	 * 注意该方法无论当前的值是多少都会重置为 0.<br/>
	 * 如果需要满足某些条件的情况下重置，为了保证操作的并发安全性，请使用 {@link #compareAndSet(String, long, long)}
	 * @param category
	 * @return 返回旧值
	 */
	public static long reset(String category) {
		try {
			return Long.parseLong(JedisWrapper.getAndSet(
					getPrefixedKey(category), "0", DEFAULT_EXPIRE_SECONDS));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * @param category
	 * @param value
	 * @see #set(String, long, int)
	 */
	public static void set(String category, long value) {
		set(category, value, DEFAULT_EXPIRE_SECONDS);
	}

	/**
	 * @param category
	 * @param value
	 * @param expireSeconds 过期时间，0则取默认过期时间{@value #DEFAULT_EXPIRE_SECONDS}秒
	 */
	public static void set(String category, long value, int expireSeconds) {
		JedisWrapper.set(getPrefixedKey(category), String.valueOf(value),
				expireSeconds > 0 ? expireSeconds : DEFAULT_EXPIRE_SECONDS);
	}

	private static String getPrefixedKey(String category) {
		return PREFIX + category;
	}
}
