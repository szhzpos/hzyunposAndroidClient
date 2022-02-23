package com.wyc.cloudapp.utils.http.callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: Result
 * @Description: http请求返回数据的基类
 * @Author: wyc
 * @CreateDate: 2021-06-23 14:40
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-23 14:40
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class Result implements Serializable {
    private String info;
    private String status;

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
    public boolean isSuccess(){
        return "y".equals(status);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull
    @Override
    public String toString() {
        return "Result{" +
                "info='" + info + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
