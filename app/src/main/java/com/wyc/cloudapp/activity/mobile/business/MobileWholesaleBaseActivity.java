package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile.business
 * @ClassName: MobileWholesaleBaseActivity
 * @Description: 批发单相关基类
 * @Author: wyc
 * @CreateDate: 2021/4/20 15:02
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/20 15:02
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class MobileWholesaleBaseActivity extends AbstractMobileAddOrderActivity {
    private TextView mBusinessCustomerTv;
    private JSONArray mCustomerList;
    private int mPriceType = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomer();
    }

    protected String getCustomerId(){
        return Utils.getViewTagValue(mBusinessCustomerTv,"-1");
    }

    protected void setCustomer(final String _id,final String code){
        setView(mBusinessCustomerTv,_id,code);
    }

    private void initCustomer(){
        mBusinessCustomerTv = findViewById(R.id.m_business_customer_tv);
        final String sup = getString(R.string.customer_colon_sz);
        mBusinessCustomerTv.setOnClickListener(v -> v.post(()->{
            final TreeListDialog treeListDialog = new TreeListDialog(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setDatas(mCustomerList,null,true);
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                mBusinessCustomerTv.setText(object.getString("item_name"));
                mBusinessCustomerTv.setTag(object.getString("item_id"));
                mPriceType = object.getIntValue("price_type");
            }
        }));
        loadCustomer();
    }

    private void loadCustomer(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("stores_id",getStoreId());
            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(this.getUrl() + "/api/supplier_search/customer_xlist",sz_param,true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                if (HttpUtils.checkBusinessSuccess(info_obj)){
                    final JSONArray data = info_obj.getJSONArray("data");
                    mCustomerList = parse_customer_info_and_set_default(data);
                }else {
                    MyDialog.ToastMessageInMainThread("查询客户信息错误:" + info_obj.getString("info"));
                }
            }
        });
    }

    public int getCustomerPriceType(){
        return mPriceType;
    }

    private JSONArray parse_customer_info_and_set_default(final JSONArray customers){
        final JSONArray array  = new JSONArray();
        if (customers != null){
            JSONObject object;
            for (int i = 0,size = customers.size();i < size;i++){
                final JSONObject tmp = customers.getJSONObject(i);
                final String id = Utils.getNullOrEmptyStringAsDefault(tmp,"c_s_id",""),name = Utils.getNullStringAsEmpty(tmp,"cs_xname");
                int cs_kf_price = tmp.getIntValue("cs_kf_price");

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",id);
                object.put("item_name",name);
                object.put("price_type",cs_kf_price);
                array.add(object);

                //
                if ("0000".equals(Utils.getNullStringAsEmpty(tmp,"cs_code"))  && mBusinessCustomerTv != null){

                    mBusinessCustomerTv.post(()->{
                        mBusinessCustomerTv.setText(name);
                        mBusinessCustomerTv.setTag(id);
                        mPriceType = cs_kf_price;
                    });
                }
            }
        }
        return array;
    }
}
