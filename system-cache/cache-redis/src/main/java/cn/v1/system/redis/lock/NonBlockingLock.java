package cn.v1.system.redis.lock;

import cn.v1.framework.exception.ServiceException;
import cn.v1.framework.utils.StringUtils;
import cn.v1.framework.utils.ThreadUtil;
import cn.v1.system.redis.JedisWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class NonBlockingLock {

	private static final String NBLOCK_PREFIX = "NBLOCK:";

	private static final Logger logger = LoggerFactory.getLogger(NonBlockingLock.class);

	private static final int DEFAULT_UNLOCK_DELAY_SECONDS = 2;

	/**
	 * 获取 由 LockType 和 id 对应的锁
	 * @param type
	 * @param id
	 * @return
	 */
	public static NonBlockingLock get(LockType type, String id) {
		return new NonBlockingLock(type, id);
	}

	private final LockType type;

	private final String key;

	private NonBlockingLock(LockType type, String id) {
		this.type = type;
		// 如果 ID 无效，创建一个所有操作都会失败的空白锁
		this.key = StringUtils.isNotBlank(id)
				? NBLOCK_PREFIX + type.name() + ":" + id
				: null;
	}

	/**
	 * 延迟释放 id 对应的锁，如果之前有锁定返回 true，否则 false。<br/>
	 * 默认延迟2秒释放，详情看
	 * @return 调用成功返回 true，否则 false
	 */
	public boolean delayUnlock() {
		return delayUnlock(DEFAULT_UNLOCK_DELAY_SECONDS);
	}

	/**
	 * 延迟delaySeconds秒释放 id 对应的锁，如果之前有锁定返回 true，否则 false。<br/>
	 * 注意事项：<br/>
	 * 一旦通过{@link #lock lock} 或者 {@link #tryLock tryLock}
	 * 获得锁，尽量在finally中调用本方法释放锁，否则可能引起超长锁，<br/>
	 * 非持有锁的线程调用会直接失败。<br/>
	 * <br/>
	 * 底层使用 Redis 实现，当 id 在 缓存 里的时候认为该 id 处于加锁状态<br/>
	 * @param delaySeconds 延迟多少秒释放，如果delaySeconds ≤ 0则直接调{@link #unlock}释放。
	 * @return  调用成功返回 true，否则 false
	 * @see #tryLock
	 * @see #lock
	 * @see #unlock
	 */
	public boolean delayUnlock(int delaySeconds) {
		if (isNullLock()) {
			return false;
		}
		if (delaySeconds > 0) {
			String threadId = ThreadUtil.getDistributedId();
			if (!threadId.equals(JedisWrapper.getString(key))) {
				return false;
			}
			long result = JedisWrapper.expire(key, delaySeconds);
			return result == 1L;
		} else {
			return unlock();
		}
	}

	/**
	 * 延迟delaySeconds秒释放 id 对应的锁，如果之前有锁定返回 true，否则 false。<br/>
	 * 注意事项：<br/>
	 * 一旦通过{@link #lock lock} 或者 {@link #tryLock tryLock}
	 * 获得锁，尽量在finally中调用本方法释放锁，否则可能引起超长锁，<br/>
	 * 非持有锁的线程调用会直接失败。<br/>
	 * <br/>
	 * 底层使用 Redis 实现，当 id 在 缓存 里的时候认为该 id 处于加锁状态<br/>
	 * @param delay 延迟多少秒释放，如果delay ≤ 0则直接调{@link #unlock}释放。
	 * @param unit 时间单位
	 * @return  调用成功返回 true，否则 false
	 * @see #tryLock
	 * @see #lock
	 * @see #unlock
	 */
	public boolean delayUnlock(int delay, TimeUnit unit) {
		return delayUnlock((int) unit.toSeconds(delay));
	}

	/**
	 * 获取id 对应的锁。<br/>
	 * 非阻塞实现，失败时抛出异常，不会阻塞在该方法上。<br/>
	 * 持有锁的线程可以重复获得锁（可重入），避免一个线程跟自己死锁。但不推荐这样做。
	 * 如果获取了锁，务必在finally里调用 {@link #unlock unlock}，否则可能引起死锁。<br/>
	 * <br/>
	 * 底层使用 Redis 实现，当 id 在 缓存 里的时候认为该 id 处于加锁状态<br/>
	 * @throws ServiceException
	 * @see #tryLock
	 */
	public void lock() throws ServiceException {
		if (!tryLock()) {
			throw new ServiceException(type.getHint());
		}
	}

	/**
	 * 尝试获取 id 对应的锁，成功返回 true，否则 false。<br/>
	 * 非阻塞实现，获取结果马上返回，不会阻塞在该方法上。<br/>
	 * 持有锁的线程可以重复获得锁（可重入），避免一个线程跟自己死锁。但不推荐这样做。
	 * 如果获取了锁，务必在finally里调用 {@link #unlock unlock}，否则可能引起死锁。<br/>
	 * <br/>
	 * 底层使用 Redis 实现，当 id 在 缓存 里的时候认为该 id 处于加锁状态<br/>
	 * @return 成功返回 true，否则 false
	 * @see #lock
	 */
	public boolean tryLock() {
		if (isNullLock()) {
			return false;
		}
		boolean success = false;
		try {
			String threadId = ThreadUtil.getDistributedId();
			success = (1L == JedisWrapper.setIfNotExists(key, threadId,
					type.getExpireSeconds()))
					|| threadId.equals(JedisWrapper.getString(key));// 持有锁的线程可重入
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return success;
	}

	/**
	 * 释放 id 对应的锁，如果之前有锁定返回 true，否则 false。<br/>
	 * 注意事项：<br/>
	 * 一旦通过{@link #lock lock} 或者 {@link #tryLock tryLock}
	 * 获得锁，尽量在finally中调用本方法释放锁，否则可能引起死锁，<br/>
	 * 非持有锁的线程调用会直接失败。<br/>
	 * <br/>
	 * 底层使用 Redis 实现，当 id 在 缓存 里的时候认为该 id 处于加锁状态<br/>
	 * @return 调用成功返回 true，否则 false
	 * @see #tryLock
	 * @see #lock
	 */
	public boolean unlock() {
		if (isNullLock()) {
			return false;
		}
		String threadId = ThreadUtil.getDistributedId();
		String lockOwner = JedisWrapper.getString(key);
		if (threadId.equals(lockOwner)) { // 必须持有锁的线程才能执行解锁
			return JedisWrapper.del(key) > 0L;
		} else {
			logger.info("{} unlock fail, lock owner is {}!", threadId,
					lockOwner);
			return false;
		}
	}

	private boolean isNullLock() {
		return key == null;
	}
}
