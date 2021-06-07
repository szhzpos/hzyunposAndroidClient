package com.wyc.cloudapp.adapter;

import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

public abstract class AbstractChargeOrderAdapter<T extends AbstractTableDataAdapter.SuperViewHolder> extends AbstractQueryDataAdapter<T> {
    public AbstractChargeOrderAdapter(MainActivity activity) {
        super(activity);
    }

    @Override
    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT \n" +
                "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                "       case transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "       status ,\n" +
                "       case status when 1 then '未付款' when '2' then '已付款' when '3' then '已完成' when '4' then '已关闭' when '5' then '待退款' when '6' then '已退款' else '其他' end status_name,\n" +
                "       b.cas_name,\n" +
                "       a.order_type,\n" +
                "       name,\n" +
                "       mobile,\n" +
                "       card_code,\n" +
                "       order_money order_amt,\n" +
                "       give_money give_amt,\n" +
                "       order_code,\n" +
                "       origin_order_code,\n" +
                "       ifnull(c.sc_name,'') sc_name\n" +
                "  FROM member_order_info a left join cashier_info b on a.cashier_id = b.cas_id left join sales_info c on a.sc_id = c.sc_id " + where_sql;

        Logger.d("sql:%s",sql);
        mData = SQLiteHelper.getListToJson(sql,err);
        if (mData != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载充值订单错误：" + err,mContext,null);
    }
}
