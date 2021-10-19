package com.wyc.cloudapp.dialog.business;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.activity.mobile.business.MobileSelectGoodsActivity;
import com.wyc.cloudapp.activity.mobile.business.MobileWholesaleBaseActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.constants.WholesalePriceType;
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
 * @Description: 业务单据选择商品对话框
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
    private boolean hasSourceOrder;
    private int mPriceType = WholesalePriceType.BUYING_PRICE;
    private View.OnClickListener mDelListener;
    private OnContinueListener mContinueListener;
    /*需要过滤商品类别*/
    private String mGoodsCategory;

    public BusinessSelectGoodsDialog(@NonNull MainActivity context) {
        this(context, null);
    }

    public BusinessSelectGoodsDialog(@NonNull MainActivity context,final JSONObject object) {
        this(context,false,object);
    }
    public BusinessSelectGoodsDialog(@NonNull MainActivity context,boolean source,final JSONObject object) {
        super(context, context.getString(R.string.scan_code_label));
        if (object != null){
            mContentObj = Utils.JsondeepCopy(object);
        }
        hasSourceOrder = source;
        if (context instanceof MobileWholesaleBaseActivity){
            mPriceType = ((MobileWholesaleBaseActivity)context).getCustomerPriceType();
        }
    }

    public void setGoodsCategory(String category) {
        this.mGoodsCategory = category;
    }

    public void setDelListener(View.OnClickListener listener) {
        this.mDelListener = listener;
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
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isModify()){
            showGoods();
        }
    }

    private boolean isModify(){
        return mContentObj != null && !mContentObj.isEmpty();
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
        }else if (id == R.id.continue_btn){
            if (mContinueListener != null){
                final JSONObject object = getContentObj();
                if (object != null && !object.isEmpty())
                    mContinueListener.onContinue(getContentObj());
            }
            reset();
        }else if (id == R.id.num_tv || id == R.id.price_tv){
            final EditText editText = (EditText)v;
            final Object o = editText.getTag();
            if (null == o){
                editText.selectAll();
                editText.setTag(true);
            }else {
                editText.setTag(null);
                editText.setSelection(editText.getSelectionStart(),editText.getSelectionEnd());
            }
        }
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        final int id = v.getId();
        if (hasFocus){
            v.setTag(true);
            if (id == R.id.num_tv){
                mNumEt.removeTextChangedListener(mTextWatcherWithNum);
                mNumEt.addTextChangedListener(mTextWatcherWithNum);

                mPriceEt.removeTextChangedListener(mTextWatcherWithPrice);
            }else if (id == R.id.price_tv){
                mPriceEt.removeTextChangedListener(mTextWatcherWithPrice);
                mPriceEt.addTextChangedListener(mTextWatcherWithPrice);

                mNumEt.removeTextChangedListener(mTextWatcherWithNum);
            }else if (id == R.id.barcode_tv){
                Utils.hideKeyBoard((EditText)v);
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

    private void reset(){
        mContentObj = null;
        hasSourceOrder = false;

        mBarcodeEt.requestFocus();
        mBarcodeEt.setText(R.string.space_sz);
        if (!mBarcodeEt.isEnabled())mBarcodeEt.setEnabled(true);

        mItemNoTv.setText(R.string.space_sz);
        mNameTv.setText(R.string.space_sz);
        mNumEt.setText(R.string.zero_p_z_sz);
        if (mPriceEt != null)mPriceEt.setText(R.string.zero_p_z_sz);
        mUnitTv.setText(R.string.space_sz);
    }

    private void initView(){
        mItemNoTv = findViewById(R.id.item_no_tv);
        mNameTv = findViewById(R.id.name_tv);
        mNumEt = findViewById(R.id.num_tv);
        mUnitTv = findViewById(R.id.unit_tv);
        mNumEt.setOnFocusChangeListener(this);
        mNumEt.setOnClickListener(this);

        final LinearLayout price_amt_layout = findViewById(R.id.price_and_amt_layout);
        if (price_amt_layout != null){
            mPriceEt = price_amt_layout.findViewById(R.id.price_tv);
            mAmtTv = price_amt_layout.findViewById(R.id.amt_tv);
            mPriceEt.setOnFocusChangeListener(this);
            mPriceEt.setOnClickListener(this);
        }
    }
    protected final TextWatcher mTextWatcherWithNum = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            double num = 0.00,price = 0.00;
            if (s.length() > 0){
                try {
                    num = Double.parseDouble(s.toString());
                    if (hasSourceOrder){
                        double old_num = Utils.getNotKeyAsNumberDefault(mContentObj,"xnum",0.0);
                        if (old_num < num){
                            num = old_num;
                            mNumEt.setText(String.valueOf(old_num));
                            mNumEt.selectAll();
                            mNumEt.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
                            MyDialog.ToastMessage(mNumEt,"当前数量不能大于来源单据数量!", getWindow());
                        }
                    }
                    price = Double.parseDouble(mPriceEt.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
            if (null != mAmtTv)mAmtTv.setText(String.format(Locale.CHINA,"%.3f",price * num));
        }
    };

    protected final TextWatcher mTextWatcherWithPrice = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            double price = 0.0,num = 0.0;
            if (s.length() > 0){
                try {
                    price = Double.parseDouble(s.toString());
                    num = Double.parseDouble(mNumEt.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
            mAmtTv.setText(String.format(Locale.CHINA,"%.3f",price * num));
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
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    mContext.startActivityForResult(intent, BARCODE_REQUEST_CODE);
                }
            }
            return false;
        });
        if(!isModify()){
            search.setOnFocusChangeListener(this);
            search.postDelayed(search::requestFocus,100);
        }else{
            search.setCompoundDrawables( null, null, null, null);
            search.setEnabled(false);
        }
        mBarcodeEt = search;
    }

    @Override
    public boolean hookEnterKey() {
        searchGoods();
        mNumEt.requestFocus();
        return true;
    }

    private void setBarcodeDrawable(boolean clear){
        if (clear){
            mBarcodeEt.setCompoundDrawables( null, null, null, null);
        }else {
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.scan,null);
            drawable .setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mBarcodeEt.setCompoundDrawables( null, null, drawable, null);
        }
    }

    private void searchGoods(){
        final String barcode = mBarcode;
        if (Utils.isNotEmpty(barcode)){
            String where_sql = " where barcode = '" + barcode + "'";
            if (Utils.isNotEmpty(mGoodsCategory)){
                where_sql = where_sql.concat(" and category_id in ("+ mGoodsCategory +")");
            }
            final StringBuilder err = new StringBuilder();
            final JSONArray barcode_ids = SQLiteHelper.getListToValue("select barcode_id from barcode_info" + where_sql + " union select barcode_id from auxiliary_barcode_info where status = 1 and fuzhu_barcode = '"+ barcode +"'",err);
            if (barcode_ids != null){
                int size = barcode_ids.size();
                if (size != 0){
                    if (size > 1){
                        final Intent intent = new Intent(mContext, MobileSelectGoodsActivity.class);
                        intent.putExtra(MobileSelectGoodsActivity.SEARCH_KEY,barcode);
                        intent.putExtra(MobileSelectGoodsActivity.IS_SEL_KEY,true);
                        mContext.startActivityForResult(intent, MobileSelectGoodsActivity.SELECT_GOODS_CODE);
                        setCodeAndExit(0);
                    }else {
                        mContentObj = new JSONObject();
                        if (selectGoodsWithBarcodeId(mContentObj,barcode_ids.getString(0),mPriceType)){
                            Logger.d_json(mContentObj.toString());
                            showGoods();
                        }else {
                            MyDialog.ToastMessage(mContentObj.getString("info"), getWindow());
                            mContentObj = null;
                        }
                    }
                }else {
                    MyDialog.ToastMessage(mBarcodeEt,mContext.getNotExistHintsString(String.format(Locale.CHINA,"商品条码%s",barcode)), getWindow());
                }
            }else {
                MyDialog.ToastMessage(mBarcodeEt,err.toString(), getWindow());
            }
        }else {
            MyDialog.ToastMessage(mBarcodeEt,mContext.getNotEmptyHintsString("搜索内容"), getWindow());
        }
    }

    private void showGoods(){
        final JSONObject object = mContentObj;
        if (null == object)return;

        double num = 0.0,price = 0.0;

        mBarcodeEt.setText(object.getString("barcode"));

        num = Utils.getNotKeyAsNumberDefault(object,"xnum",1.00);
        price = object.getDoubleValue("price");

        mItemNoTv.setText(object.getString("only_coding"));
        mNameTv.setText(object.getString("goods_title"));
        mNumEt.setText(String.valueOf(num));

        if (mPriceEt != null){
            CustomApplication.postDelayed(()->{Utils.setFocus(mContext,mPriceEt);},50);
            mPriceEt.setText(String.valueOf(price));
            if (mAmtTv != null)mAmtTv.setText(String.format(Locale.CHINA,"%.2f",num * price));
        }else{
            CustomApplication.postDelayed(()->{Utils.setFocus(mContext,mNumEt);},50);
        }

        mUnitTv.setTag(object.getString("unit_id"));
        mUnitTv.setText(object.getString("unit_name"));
    }


    public static boolean selectGoodsWithBarcodeId(final JSONObject object,final String barcode_id,final int price_type){
        String key;
        switch (price_type){//1零售价，2优惠价，3配送价，4批发价，5参考进货价
            case WholesalePriceType.RETAIL_PRICE:
                key = "ps_price,cost_price,trade_price,(buying_price * conversion) buying_price,retail_price price";
                break;
            case WholesalePriceType.COST_PRICE:
                key = "ps_price,cost_price price,trade_price,(buying_price * conversion) buying_price,retail_price";
                break;
            case WholesalePriceType.PS_PRICE:
                key = "ps_price price,cost_price,trade_price,(buying_price * conversion) buying_price,retail_price";
                break;
            case WholesalePriceType.TRADE_PRICE:
                key = "ps_price,cost_price,trade_price price,(buying_price * conversion) buying_price,retail_price";
                break;
            default:
                key = "ps_price,cost_price,trade_price,(buying_price * conversion) price,retail_price";
        }

        final String sql = "SELECT points_max_money,stock_unit_name,stock_unit_id,conversion,attr_code,\n" +
                "       attr_name,attr_id,mnemonic_code,yh_price,tax_rate,category_id,barcode_status,\n" +
                "       type,origin,brand,goods_status,shelf_life,metering_id,category_name,\n" +
                "       specifi,unit_name,unit_id,"+ key +",only_coding,goods_title,barcode,barcode_id,goods_id\n" +
                "  FROM barcode_info where barcode_id = '"+ barcode_id +"'";

        return  SQLiteHelper.execSql(object,sql);
    }

    private void initBtn(){
        final Button ok_btn = findViewById(R.id.ok_btn),cancel_btn = findViewById(R.id.cancel_btn),del_btn = findViewById(R.id.del_btn);
        ok_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);

        if (isModify() && mDelListener != null){
            del_btn.setVisibility(View.VISIBLE);
            del_btn.setOnClickListener(v -> {
                mDelListener.onClick(v);
                dismiss();
            });
        }

        final Button btn = findViewById(R.id.continue_btn);
        if (isModify()){
            btn.setVisibility(View.GONE);
        }else {
            btn.setOnClickListener(this);
        }
    }

    public interface OnContinueListener{
        void onContinue(@NonNull JSONObject object);
    }
    public BusinessSelectGoodsDialog setContinueListener(OnContinueListener listener){
        mContinueListener = listener;
        return this;
    }

    @Override
    protected double getWidthRatio(){
        return 0.98;
    }

    public JSONObject getContentObj() {
        final JSONObject object = mContentObj;
        if (object != null){
            double new_price = 0.00,new_num = 0.00;
            try {
                if (mPriceEt != null)new_price = Double.parseDouble(mPriceEt.getText().toString());
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
