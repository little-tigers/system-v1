package cn.v1.system.pojo;

import cn.v1.framework.base.BasePoJo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description: 菜单t_sys_menu
 */
public class SysMenu extends BasePoJo<SysMenu> implements Serializable {

    private static final long serialVersionUID = 2792347454638351639L;

    private String name; //区域名称

    private String href; 	// 链接

    private String target; 	// 目标（ mainFrame、_blank、_self、_parent、_top）

    private String icon; 	// 图标

    private String isShow; 	// 是否在菜单中显示（1：显示；0：不显示）

    private String permission; // 权限标识

    private Integer sort; 	// 排序

    private SysMenu parent;  //父级编号

    private List<SysMenu> childrenList;

    private String parentIds;  //父级所有编号

    private List<SysRole> roleList;  //拥有角色

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIsShow() {
        return isShow;
    }

    public void setIsShow(String isShow) {
        this.isShow = isShow;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public SysMenu getParent() {
        return parent;
    }

    public void setParent(SysMenu parent) {
        this.parent = parent;
    }

    public List<SysMenu> getChildrenList() {
        return childrenList;
    }

    public void setChildrenList(List<SysMenu> childrenList) {
        this.childrenList = childrenList;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    public List<SysRole> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<SysRole> roleList) {
        this.roleList = roleList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
