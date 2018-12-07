package cn.v1.system.dao;

import cn.v1.framework.base.BaseMapper;
import cn.v1.system.pojo.SysMenu;
import cn.v1.system.pojo.SysRole;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> findByRolesList(List<SysRole> roleList);

    List<SysMenu> findChildrenList(String id);

}
