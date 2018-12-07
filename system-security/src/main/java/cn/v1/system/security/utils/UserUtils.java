/**
 * Create by gen
 */
package cn.v1.system.security.utils;

import cn.v1.framework.holder.SpringContextHolder;
import cn.v1.system.pojo.*;
import cn.v1.system.security.realm.Principal;
import cn.v1.system.security.shiro.session.dao.SessionDAO;
import cn.v1.system.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import java.util.Collection;
import java.util.List;

/**
 * 用户工具类
 * @author ThinkGem
 * @version 2013-12-05
 */
public class UserUtils {

	private static SysUserService sysUserService = SpringContextHolder.getBean(SysUserService.class);

	private static SysMenuService sysMenuService = SpringContextHolder.getBean(SysMenuService.class);

	private static SysRoleService sysRoleService = SpringContextHolder.getBean(SysRoleService.class);

	private static SysAreaService sysAreaService = SpringContextHolder.getBean(SysAreaService.class);

	private static SysOfficeService sysOfficeService = SpringContextHolder.getBean(SysOfficeService.class);

	public static final String USER_CACHE = "userCache";
	public static final String USER_CACHE_ID_ = "id_";
	public static final String USER_CACHE_LOGIN_NAME_ = "ln";
	public static final String USER_CACHE_LIST_BY_OFFICE_ID_ = "oid_";

	public static final String CACHE_ROLE_LIST = "roleList";
	public static final String CACHE_MENU_LIST = "menuList";
    public static final String CACHE_PARENT_MENU_LIST = "parent_menuList";
	public static final String CACHE_AREA_LIST = "areaList";
	public static final String CACHE_OFFICE_LIST = "officeList";
	public static final String CACHE_OFFICE_ALL_LIST = "officeAllList";

	public static SysUserService getSysUserService() {
		return sysUserService;
	}

	public static SysMenuService getSysMenuService() {
		return sysMenuService;
	}

	public static SysRoleService getSysRoleService() {
		return sysRoleService;
	}

	public static SysAreaService getSysAreaService() {
		return sysAreaService;
	}

	public static SysOfficeService getSysOfficeService() {
		return sysOfficeService;
	}

	/**
	 * 根据ID获取用户
	 * @param id
	 * @return 取不到返回null
	 */
	public static SysUser get(String id){
		SysUser user = (SysUser)CacheUtils.get(USER_CACHE, USER_CACHE_ID_ + id);
		if (user ==  null){
			user = sysUserService.getById(id);
			if (user == null){
				return null;
			}
			CacheUtils.put(USER_CACHE, USER_CACHE_ID_ + user.getId(), user);
			CacheUtils.put(USER_CACHE, USER_CACHE_LOGIN_NAME_ + user.getLoginName(), user);
		}
		return user;
	}
	
	/**
	 * 根据登录名获取用户
	 * @param loginName
	 * @return 取不到返回null
	 */
	public static SysUser getByLoginName(String loginName){
		SysUser user = (SysUser)CacheUtils.get(USER_CACHE, USER_CACHE_LOGIN_NAME_ + loginName);
		if (user == null){
			user = sysUserService.getByLoginName(loginName);
			if (user == null){
				return null;
			}
			CacheUtils.put(USER_CACHE, USER_CACHE_ID_ + user.getId(), user);
			CacheUtils.put(USER_CACHE, USER_CACHE_LOGIN_NAME_ + user.getLoginName(), user);
		}
		return user;
	}
	
	/**
	 * 清除当前用户缓存
	 */
	public static void clearCache(){
		removeCache(CACHE_ROLE_LIST);
		removeCache(CACHE_MENU_LIST);
		removeCache(CACHE_AREA_LIST);
		removeCache(CACHE_OFFICE_LIST);
		removeCache(CACHE_OFFICE_ALL_LIST);
		UserUtils.clearCache(getUser());
	}
	
	/**
	 * 清除指定用户缓存
	 * @param user
	 */
	public static void clearCache(SysUser user){
		CacheUtils.remove(USER_CACHE, USER_CACHE_ID_ + user.getId());
		CacheUtils.remove(USER_CACHE, USER_CACHE_LOGIN_NAME_ + user.getLoginName());
		/*CacheUtils.remove(USER_CACHE, USER_CACHE_LOGIN_NAME_ + user.getOldLoginName());*/
		if (user.getOffice() != null && user.getOffice().getId() != null){
			CacheUtils.remove(USER_CACHE, USER_CACHE_LIST_BY_OFFICE_ID_ + user.getOffice().getId());
		}
	}
	
	/**
	 * 获取当前用户
	 * @return 取不到返回 new User()
	 */
	public static SysUser getUser(){
		Principal principal = getPrincipal();
		if (principal!=null){
			SysUser user = get(principal.getId());
			if (user != null){
				return user;
			}
			return new SysUser();
		}
		// 如果没有登录，则返回实例化空的User对象。
		return new SysUser();
	}

	/**
	 * 获取当前用户角色列表
	 * @return
	 */
	public static List<SysRole> getRoleList(){
		@SuppressWarnings("unchecked")
		List<SysRole> roleList = (List<SysRole>)getCache(CACHE_ROLE_LIST);
		if (roleList == null){
			SysUser user = getUser();
			if (user.isAdmin()){
				roleList = sysRoleService.getList();
			}else{
				roleList = user.getRoleList();
			}
			putCache(CACHE_ROLE_LIST, roleList);
		}
		return roleList;
	}
	
