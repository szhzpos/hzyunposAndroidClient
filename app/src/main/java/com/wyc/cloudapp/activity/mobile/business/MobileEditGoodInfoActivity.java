package com.wyc.cloudapp.activity.mobile.business;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Locale;

import butterknife.BindView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.wyc.cloudapp.constants.ScanCallbackCode.CODE_REQUEST_CODE;

public class MobileEditGoodInfoActivity extends AbstractEditArchiveActivity {
    private static final String DEFAULT_SUPPLIER_CODE = "0000";

    private String mBarcodeId;
    private String mBarcode;
    private EditText mBarcodeEt,mGoodsAttrEt,mMeteringEt;
    private TextView mBrandTv,mSupplierTv, mCategoryTv,mUnitTv;
    private JSONArray mUnitList,mCategoryList,mSupplierList,mBrandList,mAttrList,meteringList;

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
    @BindView(R.id.pf_price_et)
    EditText pf_price_et;
    @BindView(R.id.place_et)
    EditText place_et;

    private boolean isModify;
    private JSONObject mGoodsObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBarcodeId = getIntent().getStringExtra("barcodeId");
        setMiddleText(getString((isModify = Utils.isNotEmpty(mBarcodeId)) ? R.string.modify_goods : R.string.add_goods));
        Logger.d("mBarcodeId:%s",mBarcodeId);

        initUnit();
        initCategory();
        initBrand();
        initBarcode();
        initSupplier();
        initGoodsAttrAndMetering();

        //查询商品辅助档案
        getGoodsBase();
        getGoodsInfoByBarcode();
        getSupplier();
        if (!isModify)getOnlycodeAndBarcode();

