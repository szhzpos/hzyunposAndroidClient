package com.wyc.cloudapp.dialog.vip;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractOrderDetailsDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

public abstract class AbstractChargeOrderDetailsDialog extends AbstractOrderDetailsDialog {
    protected AbstractTableDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mChargeDetailsPayInfoAdapter;
    public AbstractChargeOrderDetailsDialog(@NonNull MainActivity context, final CharSequence title, final JSONObject info) {
        super(context,title,info);
    }


    @Override
    public void dismiss(){
        super.dismiss();
        Printer.dismissPrintIcon(mContext);
    }

    @Override
    public void show(){
        super.show();
        Printer.showPrintIcon(mContext);
    }

    @Override
    protected void initGoodsDetail() {

    }

    protected void verify_pay(){
        if (null != mPayRecord){
            if (mPayRecord.getIntValue("status") == 1){
                final CustomProgressDialog mProgressDialog = new CustomProgressDialog(mContext);
                mProgressDialog.setCancel(false).setMessage("正在查询支付结果...").refreshMessage().show();
                CustomApplication.execute(()->{
                    try {
                        if (mPayRecord != null){
                            final StringBuilder err = new StringBuilder();
                            Logger.d(mPayRecord);
                            disposeChargeById(mPayRecord,err);
                            mContext.runOnUiThread(()-> {
                                if (mChargeDetailsPayInfoAdapter != null)mChargeDetailsPayInfoAdapter.notifyDataSetChanged();
                                MyDialog.ToastMessage(err.toString(), getWindow());
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        mContext.runOnUiThread(()-> MyDialog.ToastMessage(e.getMessage(), getWindow()));
                    }
                    mContext.runOnUiThread(mProgressDialog::dismiss);
                });
            }else{
                MyDialog.ToastMessage(mPayRecord.getString("status_name"), getWindow());
            }
        }else {
            MyDialog.ToastMessage("请选择验证记录！", getWindow());
        }
    }

    protected boolean disposeChargeById(@NonNull final JSONObject object,@NonNull final StringBuilder err){
        boolean code = true;

        final String url = mContext.getUrl(),appId = mContext.getAppId(),appSecret = mContext.getAppSecret(),stores_id = mContext.getStoreId(),
                pay_method_id = object.getString("pay_method_id"),
                order_code = object.getString("order_code");

        final HttpRequest httpRequest = new HttpRequest();
        final ContentValues values = new ContentValues();

        final JSONObject data_ = new JSONObject();
        data_.put("appid",appId);
        data_.put("order_code",order_code);
        if (Utils.getNotKeyAsNumberDefault(object,"is_check",-1) == 2)
            data_.put("case_pay_money",Utils.getNotKeyAsNumberDefault(object,"pay_money",0.0));

        data_.put("pay_method",pay_method_id);

        Logger.d(data_);

        final String sz_param = HttpRequest.generate_request_parma(data_,appSecret);
        final JSONObject retJson = httpRequest.sendPost(url + "/api/member/cl_money_order",sz_param,true);

        switch (retJson.getIntValue("flag")) {
            case 0:
                err.append(retJson.getString("info"));
                code = false;
                break;
            case 1:
                final JSONObject info_json = JSON.parseObject(retJson.getString("info"));
                switch (info_json.getString("status")){
                    case "n":
                        err.append(info_json.getString("info"));
                        code = false;
                        break;
                    case "y":
                        Logger.d_json(info_json.toJSONString());
                        final JSONArray members = JSON.parseArray(info_json.getString("member")),money_orders = JSON.parseArray(info_json.getString("money_order"));
                        final JSONObject member = members.getJSONObject(0),pay_info = money_orders.getJSONObject(0);

                        if (pay_info != null && member != null){
                            values.clear();
                            values.put("status",3);//已完成
                            values.put("xnote",info_json.toJSONString());
                            values.put("give_money",pay_info.getDoubleValue("give_money"));

                            code = SQLiteHelper.execUpdateSql("member_order_info",values,"member_id = ? and third_order_id = ?",
                                    new String[]{object.getString("member_id"),object.getString("order_code_son")},err) < 0;
                            if (code){
                                err.append(info_json.getString("info"));
                            }
                        }else {
                            Logger.e("服务器返回member：%s,money_order：%s",members,money_orders);
                            err.append("服务器返回信息为空！");
                            code = false;
                        }
                        break;
                }
                break;
        }
        return code;
    }
}
