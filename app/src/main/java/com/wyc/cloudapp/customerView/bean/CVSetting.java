package com.wyc.cloudapp.customerView.bean;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.fragment.PeripheralSetting;

import java.io.Serializable;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.customerView
 * @ClassName: CVSetting
 * @Description: 顾显参数对象
 * @Author: wyc
 * @CreateDate: 2022/4/19 10:45
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/4/19 10:45
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class CVSetting {
    public static final String KEY = "CV";

    private String csl = PeripheralSetting.NONE;
    private String port = PeripheralSetting.NONE;
    private int boundRate = 2400;
    private String name = "";

    public String getCsl() {
        return csl;
    }

    public void setCsl(String csl) {
        this.csl = csl;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getBoundRate() {
        return boundRate;
    }

    public void setBoundRate(int boundRate) {
        this.boundRate = boundRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasSetting(){
        return !PeripheralSetting.NONE.equals(csl) && !PeripheralSetting.NONE.equals(port);
    }

    public static CVSetting getInstance(){
        JSONObject object = new JSONObject();
        if (SQLiteHelper.getLocalParameter(CVSetting.KEY, object)){
            return object.toJavaObject(CVSetting.class);
        }
        MyDialog.toastMessage("加载顾显参数错误：" + object.getString("info"));
        return new CVSetting();
    }
}
