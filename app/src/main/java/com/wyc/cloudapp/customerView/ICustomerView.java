package com.wyc.cloudapp.customerView;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.customerView
 * @ClassName: ICustomerView
 * @Description: 顾显接口
 * @Author: wyc
 * @CreateDate: 2022/4/19 10:36
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/4/19 10:36
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */

public interface ICustomerView {
    void writPrice(double price);
    void writRealPay(double amt);
    void writChange(double amt);
    void clear();
}
