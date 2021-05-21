package com.wyc.cloudapp.activity.mobile.business;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.Locale;

import butterknife.BindView;

public class MobileEditGoodInfoActivity extends AbstractEditArchiveActivity {
    private String mBarcodeId;
    private String mBarcode;
    private EditText mBarcodeEt,mCategoryEt,mUnitEt,mGoodsAttrEt,mMeteringEt,mSupplierEt;
    private JSONArray mUnitList,mCategoryList,mSupplierList;
    private JSONObject mCurrentCategory;

    @BindView(R.id.a_name_et)
    EditText mNameEt;
    @BindView(R.id.a_retail_price_et)
    EditText mRetailPriceEt;
    @BindView(R.id.a_item_no_et)
    EditText mItemIdEt;
    @BindView(R.id.a_vip_price_et)
    EditText mVipPriceEt;
    @BindView(R.id.a_pur_price_et)
    EditText mPurPriceEt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBarcodeId = getIntent().getStringExtra("barcodeId");
        setMiddleText(getString(Utils.isNotEmpty(mBarcodeId) ? R.string.modify_goods : R.string.add_goods));
        Logger.d("mBarcodeId:%s",mBarcodeId);

        initUnit();
        initCategory();

        initBarcode();
        initSupplier();
        initGoodsAttrAndMetering();

