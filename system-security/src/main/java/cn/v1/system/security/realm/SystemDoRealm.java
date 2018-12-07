/**
 * Create by gen
 */
package cn.v1.system.security.realm;

import cn.v1.framework.base.SysConfig;
import cn.v1.framework.servlet.ValidateCodeServlet;
import cn.v1.framework.utils.EncodesUtil;
import cn.v1.system.pojo.SysMenu;
import cn.v1.system.pojo.SysRole;
import cn.v1.system.pojo.SysUser;
import cn.v1.system.security.shiro.session.dao.SessionDAO;
import cn.v1.system.security.token.UsernamePasswordToken;
import cn.v1.system.security.utils.LoginUtil;
import cn.v1.system.security.utils.UserUtils;
import cn.v1.system.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

/**
 * 系统安全认证实现类
 * @author ThinkGem
 * @version 2014-7-5
 */
@Service
public class SystemDoRealm extends AuthorizingRealm {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String HASH_ALGORITHM = "SHA-1";

	public static final int HASH_INTERATIONS = 1024;

	public static final int SALT_SIZE = 8;

	@Autowired
	private SessionDAO sessionDAO;

	@Autowired
	private SysUserService userService;



	/**
	 * 认证回调函数, 登录时调用
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) {
		UsernamePasswordToken token = (UsernamePasswordToken) authToken;
		if (logger.isDebugEnabled()){
			int activeSessionSize = sessionDAO.getActiveSessions(false).size();
			logger.debug("login submit, active session size: {}, username: {}", activeSessionSize, token.getUsername());
		}

		// 校验登录验证码
		if (LoginUtil.isValidateCodeLogin(token.getUsername(), false, false)){
			Session session = UserUtils.getSession();
			String code = (String)session.getAttribute(ValidateCodeServlet.VALIDATE_CODE);
			if (token.getCaptcha() == null || !token.getCaptcha().toUpperCase().equals(code)){
				throw new AuthenticationException("msg:验证码错误, 请重试.");
			}
		}
		// 校验用户名密码
		SysUser user = UserUtils.getByLoginName(token.getUsername());
		if (user != null) {
			if (SysConfig.NO.equals(user.getLoginFlag())){
				throw new AuthenticationException("msg:该已帐号禁止登录.");
			}
			byte[] salt = EncodesUtil.decodeHex(user.getPassword().substring(0,16));
			SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(new Principal(user, token.isMobileLogin()),
					user.getPassword().substring(16), ByteSource.Util.bytes(salt), getName());
			return simpleAuthenticationInfo;
		} else {
			return null;
		}
	}

	/**
	 * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Principal principal = (Principal) getAvailablePrincipal(principals);
		// 获取当前已登录的用户
		// FIXME 临时修改，注释账户单点登录的代码（密码登录时强制其他地方的登录下线）
//		if (!Global.getBoolean("user.multiAccountLogin")
//				&& UserUtils.getSubject().isAuthenticated()) {
//			UserUtils.setLoginExpired(principal, UserUtils.getSession());
//		}
		SysUser user = userService.getByLoginName(principal.getLoginName());
		if (user != null) {
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			List<SysMenu> list = UserUtils.getMenuList();
			for (SysMenu menu : list){
				if (StringUtils.isNotBlank(menu.getPermission())){
					// 添加基于Permission的权限信息
					for (String permission : StringUtils.split(menu.getPermission(),",")){
						info.addStringPermission(permission);
					}
				}
			}
			// 添加用户权限
			info.addStringPermission("user");
			// 添加用户角色信息
			for (SysRole role : user.getRoleList()){
				info.addRole(role.getEnName());
			}
			// 更新登录IP和时间
			userService.updateUserLoginInfo(user);
			// 记录登录日志
		/*	LogUtils.saveLog(ServletUtils.getRequest(), "系统登录");*/
			return info;
		} else {
			return null;
		}
	}

	@Override
	protected void checkPermission(Permission permission, AuthorizationInfo info) {
		authorizationValidate(permission);
		super.checkPermission(permission, info);
	}

	@Override
	protected boolean[] isPermitted(List<Permission> permissions, AuthorizationInfo info) {
		if (permissions != null && !permissions.isEmpty()) {
            for (Permission permission : permissions) {
        		authorizationValidate(permission);
            }
        }
		return super.isPermitted(permissions, info);
	}

	@Override
	public boolean isPermitted(PrincipalCollection principals, Permission permission) {
		authorizationValidate(permission);
		return super.isPermitted(principals, permission);
	}

	@Override
	protected boolean isPermittedAll(Collection<Permission> permissions, AuthorizationInfo info) {
		if (permissions != null && !permissions.isEmpty()) {
            for (Permission permission : permissions) {
            	authorizationValidate(permission);
            }
        }
		return super.isPermittedAll(permissions, info);
	}

	/**
	 * 授权验证方法
	 * @param permission
	 */
	private void authorizationValidate(Permission permission){
		// 模块授权预留接口
	}

	/**
	 * 设定密码校验的Hash算法与迭代次数
	 */
	@PostConstruct
	public void initCredentialsMatcher() {
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(HASH_ALGORITHM);
		matcher.setHashIterations(HASH_INTERATIONS);
		setCredentialsMatcher(matcher);
	}

//	/**
//	 * 清空用户关联权限认证，待下次使用时重新加载
//	 */
//	public void clearCachedAuthorizationInfo(Principal principal) {
//		SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
//		clearCachedAuthorizationInfo(principals);
//	}

	/**
	 * 清空所有关联认证
	 * @Deprecated 不需要清空，授权缓存保存到session中
	 */
	@Deprecated
	public void clearAllCachedAuthorizationInfo() {
//		Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
//		if (cache != null) {
//			for (Object key : cache.keys()) {
//				cache.remove(key);
//			}
//		}
	}

}