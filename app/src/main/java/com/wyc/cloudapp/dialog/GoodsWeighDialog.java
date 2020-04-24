package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.serialScales.AbstractSerialScale;
import com.wyc.cloudapp.dialog.serialScales.ISerialScale;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android_serialport_api.SerialPort;

public class GoodsWeighDialog extends Dialog {
    private Context mContext;
    private String mBarcodeId;
    private OnYesOnclickListener mOnYesClick;
    private EditText mWvalueEt;
    private TextView mPriceTv,mAmtTv;
    private AbstractSerialScale mSerialScale;
    public GoodsWeighDialog(@NonNull Context context,final String barcode_id) {
        super(context);
        mContext = context;
        mBarcodeId = barcode_id;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.goods_weigh_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mPriceTv = findViewById(R.id.w_g_price);
        mAmtTv = findViewById(R.id.w_amt);

        //初始化重量
        initWeight();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v->GoodsWeighDialog.this.dismiss());

        //初始化商品信息
        initGoodsInfo();

        //初始化数字键盘
        initKeyboard();

    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        read();
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        if (mSerialScale != null)mSerialScale.stopRead();
    }
    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.w_value) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                Editable editable = tmp_edit.getText();
                String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    };

    private void initWeight(){
        mWvalueEt = findViewById(R.id.w_value);
        mWvalueEt.setSelectAllOnFocus(true);
        mWvalueEt.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        mWvalueEt.postDelayed(()->{
            mWvalueEt.requestFocus();
        },300);
        mWvalueEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double v = Double.valueOf(s.toString()),price =Double.valueOf(mPriceTv.getText().toString());
                    mAmtTv.setText(String.format(Locale.CHINA,"%.2f",v * price));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    MyDialog.ToastMessage(e.getMessage(),mContext,getWindow());
                }
            }
        });
    }
    private void initKeyboard(){
        ConstraintLayout keyboard_layout;
        keyboard_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button){
                switch (id) {
                    case R.id._back:
                        findViewById(id).setOnClickListener(v -> {
                            View view =  getCurrentFocus();
                            if (view != null) {
                                if (view.getId() == R.id.w_value) {
                                    EditText tmp_edit = ((EditText)view);
                                    int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                                    if (index != end && end  == tmp_edit.getText().length()){
                                        tmp_edit.setText(mContext.getString(R.string.space_sz));
                                    }else{
                                        if (index == 0)return;
                                        tmp_edit.getText().delete(index - 1, index);
                                    }
                                }
                            }
                        });
                        break;
                    case R.id.cancel:
                        findViewById(id).setOnClickListener(v -> {
                            GoodsWeighDialog.this.dismiss();
                        });
                        break;
                    case R.id._ok:
                        findViewById(id).setOnClickListener(v -> {
                            if (mOnYesClick != null){
                                mOnYesClick.onYesClick(GoodsWeighDialog.this);
                            }
                        });
                        break;
                    default:
                        tmp_v.setOnClickListener(button_click);
                        break;
                }
            }
        }
    }

    private void initGoodsInfo(){
        JSONObject object = new JSONObject();
        boolean code = SQLiteHelper.execSql(object,"select ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,retail_price price,ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and barcode_status = '1' and barcode_id = '" + mBarcodeId +"'" +
                " UNION select ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name,gp_price price,ifnull(img_url,'') img_url from goods_group where status = '1' and gp_id = '" + mBarcodeId +"'");
        if (code){
            if (!object.isEmpty()){
                final TextView name = findViewById(R.id.w_g_name),unit = findViewById(R.id.unit_name);
                final ImageView imageView = findViewById(R.id.w_g_img);
                final String img_url = Utils.getNullStringAsEmpty(object,"img_url");
                if (!"".equals(img_url)){
                    final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                    final Bitmap bitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                    imageView.setImageBitmap(bitmap);
                    name.setText(Utils.getNullStringAsEmpty(object,"goods_title"));
                    mPriceTv.setText(Utils.getNullOrEmptyStringAsDefault(object,"price","0.0"));
                    unit.setText("/".concat(Utils.getNullStringAsEmpty(object,"unit_name")));
                }else{
                    imageView.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                }
            }
        }else{
            MyDialog.ToastMessage("初始化商品错误：" + object.getString("info"),mContext,getWindow());
        }
    }

    private void read(){
        StringBuilder err = new StringBuilder();
        if (null != (mSerialScale = AbstractSerialScale.readWeight(err))){
            mSerialScale.setOnReadFinish(new AbstractSerialScale.OnReadStatus() {
                @Override
                public void onFinish(double num) {
                    mWvalueEt.post(()->{
                        mWvalueEt.setText(String.format(Locale.CHINA,"%.3f",num));
                    });
                }
                @Override
                public void onError(String err) {
                    mWvalueEt.post(()-> MyDialog.ToastMessage("读串口错误：" + err,mContext,getWindow()));
                }
            });
        }else{
            MyDialog.ToastMessage("读串口错误：" + err,mContext,getWindow());
        }
    }

    public double getContent(){
        double v = 0.0;
        try {
            v = Double.valueOf(mWvalueEt.getText().toString());
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return v;
    }

    public interface OnYesOnclickListener {
        void onYesClick(GoodsWeighDialog myDialog);
    }
    public void setOnYesOnclickListener(OnYesOnclickListener listener){
        mOnYesClick = listener;
    }

}