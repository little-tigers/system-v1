package cn.v1.system.security.realm;

import cn.v1.system.pojo.SysUser;
import cn.v1.system.security.utils.UserUtils;

import java.io.Serializable;

/**
 * @Auther: wr
 * @Date: 2018/11/5
 * @Description: 授权用户信息
 */
public class Principal implements Serializable {
    private static final long serialVersionUID = 1997543021210345061L;

    public Principal(SysUser user, boolean mobileLogin) {
        this.id = user.getId();
        this.loginName = user.getLoginName();
        this.name = user.getName();
        this.mobileLogin = mobileLogin;
    }

    private String id; // 编号

    private String loginName; // 登录名

    private String name; // 姓名

    private boolean mobileLogin; // 是否手机登录

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMobileLogin() {
        return mobileLogin;
    }

    public void setMobileLogin(boolean mobileLogin) {
        this.mobileLogin = mobileLogin;
    }

    /**
     * 获取SESSIONID
     */
    public String getSessionId() {
        try{
            return (String) UserUtils.getSession().getId();
        }catch (Exception e) {
            return "";
        }
    }
}
