package cn.v1.framework.base;

import cn.v1.framework.excel.annotation.ExcelField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * @Auther: wr
 * @Date: 2018/10/31
 * @Description:
 */
public class BasePoJo<E> implements Serializable {

    private static final long serialVersionUID = 8439206700898849229L;

    public BasePoJo(){
    }

    public BasePoJo(String id){
        this();
        this.id = id;
    }

    /**
     * 删除标记（0：正常；1：删除；2：审核；）
     */
    public static final String DEL_FLAG_NORMAL = "0";

    public static final String DEL_FLAG_DELETE = "1";

    public static final String DEL_FLAG_AUDIT = "2";

    /**
     * 实体编号（唯一标识）
     */
    protected String id;

    protected String createBy;	// 创建者

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelField(title="创建日期", align=2, sort=14)
    protected Date createDate;	// 创建日期

    protected String updateBy;	// 创建者

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date updateDate;	// 创建日期

    @Length(min=0, max=255)
    @ExcelField(title="备注", align=2, sort=19)
    protected String remarks;	// 备注

    @Length(min=1, max=1)
    protected String delFlag; 	// 删除标记（0：正常；1：删除；2：审核）

    protected Date beginTime;  //开始时间

    protected Date endTime; //结束时间

    public boolean isNewRecord() {
        return false || StringUtils.isBlank(getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void preInsert(){
        this.setDelFlag(DEL_FLAG_NORMAL);
        this.updateDate = new Date();
        if(createDate == null) {
            this.createDate = updateDate;
        }
    }

    public void preUpdate(){
        this.setUpdateDate(new Date());
    }
}
