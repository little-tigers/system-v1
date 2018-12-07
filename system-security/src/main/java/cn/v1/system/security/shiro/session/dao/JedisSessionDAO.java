/**
 * Create by gen
 */
package cn.v1.system.security.shiro.session.dao;

import cn.v1.framework.base.SysConfig;
import cn.v1.framework.utils.DateUtils;
import cn.v1.framework.utils.ServletUtils;
import cn.v1.framework.utils.StringUtils;
import cn.v1.system.redis.JedisWrapper;
import com.google.common.collect.Sets;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 自定义授权会话管理类
 * @author
 * @version 2014-7-20
 */
public class JedisSessionDAO extends AbstractSessionDAO implements SessionDAO {

	private static final String REQUEST_SESSION_CACHE_PREFIX = "session_";

	private static final Logger logger = LoggerFactory.getLogger(JedisSessionDAO.class);

	private String sessionKeyPrefix = "system_session:";

	private String sessionIdsKey = sessionKeyPrefix + "ids";

	@Override
	public void delete(Session session) {
		if (session == null || session.getId() == null) {
			return;
		}

		Jedis jedis = null;
		try {
			jedis = JedisWrapper.getResource();

			jedis.hdel(sessionIdsKey, session.getId().toString());
			jedis.del(getPrefixedKeyBytes(session.getId()));

			logger.debug("delete {} ", session.getId());
		} catch (Exception e) {
			logger.error("delete {} ", session.getId(), e);
		} finally {
			JedisWrapper.returnResource(jedis);
		}
	}

	@Override
	public Collection<Session> getActiveSessions() {
		return getActiveSessions(true);
	}

	/**
	 * 获取活动会话
	 * @param includeLeave 是否包括离线（最后访问时间大于3分钟为离线会话）
	 * @return
	 */
	@Override
	public Collection<Session> getActiveSessions(boolean includeLeave) {
		return getActiveSessions(includeLeave, null, null);
	}

	/**
	 * 获取活动会话
	 * @param includeLeave 是否包括离线（最后访问时间大于3分钟为离线会话）
	 * @param principal 根据登录者对象获取活动会话
	 * @param filterSession 不为空，则过滤掉（不包含）这个会话。
	 * @return
	 */
	@Override
	public Collection<Session> getActiveSessions(boolean includeLeave,
                                                 Object principal, Session filterSession) {
		Set<Session> sessions = Sets.newHashSet();

		Jedis jedis = null;
		try {
			jedis = JedisWrapper.getResource();
			Map<String, String> map = jedis.hgetAll(sessionIdsKey);
			for (Map.Entry<String, String> e : map.entrySet()) {
				if (StringUtils.isBlank(e.getKey())) {
					continue;
				}

				// 存储的 SESSION 有 key 无 Value
				if (StringUtils.isBlank(e.getValue())) {
					jedis.hdel(sessionIdsKey, e.getKey());
					continue;
				}

				String[] ss = StringUtils.split(e.getValue(), "|");

				// 存储的SESSION不符合规则
				if (ss == null || ss.length != 3) {
					jedis.hdel(sessionIdsKey, e.getKey());
					continue;
				}

				// jedis.exists(sessionKeyPrefix
				// + e.getKey())){
				// Session session =
				// (Session)JedisWrapper.toObject(jedis.get(JedisWrapper.getBytesKey(sessionKeyPrefix
				// + e.getKey())));
				SimpleSession session = new SimpleSession();
				session.setId(e.getKey());
				session.setAttribute("principalId", ss[0]);
				session.setTimeout(Long.valueOf(ss[1]));
				session.setLastAccessTime(new Date(Long.valueOf(ss[2])));
				try {
					// 验证SESSION
					session.validate();

					if (!includeLeave && DateUtils
							.pastMinutes(session.getLastAccessTime()) > 3) {
						// 不包括离开（指离开三分钟以上）  且 最后访问时间 大于 3分钟
						continue;
					}

					if (principal != null) {
						// 有限定登录用户
						if (!principal.toString().equals(ss[0])) {
							// 不是该登录用户
							continue;
						}
					}

					if (filterSession != null
							&& filterSession.getId().equals(session.getId())) {
						// 过滤掉的SESSION
						continue;
					}

					sessions.add(session);
				} catch (Exception ex) {
					// SESSION验证失败
					jedis.hdel(sessionIdsKey, e.getKey());
				}
			}
			logger.info("getActiveSessions size: {} ", sessions.size());
		} catch (Exception e) {
			logger.error("getActiveSessions", e);
		} finally {
			JedisWrapper.returnResource(jedis);
		}
		return sessions;
	}

