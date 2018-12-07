package cn.v1.system.service;

import cn.v1.framework.base.BaseService;
import cn.v1.system.pojo.SysMenu;
import cn.v1.system.pojo.SysRole;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
public interface SysMenuService extends BaseService<SysMenu> {

    List<SysMenu> getByRolesList(List<SysRole> roleList);

    List<SysMenu> getChildrenList(String id);

}
