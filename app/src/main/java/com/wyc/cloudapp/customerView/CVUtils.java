package com.wyc.cloudapp.customerView;

import com.alibaba.fastjson.JSON;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.customerView.bean.CVSetting;
import com.wyc.cloudapp.customerView.bean.CW_CV;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.customerView
 * @ClassName: CVUtils
 * @Description: 客显工具
 * @Author: wyc
 * @CreateDate: 2022/4/19 10:43
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/4/19 10:43
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class CVUtils {

    private volatile static ICustomerView sCv = null;

    public static ICustomerView getInstance(){
        if (sCv == null){
            synchronized (CVUtils.class){
                if (sCv == null){
                    final CVSetting setting = CVSetting.getInstance();
                    if (setting.hasSetting()){
                        try {
                            final Class<?> cls = Class.forName("com.wyc.cloudapp.customerView.bean." + setting.getCsl());
                            sCv = (ICustomerView) cls.getConstructor(String.class,Integer.class).newInstance(setting.getPort(),setting.getBoundRate());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.not_support_hint,setting.getName()));
                        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                            e.printStackTrace();
                            MyDialog.toastMessage(R.string.init_cv_error);
                        }
                    }
                }
            }
        }
        return sCv;
    }

    public static void close(){
        if (sCv != null){
            synchronized (CVUtils.class){
                if (sCv != null){
                    sCv.clear();
                    sCv = null;
                }
            }
        }
    }

    public static List<TreeListItem> support(){
        final List<TreeListItem> data = new ArrayList<>();

        TreeListItem item = new TreeListItem();
        item.setItem_id(CW_CV.class.getSimpleName());
        item.setItem_name("CW顾显");

        data.add(item);
        return data;
    }
}
