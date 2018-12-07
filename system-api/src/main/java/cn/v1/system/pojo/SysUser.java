package cn.v1.system.pojo;

import cn.v1.framework.base.BasePoJo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description: 系统用户t_sys_user
 */
public class SysUser extends BasePoJo<SysUser> implements Serializable {

    public SysUser(){}

    public SysUser(String id){
        super(id);
    }

    private static final long serialVersionUID = -3645260445876278273L;

    private String loginName;// 登录名

    private String password;// 密码

    private String no;	// 工号

    private String name; // 姓名

    private String phone;	// 手机

    private String tel;	// 电话

    private String email;	// 邮箱

    private String userType;// 用户类型

    private String headImg;	// 头像

    private String loginFlag;	// 是否允许登陆

    private String loginIp;	// 最后登陆IP

    private Date loginTime;	// 最后登陆日期

    private SysOffice company;	// 归属公司

    private SysOffice office;	// 归属部门

    private List<SysRole> roleList;  //拥有角色

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getLoginFlag() {
        return loginFlag;
    }

    public void setLoginFlag(String loginFlag) {
        this.loginFlag = loginFlag;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public SysOffice getCompany() {
        return company;
    }

    public void setCompany(SysOffice company) {
        this.company = company;
    }

    public SysOffice getOffice() {
        return office;
    }

    public void setOffice(SysOffice office) {
        this.office = office;
    }

    public List<SysRole> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<SysRole> roleList) {
        this.roleList = roleList;
    }

    public boolean isAdmin(){
        return this.id != null && "1".equals(this.id);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
