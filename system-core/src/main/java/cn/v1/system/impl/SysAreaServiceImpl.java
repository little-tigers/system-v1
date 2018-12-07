package cn.v1.system.impl;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.framework.page.PageList;
import cn.v1.system.dao.SysAreaMapper;
import cn.v1.system.pojo.SysArea;
import cn.v1.system.service.SysAreaService;
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
public class SysAreaServiceImpl implements SysAreaService {

    @Autowired
    private SysAreaMapper mapper;

    @Transactional(readOnly = true)
    public SysArea getById(String id) {
        return mapper.findById(id);
    }

    public void save(SysArea sysArea) {
        if(sysArea.isNewRecord()){
            sysArea.preInsert();
            mapper.insert(sysArea);
        }else {
            sysArea.preUpdate();
            mapper.update(sysArea);
        }
    }

    public void delete(SysArea sysArea) {
        mapper.delete(sysArea);
    }

    public Page<SysArea> getPage(SysArea sysArea, PageBounds rowBounds) {
        PageList<SysArea> list = mapper.findPage(sysArea, rowBounds);
        return new Page<SysArea>(list,list.getPagination());
    }

    public List<SysArea> getList() {
        return getList(new SysArea());
    }

    @Transactional(readOnly = true)
    public List<SysArea> getList(SysArea sysArea) {
        return mapper.findList(sysArea);
    }
}
