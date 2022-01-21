package com.wyc.cloudapp.dialog.barcodeScales;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomePopupWindow;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogContext;
import com.wyc.cloudapp.utils.Utils;

import java.lang.reflect.InvocationTargetException;

public class AddBarcodeScaleDialog extends AbstractDialogContext {
    private EditText mManufacturerEt,mProductType, mScaleName,mPort,mGCategoryEt;
    private LinearLayout mIP;
    private CustomePopupWindow mPopupWindow;
    private JSONArray mCategoryInfo,mManufacturerInfos, mScaleInfos;
    private OnGetContentCallBack mGetContent;
    private JSONObject mModifyScale;
    AddBarcodeScaleDialog(@NonNull Context context, final String title) {
        super(context,title);
    }
    AddBarcodeScaleDialog(@NonNull Context context, JSONObject object) {
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
            if (getScaleConfig()){
                AddBarcodeScaleDialog.this.dismiss();
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
                            final IBarCodeScale iBarCodeScale = AbstractBarcodeScaleImp.newInstance(class_name);
                            mPort.setText(iBarCodeScale.getPort());
                            mProductType.setTag(class_name);
                        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                            e.printStackTrace();
                            MyDialog.ToastMessage("设置默认端口错误：" + e.getMessage(), getWindow());
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
        initScaleInfo();

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
        final EditText editText = findViewById(R.id.g_c_name);

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            Utils.hideKeyBoard((EditText)v);
            if (hasFocus){
                editText.callOnClick();
            }
        });
        editText.setOnClickListener(v -> {
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,mContext.getString(R.string.d_category_sz));
            treeListDialog.setData(GoodsCategoryAdapter.getCategoryAsTreeListData(),mCategoryInfo,false);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final StringBuilder names = new StringBuilder();
                    mCategoryInfo = treeListDialog.getMultipleContent();
                    for (int i = 0,size = mCategoryInfo.size();i < size;i++){
                        final JSONObject object = mCategoryInfo.getJSONObject(i);
                        if ( null != object){
                            if (names.length() != 0){
                                names.append(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                            }
                            names.append(object.getString(TreeListBaseAdapter.COL_NAME));
                        }
                    }
                    mGCategoryEt.setText(names);
                }
            });
        });

        mGCategoryEt = editText;
    }

    private void initScaleInfo(){
        mManufacturerInfos = AbstractBarcodeScaleImp.getManufacturerInfo();
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
    private final TextWatcher ipTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().contains(".")  || s.length() > 2 ){
                CustomApplication.execute(()->{
                    final Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
                });
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

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
                object.put(TreeListBaseAdapter.COL_ID,ids[i]);
                object.put(TreeListBaseAdapter.COL_NAME,sz_name);
                mCategoryInfo.add(object);
                if (sb_name.length() != 0){
                    sb_name.append(AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                }
                sb_name.append(sz_name);
            }
            mGCategoryEt.setText(sb_name.toString());
        }
    }

    private boolean getScaleConfig(){
        boolean code = false;
        JSONObject object = new JSONObject(),tmp;
        final StringBuilder names = new StringBuilder(),ids = new StringBuilder();
        final String name = mScaleName.getText().toString(),p_type = mProductType.getText().toString(),ip = getIP(),port = mPort.getText().toString();

        if (p_type.isEmpty()){
            MyDialog.ToastMessage(mProductType,"型号不能为空", getWindow());
        }else if (ip.isEmpty()){
            MyDialog.ToastMessage(mIP,"IP不能为空", getWindow());
        }else if (port.isEmpty()){
            MyDialog.ToastMessage(mPort,"端口不能为空", getWindow());
        }else if (mCategoryInfo.size() == 0){
            MyDialog.ToastMessage("商品分类不能为空", getWindow());
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
                    names.append(tmp.getString(TreeListBaseAdapter.COL_NAME));
                    ids.append(tmp.getString(TreeListBaseAdapter.COL_ID));
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
