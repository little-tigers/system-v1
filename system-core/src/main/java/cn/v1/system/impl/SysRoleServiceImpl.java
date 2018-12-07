package cn.v1.system.impl;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.framework.page.PageList;
import cn.v1.system.dao.SysRoleMapper;
import cn.v1.system.pojo.SysRole;
import cn.v1.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
@Service
@Transactional
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper mapper;

    @Transactional(readOnly = true)
    public SysRole getById(String id) {
        return mapper.findById(id);
    }

    public void save(SysRole sysRole) {
        if(sysRole.isNewRecord()){
            sysRole.preInsert();
            mapper.insert(sysRole);
        }else {
            sysRole.preUpdate();
            mapper.update(sysRole);
        }
    }

    public void delete(SysRole sysRole) {
        mapper.delete(sysRole);
    }

    public Page<SysRole> getPage(SysRole sysRole, PageBounds rowBounds) {
        PageList<SysRole> list = mapper.findPage(sysRole, rowBounds);
        return new Page<SysRole>(list,list.getPagination());
    }

    public List<SysRole> getList() {
        return getList(new SysRole());
    }

    @Transactional(readOnly = true)
    public List<SysRole> getList(SysRole sysRole) {
        return mapper.findList(sysRole);
    }
}