        if (isModify){
            getGoodsByBarcodeId();
        }
    }

    private void getSupplier(){
       Observable.create((ObservableOnSubscribe<JSONArray>) emitter -> {
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("stores_id",getStoreId());
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/supplier_search/xlist",HttpRequest.generate_request_parm(object,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                if (HttpUtils.checkBusinessSuccess(info_obj)){
                    emitter.onNext(info_obj.getJSONArray("data"));
                }else emitter.onError(new Exception(info_obj.getString("info")));
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(array -> {
            mSupplierList = array;
            setDefaultSupplier();
        }, throwable -> MyDialog.ToastMessage("查询供应商信息错误:" + throwable.getMessage(),this,getWindow()));
    }

    private JSONArray parse_supplier_info(){
        final JSONArray array  = new JSONArray(),suppliers = mSupplierList;
        if (suppliers != null){
            JSONObject object;
            for (int i = 0,size = suppliers.size();i < size;i++){
                final JSONObject tmp = suppliers.getJSONObject(i);
                final int id = Utils.getNotKeyAsNumberDefault(tmp,"gs_id",0);
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,Utils.getNullStringAsEmpty(tmp,"gs_name"));
                array.add(object);
            }
        }
        return array;
    }

    private JSONObject getDefaultSupplier(){
        if (null == mSupplierList)return null;
        for (int i = 0,size = mSupplierList.size();i < size;i ++){
            final JSONObject object = mSupplierList.getJSONObject(i);
            if (DEFAULT_SUPPLIER_CODE.equals(object.getString("gs_code"))){
                return object;
            }
        }
        return null;
    }

    private void setDefaultSupplier(){
        final JSONObject object = getDefaultSupplier();
        if (!isModify && null != object && null != mSupplierTv){
            mSupplierTv.setText(object.getString("gs_name"));
            mSupplierTv.setTag(object.getString("gs_id"));
        }
    }

    private void getGoodsInfoByBarcode(){
        if (!Utils.isNotEmpty(mBarcode))return;

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
        if (!unit_sz.isEmpty())mUnitTv.setText(unit_sz);
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
        final TextView unit_et = findViewById(R.id.a_unit_et);
        unit_et.setOnClickListener(v -> {
            final TreeListDialog treeListDialog = new TreeListDialog(this,getString(R.string.unit_sz));
            treeListDialog.setDatas(Utils.JsondeepCopy(mUnitList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    unit_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    unit_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                }
            });
        });
        mUnitTv = unit_et;
        setDefaultUnit();
    }
    private void setDefaultUnit(){
        if (null != mUnitTv){
            mUnitTv.setText("未定");
            mUnitTv.setTag("");
        }
    }

    private void initCategory(){
        final TextView category_et = findViewById(R.id.a_category_et);
        category_et.setOnClickListener(v -> {
            final TreeListDialog treeListDialog = new TreeListDialog(this,getString(R.string.d_category_sz));
            treeListDialog.setDatas(Utils.JsondeepCopy(mCategoryList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    category_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    category_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                    getOnlycodeAndBarcode();
                }
            });
        });
        mCategoryTv = category_et;
    }

    private void getOnlycodeAndBarcode(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("category_id",Utils.getViewTagValue(mCategoryTv,"7223"));
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

    @SuppressLint("ClickableViewAccessibility")
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
            barcdoe_et.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    final float dx = motionEvent.getX();
                    final int w = barcdoe_et.getWidth();
                    if (dx > (w - barcdoe_et.getCompoundPaddingRight())) {
                        barcdoe_et.requestFocus();
                        final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                        startActivityForResult(intent, CODE_REQUEST_CODE);
                    }
                }
                return false;
            });
            mBarcodeEt = barcdoe_et;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode == RESULT_OK ){
            final String _code = intent.getStringExtra("auth_code");
            if (requestCode == CODE_REQUEST_CODE) {
                mBarcode = _code;
                getGoodsInfoByBarcode();
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }

    private void initSupplier(){
        final TextView supplier_et = findViewById(R.id.a_supplier_et);
        supplier_et.setOnClickListener(v -> {
            final String sup = getString(R.string.a_supplier_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setDatas(Utils.JsondeepCopy(parse_supplier_info()),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    supplier_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    supplier_et.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                    mNameEt.requestFocus();
                }
            });
        });
        mSupplierTv = supplier_et;
    }

    private void initBrand(){
        final TextView brand = findViewById(R.id.brand_et);
        brand.setOnClickListener(v -> {
            final String sup = getString(R.string.brand_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setDatas(Utils.JsondeepCopy(mBrandList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    brand.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    brand.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                }
            });
        });
        mBrandTv = brand;
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
                        runOnUiThread(this::setDefaultCategory);

                        mBrandList = parse_brand_info(Utils.getNullObjectAsEmptyJsonArray(data,"brand"));
                        runOnUiThread(this::setDefaultBrand);
                    }
                    break;
            }
        });
    }

    private JSONObject getDefaultCategory(){
        if (mCategoryList != null) {
            for (int i = 0,size = mCategoryList.size();i < size;i ++){
                final JSONObject object = mCategoryList.getJSONObject(i);
                if ("00".equals(object.getString("category_code"))){
                    return object;
                }
            }
        }
        return null;
    }

    private void setDefaultCategory(){
        final JSONObject object = getDefaultCategory();
        if (!isModify && null != object && mCategoryTv != null){
            mCategoryTv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
            mCategoryTv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
        }
    }

    private JSONArray parse_brand_info(final JSONArray brands){
        final JSONArray array  = new JSONArray();
        if (brands != null){
            JSONObject object,tmp;
            for (int i = 0,size = brands.size();i < size;i++){
                tmp = brands.getJSONObject(i);

                final String id = Utils.getNullStringAsEmpty(tmp,"gb_id"),name = Utils.getNullStringAsEmpty(tmp,"gb_name");
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,name);
                array.add(object);
            }
        }
        return array;
    }

    private JSONObject getDefaultBrand(){
        if (mBrandList != null && !mBrandList.isEmpty()){
            return mBrandList.getJSONObject(0);
        }
        return null;
    }

    private void setDefaultBrand(){
        final JSONObject object = getDefaultBrand();
        if (!isModify && null != object && mBrandTv != null){
            mBrandTv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
            mBrandTv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
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
                object.put(TreeListBaseAdapter.COL_ID,Utils.getNullStringAsEmpty(tmp,"unit_id"));
                object.put(TreeListBaseAdapter.COL_NAME,Utils.getNullStringAsEmpty(tmp,"unit_name"));
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
            item.put(TreeListBaseAdapter.COL_ID,category_json.getString("category_id"));
            item.put("category_code",category_json.getString("category_code"));
            item.put(TreeListBaseAdapter.COL_NAME,category_json.getString("name"));

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
        setAttrList();
        setMeteringList();
        goods_attr_et.setOnClickListener(v -> {
            final String attr = getString(R.string.a_goods_attr_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(this,attr.substring(0,attr.length() - 1));
            treeListDialog.setDatas(mAttrList,null,true);

            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    final String id = object.getString(TreeListBaseAdapter.COL_ID);
                    goods_attr_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    goods_attr_et.setTag(id);
                    if ("2".equals(id)){
                        final JSONObject obj = getMeteringById("0");
                        if (null != obj){
                            metering_et.setText(obj.getString(TreeListBaseAdapter.COL_NAME));
                            metering_et.setTag(obj.getString(TreeListBaseAdapter.COL_ID));
                            metering_et.setVisibility(View.VISIBLE);
                        }
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
        final JSONObject attr = getAttrById("1");
        if (null != attr){
            goods_attr_et.setText(attr.getString(TreeListBaseAdapter.COL_NAME));
            goods_attr_et.setTag(attr.getString(TreeListBaseAdapter.COL_ID));
        }
        mGoodsAttrEt = goods_attr_et;

        metering_et.setOnClickListener(v -> {
            final String attr_sz = getString(R.string.a_goods_attr_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(this,attr_sz.substring(0,attr_sz.length() - 1));
            treeListDialog.setDatas(meteringList,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    metering_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    metering_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
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
    private void setAttrList(){
        mAttrList = new JSONArray();
        final JSONObject obj = new JSONObject();
        obj.put("level",0);
        obj.put("unfold",false);
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"1");
        obj.put(TreeListBaseAdapter.COL_NAME,"普通商品");
        mAttrList.add(Utils.JsondeepCopy(obj));
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"2");
        obj.put(TreeListBaseAdapter.COL_NAME,"称重商品");
        mAttrList.add(obj);
    }
    private void setMeteringList(){
        meteringList = new JSONArray();
        final JSONObject obj = new JSONObject();
        obj.put("level",0);
        obj.put("unfold",false);
        obj.put("isSel",true);
        obj.put(TreeListBaseAdapter.COL_ID,"0");
        obj.put(TreeListBaseAdapter.COL_NAME,"计重");
        meteringList.add(Utils.JsondeepCopy(obj));
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"1");
        obj.put(TreeListBaseAdapter.COL_NAME,"计件");
        meteringList.add(Utils.JsondeepCopy(obj));
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"2");
        obj.put(TreeListBaseAdapter.COL_NAME,"定重");
        meteringList.add(obj);
    }
    private JSONObject getAttrById(final String id){
        if (mAttrList != null){
            for (int i = 0,size = mAttrList.size();i < size;i ++){
                final JSONObject object = mAttrList.getJSONObject(i);
                if (Utils.getNullStringAsEmpty(object,TreeListBaseAdapter.COL_ID).equals(id)){
                    return object;
                }
            }
        }
        return null;
    }
    private JSONObject getMeteringById(final String id){
        if (meteringList != null){
            for (int i = 0,size = meteringList.size();i < size;i ++){
                final JSONObject object = meteringList.getJSONObject(i);
                if (Utils.getNullStringAsEmpty(object,TreeListBaseAdapter.COL_ID).equals(id)){
                    return object;
                }
            }
        }
        return null;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_mobile_edit_good_info;
    }

    @Override
    protected void sure() {
        addGoods(generateParameter(),false);
    }
    private JSONObject generateParameter(){
        final JSONObject data = new JSONObject();

        final String barcode = mBarcode,name = mNameEt.getText().toString(),category = mCategoryTv.getText().toString(),unit = mUnitTv.getText().toString(),
                only_coding = mItemIdEt.getText().toString(),supplier = mSupplierTv.getText().toString();
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
        if (supplier.isEmpty()){
            mSupplierTv.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.supplier_setting)),this,getWindow());
            return data;
        }
        if (category.isEmpty()){
            mCategoryTv.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.d_category_sz)),this,getWindow());
            return data;
        }
        if (unit.isEmpty()){
            mUnitTv.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.unit_sz)),this,getWindow());
            return data;
        }
        if (only_coding.isEmpty()){
            mItemIdEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.item_no_sz)),this,getWindow());
            return data;
        }

        data.put("attr_id",0);
        data.put("type",isModify ? 2 : 1);
        data.put("buying_price",mPurPriceEt.getText().toString());
        data.put("category_id",Utils.getViewTagValue(mCategoryTv,""));
        data.put("goods_title",name);
        data.put("gs_id",Utils.getViewTagValue(mSupplierTv,0));
        data.put("spec_id",Utils.getViewTagValue(mGoodsAttrEt,"1"));
        data.put("only_coding",only_coding);
        data.put("cash_flow_mode",2);
        data.put("unit",unit);
        data.put("current_goods", getCurPriceFlag());
        data.put("gb_id",Utils.getViewTagValue(mBrandTv,""));

        final JSONArray barcode_info = new JSONArray();
        final JSONObject goods = new JSONObject();
        goods.put("barcode",barcode);
        goods.put("mnemonic_code",Utils.getNullStringAsEmpty(mGoodsObj,"mnemonic_code"));
        goods.put("goods_spec_code",Utils.getNullStringAsEmpty(mGoodsObj,"goods_spec_code"));
        goods.put("metering_id",getMetering());
        goods.put("retail_price",mRetailPriceEt.getText().toString());
        goods.put("sys_unit",unit);
        goods.put("conversion",Utils.getNullOrEmptyStringAsDefault(mGoodsObj,"conversion","1"));
        goods.put("yh_mode",Utils.getNotKeyAsNumberDefault(mGoodsObj,"yh_mode",0));
        goods.put("yh_price",mVipPriceEt.getText().toString());
        goods.put("cost_price",0);
        goods.put("ps_price",0);
        goods.put("trade_price",pf_price_et.getText().toString());
        goods.put("goods_id",Utils.getNullStringAsEmpty(mGoodsObj,"goods_id"));
        if (isModify)goods.put("barcode_id",mBarcodeId);
        goods.put("del_status",1);

        barcode_info.add(goods);

        data.put("appid",getAppId());
        data.put("goods",barcode_info);

        return data;
    }

    private int getCurPriceFlag(){
        final RadioButton yes = findViewById(R.id.y);
        return yes.isChecked() ? 1 : 0;
    }
    private void setCurPriceFlag(boolean flag){
        RadioButton rb;
        if (flag){
            rb = findViewById(R.id.y);
        }else {
            rb = findViewById(R.id.n);
        }
        rb.setChecked(true);
    }

    private String getMetering(){
        if ("1".equals(Utils.getViewTagValue(mGoodsAttrEt,"1"))){
            return "0";
        }else
            return Utils.getViewTagValue(mMeteringEt,"1");
    }

    private void addGoods(final JSONObject data,boolean reset){
        Logger.d_json(data);
        if (data.isEmpty())return;

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
            MyDialog.displayErrorMessage(this, "编辑商品错误:" + err);
        }else {
            if (reset)
                reset();
            else
                finish();
        }
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
                        code = SQLiteHelper.execSQLByBatchFromJson(new_goods,"barcode_info" , CustomApplication.getGoodsCols(),err,1);
                        break;
                }
                break;
        }
        return code ;
    }

    @Override
    protected void saveAndAdd() {
        addGoods(generateParameter(),true);
    }

    private void reset(){
        isModify = false;
        mGoodsObj = null;

        mNameEt.setText(R.string.space_sz);
        resetCurPrice();
        setDefaultSupplier();
        setDefaultCategory();
        setDefaultUnit();

        mRetailPriceEt.setText(R.string.zero_p_z_sz);
        mVipPriceEt.setText(R.string.zero_p_z_sz);
        mPurPriceEt.setText(R.string.zero_p_z_sz);
        pf_price_et.setText(R.string.zero_p_z_sz);
        mUnitTv.setText("");

        getOnlycodeAndBarcode();
    }

    private void resetCurPrice(){
        final RadioButton n = findViewById(R.id.n);
        if (!n.isChecked())n.setChecked(true);
    }

    private void getGoodsByBarcodeId(){
        final StringBuilder sql = new StringBuilder("select ");
        final String[] cols = CustomApplication.getGoodsCols();
        for (int i = 0,size = cols.length;i < size;i ++){
            sql.append(cols[i]);
            if (i != size -1)sql.append(",");
        }
        sql.append(" from barcode_info where barcode_id = ").append(mBarcodeId);

        mGoodsObj = new JSONObject();
        if (SQLiteHelper.execSql(mGoodsObj,sql.toString())){
            showGoods(mGoodsObj);
        }else {
            Toast.makeText(this,"查询商品信息错误..." + mGoodsObj.getString("info"),Toast.LENGTH_LONG).show();
        }
    }
    private void showGoods(final JSONObject goods){

        final JSONObject attr_obj = getAttrById(goods.getString("type"));
        if (null != attr_obj){
            String id = attr_obj.getString(TreeListBaseAdapter.COL_ID);
            mGoodsAttrEt.setTag(id);
            mGoodsAttrEt.setText(attr_obj.getString(TreeListBaseAdapter.COL_NAME));

            if ("2".equals(id)){
                final JSONObject obj = getMeteringById(goods.getString("metering_id"));
                if (null != obj){
                    mMeteringEt.setText(obj.getString(TreeListBaseAdapter.COL_NAME));
                    mMeteringEt.setTag(obj.getString(TreeListBaseAdapter.COL_ID));
                    mMeteringEt.setVisibility(View.VISIBLE);
                }
            }
        }
        setCurPriceFlag(goods.getIntValue("current_goods") == 1);

        mBarcodeEt.setText(goods.getString("barcode"));
        mNameEt.setText(goods.getString("goods_title"));

        mSupplierTv.setTag(goods.getString("gs_id"));
        mSupplierTv.setText(goods.getString("gs_name"));

        mItemIdEt.setText(goods.getString("only_coding"));

        mCategoryTv.setTag(goods.getString("category_id"));
        mCategoryTv.setText(goods.getString("category_name"));

        mBrandTv.setTag(goods.getString("brand_id"));
        mBrandTv.setText(goods.getString("brand"));

        mUnitTv.setTag(goods.getString("unit_id"));
        mUnitTv.setText(goods.getString("unit_name"));

        place_et.setText(goods.getString("origin"));

        mPurPriceEt.setText(goods.getString("buying_price"));
        pf_price_et.setText(goods.getString("trade_price"));
        mRetailPriceEt.setText(goods.getString("retail_price"));
        mVipPriceEt.setText(goods.getString("yh_price"));
    }

    public static void start(Context context,final String barcode_id){
        /*barcode_id 为空时 ，以新增模式打开*/
        Intent intent = new Intent(context,MobileEditGoodInfoActivity.class);
        intent.putExtra("barcodeId",barcode_id);
        context.startActivity(intent);
    }
}