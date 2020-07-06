package com.wyc.cloudapp.dialog.serialScales;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomizationView.KeyboardView;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnMainActivityImp;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class GoodsWeighDialog extends AbstractDialogBaseOnMainActivityImp {
    private String mBarcodeId;
    private OnYesOnclickListener mOnYesClick;
    private EditText mWvalueEt;
    private TextView mPriceTv,mAmtTv;
    private AbstractSerialScaleImp mSerialScale;
    public GoodsWeighDialog(@NonNull MainActivity context, final String title, final String barcode_id) {
        super(context,title);
        mBarcodeId = barcode_id;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPriceTv = findViewById(R.id.w_g_price);
        mAmtTv = findViewById(R.id.w_amt);

        //初始化重量
        initWvalueEt();

        //初始化商品信息
        initGoodsInfo();

        //初始化数字键盘
        initKeyboardView();

    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.goods_weigh_dialog_layout;
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

    private void initWvalueEt(){
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
                double v = 0.0,price = 0.0;
                try {
                    if (s.length() != 0)
                        v = Double.valueOf(s.toString());

                    price =Double.valueOf(mPriceTv.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    v = 0.0;
                    price = 0.0;
                }
                mAmtTv.setText(String.format(Locale.CHINA,"%.2f",v * price));
            }
        });
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
            if (mOnYesClick != null){
                mOnYesClick.onYesClick(GoodsWeighDialog.this);
            }
        });
    }
    private void initGoodsInfo(){
        final JSONObject object = new JSONObject();
        boolean code = SQLiteHelper.execSql(object,"select ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,retail_price price,ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and barcode_status = '1' and barcode_id = '" + mBarcodeId +"'" +
                " UNION select ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name,gp_price price,ifnull(img_url,'') img_url from goods_group where status = '1' and gp_id = '" + mBarcodeId +"'");
        if (code){
            if (!object.isEmpty()){
                final TextView name = findViewById(R.id.w_g_name),unit = findViewById(R.id.unit_name);
                name.setText(Utils.getNullStringAsEmpty(object,"goods_title"));
                mPriceTv.setText(Utils.getNullOrEmptyStringAsDefault(object,"price","0.0"));
                unit.setText("/".concat(Utils.getNullStringAsEmpty(object,"unit_name")));

                final String img_url = Utils.getNullStringAsEmpty(object,"img_url");
                final ImageView imageView = findViewById(R.id.w_g_img);
                if (!"".equals(img_url)){
                    final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                    final Bitmap bitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                    imageView.setImageBitmap(bitmap);
                }else{
                    imageView.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                }
                mWvalueEt.setText(String.format(Locale.CHINA,"%.2f",1.0));
            }
        }else{
            MyDialog.ToastMessage("初始化商品错误：" + object.getString("info"),mContext,getWindow());
        }
    }

    private void read(){
        final JSONObject object = new JSONObject();
        int code = AbstractSerialScaleImp.readWeight(object);
        if (code >= 0){
            if (code == 0){
                mSerialScale = (AbstractSerialScaleImp) object.get("info");
                if (mSerialScale != null){
                    mSerialScale.setOnReadListener(new AbstractSerialScaleImp.OnReadStatus() {
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
                    }).startRead();
                }
            }
        }else{
            MyDialog.ToastMessage("读串口错误：" + Utils.getNullStringAsEmpty(object,"info"),mContext,getWindow());
        }
    }

    public final double getContent(){
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
    public final void setOnYesOnclickListener(OnYesOnclickListener listener){
        mOnYesClick = listener;
    }

}
