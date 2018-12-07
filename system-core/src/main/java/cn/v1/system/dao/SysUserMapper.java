package cn.v1.system.dao;

import cn.v1.framework.base.BaseMapper;
import cn.v1.system.pojo.SysUser;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser findByLoginName(String loginName);

}
