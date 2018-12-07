package cn.v1.framework.base;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
public interface BaseService<T> {

    T getById(String id);

    void save(T t);

    void delete(T t);

    Page<T> getPage(T t, PageBounds rowBounds);

    List<T> getList();

    List<T> getList(T t);
}
