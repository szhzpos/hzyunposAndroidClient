package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
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
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android_serialport_api.SerialPort;

public class GoodsWeighDialog extends Dialog {
    private Context mContext;
    private String mBarcodeId;
    private OnYesOnclickListener mOnYesClick;
    private EditText mWvalueEt;
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

        //初始化重量
        initWeight();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v->GoodsWeighDialog.this.dismiss());

        //初始化商品信息
        initGoodsInfo();

        //初始化数字键盘
        initKeyboard();

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
                final TextView name = findViewById(R.id.w_g_name),price = findViewById(R.id.w_g_price);
                final ImageView imageView = findViewById(R.id.w_g_img);
                final String img_url = Utils.getNullStringAsEmpty(object,"img_url");
                if (!"".equals(img_url)){
                    final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                    final Bitmap bitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                    imageView.setImageBitmap(bitmap);
                    name.setText(Utils.getNullStringAsEmpty(object,"goods_title"));
                    price.setText(Utils.getNullStringAsEmpty(object,"price").concat("/").concat(Utils.getNullStringAsEmpty(object,"unit_name")));

                    read();
                }else{
                    imageView.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                }
            }else{
                MyDialog.ToastMessage("商品未找到！",mContext,getWindow());
            }
        }else{
            MyDialog.ToastMessage("初始化商品错误：" + object.getString("info"),mContext,getWindow());
        }
    }

    public interface OnYesOnclickListener {
        void onYesClick(GoodsWeighDialog myDialog);
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
    public void setOnYesOnclickListener(OnYesOnclickListener listener){
        mOnYesClick = listener;
    }

    private void read(){
        CustomApplication.execute(()->{
            try {
                // 打开/dev/ttyUSB0路径设备的串口
                SerialPort mSerialPort = new SerialPort(new File("/dev/ttyUSB0"), 9600, 0);
                final InputStream inputStream = mSerialPort.getInputStream();
                byte[] buffer = new byte[1024];
                int size = inputStream.read(buffer);
                byte[] readBytes = new byte[size];
                System.arraycopy(buffer, 0, readBytes, 0, size);

                System.out.println("received data => " + new String(readBytes));

            } catch (IOException e) {
                e.printStackTrace();
                mWvalueEt.post(()->{
                    MyDialog.ToastMessage(e.getMessage(),mContext,null);
                });
            }
        });
    }
}
