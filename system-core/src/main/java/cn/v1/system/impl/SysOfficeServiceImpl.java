package cn.v1.system.impl;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.framework.page.PageList;
import cn.v1.system.dao.SysOfficeMapper;
import cn.v1.system.pojo.SysOffice;
import cn.v1.system.service.SysOfficeService;
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
public class SysOfficeServiceImpl implements SysOfficeService {

    @Autowired
    private SysOfficeMapper mapper;

    @Transactional(readOnly = true)
    public SysOffice getById(String id) {
        return mapper.findById(id);
    }

    public void save(SysOffice sysOffice) {
        if(sysOffice.isNewRecord()){
            sysOffice.preInsert();
            mapper.insert(sysOffice);
        }else {
            sysOffice.preUpdate();
            mapper.update(sysOffice);
        }
    }

    public void delete(SysOffice sysOffice) {
        mapper.delete(sysOffice);
    }

    public Page<SysOffice> getPage(SysOffice sysOffice, PageBounds rowBounds) {
        PageList<SysOffice> list = mapper.findPage(sysOffice, rowBounds);
        return new Page<SysOffice>(list,list.getPagination());
    }

    public List<SysOffice> getList() {
        return getList(new SysOffice());
    }

    @Transactional(readOnly = true)
    public List<SysOffice> getList(SysOffice sysOffice) {
        return mapper.findList(sysOffice);
    }
}
