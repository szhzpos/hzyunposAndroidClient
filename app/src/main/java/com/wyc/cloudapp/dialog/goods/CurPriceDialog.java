package com.wyc.cloudapp.dialog.goods;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.customizationView.KeyboardView;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.dialog.serialScales.AbstractWeightedScaleImp;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.goods
 * @ClassName: CurPriceDialog
 * @Description: 无码、时价商品调整窗口
 * @Author: wyc
 * @CreateDate: 2021-12-14 16:54
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-14 16:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CurPriceDialog extends AbstractDialogSaleActivity {
    private OnYesOnclickListener mOnYesClick;
    private TextView mAmtTv,mNumTv;
    private double mNum = 1.0,mPrice;
    private final String mGoodsName;
    private AbstractWeightedScaleImp mSerialScale;

    public CurPriceDialog(@NonNull SaleActivity context,final String name,int type) {
        super(context,type == 0 ? context.getString(R.string.no_barcode_goods) : context.getString(R.string.cur_price_goods));
        mGoodsName = name;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAmtTv = findViewById(R.id.w_amt);
        initPrice();
        initNum();
        initName();
        initKeyboardView();
        initGetWeigh();
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        stopRead();
    }

    private void initGetWeigh(){
        if (AbstractWeightedScaleImp.hasSettingSerialPortScale(null)){
            final Button btn = findViewById(R.id.weight_btn);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(v -> read());
        }
    }

    private void initName(){
        final TextView name = findViewById(R.id._name);
        name.setText(mGoodsName);
    }
    private void initPrice(){
        final EditText price = findViewById(R.id.price);
        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double v = 0.0;
                try {
                    if (s.length() != 0)
                        v = Double.parseDouble(s.toString());

                }catch (NumberFormatException e){
                    e.printStackTrace();
                    v = 0.0;
                }
                mPrice = v;
                mAmtTv.setText(String.format(Locale.CHINA,"%.2f",v * mNum));
            }
        });
    }

    private void initNum(){
        mNumTv = findViewById(R.id.new_num);
        mNumTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double v = 0.0;
                try {
                    if (s.length() != 0)
                        v = Double.parseDouble(s.toString());

                }catch (NumberFormatException e){
                    e.printStackTrace();
                    v = 0.0;
                }
                mNum = v;
                mAmtTv.setText(String.format(Locale.CHINA,"%.2f",v * mPrice));
            }
        });
    }

    private void initKeyboardView(){
        final KeyboardView view = findViewById(R.id.keyboard_view);
        view.layout(R.layout.change_price_keyboard_layout);
        view.setCurrentFocusListener(() -> {
            final View focus = getCurrentFocus();
            if (focus instanceof EditText){
                return (EditText) focus;
            }
            return null;
        });
        view.setCancelListener(v -> closeWindow());
        view.setOkListener(v -> {
            if (mOnYesClick != null){
                mOnYesClick.onYesClick(mNum,mPrice);
            }else setCodeAndExit(1);
        });
    }

    public void read(){
        if (AbstractWeightedScaleImp.hasAutoGetWeigh()){
            mNumTv.setText(String.format(Locale.CHINA,"%.3f",mContext.getWeigh()));
        }else
            if (mSerialScale == null){
                final JSONObject object = new JSONObject();
                int code = AbstractWeightedScaleImp.readWeight(object);
                if (code >= 0){
                    mSerialScale = (AbstractWeightedScaleImp) object.get("info");
                    if (mSerialScale != null){
                        mSerialScale.setOnReadListener(new AbstractWeightedScaleImp.OnReadStatus() {
                            @Override
                            public void onFinish(int stat,double num) {
                                if (null != mNumTv)CustomApplication.runInMainThread(()-> mNumTv.setText(String.format(Locale.CHINA,"%.3f",num)));
                            }
                            @Override
                            public void onError(String err) {
                                mContext.runOnUiThread(()-> MyDialog.ToastMessage("读串口错误：" + err, getWindow()));
                            }
                        }).startRead();
                    }
                }else{
                    MyDialog.ToastMessage("读串口错误：" + Utils.getNullStringAsEmpty(object,"info"), getWindow());
                }
            }
    }

    private void stopRead(){
        if (mSerialScale != null){
            mSerialScale.stopRead();
            mSerialScale = null;
        }
    }

    public final double getPrice(){
        return mPrice;
    }
    public final double getNum(){
        return mNum;
    }


    public interface OnYesOnclickListener {
        void onYesClick(double num,double price);
    }
    public final void setOnYesOnclickListener(OnYesOnclickListener listener){
        mOnYesClick = listener;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.cur_price_dialog;
    }
}
