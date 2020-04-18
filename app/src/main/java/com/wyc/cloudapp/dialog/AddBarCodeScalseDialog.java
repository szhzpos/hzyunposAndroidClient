package com.wyc.cloudapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
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

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddBarCodeScalseDialog extends Dialog {
    private Context mContext;
    private EditText mManufacturerEt,mProductType,mScalseName,mPort,mGCategoryEt;
    private LinearLayout mIP;
    private CustomePopupWindow mPopupWindow;
    private JSONArray mCategoryInfo,mManufacturerInfos,mScalseInfos;
    private OnGetContent mGetContent;
    public AddBarCodeScalseDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.add_b_scalse_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mScalseName = findViewById(R.id.remark);
        mPort = findViewById(R.id.s_port);
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
        findViewById(R.id._close).setOnClickListener(v -> this.dismiss());
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getScalseConfig()){
                    AddBarCodeScalseDialog.this.dismiss();
                }
            }
        });

    }

    private void initProductType(){
        mProductType = findViewById(R.id.product_type);
        mProductType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Utils.hideKeyBoard((EditText)v);
                if (hasFocus){
                    mProductType.callOnClick();
                }
            }
        });
        mProductType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScalseInfos != null){
                    mPopupWindow.initContent(null, mProductType,mScalseInfos, new String[]{"s_id", "s_type"}, 2, true, new CustomePopupWindow.OngetSelectContent() {
                        @Override
                        public void getContent(JSONObject json) {
                            if (json != null){
                                if ("DH".equals(json.optString("s_id"))){
                                    mPort.setText("3030");
                                }
                            }
                        }
                    });
                    mPopupWindow.setClippingEnabled(false);
                    mPopupWindow.setElevation(100f);
                    mPopupWindow.show(getWindow().getDecorView(),3);
                }
            }
        });
    }

    private void initManufacturer(){
        initScalseInfo();

        mManufacturerEt = findViewById(R.id.s_manufacturer);
        mManufacturerEt.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        mManufacturerEt.setSelectAllOnFocus(true);

        mManufacturerEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Utils.hideKeyBoard((EditText)v);
                if (hasFocus){
                    mManufacturerEt.callOnClick();
                }
            }
        });
        mManufacturerEt.setSelectAllOnFocus(true);
        mManufacturerEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.initContent(null, mManufacturerEt,mManufacturerInfos, new String[]{"name"}, 0, true, new CustomePopupWindow.OngetSelectContent() {
                    @Override
                    public void getContent(JSONObject json) {
                        mScalseInfos = json.optJSONArray("products");
                    }
                });
                mPopupWindow.setClippingEnabled(false);
                mPopupWindow.setElevation(100f);
                mPopupWindow.show(getWindow().getDecorView(),3);
            }
        });
        //默认第一个
        JSONObject object = mManufacturerInfos.optJSONObject(0);
        if (object != null){
            mManufacturerEt.setText(object.optString("name"));
            mScalseInfos = object.optJSONArray("products");
        }
    }

    private void initCategory(){
        mGCategoryEt = findViewById(R.id.g_c_name);
        mGCategoryEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Utils.hideKeyBoard((EditText)v);
                if (hasFocus){
                    mGCategoryEt.callOnClick();
                }
            }
        });
        mGCategoryEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    chooseDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.ToastMessage(e.getMessage(),mContext,getWindow());
                }
            }
        });
    }

    private void initScalseInfo(){
        mManufacturerInfos = new JSONArray();
        try {
            mManufacturerInfos.put(getDHManufacturer());
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private JSONObject getDHManufacturer() throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray products = new JSONArray();

        object.put("name","大华系列");

        products.put(getScalseProduct("DH","大华TM-15A"));

        object.put("products",products);

        return object;
    }

    private JSONObject getScalseProduct(final String s_id,final String s_type) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("s_id",s_id);
        object.put("s_type",s_type);
         return object;
    }

    private void chooseDialog() throws JSONException {
        StringBuilder err = new StringBuilder();
        final JSONArray tmps = SQLiteHelper.getListToJson("SELECT category_id, name FROM shop_category",err),category_info_copy = Utils.JsondeepCopy(mCategoryInfo);
        if (tmps == null){
            MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        int tmps_len = tmps.length();
        final String[] items = new String[tmps_len];
        final boolean[] isCheckeds  = new boolean[tmps_len];

        JSONObject object;
        int c_id = -1;

        for (int i = 0,size = tmps.length();i < size;i++){
            object = tmps.optJSONObject(i);
            items[i] = object.optString("name");

            c_id = object.optInt("category_id");

            for (int j = 0,length = category_info_copy.length();j < length; j++){
                object = category_info_copy.optJSONObject(j);
                if (object != null){
                    if (object.optInt("category_id") == c_id){
                        isCheckeds[i] = true;
                        break;
                    }
                }
            }
        }

        builder.setMultiChoiceItems(items, isCheckeds, new OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                JSONObject object = tmps.optJSONObject(which);
                int id = object.optInt("category_id");
                if (isChecked){
                    category_info_copy.put(object);
                }else {
                    for (int j = 0,length = category_info_copy.length();j < length; j++) {
                        object = category_info_copy.optJSONObject(j);
                        if (object != null) {
                            if (object.optInt("category_id") == id) {
                                category_info_copy.remove(j);
                                break;
                            }
                        }
                    }
                }
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                JSONObject object;
                StringBuilder names = new StringBuilder();
                mCategoryInfo = category_info_copy;
                for (int i = 0,size = mCategoryInfo.length();i < size;i++){
                    object = mCategoryInfo.optJSONObject(i);
                    if ( null != object){
                        if (names.length() != 0){
                            names.append(",");
                        }
                        names.append(object.optString("name"));
                    }
                }
                mGCategoryEt.setText(names);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();

        int blue = mContext.getColor(R.color.blue);

        TextView title = new TextView(mContext);
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        title.setPadding(5,5,5,5);
        title.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_24));
        title.setTextColor(blue);
        title.setText(mContext.getString(R.string.d_category_sz));
        alertDialog.setCustomTitle(title);

        alertDialog.show();


        ListView listView = alertDialog.getListView();
        listView.setDivider(mContext.getDrawable(R.color.gray__subtransparent));
        listView.setDividerHeight(1);
        listView.setBackground(mContext.getDrawable(R.drawable.border_sub_gray));

        Button cancel = alertDialog.getButton(BUTTON_NEGATIVE), ok = alertDialog.getButton(BUTTON_POSITIVE);
        cancel.setTextColor(blue);
        ok.setTextColor(blue);
        cancel.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_16));
        ok.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_16));

        WindowManager.LayoutParams  lp= alertDialog.getWindow().getAttributes();
        lp.width=588;//定义宽度
        lp.height=442;//定义高度
        alertDialog.getWindow().setAttributes(lp);
    }

    private String getIP(){
        View view;
        StringBuilder ip = new StringBuilder();
        for (int i = 0,size = mIP.getChildCount();i < size;i ++){
            view = mIP.getChildAt(i);
            if (view instanceof TextView){
                ip.append(((TextView)view).getText().toString());
            }
        }
        return ip.toString();
    }

    private boolean getScalseConfig(){
        boolean code = false;
        JSONObject object = new JSONObject(),tmp;
        StringBuilder names = new StringBuilder(),ids = new StringBuilder();
        String name = mScalseName.getText().toString(),p_type = mProductType.getText().toString(),ip = getIP(),port = mPort.getText().toString();

        if (p_type.isEmpty()){
            MyDialog.ToastMessage(mProductType,"型号不能为空",mContext,getWindow());
        }else if (ip.isEmpty()){
            MyDialog.ToastMessage(mIP,"IP不能为空",mContext,getWindow());
        }else if (port.isEmpty()){
            MyDialog.ToastMessage(mPort,"端口不能为空",mContext,getWindow());
        }else if (mCategoryInfo.length() == 0){
            MyDialog.ToastMessage("商品分类不能为空",mContext,getWindow());
        }else{
            try {
                object.put("remark",name);
                object.put("product_type",p_type);
                object.put("scale_ip",ip);
                object.put("scale_port",port);

                for (int i = 0,size = mCategoryInfo.length();i < size;i++){
                    tmp = mCategoryInfo.optJSONObject(i);
                    if ( null != tmp){
                        if (names.length() != 0){
                            names.append(",");
                        }
                        if (ids.length() != 0){
                            ids.append(",");
                        }
                        names.append(tmp.optString("name"));
                        ids.append(tmp.optString("category_id"));
                    }
                }
                object.put("g_c_name",names);
                object.put("g_c_id",ids);
                if (mGetContent != null){
                    mGetContent.getContent(object);
                }
                code = true;
            } catch (JSONException e) {
                e.printStackTrace();
                MyDialog.ToastMessage(e.getMessage(),mContext,getWindow());
            }
        }
        return code;
    }

    public void setGetContent(OnGetContent listener){
        mGetContent = listener;
    }

    public interface OnGetContent{
        void getContent(JSONObject object);
    }
}
