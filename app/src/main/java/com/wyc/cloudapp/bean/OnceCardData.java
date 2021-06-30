package com.wyc.cloudapp.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: OnceCardData
 * @Description: 次卡数据
 * @Author: wyc
 * @CreateDate: 2021-06-29 18:14
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-29 18:14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class OnceCardData implements Serializable {
    List<OnceCardInfo> card;

    public void setCard(List<OnceCardInfo> card) {
        this.card = card;
    }

    public List<OnceCardInfo> getCard() {
        return card;
    }
}
