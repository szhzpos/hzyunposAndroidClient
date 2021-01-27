package com.wyc.cloudapp.dialog.goods;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.CustomizationView.KeyboardView;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;


public final class GoodsPriceAdjustDialog extends AbstractDialogSaleActivity {
    private JSONObject mGoods;
    private EditText mNewRetailPriceEt,mNewVipPriceEt;
    public GoodsPriceAdjustDialog(@NonNull SaleActivity context, final JSONObject object) {
        super(context, context.getText(R.string.price_adjust_sz));
        mGoods = object;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initKeyboardView();
        mNewVipPriceEt = findViewById(R.id.new_vip_price_et);
        mNewRetailPriceEt = findViewById(R.id.new_retail_price_et);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.goods_price_adjust_dialog_layout;
    }
    @Override
    public void show(){
        super.show();
        showGoodsInfo();
    }

    private void initKeyboardView(){
        final KeyboardView view = findViewById(R.id.keyboard_view);
        view.layout(R.layout.change_price_keyboard_layout);
        view.setCurrentFocusListenner(() -> {
            final View focus = getCurrentFocus();
            if (focus instanceof EditText){
                return (EditText) focus;
            }
            return null;
        });
        view.setCancelListener(v -> closeWindow());
        view.setOkListener(v -> {
            if (mGoods != null)adjustRequest();
        });
    }

    private void adjustRequest(){
        final String retail_price = mNewRetailPriceEt.getText().toString(),vip_price = mNewVipPriceEt.getText().toString();
        if (retail_price.isEmpty()){
            mNewRetailPriceEt.requestFocus();
            MyDialog.ToastMessage(mNewRetailPriceEt,mContext.getString(R.string.not_empty_hint_sz,mNewRetailPriceEt.getHint()),mContext,getWindow());
            return;
        }

        final JSONObject object = new JSONObject(),goods = new JSONObject();
        if (!SQLiteHelper.execSql(object,"SELECT pt_user_id FROM cashier_info where cas_code = '"+ mContext.getCashierCode() +"'")){
            MyDialog.ToastMessage(object.getString("info"),mContext,getWindow());
            return;
        }
        if ("".equals(Utils.getNullStringAsEmpty(object,"pt_user_id"))){
            MyDialog.ToastMessage(mContext.getString(R.string.not_empty_hint_sz,"制单人"),mContext,getWindow());
            return;
        }
        final JSONArray array = new JSONArray();
        final JEventLoop loop = new JEventLoop();
        final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
        progressDialog.setMessage("正在更新商品信息...").show();
        CustomApplication.execute(()->{
            try {
                final HttpRequest httpRequest = new HttpRequest();
                object.put("appid",mContext.getAppId());

                goods.put("barcode_id",mGoods.getString("barcode_id"));
                goods.put("retail_price",retail_price);
                if (!vip_price.isEmpty())goods.put("yh_price",vip_price);

                array.add(goods);
                object.put("goods",array);

                final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/goods_set/goods_adjust_price",HttpRequest.generate_request_parm(object,mContext.getAppSecret()),true);
                switch (retJson.getIntValue("flag")){
                    case 0:
                        loop.done(0);
                        mContext.runOnUiThread(()->{
                            MyDialog.displayErrorMessage(mContext, retJson.getString("info"));
                        });
                        break;
                    case 1:
                        final JSONObject info = JSON.parseObject(retJson.getString("info"));
                        if ("y".equals(info.getString("status"))){
                            goods.put("price",retail_price);
                            loop.done(1);
                        }else {
                            loop.done(0);
                            mContext.runOnUiThread(()->{
                                MyDialog.displayErrorMessage(mContext, info.getString("info"));
                            });
                        }
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        });
        int code = loop.exec();
        progressDialog.dismiss();
        if (code == 1){
            mContext.adjustPriceRefreshGoodsInfo(array);
            closeWindow();
        }
    }

    private void showGoodsInfo(){
        if (mGoods != null){
            final TextView g_name_tv = findViewById(R.id.g_name_tv),ori_retail_price_tv = findViewById(R.id.ori_retail_price_tv),ori_vip_price_tv = findViewById(R.id.ori_vip_price_tv);
            g_name_tv.setText(mGoods.getString("goods_title"));
            ori_retail_price_tv.setText(mGoods.getString("price"));
            ori_vip_price_tv.setText(mGoods.getString("yh_price"));
        }
    }
}
