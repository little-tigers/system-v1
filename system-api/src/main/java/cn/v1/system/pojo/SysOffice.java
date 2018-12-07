package cn.v1.system.pojo;

import cn.v1.framework.base.BasePoJo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description: 机构 t_sys_office
 */
public class SysOffice extends BasePoJo<SysOffice> implements Serializable {

    private static final long serialVersionUID = 7318361813711185299L;

    private String name; 	//机构名称

    private String code; 	// 机构编码

    private String type; 	// 机构类型（1：公司；2：部门；3：小组）

    private String grade; 	// 机构等级（1：一级；2：二级；3：三级；4：四级）

    private String address; // 联系地址

    private String zipCode; // 邮政编码

    private String master; 	// 负责人

    private String phone; 	// 电话

    private String fax; 	// 传真

    private String email; 	// 邮箱

    private String usable; //是否可用

    private SysUser primary; //主负责人

    private SysUser deputy; //副负责人

    private SysArea area;	// 归属区域

    private Integer sort;		// 排序

    private SysOffice parent;  //父级编号

    private String parentIds; //父级所有编号

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsable() {
        return usable;
    }

    public void setUsable(String usable) {
        this.usable = usable;
    }

    public SysUser getPrimary() {
        return primary;
    }

    public void setPrimary(SysUser primary) {
        this.primary = primary;
    }

    public SysUser getDeputy() {
        return deputy;
    }

    public void setDeputy(SysUser deputy) {
        this.deputy = deputy;
    }

    public SysArea getArea() {
        return area;
    }

    public void setArea(SysArea area) {
        this.area = area;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public SysOffice getParent() {
        return parent;
    }

    public void setParent(SysOffice parent) {
        this.parent = parent;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
