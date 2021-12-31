package com.wyc.cloudapp.dialog.serialScales;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.customizationView.KeyboardView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.customizationView.WeightInfoView;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FontSizeTagHandler;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class GoodsWeighDialog extends AbstractDialogSaleActivity {
    private int mBarcodeId;
    private OnYesOnclickListener mOnYesClick;
    private EditText mWvalueEt;
    private TextView mPriceTv,mAmtTv;
    private AbstractSerialScaleImp mSerialScale;
    private volatile double mValue;
    private volatile boolean mStable = false;
    /**
     * 自动取重
     * */
    private static final boolean mAuto = AbstractSerialScaleImp.hasAutoGetWeigh();
    private final boolean mContinuousWeighing = false;

    public GoodsWeighDialog(@NonNull SaleActivity context, final String title) {
        super(context,title);
        read();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPriceTv = findViewById(R.id.w_g_price);
        mAmtTv = findViewById(R.id.w_amt);

        //初始化重量
        initWvalueEt();

        //初始化数字键盘
        initKeyboardView();
        initClick();
    }
    private void initClick(){
        findViewById(R.id.d_zero).setOnClickListener(v -> rZero());
        findViewById(R.id.d_tare).setOnClickListener(v -> tare());
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.goods_weigh_dialog_layout;
    }
    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();

        //初始化商品信息
        initGoodsInfo();

        if (getCurrentFocus() != mWvalueEt && mWvalueEt != null)
            mWvalueEt.postDelayed(()->{mWvalueEt.requestFocus();},300);
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        if (!mAuto)stopRead();
    }

    @Override
    public void show() {
        if (mStable && mAuto){
            getWeigh();
        }else
            super.show();
    }

    public void setBarcodeId(final int id){
        mBarcodeId = id;
    }
    private void initWvalueEt(){
        mWvalueEt = findViewById(R.id.w_value);
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
                mValue = v;
                mAmtTv.setText(String.format(Locale.CHINA,"%.2f",v * price));
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
        view.setOkListener(v -> getWeigh());
    }
    private void getWeigh(){
        if (mOnYesClick != null){
            double num = getContent();
            if (Utils.greaterDouble(num,0)){
                mOnYesClick.onYesClick(getContent());
                dismiss();
            }else MyDialog.toastMessage("重量异常");
        }
    }

    private void initGoodsInfo(){
        final JSONObject object = new JSONObject();
        boolean code = SQLiteHelper.execSql(object,"select barcode_id,brand_id,gs_id,a.category_id,b.path path,ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,retail_price price,ifnull(img_url,'') img_url from " +
                "barcode_info a inner join shop_category b on a.category_id = b.category_id where goods_status = '1' and barcode_status = '1' and barcode_id = '" + mBarcodeId +"'" +
                " UNION select '' brand_id,'' gs_id, '' category_id,'' path, -1 barcode_id,ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name,gp_price price,ifnull(img_url,'') img_url from goods_group where status = '1' and gp_id = '" + mBarcodeId +"'");
        if (code){
            if (!object.isEmpty()){

                CharSequence goods_title = Utils.getNullStringAsEmpty(object,"goods_title");
                if (GoodsInfoViewAdapter.getPromotionInfo(object,mContext.getStoreId(),mContext.getVipGradeId())){

                    if (GoodsInfoViewAdapter.isSpecialPromotion(object))
                        goods_title = Html.fromHtml(goods_title + "<font color='red'><size value='14'>(促销)</size></font> ",null,new FontSizeTagHandler(mContext));

                    final TextView name = findViewById(R.id.w_g_name),unit = findViewById(R.id.unit_name);
                    name.setText(goods_title);
                    mPriceTv.setText(Utils.getNullOrEmptyStringAsDefault(object,"price","0.0"));
                    unit.setText("/".concat(Utils.getNullStringAsEmpty(object,"unit_name")));

                    final String img_url = Utils.getNullStringAsEmpty(object,"img_url");
                    final ImageView imageView = findViewById(R.id.w_g_img);
                    if (!"".equals(img_url)){
                        final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                        final Bitmap bitmap = BitmapFactory.decodeFile(CustomApplication.getGoodsImgSavePath() + szImage);
                        imageView.setImageBitmap(bitmap);
                    }else{
                        imageView.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                    }
                    mWvalueEt.setText(String.format(Locale.CHINA,"%.2f",1.0));
                }else {
                    MyDialog.ToastMessage("查询促销信息错误：" +object.getString("info"), getWindow());
                }
            }
        }else{
            MyDialog.ToastMessage("初始化商品错误：" + object.getString("info"), getWindow());
        }
    }

    public void stopRead(){
        if (mWvalueEt != null)mWvalueEt.clearFocus();
        if (mSerialScale != null){
            mSerialScale.stopRead();
            mSerialScale = null;
        }
    }
    public void read(){
        if (mSerialScale == null){
            final JSONObject object = new JSONObject();
            int code = AbstractSerialScaleImp.readWeight(object);
            if (code >= 0){
                mSerialScale = (AbstractSerialScaleImp) object.get("info");
                if (mSerialScale != null){
                    mSerialScale.setOnReadListener(new AbstractSerialScaleImp.OnReadStatus() {
                        @Override
                        public void onFinish(int stat,double num) {
                            mContext.setScaleInfo(stat,(float) num);
                            boolean invalid = WeightInfoView.hasInvalidWeight(num);
                            if (mContinuousWeighing){
                                if (mOnYesClick != null)CustomApplication.runInMainThread(()-> mOnYesClick.onYesClick(invalid ? 0.0 : num));
                            }else
                            if (null != mWvalueEt)CustomApplication.runInMainThread(()-> mWvalueEt.setText(invalid? CustomApplication.getStringByResId(R.string.invalid_weight) : String.format(Locale.CHINA,"%.3f",num)));

                            mValue = num;
                            mStable = stat == AbstractSerialScaleImp.OnReadStatus.STABLE;
                        }
                        @Override
                        public void onError(String err) {
                            CustomApplication.runInMainThread(()-> MyDialog.ToastMessage("读数量错误：" + err, getWindow()));
                        }
                    }).startRead();
                }
            }else{
                MyDialog.ToastMessage("称初始化错误：" + Utils.getNullStringAsEmpty(object,"info"), getWindow());
            }
        }
    }

    public boolean isContinuousWeighing(){
        return mContinuousWeighing;
    }

    public final double getContent(){
        return mValue;
    }

    public interface OnYesOnclickListener {
        void onYesClick(double num);
    }
    public final void setOnYesOnclickListener(OnYesOnclickListener listener){
        mOnYesClick = listener;
    }
    public void rZero(){
        if (mSerialScale != null)mSerialScale.rZero();
    }
    public void tare(){
        if (mSerialScale != null)mSerialScale.tare();
    }
    public static boolean isAutoGetWeigh(){
        return mAuto;
    }
}
