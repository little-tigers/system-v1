package cn.v1.system.pojo;

import cn.v1.framework.base.BasePoJo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:地区 t_sys_area
 */
public class SysArea extends BasePoJo<SysArea> implements Serializable {

    private static final long serialVersionUID = 4170917889767619186L;

    private String name; //区域名称

    private String code; //区域编码

    private Integer sort; //排序

    private String type; //区域类型（1：国家；2：省份、直辖市；3：地市；4：区县）

    private SysArea parent;  //父级编号

    private String parentIds;  //父级所有编号

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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SysArea getParent() {
        return parent;
    }

    public void setParent(SysArea parent) {
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
