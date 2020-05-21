package com.wyc.cloudapp.dialog.barcodeScales;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomePopupWindow;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnContextImp;
import com.wyc.cloudapp.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AddBarCodeScaleDialog extends DialogBaseOnContextImp {
    private EditText mManufacturerEt,mProductType, mScaleName,mPort,mGCategoryEt;
    private LinearLayout mIP;
    private CustomePopupWindow mPopupWindow;
    private JSONArray mCategoryInfo,mManufacturerInfos, mScaleInfos;
    private OnGetContentCallBack mGetContent;
    private JSONObject mModifyScale;
    AddBarCodeScaleDialog(@NonNull Context context,final String title) {
        super(context,title);
    }
    AddBarCodeScaleDialog(@NonNull Context context,JSONObject object) {
        this(context,context.getString(R.string.modify_scale_sz));
        mModifyScale = object;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScaleName = findViewById(R.id.remark);
        mPort = findViewById(R.id.ser_port);
        mGCategoryEt = findViewById(R.id.g_c_name);

        mPopupWindow = new CustomePopupWindow(mContext);
        mCategoryInfo = new JSONArray();

        //型号
        initProductType();
        //厂商
        initManufacturer();

        //商品类别
        initCategory();

        //IP
        initIP();

        //初始化按钮事件
        findViewById(R.id.save).setOnClickListener(v -> {
            if (getScalseConfig()){
                AddBarCodeScaleDialog.this.dismiss();
            }
        });

    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.add_b_scalse_dialog_layout;
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();

        //修内容
        if (mModifyScale != null){
            initModifyScaleInfo();
        }
    }

    private void initProductType(){
        mProductType = findViewById(R.id.product_type);
        mProductType.setOnFocusChangeListener((v, hasFocus) -> {
            Utils.hideKeyBoard((EditText)v);
            if (hasFocus){
                mProductType.callOnClick();
            }
        });
        mProductType.setOnClickListener(v -> {
            if (mScaleInfos != null){
                mPopupWindow.initContent(null, mProductType, mScaleInfos, new String[]{"s_type"}, 2, true, json -> {
                    if (json != null){
                        final String class_name = json.getString("s_id");
                        try {
                            IBarCodeScale iBarCodeScale = AbstractBarcodeScaleImp.newInstance(class_name);
                            mPort.setText(iBarCodeScale.getPort());
                            mProductType.setTag(class_name);
                        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                            e.printStackTrace();
                            MyDialog.ToastMessage("设置默认端口错误：" + e.getMessage(),mContext,getWindow());
                        }
                    }
                });
                mPopupWindow.setClippingEnabled(false);
                mPopupWindow.setElevation(100f);
                mPopupWindow.show(getWindow().getDecorView(),3);
            }
        });
    }

    private void initManufacturer(){
        initScalseInfo();

        mManufacturerEt = findViewById(R.id.s_manufacturer);
        mManufacturerEt.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        mManufacturerEt.setSelectAllOnFocus(true);

        mManufacturerEt.setOnFocusChangeListener((v, hasFocus) -> {
            Utils.hideKeyBoard((EditText)v);
            if (hasFocus){
                mManufacturerEt.callOnClick();
            }
        });
        mManufacturerEt.setSelectAllOnFocus(true);
        mManufacturerEt.setOnClickListener(v -> {
            mPopupWindow.initContent(null, mManufacturerEt,mManufacturerInfos, new String[]{"name"}, 0, true, new CustomePopupWindow.OngetSelectContent() {
                @Override
                public void getContent(JSONObject json) {
                    mScaleInfos = json.getJSONArray("products");
                }
            });
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setElevation(100f);
            mPopupWindow.show(getWindow().getDecorView(),3);
        });
        //默认第一个
        final JSONObject object = mManufacturerInfos.getJSONObject(0);
        if (object != null){
            mManufacturerEt.setText(object.getString("name"));
            mScaleInfos = object.getJSONArray("products");
        }
    }

    private void initCategory(){
        mGCategoryEt = findViewById(R.id.g_c_name);
        mGCategoryEt.setOnFocusChangeListener((v, hasFocus) -> {
            Utils.hideKeyBoard((EditText)v);
            if (hasFocus){
                mGCategoryEt.callOnClick();
            }
        });
        mGCategoryEt.setOnClickListener(v -> {
            chooseDialog();
        });
    }

    private void initScalseInfo(){
        mManufacturerInfos = new JSONArray();
        mManufacturerInfos.add(AbstractBarcodeScaleImp.getDHManufacturer());
    }

    private void initIP(){
        mIP = findViewById(R.id.ip_linearLayout);
        View view;
        EditText et;
        for (int i = 0,size = mIP.getChildCount();i < size;i ++){
            view = mIP.getChildAt(i);
            if (view instanceof EditText){
                et = (EditText)view;
                et.setSelectAllOnFocus(true);
                et.addTextChangedListener(ipTextWatcher);
            }
        }
    }
    private TextWatcher ipTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().contains(".")  || s.length() > 2 ){
                CustomApplication.execute(()->{
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
                });
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void chooseDialog(){
        final StringBuilder err = new StringBuilder();
        final JSONArray tmps = SQLiteHelper.getListToJson("SELECT category_id, name FROM shop_category ",err),category_info_copy = Utils.JsondeepCopy(mCategoryInfo);
        if (tmps == null){
            MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        int tmps_len = tmps.size();
        final boolean[] isCheckeds  = new boolean[tmps_len];

        JSONObject object;
        int c_id = -1;

        final List<String> list = new ArrayList<>();

        for (int i = 0,size = tmps.size();i < size;i++){
            object = tmps.getJSONObject(i);
            list.add(object.getString("name"));

            c_id = object.getIntValue("category_id");

            for (int j = 0,length = category_info_copy.size();j < length; j++){
                object = category_info_copy.getJSONObject(j);
                if (object != null){
                    if (object.getIntValue("category_id") == c_id){
                        isCheckeds[i] = true;
                        break;
                    }
                }
            }
        }

        builder.setMultiChoiceItems(list.toArray(new String[tmps_len]), isCheckeds, (dialog, which, isChecked) -> {
            JSONObject object1 = tmps.getJSONObject(which);
            int id = object1.getIntValue("category_id");
            if (isChecked){
                category_info_copy.add(object1);
            }else {
                for (int j = 0,length = category_info_copy.size();j < length; j++) {
                    object1 = category_info_copy.getJSONObject(j);
                    if (object1 != null) {
                        if (object1.getIntValue("category_id") == id) {
                            category_info_copy.remove(j);
                            break;
                        }
                    }
                }
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.OK), (dialog, which) -> {
            JSONObject object12;
            StringBuilder names = new StringBuilder();
            mCategoryInfo = category_info_copy;
            for (int i = 0,size = mCategoryInfo.size();i < size;i++){
                object12 = mCategoryInfo.getJSONObject(i);
                if ( null != object12){
                    if (names.length() != 0){
                        names.append(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                    }
                    names.append(object12.getString("name"));
                }
            }
            mGCategoryEt.setText(names);
            dialog.dismiss();
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });

        final AlertDialog alertDialog = builder.create();

        int blue = mContext.getColor(R.color.blue);

        final TextView title = new TextView(mContext);
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        title.setPadding(5,5,5,5);
        title.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_24));
        title.setTextColor(blue);
        title.setText(mContext.getString(R.string.d_category_sz));
        alertDialog.setCustomTitle(title);

        alertDialog.show();


        final ListView listView = alertDialog.getListView();
        listView.setDivider(mContext.getDrawable(R.color.gray__subtransparent));
        listView.setDividerHeight(1);
        listView.setBackground(mContext.getDrawable(R.drawable.border_sub_gray));

        final Button cancel = alertDialog.getButton(BUTTON_NEGATIVE), ok = alertDialog.getButton(BUTTON_POSITIVE);
        cancel.setTextColor(blue);
        ok.setTextColor(blue);
        cancel.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_16));
        ok.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_16));

        final WindowManager.LayoutParams  lp= alertDialog.getWindow().getAttributes();
        lp.width=588;//定义宽度
        lp.height=442;//定义高度
        alertDialog.getWindow().setAttributes(lp);
    }

    private String getIP(){
        View view;
        EditText et;
        StringBuilder ip = new StringBuilder();
        for (int i = 0,size = mIP.getChildCount();i < size;i ++){
            view = mIP.getChildAt(i);
            if (view instanceof EditText){
                et = (EditText)view;
                if (et.length() != 0){
                    ip.append(et.getText().toString());
                }else
                    return "";

            }else if (view instanceof TextView){
                ip.append(((TextView)view).getText().toString());
            }
        }
        return ip.toString();
    }

    private void setIP(final String ip){
        View view;
        EditText et;
        final String[] ips = ip.split("\\.");
        int index = 0;
        if (ips.length == 4){
            for (int i = 0,size = mIP.getChildCount();i < size;i ++){
                view = mIP.getChildAt(i);
                if (view instanceof EditText){
                    et = (EditText)view;
                    et.setText(ips[index++]);
                }
            }
        }
    }

    private void setCategoryInfo(final String id,final String name){
        final String[] ids = id.split(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
        final String[] names = name.split(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
        JSONObject object;
        StringBuilder sb_name = new StringBuilder();
        String sz_name;
        if (ids.length == names.length){
            for (int i = 0,length = ids.length;i < length;i++){
                object = new JSONObject();
                sz_name = names[i];
                object.put("category_id",ids[i]);
                object.put("name",sz_name);
                mCategoryInfo.add(object);
                if (sb_name.length() != 0){
                    sb_name.append(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                }
                sb_name.append(sz_name);
            }
            mGCategoryEt.setText(sb_name.toString());
        }
    }

    private boolean getScalseConfig(){
        boolean code = false;
        JSONObject object = new JSONObject(),tmp;
        StringBuilder names = new StringBuilder(),ids = new StringBuilder();
        String name = mScaleName.getText().toString(),p_type = mProductType.getText().toString(),ip = getIP(),port = mPort.getText().toString();

        if (p_type.isEmpty()){
            MyDialog.ToastMessage(mProductType,"型号不能为空",mContext,getWindow());
        }else if (ip.isEmpty()){
            MyDialog.ToastMessage(mIP,"IP不能为空",mContext,getWindow());
        }else if (port.isEmpty()){
            MyDialog.ToastMessage(mPort,"端口不能为空",mContext,getWindow());
        }else if (mCategoryInfo.size() == 0){
            MyDialog.ToastMessage("商品分类不能为空",mContext,getWindow());
        }else{
            if (mModifyScale != null){
                object.put("_id",mModifyScale.getIntValue("_id"));
            }
            object.put("remark",name);
            object.put("s_manufacturer",mManufacturerEt.getText().toString());
            object.put("s_class_id",mProductType.getTag());
            object.put("s_product_t",p_type);
            object.put("scale_ip",ip);
            object.put("scale_port",port);

            for (int i = 0,size = mCategoryInfo.size();i < size;i++){
                tmp = mCategoryInfo.getJSONObject(i);
                if ( null != tmp){
                    if (names.length() != 0){
                        names.append(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                    }
                    if (ids.length() != 0){
                        ids.append(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                    }
                    names.append(tmp.getString("name"));
                    ids.append(tmp.getString("category_id"));
                }
            }
            object.put("g_c_name",names);
            object.put("g_c_id",ids);
            if (mGetContent != null){
                mGetContent.getContent(object);
            }
            code = true;
         }
        return code;
    }

    private void initModifyScaleInfo(){
        mScaleName.setText(mModifyScale.getString("remark"));
        mManufacturerEt.setText(mModifyScale.getString("s_manufacturer"));
        mProductType.setText(mModifyScale.getString("s_product_t"));
        mProductType.setTag(mModifyScale.getString("s_class_id"));
        setIP(mModifyScale.getString("scale_ip"));
        mPort.setText(mModifyScale.getString("scale_port"));
        setCategoryInfo(mModifyScale.getString("g_c_id"),mModifyScale.getString("g_c_name"));
    }

    void setGetContent(OnGetContentCallBack listener){
        mGetContent = listener;
    }

    public interface OnGetContentCallBack {
        void getContent(JSONObject object);
    }
}