        //查询商品辅助档案
        getGoodsBase();
        getGoodsInfoByBarcode();
        getSupplier();
        getOnlycodeAndBarcode();
    }

    private void getSupplier(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("stores_id",getStoreId());
            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(getUrl() + "/api/supplier_search/xlist",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    runOnUiThread(()-> MyDialog.ToastMessage("查询供应商信息错误:" + retJson.getString("info"),this,getWindow()));
                    break;
                case 1:
                    final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                    if ("n".equals(Utils.getNullOrEmptyStringAsDefault(info_obj,"status","n"))){
                        runOnUiThread(()-> MyDialog.ToastMessage("查询供应商信息错误:" + info_obj.getString("info"),this,getWindow()));
                    }else{
                        final JSONArray data = info_obj.getJSONArray("data");
                        mSupplierList = parse_supplier_info_and_set_default(data);
                    }
                    break;
            }
        });
    }

    private JSONArray parse_supplier_info_and_set_default(final JSONArray suppliers){
        final JSONArray array  = new JSONArray();
        if (suppliers != null){
            JSONObject object;
            for (int i = 0,size = suppliers.size();i < size;i++){
                final JSONObject tmp = suppliers.getJSONObject(i);
                final int id = Utils.getNotKeyAsNumberDefault(tmp,"gs_id",0);
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",id);
                object.put("item_name",Utils.getNullStringAsEmpty(tmp,"gs_name"));
                array.add(object);

                //
                if ("0000".equals(Utils.getNullStringAsEmpty(tmp,"gs_code")) && mSupplierEt != null){
                    CustomApplication.runInMainThread(()->{
                        mSupplierEt.setText(Utils.getNullStringAsEmpty(tmp,"gs_name"));
                        mSupplierEt.setTag(id);
                    });
                }
            }
        }
        return array;
    }

    private void getGoodsInfoByBarcode(){
        if (mBarcode == null || mBarcode.isEmpty())return;

        final CustomProgressDialog progressDialog = new CustomProgressDialog(this);
        final JEventLoop jEventLoop = new JEventLoop();
        progressDialog.setMessage("正在加载商品信息...").setCancel(false).show();
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("itemcode",mBarcode);
            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost("http://adm.hzyunpos.com/api/getgoods/get_goods",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    runOnUiThread(()-> MyDialog.ToastMessage("查询商品信息错误:" + retJson.getString("info"),this,getWindow()));
                    break;
                case 1:
                    runOnUiThread(()-> showGoodsInfo(JSONObject.parseObject(retJson.getString("info"))));
                    break;
            }
            jEventLoop.done(0);
        });
        jEventLoop.exec();
        progressDialog.dismiss();
    }
    private void showGoodsInfo(final JSONObject info){
        final String unit_sz = Utils.getNullStringAsEmpty(info,"unit");
        if (!unit_sz.isEmpty())mUnitEt.setText(unit_sz);
        mRetailPriceEt.setText(String.format(Locale.CHINA,"%.2f",info.getDoubleValue("price")));

        final String name = Utils.getNullStringAsEmpty(info,"name");
        if (name.isEmpty()){
            mNameEt.requestFocus();
        }else {
            mNameEt.setText(name);
            mRetailPriceEt.requestFocus();
        }
    }
    private void initUnit(){
        final EditText unit_et = findViewById(R.id.a_unit_et);
        unit_et.setOnClickListener(v -> {
            final TreeListDialog treeListDialog = new TreeListDialog(this,getString(R.string.unit_sz));
            treeListDialog.setDatas(Utils.JsondeepCopy(mUnitList),null,true);
            CustomApplication.runInMainThread(()->{
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
        unit_et.setText("未定");
        unit_et.setTag("");

        mUnitEt = unit_et;
    }

    private void initCategory(){
        final EditText category_et = findViewById(R.id.a_category_et);
        category_et.setOnClickListener(v -> {
            final TreeListDialog treeListDialog = new TreeListDialog(this,getString(R.string.d_category_sz));
            treeListDialog.setDatas(Utils.JsondeepCopy(mCategoryList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    category_et.setText(object.getString("item_name"));
                    category_et.setTag(object.getString("item_id"));
                    getOnlycodeAndBarcode();
                }
            });
        });
        category_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(category_et);
        });
        if (mCurrentCategory == null){
            category_et.setText("不定类");
            category_et.setTag("7223");
        }else{
            category_et.setText(mCurrentCategory.getString("item_name"));
            category_et.setTag(mCurrentCategory.getString("item_id"));
        }

        mCategoryEt = category_et;
    }

    private void getOnlycodeAndBarcode(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("category_id",Utils.getViewTagValue(mCategoryEt,"7223"));
            object.put("spec_id",Utils.getViewTagValue(mGoodsAttrEt,"1"));

            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(getUrl() + "/api/goods_set/get_onlycode_barcode",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    runOnUiThread(()-> MyDialog.ToastMessage("生成条码信息错误:" + retJson.getString("info"),this,getWindow()));
                    break;
                case 1:
                    final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                    if ("n".equals(Utils.getNullOrEmptyStringAsDefault(info_obj,"status","n"))){
                        runOnUiThread(()-> MyDialog.ToastMessage("生成条码信息错误:" + info_obj.getString("info"),this,getWindow()));
                    }else{
                        final JSONObject data = info_obj.getJSONObject("data");
                        if (data != null){
                            runOnUiThread(()-> setBarcodeAndItemId(data));
                        }
                    }
                    break;
            }
        });
    }

    private void setBarcodeAndItemId(final @NonNull JSONObject data){
        final String only_coding = data.getString("only_coding");
        if (mItemIdEt != null)mItemIdEt.setText(only_coding);
        if (mBarcodeEt != null){
            if ("2".equals(Utils.getViewTagValue(mGoodsAttrEt,"1"))){
                mBarcodeEt.setText(only_coding);
            }else{
                if(mBarcode == null || mBarcode.isEmpty()){
                    mBarcodeEt.setText(data.getString("barcode"));
                    mBarcodeEt.selectAll();
                }
            }
        }
    }

    private void initBarcode(){
        final EditText barcdoe_et = findViewById(R.id.a_barcode_et);
        if (barcdoe_et != null){
            barcdoe_et.setText(mBarcode);
            barcdoe_et.requestFocus();
            barcdoe_et.addTextChangedListener(new TextWatcher() {
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
            mBarcodeEt = barcdoe_et;
        }
    }

    private void initSupplier(){
        final EditText supplier_et = findViewById(R.id.a_supplier_et);
        supplier_et.setOnClickListener(v -> {
            final String sup = getString(R.string.a_supplier_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setDatas(Utils.JsondeepCopy(mSupplierList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    supplier_et.setText(object.getString("item_name"));
                    supplier_et.setTag(object.getIntValue("item_id"));
                    mNameEt.requestFocus();
                }
            });
        });
        supplier_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(supplier_et);
        });
        mSupplierEt = supplier_et;
    }

    private void getGoodsBase(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(getUrl() + "/api/goods_set/get_bases",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    runOnUiThread(()-> MyDialog.ToastMessage("查询商品基本信息错误:" + retJson.getString("info"),this,getWindow()));
                    break;
                case 1:
                    final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                    if ("n".equals(Utils.getNullOrEmptyStringAsDefault(info_obj,"status","n"))){
                        runOnUiThread(()-> MyDialog.ToastMessage("查询商品基本信息错误:" + info_obj.getString("info"),this,getWindow()));
                    }else{
                        final JSONObject data = info_obj.getJSONObject("data");
                        mUnitList = parse_unit_info(Utils.getNullObjectAsEmptyJsonArray(data,"units"));
                        final JSONArray categorys = new JSONArray();
                        parse_category_info(Utils.getNullObjectAsEmptyJsonArray(data,"category"),null,0,categorys);
                        mCategoryList = categorys;
                    }
                    break;
            }
        });
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

    private void initGoodsAttrAndMetering(){
        final EditText goods_attr_et = findViewById(R.id.a_goods_attr_et),metering_et = findViewById(R.id.a_metering_et);
        goods_attr_et.setOnClickListener(v -> {
            final String attr = getString(R.string.a_goods_attr_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(this,attr.substring(0,attr.length() - 1));
            final JSONArray array = new JSONArray();
            final JSONObject obj = new JSONObject();
            obj.put("level",0);
            obj.put("unfold",false);
            obj.put("isSel",false);
            obj.put("item_id","1");
            obj.put("item_name","普通商品");
            array.add(Utils.JsondeepCopy(obj));
            obj.put("isSel",false);
            obj.put("item_id","2");
            obj.put("item_name","称重商品");
            array.add(obj);

            treeListDialog.setDatas(array,null,true);

            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    final String id = object.getString("item_id");
                    goods_attr_et.setText(object.getString("item_name"));
                    goods_attr_et.setTag(id);
                    if ("2".equals(id)){
                        metering_et.setText("计重");
                        metering_et.setTag("0");
                        metering_et.setVisibility(View.VISIBLE);
                    }else{
                        metering_et.setVisibility(View.GONE);
                    }
                    getOnlycodeAndBarcode();

                    mBarcodeEt.requestFocus();
                }
            });
        });
        goods_attr_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(goods_attr_et);
        });
        goods_attr_et.setText("普通商品");
        goods_attr_et.setTag("1");
        mGoodsAttrEt = goods_attr_et;

        metering_et.setOnClickListener(v -> {
            final String attr = getString(R.string.a_goods_attr_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(this,attr.substring(0,attr.length() - 1));
            final JSONArray array = new JSONArray();

            final JSONObject obj = new JSONObject();
            obj.put("level",0);
            obj.put("unfold",false);
            obj.put("isSel",true);
            obj.put("item_id","0");
            obj.put("item_name","计重");
            array.add(Utils.JsondeepCopy(obj));
            obj.put("isSel",false);
            obj.put("item_id","1");
            obj.put("item_name","计件");
            array.add(Utils.JsondeepCopy(obj));
            obj.put("isSel",false);
            obj.put("item_id","2");
            obj.put("item_name","定重");
            array.add(obj);

            treeListDialog.setDatas(array,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    metering_et.setText(object.getString("item_name"));
                    metering_et.setTag(object.getString("item_id"));
                    mBarcodeEt.requestFocus();
                }
            });
        });
        metering_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(metering_et);
        });
        mMeteringEt = metering_et;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_mobile_edit_good_info;
    }

    @Override
    protected void sure() {
        addGoods(generateParameter());
    }
    private JSONObject generateParameter(){
        final JSONObject data = new JSONObject();

        final String barcode = mBarcode,name = mNameEt.getText().toString(),category = mCategoryEt.getText().toString(),unit = mUnitEt.getText().toString(),only_coding = mItemIdEt.getText().toString();
        if (barcode == null || barcode.isEmpty()){
            mBarcodeEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.barcode_sz)),this,getWindow());
            return data;
        }
        if (name.isEmpty()){
            mNameEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.g_name_sz)),this,getWindow());
            return data;
        }
        if (category.isEmpty()){
            mCategoryEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.d_category_sz)),this,getWindow());
            return data;
        }
        if (unit.isEmpty()){
            mUnitEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.unit_sz)),this,getWindow());
            return data;
        }
        if (only_coding.isEmpty()){
            mItemIdEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.item_no_sz)),this,getWindow());
            return data;
        }

        data.put("attr_id",0);
        data.put("buying_price",mPurPriceEt.getText().toString());
        data.put("category_id",Utils.getViewTagValue(mCategoryEt,""));
        data.put("goods_title",name);
        data.put("gs_id",Utils.getViewTagValue(mSupplierEt,0));
        data.put("spec_id",Utils.getViewTagValue(mGoodsAttrEt,"1"));
        data.put("only_coding",only_coding);
        data.put("cash_flow_mode",2);
        data.put("unit",unit);

        final JSONArray barcode_info = new JSONArray();
        final JSONObject goods = new JSONObject();
        goods.put("barcode",barcode);
        goods.put("mnemonic_code","");
        goods.put("goods_spec_code","");
        goods.put("metering_id",getMetering());
        goods.put("retail_price",mRetailPriceEt.getText().toString());
        goods.put("sys_unit",unit);
        goods.put("conversion",1);
        goods.put("yh_mode",0);
        goods.put("yh_price",mVipPriceEt.getText().toString());
        goods.put("cost_price",0);
        goods.put("ps_price",0);
        goods.put("goods_id",0);
        goods.put("barcode_id",0);
        goods.put("del_status",1);

        barcode_info.add(goods);

        data.put("appid",getAppId());
        data.put("goods",barcode_info);

        return data;
    }
    private String getMetering(){
        if ("1".equals(Utils.getViewTagValue(mGoodsAttrEt,"1"))){
            return "0";
        }else
            return Utils.getViewTagValue(mMeteringEt,"1");
    }

    private boolean addGoods(final JSONObject data){
        if (data.isEmpty())return false ;

        final CustomProgressDialog progressDialog = new CustomProgressDialog(this);
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        progressDialog.setCancel(false).setMessage("正在上传商品信息...").show();

        CustomApplication.execute(()->{

            final HttpRequest httpRequest = new HttpRequest();

            final String param = HttpRequest.generate_request_parm(data,getAppSecret());

            final JSONObject retJson = httpRequest.sendPost(getUrl() +"/api/goods_set/goods_sets",param,true);

            switch (retJson.getIntValue("flag")){
                case 0:
                    loop.done(0);
                    err.append(retJson.getString("info"));
                    break;
                case 1:
                    final JSONObject info  = JSON.parseObject(retJson.getString("info"));
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
            MyDialog.displayErrorMessage(this, "新增商品错误:" + err);
        }
        return code == 1;
    }

    private boolean getNewGoodsAndSave(final HttpRequest httpRequest, final StringBuilder err){
        boolean code = true;

        final JSONObject data = new JSONObject();
        data.put("appid",getAppId());
        data.put("pos_num",getPosNum());
        data.put("stores_id",getStoreId());
        final String param = HttpRequest.generate_request_parm(data,getAppSecret());
        final JSONObject retJson = httpRequest.sendPost(getUrl() +"/api/goods/get_goods_all",param,true);
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

    @Override
    protected void saveAndAdd() {

    }
    public static void start(Context context,final String barcode_id){
        /*barcode_id 为空时 ，已新增模式打开*/
        Intent intent = new Intent(context,MobileEditGoodInfoActivity.class);
        intent.putExtra("barcodeId",barcode_id);
        context.startActivity(intent);
    }
}