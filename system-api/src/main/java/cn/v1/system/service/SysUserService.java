package cn.v1.system.service;

import cn.v1.framework.base.BaseService;
import cn.v1.system.pojo.SysUser;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
public interface SysUserService extends BaseService<SysUser> {

    SysUser getByLoginName(String loginName);

    void updateUserLoginInfo(SysUser user);

}
