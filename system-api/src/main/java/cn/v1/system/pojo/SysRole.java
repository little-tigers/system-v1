package cn.v1.system.pojo;

import cn.v1.framework.base.BasePoJo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description: 角色 t_sys_role
 */
public class SysRole extends BasePoJo<SysRole> implements Serializable {

    private static final long serialVersionUID = 6448713784790012602L;

    private String name; //角色名称

    private String enName; //英文名称

    private String roleType; //权限类型

    private String dataScope; //数据范围

    private String isSys; //是否是系统数据

    private String usable; 	//是否是可用

    private SysOffice office;	// 归属机构

    private List<SysUser> userList;  //拥有系统用户

    private List<SysMenu> menuList;  //拥有菜单

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public String getIsSys() {
        return isSys;
    }

    public void setIsSys(String isSys) {
        this.isSys = isSys;
    }

    public String getUsable() {
        return usable;
    }

    public void setUsable(String usable) {
        this.usable = usable;
    }

    public SysOffice getOffice() {
        return office;
    }

    public void setOffice(SysOffice office) {
        this.office = office;
    }

    public List<SysUser> getUserList() {
        return userList;
    }

    public void setUserList(List<SysUser> userList) {
        this.userList = userList;
    }

    public List<SysMenu> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<SysMenu> menuList) {
        this.menuList = menuList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
