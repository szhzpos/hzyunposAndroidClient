package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.Locale;

public class AddGoodsInfoDialog extends DialogBaseOnMainActivityImp {
    private MainActivity mContext;
    private String mBarcode;
    private EditText mBarcodeEt,mNameEt,mPurPriceEt,mRetailPriceEt,mCategoryEt,mUnitEt;
    private JSONArray mUnitList,mCategoryList;
    private OnFinishListener mFinishListener;
    public AddGoodsInfoDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.a_goods_sz));
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNameEt = findViewById(R.id.a_name_et);
        mRetailPriceEt = findViewById(R.id.a_retail_price_et);
        initUnit();
        initCategory();
        initCkml();
        initBarcode();
        initSaveBtn();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.add_goods_dialog_layout;
    }

    @Override
    public void show(){
        super.show();

        getGoodsInfoByBarcode();
    }
    private void initUnit(){
        final EditText unit_et = findViewById(R.id.a_unit_et);
        unit_et.setOnClickListener(v -> {
            final TreeListDialog treeListDialog = new TreeListDialog(mContext,mContext.getString(R.string.unit_sz));
            treeListDialog.setDatas(Utils.JsondeepCopy(mUnitList),null,true);
            unit_et.post(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    unit_et.setText(object.getString("item_name"));
                    unit_et.setTag(object.getString("item_id"));
                }
            });
        });
        unit_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(unit_et);
        });

        mUnitEt = unit_et;
    }
    private void initCategory(){
        final EditText category_et = findViewById(R.id.a_category_et);
        category_et.setOnClickListener(v -> {
            final TreeListDialog treeListDialog = new TreeListDialog(mContext,mContext.getString(R.string.d_category_sz));
            treeListDialog.setDatas(Utils.JsondeepCopy(mCategoryList),null,true);
            category_et.post(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    category_et.setText(object.getString("item_name"));
                    category_et.setTag(object.getString("item_id"));
                }
            });
        });
        category_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(category_et);
        });

        mCategoryEt = category_et;
    }
    private void initBarcode(){
        final EditText barcdoe_et = findViewById(R.id.a_barcode_et);
        if (barcdoe_et != null){
            barcdoe_et.setText(mBarcode);
        }
        mBarcodeEt = barcdoe_et;
    }
    private void initCkml(){
        final EditText et = findViewById(R.id.a_ckml_et),pur_price_et = findViewById(R.id.a_pur_price_et);;
        pur_price_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double retail_pirce_et = 0.0,pur_price = 0.0;
                try {
                    retail_pirce_et = Double.valueOf(mRetailPriceEt.getText().toString());
                    pur_price = Double.valueOf(s.toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                et.setText(String.format(Locale.CHINA,"%.2f",retail_pirce_et - pur_price));
            }
        });
        mPurPriceEt = pur_price_et;
    }
    public static boolean verifyGoodsAddPermissions(MainActivity context){
        return context.verifyPermissions("31",null,false);
    }
    public void setBarcode(final String barcode){
        mBarcode = barcode;
    }
    private void getGoodsBase(){
        final HttpRequest httpRequest = new HttpRequest();
        final JSONObject object = new JSONObject();
        object.put("appid",mContext.getAppId());
        final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
        final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/goods_set/get_bases",sz_param,true);
        switch (retJson.getIntValue("flag")){
            case 0:
                mContext.runOnUiThread(()->{
                    MyDialog.ToastMessage("查询商品基本信息错误:" + retJson.getString("info"),mContext,getWindow());
                });
                break;
            case 1:
                final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                final JSONObject data = info_obj.getJSONObject("data");
                mUnitList = Utils.getNullObjectAsEmptyJsonArray(data,"units");
                mUnitList = parse_unit_info(mUnitList);

                mCategoryList = Utils.getNullObjectAsEmptyJsonArray(data,"category");
                final JSONArray categorys = new JSONArray();
                parse_category_info(mCategoryList,null,0,categorys);
                mCategoryList = categorys;

                break;
        }
    }
    private void parse_category_info(final JSONArray category_jsons,final JSONObject parent,int level,final JSONArray categorys) {
        JSONObject item,category_json;
        JSONArray kids,childs;
        for (int i = 0, length = category_jsons.size(); i < length; i++) {
            category_json = category_jsons.getJSONObject(i);

            item = new JSONObject();
            item.put("level",level);
            item.put("unfold",false);
            item.put("isSel",false);
            item.put("item_id",category_json.getString("category_id"));
            item.put("item_name",category_json.getString("name"));

            item.put("kids",new JSONArray());

            if (parent != null){
                item.put("p_ref",parent);
                kids = parent.getJSONArray("kids");
                kids.add(item);
            }

            if (category_json.containsKey("childs")) {
                childs = (JSONArray) category_json.remove("childs");
                if (childs != null && childs.size() != 0) {
                    parse_category_info(childs,item,level + 1, null);
                }
            }
            if (categorys != null)categorys.add(item);
        }
    }
    private JSONArray parse_unit_info(final JSONArray units){
        final JSONArray array  = new JSONArray();
        if (units != null){
            JSONObject object,tmp;
            for (int i = 0,size = units.size();i < size;i++){
                tmp = units.getJSONObject(i);

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",Utils.getNullStringAsEmpty(tmp,"unit_id"));
                object.put("item_name",Utils.getNullStringAsEmpty(tmp,"unit_name"));
                array.add(object);
            }
        }
        return array;
    }
    private void getGoodsInfoByBarcode(){
        final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
        final JEventLoop jEventLoop = new JEventLoop();
        progressDialog.setMessage("正在加载商品信息...").setCancel(false).show();
        CustomApplication.execute(()->{

            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",mContext.getAppId());
            object.put("itemcode",mBarcode);
            final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
            final JSONObject retJson = httpRequest.sendPost("http://adm.hzyunpos.com/api/getgoods/get_goods",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    mContext.runOnUiThread(()->{
                        MyDialog.ToastMessage("查询商品信息错误:" + retJson.getString("info"),mContext,getWindow());
                    });
                    break;
                case 1:
                    try {
                        getGoodsBase();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    mContext.runOnUiThread(()->{
                        showGoodsInfo(JSONObject.parseObject(retJson.getString("info")));
                    });
                    break;
            }
            jEventLoop.done(0);
        });
        jEventLoop.exec();
        progressDialog.dismiss();
    }
    private void showGoodsInfo(final JSONObject info){
        mUnitEt.setText(Utils.getNullStringAsEmpty(info,"unit"));
        mRetailPriceEt.setText(String.format(Locale.CHINA,"%.2f",info.getDoubleValue("price")));

        final String name = Utils.getNullStringAsEmpty(info,"name");
        if (name.isEmpty()){
            mNameEt.requestFocus();
        }else {
            mNameEt.setText(name);
            mRetailPriceEt.requestFocus();
        }
    }

    private void initSaveBtn(){
        final Button btn = findViewById(R.id.save);
        if (btn != null){
            btn.setOnClickListener(v -> {
                final JSONObject data = generateParameter();
                if (!data.isEmpty()){
                    if (addGoods(data) && mFinishListener != null){
                        this.dismiss();
                        mFinishListener.onFinish(mBarcode);
                    }
                }
            });
        }
    }

    private boolean addGoods(final JSONObject data){
        final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        progressDialog.setCancel(false).setMessage("正在上传商品信息...").show();

        CustomApplication.execute(()->{

            final HttpRequest httpRequest = new HttpRequest();

            String param = HttpRequest.generate_request_parm(data,mContext.getAppSecret());
            JSONObject retJson = httpRequest.sendPost(mContext.getUrl() +"/api/goods_set/goods_sets",param,true);

            switch (retJson.getIntValue("flag")){
                case 0:
                    loop.done(0);
                    err.append(retJson.getString("info"));
                    break;
                case 1:
                    JSONObject info  = JSON.parseObject(retJson.getString("info"));
                    switch (Utils.getNullStringAsEmpty(info,"status")){
                        case "n":
                            loop.done(0);
                            err.append(info.getString("info"));
                            break;
                        case "y":
                            if (getNewGoodsAndSave(httpRequest,err)){
                                loop.done(1);
                            }else
                                loop.done(0);
                            break;
                    }
                    break;
            }
        });
        int code = loop.exec();
        progressDialog.dismiss();
        if (code != 1){
            MyDialog.displayErrorMessage(null,"新增商品错误:" + err,mContext);
        }
        return code == 1;
    }
    private boolean getNewGoodsAndSave(final HttpRequest httpRequest, final StringBuilder err){
        boolean code = true;
        final MainActivity activity = mContext;
        final JSONObject data = new JSONObject();
        data.put("appid",activity.getAppId());
        data.put("pos_num",activity.getPosNum());
        data.put("stores_id",activity.getStoreInfo().getString("stores_id"));
        final String param = HttpRequest.generate_request_parm(data,activity.getAppSecret());
        final JSONObject retJson = httpRequest.sendPost(activity.getUrl() +"/api_v2/goods/get_goods_all",param,true);
        switch (retJson.getIntValue("flag")){
            case 0:
                code = false;
                err.append(retJson.getString("info"));
                break;
            case 1:
                final JSONObject info = JSON.parseObject(Utils.getNullOrEmptyStringAsDefault(retJson,"info","{}"));
                switch (Utils.getNullStringAsEmpty(info,"status")){
                    case "n":
                        code = false;
                        err.append(info.getString("info"));
                        break;
                    case "y":
                        final JSONArray new_goods = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(info,"data","[]"));
                        final String[] table_cls = new String[]{"goods_id","barcode_id","barcode","goods_title","only_coding","retail_price","buying_price","trade_price","cost_price","ps_price",
                                "unit_id","unit_name","specifi","category_name","metering_id","shelf_life","goods_status","brand","origin","type","goods_tare","barcode_status","category_id",
                                "tax_rate","tc_mode","tc_rate","yh_mode","yh_price","mnemonic_code","image","attr_id","attr_name","attr_code","conversion","update_price","stock_unit_id","stock_unit_name","img_url"};
                        code = SQLiteHelper.execSQLByBatchFromJson(new_goods,"barcode_info" ,table_cls,err,1);
                        break;
                }
                break;
        }
        return code ;
    }
    private JSONObject generateParameter(){
        final JSONObject data = new JSONObject();
        final MainActivity activity = mContext;
        final String barcode = mBarcode,name = mNameEt.getText().toString(),category = mCategoryEt.getText().toString(),unit = mUnitEt.getText().toString();
        if (barcode.isEmpty()){
            mBarcodeEt.requestFocus();
            MyDialog.ToastMessage(activity.getString(R.string.not_empty_hint_sz,activity.getString(R.string.barcode_sz)),activity,getWindow());
            return data;
        }
        if (name.isEmpty()){
            mNameEt.requestFocus();
            MyDialog.ToastMessage(activity.getString(R.string.not_empty_hint_sz,activity.getString(R.string.g_name_sz)),activity,getWindow());
            return data;
        }
        if (category.isEmpty()){
            mCategoryEt.requestFocus();
            MyDialog.ToastMessage(activity.getString(R.string.not_empty_hint_sz,activity.getString(R.string.d_category_sz)),activity,getWindow());
            return data;
        }
        if (unit.isEmpty()){
            mUnitEt.requestFocus();
            MyDialog.ToastMessage(activity.getString(R.string.not_empty_hint_sz,activity.getString(R.string.unit_sz)),activity,getWindow());
            return data;
        }

        data.put("attr_id",0);
        data.put("buying_price",mPurPriceEt.getText().toString());
        data.put("category_id",Utils.getViewTagValue(mCategoryEt,""));
        data.put("goods_title",name);
        data.put("spec_id",1);
        data.put("unit",unit);

        final JSONArray barcode_info = new JSONArray();
        final JSONObject goods = new JSONObject();
        goods.put("barcode",barcode);
        goods.put("mnemonic_code","");
        goods.put("goods_spec_code","");
        goods.put("metering_id",0);
        goods.put("retail_price",mRetailPriceEt.getText().toString());
        goods.put("sys_unit",unit);
        goods.put("conversion",1);
        goods.put("yh_mode",0);
        goods.put("yh_price",0);
        goods.put("cost_price",0);
        goods.put("ps_price",0);
        goods.put("goods_id",0);
        goods.put("barcode_id",0);
        goods.put("del_status",1);

        barcode_info.add(goods);

        data.put("appid",activity.getAppId());
        data.put("goods",barcode_info);

        return data;
    }

    public interface OnFinishListener{
        void onFinish(final String barcode);
    }
    public void setFinishListener(OnFinishListener listener){
        mFinishListener = listener;
    }

}
