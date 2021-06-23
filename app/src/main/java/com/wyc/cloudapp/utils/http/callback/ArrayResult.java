package com.wyc.cloudapp.utils.http.callback;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: ArrayResult
 * @Description: 数组数据
 * @Author: wyc
 * @CreateDate: 2021-06-23 14:49
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-23 14:49
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ArrayResult<T> extends Result {
    private List<T> data;

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }
}
