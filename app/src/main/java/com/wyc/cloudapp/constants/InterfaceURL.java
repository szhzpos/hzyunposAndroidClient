package com.wyc.cloudapp.constants;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.constants
 * @ClassName: URL
 * @Description: 服务器接口地址
 * @Author: wyc
 * @CreateDate: 2021-06-23 14:29
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-23 14:29
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class InterfaceURL {
    public static final String VIP_GRADE = "/api/member/get_member_grade";
    public static final String ONCE_CARD = "/api/once_cards/index";
    public static final String ONCE_CARD_UPLOAD = "/api/once_cards/place_order";
    public static final String ONCE_CARD_PAY = "/api/once_cards/pay";
    public static final String UNIFIED_PAY = "/api/pay2/index";
    public static final String UNIFIED_PAY_QUERY = "/api/pay2_query/query";
    public static final String VIP_TIME_CARD = "/api/once_cards/member_card";
    public static final String VIP_TIME_CARD_USE = "/api/once_cards/card_use";
    public static final String GIFT_CARD_INFO = "/api/api_shopping/get_shopping_list";
    public static final String GIFT_CARD_ORDER_UPLOAD = "/api/api_shopping/mk_shopping_order";
    public static final String ENQUIRY_ORDER_DETAIL = "/api/api_yaohuo/xinfo";
    public static final String O_OUT_IN_UPLOAD = "/api/bgd/add";
    public static final String OUT_IN_SH= "/api/bgd/sh";
    public static final String COUPON_CHECK = "/api/coupon_check/details";
    public static final String COUPON_VERIFY = "/api/coupon_check/verify";
}
