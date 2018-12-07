package cn.v1.framework.jdbc.dialect;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 类似hibernate的Dialect,但只精简出分页部分
 *
 * @author up72
 */
public class Dialect {

    // 解决mybatis 缓存分页
    protected List<PageParameter> pageParameter = Lists.newArrayList();


    public boolean supportsLimit() {
        return false;
    }

    public boolean supportsLimitOffset() {
        return supportsLimit();
    }

    /**
     * 将sql变成分页sql语句,直接使用offset,limit的值作为占位符.</br>
     * 源代码为: getLimitString(sql,offset,String.valueOf(offset),limit,String.valueOf(limit))
     */
    public String getLimitString(String sql, int offset, int limit) {
        return getLimitString(sql, offset, String.valueOf(offset), limit, String.valueOf(limit));
    }

    /**
     * 将sql变成分页sql语句,提供将offset及limit使用占位符(placeholder)替换.
     * <pre>
     * 如mysql
     * dialect.getLimitString("select * from user", 12, ":offset",0,":limit") 将返回
     * select * from user limit :offset,:limit
     * </pre>
     *
     * @return 包含占位符的分页sql
     */
    public String getLimitString(String sql, int offset, String offsetPlaceholder, int limit, String limitPlaceholder) {
       return getLimitString(sql,  offset,  offsetPlaceholder,  limit,  limitPlaceholder,  false);
    }

    public String getLimitString(String sql, int offset, String offsetPlaceholder, int limit, String limitPlaceholder, boolean isPageParameter) {
        throw new UnsupportedOperationException("paged queries not supported");
    }


    public List<PageParameter> getPageParameter() {
        return pageParameter;
    }

    protected void setPageParameter(String name, Object value){
        pageParameter.add(new PageParameter(name, value));
    }

    public class PageParameter{
        private String name;
        private Object value;

        public PageParameter(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }
}
