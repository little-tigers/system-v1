package cn.v1.system.security.filter;

import org.apache.shiro.session.Session;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class SessionExpiredFilter extends AccessControlFilter {

	public static final String SESSION_EXPIRED = "sessionExpired";

	@Override
	protected boolean isAccessAllowed(ServletRequest request,
                                      ServletResponse response, Object mappedValue) throws Exception {
		Session session = getSubject(request, response).getSession(false);
		if (session == null) {
			return true;
		}
		return session.getAttribute(SESSION_EXPIRED) == null;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request,
                                     ServletResponse response) throws Exception {
		try {
			//强制退出  
			getSubject(request, response).logout();
		} catch (Exception e) {
			/*ignore exception*/
		}
		WebUtils.issueRedirect(request, response, getLoginUrl());
		return false;
	}

}