	/**
	 * 获取当前用户授权菜单
	 * @return
	 */
	public static List<SysMenu> getMenuList(){
		@SuppressWarnings("unchecked")
		List<SysMenu> menuList = (List<SysMenu>)getCache(CACHE_MENU_LIST);
		if (menuList == null){
			SysUser user = getUser();
			if (user.isAdmin()){
				menuList = sysMenuService.getList();
			}else{
				menuList = sysMenuService.getByRolesList(user.getRoleList());
			}
			putCache(CACHE_MENU_LIST, menuList);
		}
		return menuList;
	}

    public static List<SysMenu> getMenuChildrenList(String id){
        @SuppressWarnings("unchecked")
        List<SysMenu> menuList = (List<SysMenu>)getCache(CACHE_PARENT_MENU_LIST);
        if (menuList == null){
            menuList = sysMenuService.getChildrenList(id);
            putCache(CACHE_PARENT_MENU_LIST, menuList);
        }
        return menuList;
    }

	/**
	 * 获取超级管理yuan用户
	 * @return 取不到返回 new User()
	 */
	public static SysUser getAdmin(){
		return new SysUser("1");
	}
	
	/**
	 * 获取当前用户授权的区域
	 * @return
	 */
	public static List<SysArea> getAreaList(){
		@SuppressWarnings("unchecked")
		List<SysArea> areaList = (List<SysArea>)getCache(CACHE_AREA_LIST);
		if (areaList == null){
			areaList = sysAreaService.getList();
			putCache(CACHE_AREA_LIST, areaList);
		}
		return areaList;
	}
	
	/**
	 * 获取当前用户有权限访问的部门
	 * @return
	 */
	public static List<SysOffice> getOfficeList(){
		@SuppressWarnings("unchecked")
		List<SysOffice> officeList = (List<SysOffice>)getCache(CACHE_OFFICE_LIST);
		if (officeList == null){
			SysUser user = getUser();
			if (user.isAdmin()){
				officeList = getOfficeAllList();
			}else{
				/*Office office = new Office();
				office.getSqlMap().put("dsf", BaseService.dataScopeFilter(user, "a", ""));
				officeList = user.findList(office);*/
			}
			putCache(CACHE_OFFICE_LIST, officeList);
		}
		return officeList;
	}

	/**
	 * 获取当前用户有权限访问的部门
	 * @return
	 */
	public static List<SysOffice> getOfficeAllList(){
		@SuppressWarnings("unchecked")
		List<SysOffice> officeList = (List<SysOffice>)getCache(CACHE_OFFICE_ALL_LIST);
		if (officeList == null){
			officeList = sysOfficeService.getList();
		}
		return officeList;
	}
	/**
	 * 获取授权主要对象
	 */
	public static Subject getSubject(){
		return SecurityUtils.getSubject();
	}
	
	/**
	 * 获取当前登录者对象
	 */
	public static Principal getPrincipal(){
		try{
			Subject subject = SecurityUtils.getSubject();
			Principal principal = (Principal)subject.getPrincipal();
			if (principal != null){
				return principal;
			}
//			subject.logout();
		}catch (UnavailableSecurityManagerException e) {
			
		}catch (InvalidSessionException e){
			
		}
		return null;
	}
	
	public static Session getSession(){
		try{
			Subject subject = SecurityUtils.getSubject();
			Session session = subject.getSession(false);
			if (session == null){
				session = subject.getSession();
			}
			if (session != null){
				return session;
			}
//			subject.logout();
		}catch (InvalidSessionException e){
			
		}
		return null;
	}
	
	// ============== User Cache ==============
	
	public static Object getCache(String key) {
		return getCache(key, null);
	}
	
	public static Object getCache(String key, Object defaultValue) {
//		Object obj = getCacheMap().get(key);
		Object obj = getSession().getAttribute(key);
		return obj==null?defaultValue:obj;
	}

	public static void putCache(String key, Object value) {
//		getCacheMap().put(key, value);
		getSession().setAttribute(key, value);
	}

	public static void removeCache(String key) {
//		getCacheMap().remove(key);
		getSession().removeAttribute(key);
	}
	
	public static boolean logout(boolean includeAuthenticated) {
		Subject subject = getSubject();
		if (includeAuthenticated || subject.isRemembered()) {
			subject.logout();
			return true;
		}
		return false;
	}
	public static void setLoginExpired(Object principal,
			Session excludeSession) {
		SessionDAO sessionDao = SpringContextHolder.getBean(SessionDAO.class);
		Collection<Session> sessions = sessionDao.getActiveSessions(true,
				principal, excludeSession);
		for (Session s : sessions) {
			try {
				//s.setAttribute(SessionExpiredFilter.SESSION_EXPIRED, true);
				s.setTimeout(1);
				sessionDao.update(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void setLoginExpired(SysUser user) {
		if (user == null || StringUtils.isBlank(user.getId())) {
			return;
		}
		setLoginExpired(new Principal(user, false), null);
	}

	
	
//	public static Map<String, Object> getCacheMap(){
//		Principal principal = getPrincipal();
//		if(principal!=null){
//			return principal.getCacheMap();
//		}
//		return new HashMap<String, Object>();
//	}
	
}
