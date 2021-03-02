package com.wyc.cloudapp.dialog.goods;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.business.AbstractMobileAddOrderActivity;
import com.wyc.cloudapp.activity.mobile.business.SelectGoodsActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.goods
 * @ClassName: BusinessSelectGoodsDialog
 * @Description: 业务单价选择商品对话框
 * @Author: wyc
 * @CreateDate: 2021/2/25 17:10
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/25 17:10
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BusinessSelectGoodsDialog extends AbstractDialogMainActivity implements View.OnClickListener,MainActivity.ScanCallback, View.OnFocusChangeListener {
    public static final int BARCODE_REQUEST_CODE = 0x000000dd;
    private JSONObject mContentObj;
    private String mBarcode;
    private EditText mBarcodeEt,mNumEt,mPriceEt;
    private TextView mItemNoTv,mNameTv,mAmtTv,mUnitTv;
    public BusinessSelectGoodsDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.scan_code_label));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext.setScanCallback(this);

        initBtn();
        initSearchContent();
        initView();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.business_select_goods_dialog_layout;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.ok_btn){
            setCodeAndExit(1);
        }else if (id == R.id.cancel_btn){
            setCodeAndExit(0);
        }
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        final int id = v.getId();
        if (hasFocus){
            if (id == R.id.num_tv){
                mNumEt.removeTextChangedListener(mTextWatcherWithNum);
                mNumEt.addTextChangedListener(mTextWatcherWithNum);

                mPriceEt.removeTextChangedListener(mTextWatcherWithPrice);
            }else if (id == R.id.price_tv){
                mPriceEt.removeTextChangedListener(mTextWatcherWithPrice);
                mPriceEt.addTextChangedListener(mTextWatcherWithPrice);

                mNumEt.removeTextChangedListener(mTextWatcherWithNum);
            }
        }else {
            if (id == R.id.num_tv){
                mNumEt.removeTextChangedListener(mTextWatcherWithNum);
            }else if (id == R.id.price_tv){
                mPriceEt.removeTextChangedListener(mTextWatcherWithPrice);
            }
        }
    }
    @Override
    public void callback(String code) {
        if (null != mBarcodeEt)mBarcodeEt.setText(code);
        searchGoods();
    }

    private void initView(){
        mItemNoTv = findViewById(R.id.item_no_tv);
        mNameTv = findViewById(R.id.name_tv);
        mAmtTv = findViewById(R.id.amt_tv);
        mNumEt = findViewById(R.id.num_tv);
        mPriceEt = findViewById(R.id.price_tv);
        mUnitTv = findViewById(R.id.unit_tv);

        mNumEt.setOnFocusChangeListener(this);
        mPriceEt.setOnFocusChangeListener(this);
    }
    private final TextWatcher mTextWatcherWithNum = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0){
                double num = 0.00,price = 0.00;
                try {
                    num = Double.parseDouble(s.toString());
                    price = Double.parseDouble(mPriceEt.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                mAmtTv.setText(String.format(Locale.CHINA,"%.3f",price * num));
            }
        }
    };

    private final TextWatcher mTextWatcherWithPrice = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0){
                double price = Double.parseDouble(s.toString()),num = Double.parseDouble(mNumEt.getText().toString());
                try {
                    price = Double.parseDouble(s.toString());
                    num = Double.parseDouble(mNumEt.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                mAmtTv.setText(String.format(Locale.CHINA,"%.3f",price * num));
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.barcode_tv);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBarcode = s.toString();
            }
        });
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                searchGoods();
                mNumEt.requestFocus();
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    search.requestFocus();
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    mContext.startActivityForResult(intent, BARCODE_REQUEST_CODE);
                }
            }
            return false;
        });
        search.requestFocus();

        mBarcodeEt = search;
    }

    private void searchGoods(){
        final String barcode = mBarcode;
        if (Utils.isNotEmpty(barcode)){
            final StringBuilder err = new StringBuilder();
            final JSONArray barcode_ids = SQLiteHelper.getListToValue("select barcode_id from barcode_info where barcode = '" + barcode + "'",err);
            if (barcode_ids != null){
                int size = barcode_ids.size();
                if (size != 0){
                    if (size > 1){
                        final Intent intent = new Intent(mContext, SelectGoodsActivity.class);
                        intent.putExtra("barcode",barcode);
                        mContext.startActivityForResult(intent,SelectGoodsActivity.SELECT_GOODS_CODE);
                        setCodeAndExit(0);
                    }else {
                        mContentObj = new JSONObject();
                        if (selectGoodsWithBarcodeId(mContentObj,barcode_ids.getString(0))){
                            Logger.d_json(mContentObj.toString());
                            showGoods();
                        }else {
                            MyDialog.ToastMessage(mContentObj.getString("info"),mContext,getWindow());
                            mContentObj = null;
                        }
                    }
                }else {
                    MyDialog.ToastMessage(mBarcodeEt,mContext.getNotExistHintsString(String.format(Locale.CHINA,"商品条码%s",barcode)),mContext,getWindow());
                }
            }else {
                MyDialog.ToastMessage(mBarcodeEt,err.toString(),mContext,getWindow());
            }
        }else {
            MyDialog.ToastMessage(mBarcodeEt,mContext.getNotEmptyHintsString("搜索内容"),mContext,getWindow());
        }
    }

    private void showGoods(){
        final JSONObject object = mContentObj;

        mItemNoTv.setText(object.getString("only_coding"));
        mNameTv.setText(object.getString("goods_title"));
        mNumEt.setText(String.valueOf(1.00));

        mPriceEt.requestFocus();
        mPriceEt.setText(object.getString("buying_price"));

        mUnitTv.setTag(object.getString("stock_unit_id"));
        mUnitTv.setText(object.getString("stock_unit_name"));
    }

    public static boolean selectGoodsWithBarcodeId(final JSONObject object,final String barcode_id){
        final String sql = "SELECT points_max_money,stock_unit_name,stock_unit_id,conversion,attr_code,\n" +
                "       attr_name,attr_id,mnemonic_code,yh_price,tax_rate,category_id,barcode_status,\n" +
                "       type,origin,brand,goods_status,shelf_life,metering_id,category_name,\n" +
                "       specifi,unit_name,unit_id,ps_price,cost_price,trade_price,buying_price,\n" +
                "       retail_price,only_coding,goods_title,barcode,barcode_id,goods_id\n" +
                "  FROM barcode_info where barcode_id = '"+ barcode_id +"'";

        return  SQLiteHelper.execSql(object,sql);
    }

    private void initBtn(){
        final Button ok_btn = findViewById(R.id.ok_btn),cancel_btn = findViewById(R.id.cancel_btn);
        ok_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
    }

    @Override
    protected void initWindowSize() {
        final Display d = mContext.getDisplay(); // 获取屏幕宽、高用
        final Point point = new Point();
        d.getSize(point);
        final Window dialogWindow = this.getWindow();
        if (dialogWindow != null){
            final WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.CENTER);
            lp.width = (int)(0.98 * point.x);
            dialogWindow.setAttributes(lp);
        }
    }

    public JSONObject getContentObj() {
        final JSONObject object = mContentObj;
        if (object != null){
            double new_price = 0.00,new_num = 0.00;
            try {
                new_price = Double.parseDouble(mPriceEt.getText().toString());
                new_num = Double.parseDouble(mNumEt.getText().toString());
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
            object.put("new_price",new_price);
            object.put("new_num",new_num);
        }
        return object;
    }

}
