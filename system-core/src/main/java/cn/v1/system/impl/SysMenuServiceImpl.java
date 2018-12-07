package cn.v1.system.impl;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.framework.page.PageList;
import cn.v1.framework.utils.ObjectUtils;
import cn.v1.system.dao.SysMenuMapper;
import cn.v1.system.pojo.SysMenu;
import cn.v1.system.pojo.SysRole;
import cn.v1.system.service.SysMenuService;
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
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    private SysMenuMapper mapper;

    @Transactional(readOnly = true)
    public SysMenu getById(String id) {
        return mapper.findById(id);
    }

    public void save(SysMenu menu) {
        if(menu.isNewRecord()){
            menu.preInsert();
            mapper.insert(menu);
        }else {
            menu.preUpdate();
            mapper.update(menu);
        }
    }

    public void delete(SysMenu menu) {
        mapper.delete(menu);
    }

    public Page<SysMenu> getPage(SysMenu menu, PageBounds rowBounds) {
        PageList<SysMenu> list = mapper.findPage(menu, rowBounds);
        return new Page<SysMenu>(list,list.getPagination());
    }

    public List<SysMenu> getList() {
        return getList(new SysMenu());
    }

    @Transactional(readOnly = true)
    public List<SysMenu> getList(SysMenu menu) {
        return mapper.findList(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysMenu> getByRolesList(List<SysRole> roleList) {
        if(ObjectUtils.isEmpty(roleList)){
            return null;
        }
        return mapper.findByRolesList(roleList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysMenu> getChildrenList(String id) {
        return mapper.findChildrenList(id);
    }
}
