package com.wyc.cloudapp.activity.mobile.report;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.client.android.CaptureActivity;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.report.MobileStockQueryAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MobileStockQueryActivity extends AbstractReportActivity {
    private static final int CODE_REQUEST_CODE = 0x000000bb;//条码请求标识
    private TextView mBrandCondTv, mCategoryCondTv;
    private int mCurrentBrandIndex,mCurrentCategoryIndex;
    private JSONArray mCategoryList,mBrandList;
    private Button mQueryBtn;
    private EditText mQueryConditionEt;
    private MobileStockQueryAdapter mAdapter;
    private Spinner mConditionSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBrandAndCategory();
        initQueryBtn();
        initQueryCondition();
        initStockList();
        initConditionSpinner();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_stock_query;
    }

    @Override
    public void onResume(){
        super.onResume();
        getGoodsBase();
        mBrandCondTv.postDelayed(()-> mAdapter.setDatas(mQueryConditionObj),100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {//条码回调
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && requestCode == CODE_REQUEST_CODE) {
            if (mQueryConditionEt != null && mQueryBtn != null) {
                mQueryConditionEt.setText(intent.getStringExtra(CaptureActivity.CALLBACK_CODE));
                mQueryBtn.callOnClick();
            }
        }
    }

    private void getGoodsBase(){
        CustomApplication.execute(()->{
            final JSONObject object = new JSONObject();

            try{
                object.put("appid",mAppId);

                final JSONObject retJson = HttpUtils.sendPost(mUrl + "/api/goods_set/get_bases", HttpRequest.generate_request_parm(object, mAppSecret),true);
                switch (retJson.getIntValue("flag")){
                    case 0:
                        MyDialog.ToastMessageInMainThread(CustomApplication.self().getString(R.string.query_goods_hint, retJson.getString("info")));
                        break;
                    case 1:
                        final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                        if ("n".equals(info_obj.getString("status"))){
                            MyDialog.ToastMessageInMainThread(CustomApplication.self().getString(R.string.query_goods_hint,info_obj.getString("info")));
                        }else{
                            final JSONObject data = info_obj.getJSONObject("data");

                            JSONObject default_obj = new JSONObject();
                            default_obj.put("gb_id",-1);
                            default_obj.put("gb_name","全部品牌");
                            mBrandList = new JSONArray();
                            mBrandList.add(default_obj);
                            final JSONArray brands = Utils.getNullObjectAsEmptyJsonArray(data,"brand");
                            for (int i = 0,size = brands.size();i < size;i++){
                                mBrandList.add(brands.getJSONObject(i));
                            }

                            final JSONArray array = Utils.getNullObjectAsEmptyJsonArray(data,"category");
                            mCategoryList = new JSONArray();
                            default_obj = new JSONObject();
                            default_obj.put("category_id",-1);
                            default_obj.put("name","全部类别");
                            mCategoryList.add(default_obj);

                            parse_category_info(array,mCategoryList);

                        }
                        break;
                }

            }catch (Exception e){
                e.printStackTrace();
                MyDialog.ToastMessageInMainThread(CustomApplication.self().getString(R.string.query_goods_hint,e.getMessage()));
            }
        });
    }
    private void parse_category_info( JSONArray category_jsons, JSONArray categorys){
        for(int i = 0,length = category_jsons.size();i < length;i++) {
            final JSONObject category_json = category_jsons.getJSONObject(i);
            categorys.add(category_json);
            if (category_json.containsKey("childs")) {
                final JSONArray childs = ( JSONArray) category_json.remove("childs");
                if (null != childs)
                    if(childs.size() != 0){
                        parse_category_info(childs,categorys);
                    }
            }
        }
    }

    private void initConditionSpinner(){
        final Spinner spinner = findViewById(R.id._condition_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.drop_down_style);
        adapter.add("条码");
        adapter.add("名称");
        spinner.setAdapter(adapter);
        mConditionSpinner = spinner;
    }

    private void initStockList(){
        final RecyclerView recyclerView = findViewById(R.id.stock_list);
        if (null != recyclerView){
            mAdapter = new MobileStockQueryAdapter(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            recyclerView.setAdapter(mAdapter);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initQueryCondition(){
        final EditText _condition = findViewById(R.id._contition);
        _condition.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (motionEvent.getX() > (_condition.getWidth() - _condition.getCompoundPaddingRight())) {
                    _condition.requestFocus();
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    startActivityForResult(intent, CODE_REQUEST_CODE);
                }
            }
            return false;
        });
        mQueryConditionEt = _condition;
    }

    private void initQueryBtn(){
        final Button _btn = findViewById(R.id.query_btn);
        _btn.setOnClickListener(v -> {
            setQueryCondition();
            if (mAdapter != null)mAdapter.setDatas(mQueryConditionObj);
        });
        mQueryBtn = _btn;
    }

    private void setQueryCondition(){
        String col_name = "barcode";
        if(mConditionSpinner.getSelectedItemPosition() == 1){
            mQueryConditionObj.remove(col_name);
            col_name = "goods_title";
        }else{
            mQueryConditionObj.remove("goods_title");
        }
        if (mQueryConditionEt != null){
            mQueryConditionObj.put(col_name,mQueryConditionEt.getText());
        }
    }

    public void showNumAndAmt(final String stock_num,final String stock_amt){
        final TextView stock_num_tv = findViewById(R.id.stock_num),stock_amt_tv = findViewById(R.id.stock_amt);
        stock_num_tv.setText(stock_num);
        stock_amt_tv.setText(stock_amt);
    }

    private void initBrandAndCategory(){
        final TextView brand_cond = findViewById(R.id.brand_cond),category_cond = findViewById(R.id.category_cond);
        brand_cond.setText(Html.fromHtml("<u>全部品牌</u>"));
        final Drawable drawable = getDrawable(R.drawable.unfold);
        if (drawable != null) drawable.setBounds(0, 0, drawable.getIntrinsicWidth() / 2 , drawable.getIntrinsicHeight() / 2 );
        brand_cond.setCompoundDrawables(null,null,drawable,null);
        brand_cond.setOnClickListener(view -> chooseBrandDialog());
        mBrandCondTv = brand_cond;


        category_cond.setText(Html.fromHtml("<u>全部类别</u>"));
        category_cond.setCompoundDrawables(null,null,drawable,null);
        category_cond.setOnClickListener(view -> chooseCategoryDialog());
        mCategoryCondTv = category_cond;
    }

    private void chooseBrandDialog(){
        final JSONArray brandList = mBrandList;
        if (null != brandList){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final int length = brandList.size();
            final String[] items = new String[length];
            JSONObject object;
            for (int i = 0;i < length;i++){
                object = brandList.getJSONObject(i);
                items[i] = Utils.getNullStringAsEmpty(object,"gb_name");
            }
            builder.setSingleChoiceItems(items, mCurrentBrandIndex, (dialogInterface, i) -> mCurrentBrandIndex = i);
            builder.setPositiveButton("确定", (dialog, which) -> {
                final JSONObject brand = brandList.getJSONObject(mCurrentBrandIndex);
                if (null != brand){
                    if (mCurrentBrandIndex != 0)
                        mQueryConditionObj.put("gb_id",brand.getIntValue("gb_id"));
                    else
                        mQueryConditionObj.remove("gb_id");
                    if (mBrandCondTv != null) mBrandCondTv.setText(Html.fromHtml("<u>" + brand.getString("gb_name") +"</u>"));
                    if (mQueryBtn != null)mQueryBtn.callOnClick();
                }
                dialog.dismiss();
            });
            showAlertDialog(builder,"品牌");
            int i = 0;
        }
    }

    private void chooseCategoryDialog(){
        final JSONArray categoryList = mCategoryList;

        if (null != categoryList){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final int length = categoryList.size();
            final String[] items = new String[length];
            JSONObject object;
            for (int i = 0;i < length;i++){
                object = categoryList.getJSONObject(i);
                if (object != null){
                    if (Utils.getNotKeyAsNumberDefault(object,"category_id",-1) == -1 ){
                        items[i] = Utils.getNullStringAsEmpty(object,"name");
                    }else
                        items[i] = Utils.getNullStringAsEmpty(object,"name") + "(" + Utils.getNullStringAsEmpty(object,"category_code") +")";
                }
            }
            builder.setSingleChoiceItems(items, mCurrentCategoryIndex, (dialogInterface, i) -> mCurrentCategoryIndex = i);
            builder.setPositiveButton("确定", (dialog, which) -> {
                final JSONObject category = categoryList.getJSONObject(mCurrentCategoryIndex);
                if (null != category){
                    if (mCurrentCategoryIndex != 0)
                        mQueryConditionObj.put("category_ids",category.getIntValue("category_id"));
                    else
                        mQueryConditionObj.remove("category_ids");
                    if (mCategoryCondTv != null) mCategoryCondTv.setText(Html.fromHtml("<u>" + category.getString("name") +"</u>"));
                    if (mQueryBtn != null)mQueryBtn.callOnClick();
                }
                dialog.dismiss();
            });
            showAlertDialog(builder,"类别");
        }
    }

    private void showAlertDialog(@NonNull final AlertDialog.Builder builder, final String title){
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });

        final AlertDialog alertDialog = builder.create();

        int blue = getColor(R.color.lightBlue);

        final TextView titleTv = new TextView(this);
        titleTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        titleTv.setPadding(5,5,5,5);
        titleTv.setTextSize(22);
        titleTv.setTextColor(blue);
        titleTv.setText(title);
        alertDialog.setCustomTitle(titleTv);

        alertDialog.show();

        final ListView listView = alertDialog.getListView();
        listView.setDivider(getDrawable(R.color.gray_subtransparent));
        listView.setDividerHeight(1);

        final Button cancel = alertDialog.getButton(BUTTON_NEGATIVE), ok = alertDialog.getButton(BUTTON_POSITIVE);
        cancel.setTextColor(blue);
        cancel.setTextSize(16);

        ok.setTextColor(blue);
        ok.setTextSize(16);
    }
}