	@Override
	public Session readSession(Serializable sessionId)
			throws UnknownSessionException {
		try {
			return super.readSession(sessionId);
		} catch (UnknownSessionException e) {
			return null;
		}
	}

	public void setSessionKeyPrefix(String sessionKeyPrefix) {
		this.sessionKeyPrefix = sessionKeyPrefix;
		this.sessionIdsKey = sessionKeyPrefix + "ids";
	}

	@Override
	public void update(Session session) throws UnknownSessionException {
		if (session == null || session.getId() == null) {
			return;
		}

		HttpServletRequest request = ServletUtils.getRequest();
		if (request != null) {
			String uri = request.getServletPath();
			// 如果是静态文件，则不更新SESSION
			if (ServletUtils.isStaticFile(uri)) {
				return;
			}
			// 如果是视图文件，则不更新SESSION
			if (StringUtils
					.startsWith(uri, "/WEB-INF/views/")
					&& StringUtils.endsWith(uri,
							".jsp")) {
				return;
			}
			// 手动控制不更新SESSION
			if (SysConfig.NO.equals(request.getParameter("updateSession"))) {
				return;
			}
		}

		Jedis jedis = null;
		try {

			jedis = JedisWrapper.getResource();

			// 获取登录者编号
			PrincipalCollection pc = (PrincipalCollection) session
					.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
			String principalId = pc != null ? pc.getPrimaryPrincipal()
					.toString() : StringUtils.EMPTY;

			jedis.hset(sessionIdsKey, session.getId().toString(), principalId
					+ "|" + session.getTimeout() + "|"
					+ session.getLastAccessTime().getTime());
			jedis.set(getPrefixedKeyBytes(session.getId()),
					JedisWrapper.toBytes(session));

			// 设置超期时间
			int timeoutSeconds = (int) (session.getTimeout() / 1000);
			jedis.expire(getPrefixedKey(session.getId()), timeoutSeconds);

			logger.debug("update {} {}", session.getId(),
					request != null ? request.getRequestURI() : "");
		} catch (Exception e) {
			logger.error("update {} {}", session.getId(),
					request != null ? request.getRequestURI() : "", e);
		} finally {
			JedisWrapper.returnResource(jedis);
		}
	}

	/**
	 * 给定 id，返回加了前缀的 key
	 * @param id
	 * @return
	 */
	private String getPrefixedKey(Serializable id) {
		return sessionKeyPrefix + id.toString();
	}

	/**
	 * 给定 id，返回加了前缀的 key，并且转换成 bytes
	 * @param id
	 * @return
	 */
	private byte[] getPrefixedKeyBytes(Serializable id) {
		return SafeEncoder.encode(getPrefixedKey(id));
	}

	@Override
	protected Serializable doCreate(Session session) {
		HttpServletRequest request = ServletUtils.getRequest();
		if (request != null) {
			String uri = request.getServletPath();
			// 如果是静态文件，则不创建SESSION
			if (ServletUtils.isStaticFile(uri)) {
				return null;
			}
		}
		Serializable sessionId = this.generateSessionId(session);
		this.assignSessionId(session, sessionId);
		this.update(session);
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {

		Session s = null;
		HttpServletRequest request = ServletUtils.getRequest();
		if (request != null) {
			String uri = request.getServletPath();
			// 如果是静态文件，则不获取SESSION
			if (ServletUtils.isStaticFile(uri)) {
				return null;
			}
			s = (Session) request.getAttribute(REQUEST_SESSION_CACHE_PREFIX + sessionId);
		}
		if (s != null) {
			return s;
		}

		Session session = null;
		Jedis jedis = null;
		try {
			jedis = JedisWrapper.getResource();
			// if (jedis.exists(sessionKeyPrefix + sessionId)){
			session = (Session) JedisWrapper.toObject(jedis
					.get(getPrefixedKeyBytes(sessionId)));
			// }
			if (logger.isDebugEnabled()) {
				logger.debug("doReadSession {} {} session:{}", sessionId,
						request != null ? request.getRequestURI() : "", session);
			}
		} catch (Exception e) {
			logger.error(
					"doReadSession " + sessionId + " " + request != null ? request
							.getRequestURI() : "", e);
		} finally {
			JedisWrapper.returnResource(jedis);
		}

		if (request != null && session != null) {
			request.setAttribute(REQUEST_SESSION_CACHE_PREFIX + sessionId,
					session);
		} else if (logger.isDebugEnabled()) {
			logger.debug("doReadSession {} failed ", sessionId);
		}

		return session;
	}

}
