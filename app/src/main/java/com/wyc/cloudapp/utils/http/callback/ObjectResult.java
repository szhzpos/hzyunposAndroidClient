package com.wyc.cloudapp.utils.http.callback;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: ObjectResult
 * @Description: 对象数据
 * @Author: wyc
 * @CreateDate: 2021-06-23 14:47
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-23 14:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ObjectResult<T> extends Result {
    private T data;

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
