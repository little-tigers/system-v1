package cn.v1.system.impl;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.framework.page.PageList;
import cn.v1.system.dao.SysUserMapper;
import cn.v1.system.pojo.SysUser;
import cn.v1.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
@Service
@Transactional
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper mapper;

    @Transactional(readOnly = true)
    public SysUser getById(String id) {
        return mapper.findById(id);
    }

    public void save(SysUser user) {
        if(user.isNewRecord()){
            user.preInsert();
            mapper.insert(user);
        }else {
            user.preUpdate();
            mapper.update(user);
        }
    }

    public void delete(SysUser SysUser) {
        mapper.delete(SysUser);
    }

    public Page<SysUser> getPage(SysUser SysUser, PageBounds rowBounds) {
        PageList<SysUser> list = mapper.findPage(SysUser, rowBounds);
        return new Page<SysUser>(list,list.getPagination());
    }

    public List<SysUser> getList() {
        return getList(new SysUser());
    }

    @Transactional(readOnly = true)
    public List<SysUser> getList(SysUser user) {
        return mapper.findList(user);
    }


    @Override
    @Transactional(readOnly = true)
    public SysUser getByLoginName(String loginName) {
        return mapper.findByLoginName(loginName);
    }

    @Override
    public void updateUserLoginInfo(SysUser user) {
        // 保存上次登录信息
       /* user.setOldLoginIp(user.getLoginIp());
        user.setOldLoginDate(user.getLoginDate());
        // 更新本次登录信息
        user.setLoginIp(StringUtils.getRemoteAddr());
        user.setLoginDate(new Date());
        mapper.updateLoginInfo(user);*/
    }
}
