package cn.v1.framework.base;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.framework.page.PageList;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
public interface BaseMapper<T> {

    T findById(String id);

    void insert(T t);

    void update(T t);

    PageList<T> findPage(T t, PageBounds rowBounds);

    List<T> findList(T t);

    void delete(T t);
}
