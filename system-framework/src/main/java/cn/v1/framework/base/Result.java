package cn.v1.framework.base;

import java.io.Serializable;

/**
 * @Auther: wr
 * @Date: 2018/12/3
 * @Description:
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 4091343026133736631L;

    //成功状态码
    public static final Integer SUCCESS = 0;
    //失败状态码
    public static final Integer FAIL = 1;

    public Result(){
        this.status = FAIL;
        this.message = "";
    }

    private Integer status;  //0：成功、1失败

    private String message;

    private T data;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /** 成功*/
    public Result<T> success(){
        setStatus(SUCCESS);
        return this;
    }
    /** 失败*/
    public Result<T> fail(){
        setStatus(FAIL);
        return this;
    }

    public Result<T> message(String message){
        setMessage(message);
        return this;
    }

    public Result<T> data(T data){
        setData(data);
        return this;
    }
}
