package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile.business
 * @ClassName: MobileQuerySourceOrderActivity
 * @Description: 业务单据需要来源单号的Activity的抽象父类
 * @Author: wyc
 * @CreateDate: 2021/4/12 10:39
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/12 10:39
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractMobileQuerySourceOrderActivity extends AbstractMobileAddOrderActivity {
    public static final int SELECT_ORDER_CODE = 12;
    private TextView mSourceOrderCodeTv;
    @Override
    protected void initView() {
        super.initView();
        initSourceOrder();
    }

    protected String getSourceOrder(){
        return mSourceOrderCodeTv != null ? mSourceOrderCodeTv.getText().toString() : "";
    }

    protected void setSourceOrder(final String _id,final String code){
        setView(mSourceOrderCodeTv,_id,code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK ){
            if (requestCode == SELECT_ORDER_CODE){
                querySourceOrderInfo(data.getStringExtra("order_id"));
            }
        }
    }

    @Override
    protected boolean hasSource() {
        return null != mSourceOrderCodeTv && !mSourceOrderCodeTv.getText().toString().isEmpty();
    }

    private void initSourceOrder(){
        mSourceOrderCodeTv = findViewById(R.id.m_source_order_tv);
        if (null != mSourceOrderCodeTv)
            mSourceOrderCodeTv.setOnClickListener(v -> {
                if (!isDetailsEmpty()){
                    if (MyDialog.showMessageToModalDialog(this,"已存在商品明细，是否替换？") == 0){
                        return;
                    }
                }
                startActivityForResult(launchSourceActivity(),SELECT_ORDER_CODE);
            });
    }

    protected void setWarehouse(final JSONObject order){
        final JSONObject object = new JSONObject();
        if (SQLiteHelper.execSql(object,String.format(Locale.CHINA,"SELECT stores_name,stores_id,wh_id FROM shop_stores where wh_id = '%s'", Utils.getNullStringAsEmpty(order,"wh_id")))){
            if (object.isEmpty()){
                MyDialog.toastMessage("仓库对应门店信息不存在!");
            }else{
                setView(mWarehouseTv,Utils.getNullStringAsEmpty(object,"stores_id"),Utils.getNullStringAsEmpty(object,"stores_name"));
            }
        }else {
            MyDialog.toastMessage("查询门店信息错误:" + object.getString("info"));
        }
    }

    protected abstract void querySourceOrderInfo(final String order_id);

    @CallSuper
    protected Intent launchSourceActivity(){
        final Intent intent = new Intent();
        intent.putExtra("FindSource",true);
        return intent;
    }
}
