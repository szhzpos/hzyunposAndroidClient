package com.wyc.cloudapp.bean;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: DeliveryData
 * @Description: 商城配送数据
 * @Author: wyc
 * @CreateDate: 2022/6/14 17:52
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/6/14 17:52
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public final class DeliveryData {
    private List<DeliveryOrderInfo> order_info;

    public List<DeliveryOrderInfo> getOrder_info() {
        return order_info;
    }

    public void setOrder_info(List<DeliveryOrderInfo> order_info) {
        this.order_info = order_info;
    }
}
