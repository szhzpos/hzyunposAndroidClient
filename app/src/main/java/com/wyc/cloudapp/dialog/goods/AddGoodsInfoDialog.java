package com.wyc.cloudapp.dialog.goods;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddGoodsInfoDialog extends AbstractDialogMainActivity {
    private String mBarcode;
    private EditText mBarcodeEt,mNameEt,mPurPriceEt,mRetailPriceEt,mCategoryEt,mUnitEt,mGoodsAttrEt,mItemIdEt, mMeteringEt,mSupplierEt,mVipPriceEt;
    private JSONArray mUnitList,mCategoryList,mSupplierList;
    private OnFinishListener mFinishListener;
    private JSONObject mCurrentCategory;

    @BindView(R.id.hz_method_tv)
    TextView hz_method_tv;
    @BindView(R.id.ly_ratio_tv)
    TextView ly_ratio_tv;

    public AddGoodsInfoDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.a_goods_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        mNameEt = findViewById(R.id.a_name_et);
        mRetailPriceEt = findViewById(R.id.a_retail_price_et);
        mItemIdEt = findViewById(R.id.a_item_no_et);
        mVipPriceEt = findViewById(R.id.a_vip_price_et);

        initUnit();
        initCategory();
        initCkml();
        initBarcode();
        initSupplier();
        initGoodsAttrAndMetering();
        initSaveBtn();
    }

    @Override
    protected int getContentLayoutId() {
        if (mContext.lessThan7Inches())return R.layout.mobile_add_goods_dialog_layout;
        return R.layout.add_goods_dialog_layout;
    }

    @Override
    public void show(){
        super.show();

        getGoodsBase();
        getGoodsInfoByBarcode();
        getSupplier();
        getOnlycodeAndBarcode();
    }

    protected double getWidthRatio(){
        if (mContext.lessThan7Inches())
            return 0.98;
        return super.getWidthRatio();
    }

    private int getCurPriceFlag(){
        final RadioButton yes = findViewById(R.id.y);
        return yes.isChecked() ? 1 : 0;
    }

    private void initUnit(){
        final EditText unit_et = findViewById(R.id.a_unit_et);
        unit_et.setOnClickListener(v -> {
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,mContext.getString(R.string.unit_sz));
            treeListDialog.setData(Utils.JsondeepCopy(mUnitList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    unit_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    unit_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
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
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,mContext.getString(R.string.d_category_sz));
            treeListDialog.setData(Utils.JsondeepCopy(mCategoryList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    category_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    category_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
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
            category_et.setText(mCurrentCategory.getString(TreeListBaseAdapter.COL_NAME));
            category_et.setTag(mCurrentCategory.getString(TreeListBaseAdapter.COL_ID));
        }

        mCategoryEt = category_et;
    }

    public void setCurrentCategory(final JSONObject object){
        mCurrentCategory = object;
    }

    private void initSupplier(){
        final EditText supplier_et = findViewById(R.id.a_supplier_et);
        supplier_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setHz_method();
            }
        });
        supplier_et.setOnClickListener(v -> {
            final String sup = mContext.getString(R.string.a_supplier_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,sup.substring(0,sup.length() - 1));
            treeListDialog.setData(Utils.JsondeepCopy(mSupplierList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    supplier_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                    supplier_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
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
    private void setHz_method(){
        final JSONObject supplier = getSupplierById(Utils.getViewTagValue(mSupplierEt,""));
        if (null != supplier){
            final String hz_method = supplier.getString("hz_method");
            hz_method_tv.setTag(hz_method);
            hz_method_tv.setText(supplier.getString("hz_method_name"));
            setLyRatio("1".equals(hz_method) ? View.VISIBLE : View.INVISIBLE);
        }
    }
    private JSONObject getSupplierById(final String gs_id){
        if (null == mSupplierList)return null;
        for (int i = 0,size = mSupplierList.size();i < size;i ++){
            final JSONObject object = mSupplierList.getJSONObject(i);
            if (object.getString(TreeListBaseAdapter.COL_ID).equals(gs_id)){
                return object;
            }
        }
        return null;
    }
    private void setLyRatio(int visibility){
        //设置联营扣率
        final TextView ly_ratio_label = findViewById(R.id.ly_ratio_label);
        ly_ratio_label.setVisibility(visibility);
        ly_ratio_tv.setVisibility(visibility);
        if (visibility == View.GONE){
            ly_ratio_tv.setText(R.string.zero_p_z_sz);
        }
    }

    private void initGoodsAttrAndMetering(){
        final EditText goods_attr_et = findViewById(R.id.a_goods_attr_et),metering_et = findViewById(R.id.a_metering_et);
        goods_attr_et.setOnClickListener(v -> {
            final String attr = mContext.getString(R.string.a_goods_attr_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,attr.substring(0,attr.length() - 1));
            final JSONArray array = new JSONArray();
            final JSONObject obj = new JSONObject();
            obj.put("level",0);
            obj.put("unfold",false);
            obj.put("isSel",false);
            obj.put(TreeListBaseAdapter.COL_ID,"1");
            obj.put(TreeListBaseAdapter.COL_NAME,"普通商品");
            array.add(Utils.JsondeepCopy(obj));
            obj.put("isSel",false);
            obj.put(TreeListBaseAdapter.COL_ID,"2");
            obj.put(TreeListBaseAdapter.COL_NAME,"称重商品");
            array.add(obj);

            treeListDialog.setData(array,null,true);

            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    final String id = object.getString(TreeListBaseAdapter.COL_ID);
                    goods_attr_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
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
            final String attr = mContext.getString(R.string.a_goods_attr_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(mContext,attr.substring(0,attr.length() - 1));
            final JSONArray array = new JSONArray();

            final JSONObject obj = new JSONObject();
            obj.put("level",0);
            obj.put("unfold",false);
            obj.put("isSel",true);
            obj.put(TreeListBaseAdapter.COL_ID,"0");
            obj.put(TreeListBaseAdapter.COL_NAME,"计重");
            array.add(Utils.JsondeepCopy(obj));
            obj.put("isSel",false);
            obj.put(TreeListBaseAdapter.COL_ID,"1");
            obj.put(TreeListBaseAdapter.COL_NAME,"计件");
            array.add(Utils.JsondeepCopy(obj));
            obj.put("isSel",false);
            obj.put(TreeListBaseAdapter.COL_ID,"2");
            obj.put(TreeListBaseAdapter.COL_NAME,"定重");
            array.add(obj);

            treeListDialog.setData(array,null,true);
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
                    retail_pirce_et = Double.parseDouble(mRetailPriceEt.getText().toString());
                    pur_price = Double.parseDouble(s.toString());
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
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",mContext.getAppId());
            final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/goods_set/get_bases",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    mContext.runOnUiThread(()-> MyDialog.ToastMessage("查询商品基本信息错误:" + retJson.getString("info"),mContext,getWindow()));
                    break;
                case 1:
                    final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                    if ("n".equals(Utils.getNullOrEmptyStringAsDefault(info_obj,"status","n"))){
                        mContext.runOnUiThread(()-> MyDialog.ToastMessage("查询商品基本信息错误:" + info_obj.getString("info"),mContext,getWindow()));
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
    private void getSupplier(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",mContext.getAppId());
            object.put("stores_id",mContext.getStoreId());
            final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/supplier_search/xlist",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    mContext.runOnUiThread(()->{
                        MyDialog.ToastMessage("查询供应商信息错误:" + retJson.getString("info"),mContext,getWindow());
                    });
                    break;
                case 1:
                    final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                    if ("n".equals(Utils.getNullOrEmptyStringAsDefault(info_obj,"status","n"))){
                        mContext.runOnUiThread(()-> MyDialog.ToastMessage("查询供应商信息错误:" + info_obj.getString("info"),mContext,getWindow()));
                    }else{
                        final JSONArray data = info_obj.getJSONArray("data");
                        mSupplierList = parse_supplier_info_and_set_default(data);
                    }
                    break;
            }
        });
    }

    private void getOnlycodeAndBarcode(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",mContext.getAppId());
            object.put("category_id",Utils.getViewTagValue(mCategoryEt,"7223"));
            object.put("spec_id",Utils.getViewTagValue(mGoodsAttrEt,"1"));

            final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/goods_set/get_onlycode_barcode",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    mContext.runOnUiThread(()-> MyDialog.ToastMessage("生成条码信息错误:" + retJson.getString("info"),mContext,getWindow()));
                    break;
                case 1:
                    final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                    if ("n".equals(Utils.getNullOrEmptyStringAsDefault(info_obj,"status","n"))){
                        mContext.runOnUiThread(()-> MyDialog.ToastMessage("生成条码信息错误:" + info_obj.getString("info"),mContext,getWindow()));
                    }else{
                        final JSONObject data = info_obj.getJSONObject("data");
                        if (data != null){
                            mContext.runOnUiThread(()-> setBarcodeAndItemId(data));
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

    private JSONArray parse_supplier_info_and_set_default(final JSONArray suppliers){
        final JSONArray array  = new JSONArray();
        if (suppliers != null){
            JSONObject object;
            for (int i = 0,size = suppliers.size();i < size;i++){
                final JSONObject tmp = suppliers.getJSONObject(i);
                final String id = Utils.getNullStringAsEmpty(tmp,"gs_id");
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("hz_method",tmp.getString("hz_method"));
                object.put("hz_method_name",tmp.getString("hz_method_name"));
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,Utils.getNullStringAsEmpty(tmp,"gs_name"));
                array.add(object);

                //
                if ("0000".equals(Utils.getNullStringAsEmpty(tmp,"gs_code")) && mSupplierEt != null){
                    CustomApplication.runInMainThread(()->{
                        mSupplierEt.setTag(id);
                        mSupplierEt.setText(Utils.getNullStringAsEmpty(tmp,"gs_name"));
                    });
                }
            }
        }
        return array;
    }
    private void getGoodsInfoByBarcode(){
        if (mBarcode == null || mBarcode.isEmpty())return;

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
                    mContext.runOnUiThread(()-> MyDialog.ToastMessage("查询商品信息错误:" + retJson.getString("info"),mContext,getWindow()));
                    break;
                case 1:
                    mContext.runOnUiThread(()-> showGoodsInfo(JSONObject.parseObject(retJson.getString("info"))));
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

    private void initSaveBtn(){
        final Button btn = findViewById(R.id.save);
        if (btn != null){
            btn.setOnClickListener(v -> {
                final JSONObject data = generateParameter();
                if (!data.isEmpty()){
                    if (addGoods(data) && mFinishListener != null){
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

            final String param = HttpRequest.generate_request_parm(data,mContext.getAppSecret());

            final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() +"/api/goods_set/goods_sets",param,true);

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
            MyDialog.displayErrorMessage(mContext, "新增商品错误:" + err);
        }
        return code == 1;
    }
    private boolean getNewGoodsAndSave(final HttpRequest httpRequest, final StringBuilder err){
        boolean code = true;
        final MainActivity activity = mContext;
        final JSONObject data = new JSONObject();
        data.put("appid",activity.getAppId());
        data.put("pos_num",activity.getPosNum());
        data.put("stores_id",activity.getStoreId());
        final String param = HttpRequest.generate_request_parm(data,activity.getAppSecret());
        final JSONObject retJson = httpRequest.sendPost(activity.getUrl() +"/api/goods/get_goods_all",param,true);
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
                        code = SQLiteHelper.execSQLByBatchFromJson(new_goods,"barcode_info" ,CustomApplication.getGoodsCols(),err,1);
                        break;
                }
                break;
        }
        return code ;
    }
    private JSONObject generateParameter(){
        final JSONObject data = new JSONObject();
        final MainActivity activity = mContext;
        final String barcode = mBarcode,name = mNameEt.getText().toString(),category = mCategoryEt.getText().toString(),unit = mUnitEt.getText().toString(),only_coding = mItemIdEt.getText().toString();
        if (barcode == null || barcode.isEmpty()){
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
        if (only_coding.isEmpty()){
            mItemIdEt.requestFocus();
            MyDialog.ToastMessage(activity.getString(R.string.not_empty_hint_sz,activity.getString(R.string.item_no_sz)),activity,getWindow());
            return data;
        }

        data.put("attr_id",0);
        data.put("buying_price",mPurPriceEt.getText().toString());
        data.put("category_id",Utils.getViewTagValue(mCategoryEt,""));
        data.put("goods_title",name);
        data.put("gs_id",Utils.getViewTagValue(mSupplierEt,0));
        data.put("spec_id",Utils.getViewTagValue(mGoodsAttrEt,"1"));
        data.put("only_coding",only_coding);
        data.put("current_goods", getCurPriceFlag());
        data.put("cash_flow_mode",Utils.getViewTagValue(hz_method_tv,""));
        data.put("cash_flow_ratio",ly_ratio_tv.getText().toString());
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

        data.put("appid",activity.getAppId());
        data.put("goods",barcode_info);

        return data;
    }
    private String getMetering(){
        if ("1".equals(Utils.getViewTagValue(mGoodsAttrEt,"1"))){
            return "0";
        }else
            return Utils.getViewTagValue(mMeteringEt,"1");
    }

    public interface OnFinishListener{
        void onFinish(final String barcode);
    }
    public void setFinishListener(OnFinishListener listener){
        mFinishListener = listener;
    }

}
