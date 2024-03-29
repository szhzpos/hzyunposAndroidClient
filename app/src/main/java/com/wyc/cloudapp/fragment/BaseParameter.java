package com.wyc.cloudapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.DigitKeyboardPopup;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class BaseParameter extends AbstractParameterFragment {
    private static final String mTitle = "基本参数";
    private static final String CATEGORY_SEPARATE = ",";

    private final List<TreeListItem> mSelectedCategoryItem = new ArrayList<>();


    public BaseParameter() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject loadContent() {
        get_or_show_saveDataPeriod(false);
        get_or_show_dual_view(false);
        get_or_show_goodsImgSetting(false);
        get_or_show_secLevelCategorySetting(false);
        get_or_show_goodsGroupSetting(false);
        get_or_show_fast_pay(false);
        get_or_show_cumulative(false);
        get_or_show_usable_category(false);
         return null;
    }

    @Override
    public boolean saveContent() {
        final String p_id_key  = "parameter_id",p_c_key = "parameter_content",p_desc_key = "parameter_desc";
        JSONObject content = new JSONObject();
        final JSONArray array = new JSONArray();
        final StringBuilder err = new StringBuilder();

        content.put(p_id_key,"d_s_period");
        content.put(p_c_key, get_or_show_saveDataPeriod(true));
        content.put(p_desc_key,"本地数据保存周期");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key,"dual_v");
        content.put(p_c_key, get_or_show_dual_view(true));
        content.put(p_desc_key,"双屏设置");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key,"g_i_show");
        content.put(p_c_key, get_or_show_goodsImgSetting(true));
        content.put(p_desc_key,"显示商品图片设置");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key,"sec_l_c_show");
        content.put(p_c_key, get_or_show_secLevelCategorySetting(true));
        content.put(p_desc_key,"二级类别显示设置");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key,"cumulative");
        content.put(p_c_key, get_or_show_cumulative(true));
        content.put(p_desc_key,"商品累加");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key,"goods_group_show");
        content.put(p_c_key, get_or_show_goodsGroupSetting(true));
        content.put(p_desc_key,"显示组合商品");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key,"fast_pay");
        content.put(p_c_key, get_or_show_fast_pay(true));
        content.put(p_desc_key,"自动抹零设置");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key,"usable_category");
        content.put(p_c_key, get_or_show_usable_category(true));
        content.put(p_desc_key,"可用商品类别");
        array.add(content);

        if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err,1)){
            MyDialog.ToastMessage(null,err.toString(), null);
        }else{
            MyDialog.ToastMessage(null,mContext.getString(R.string.save_hint), null);
        }
        return false;
    }

    @Override
    protected void viewCreated() {
        //初始化事件
        set_save_period();//数据保存周期
        _dual_view();//双屏设置
        goodsCategory();//可用商品类别
        findViewById(R.id.save).setOnClickListener(v->saveContent());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_param_content_layout,container);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadContent();
    }

    private void goodsCategory(){
        final TextView goods_category_tv = findViewById(R.id.goods_category_tv);
        assert goods_category_tv != null;
        goods_category_tv.setOnClickListener(v -> {
            final TreeListDialogForObj treeListDialog = new TreeListDialogForObj(mContext,mContext.getString(R.string.d_category_sz));
            treeListDialog.setData(GoodsCategoryAdapter.getTopLevelCategory(),mSelectedCategoryItem,false);
            v.post(()->{
                if (treeListDialog.exec() == 1){
                    final StringBuilder names = new StringBuilder();
                    mSelectedCategoryItem.clear();
                    mSelectedCategoryItem.addAll(treeListDialog.getMultipleContent());
                    for (TreeListItem item : mSelectedCategoryItem){
                        if (names.length() != 0){
                            names.append(CATEGORY_SEPARATE);
                        }
                        names.append(item.getItem_name());
                    }
                    goods_category_tv.setText(names);
                }
            });
        });
    }

    private void set_save_period(){
        final RadioGroup group = findViewById(R.id.save_period);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            final RadioButton rb = group1.findViewById(checkedId);
            switch (checkedId){
                case R.id._a_week:
                    rb.setTag(7);
                    break;
                case R.id._a_month:
                    rb.setTag(30);
                    break;
                case R.id._three_month:
                    rb.setTag(90);
                    break;
            }
        });
    }
    private void _dual_view(){
        final Switch sh = findViewById(R.id._dual_view_switch);
        final View dual_v = findViewById(R.id.dual_v);
        sh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final EditText dualview_img_show_interval_et = dual_v.findViewById(R.id.dualview_img_show_interval);
            if (null != dualview_img_show_interval_et)
                if (isChecked){
                    dual_v.setVisibility(View.VISIBLE);
                    dualview_img_show_interval_et.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus){
                            v.callOnClick();
                        }
                    });
                    dualview_img_show_interval_et.setOnClickListener(v -> {
                        Utils.hideKeyBoard((EditText)v);
                        DigitKeyboardPopup digitKeyboardPopup = new DigitKeyboardPopup(mContext);
                        digitKeyboardPopup.showAsDropDown(v);
                    });
                }else {
                    dual_v.setVisibility(View.GONE);
                    dualview_img_show_interval_et.setOnFocusChangeListener(null);
                    dualview_img_show_interval_et.setOnClickListener(null);
                }
        });
    }

    private JSONObject get_or_show_saveDataPeriod(boolean way){
        //数据保存周期
        final JSONObject value_obj = new JSONObject();
        final RadioGroup group = findViewById(R.id.save_period);
        final RadioButton rb_check = group.findViewById(group.getCheckedRadioButtonId());
        if (way){
            if ( null != rb_check){
                value_obj.put("id",rb_check.getId());
                value_obj.put("v",rb_check.getTag());
            }else{
                value_obj.put("id",-1);
                value_obj.put("v",0);
            }
        }else{
            if (SQLiteHelper.getLocalParameter("d_s_period",value_obj)){
                group.check(Utils.getNotKeyAsNumberDefault(value_obj,"id",R.id._a_month));//触发click事件,不需要单独保存value
            }else{
                MyDialog.ToastMessage("加载数据保存周期参数错误：" + value_obj.getString("info"), null);
            }
        }
        return value_obj;
    }
    private JSONObject get_or_show_dual_view(boolean b){
        //双屏
        final JSONObject value_obj = new JSONObject();
        final Switch dual_view_sh = findViewById(R.id._dual_view_switch);
        final EditText show_interval = findViewById(R.id.dualview_img_show_interval);
        int status = 0,interval = 0;
        if (b){
            if (dual_view_sh.isChecked()){
                status = 1;
                try {
                    interval = Integer.valueOf(show_interval.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    interval = 5;
                }
            }
            value_obj.put("s",status);
            value_obj.put("v",interval);
        }else{
            if (SQLiteHelper.getLocalParameter("dual_v",value_obj)){

                status = value_obj.getIntValue("s");
                dual_view_sh.setChecked(status == 1);
                View dual_v = findViewById(R.id.dual_v);
                if (status == 1){
                    dual_v.setVisibility(View.VISIBLE);
                    show_interval.setText(value_obj.getString("v"));
                }else{
                    dual_v.setVisibility(View.GONE);
                }
            }else{
                MyDialog.ToastMessage("加载双屏设置参数错误：" + value_obj.getString("info"), null);
            }
        }

        return value_obj;
    }
    private JSONObject get_or_show_goodsImgSetting(boolean b){
        JSONObject value_obj = new JSONObject();
        Switch sh = findViewById(R.id._goods_img_show_switch);
        int status = 0;
        if (b){
            if (sh.isChecked()){
                status = 1;
            }
            value_obj.put("s",status);
        }else{
            if (SQLiteHelper.getLocalParameter("g_i_show",value_obj)){
                sh.setChecked(Utils.getNotKeyAsNumberDefault(value_obj,"s",1) == 1);
            }else{
                MyDialog.ToastMessage("加载商品显示图片参数错误：" + value_obj.getString("info"), null);
            }
        }

        return value_obj;
    }
    public static boolean hasPic(){
        final JSONObject jsonObject = new JSONObject();
        if (SQLiteHelper.getLocalParameter("g_i_show",jsonObject)){
            return Utils.getNotKeyAsNumberDefault(jsonObject,"s",1) == 1;
        }else{
            MyDialog.ToastMessage("加载是否显示商品图片参数错误：" + jsonObject.getString("info"), null);
        }
        return false;
    }

    private JSONObject get_or_show_secLevelCategorySetting(boolean b){
        JSONObject value_obj = new JSONObject();
        Switch sh = findViewById(R.id._sec_level_category_show);
        int status = 0;
        if (b){
            if (sh.isChecked()){
                status = 1;
            }
            value_obj.put("s",status);
        }else{
            if (SQLiteHelper.getLocalParameter("sec_l_c_show",value_obj)){
                sh.setChecked(value_obj.getIntValue("s") == 1);
            }else{
                MyDialog.ToastMessage("加载显示二级分类错误：" + value_obj.getString("info"), null);
            }
        }
        return value_obj;
    }

    private JSONObject get_or_show_cumulative(boolean b){
        final JSONObject value_obj = new JSONObject();
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        final Switch sh = findViewById(R.id.cumulative);
        if (sh != null){
            int status = 0;
            if (b){
                if (sh.isChecked()){
                    status = 1;
                }
                value_obj.put("s",status);
            }else{
                sh.setChecked(hasCumulative(value_obj));
            }
        }
        return value_obj;
    }
    public static boolean hasCumulative(JSONObject value_obj){
        if (value_obj == null)value_obj = new JSONObject();
        if (SQLiteHelper.getLocalParameter("cumulative",value_obj)){
            return Utils.getNotKeyAsNumberDefault(value_obj,"s",1) == 1;
        }else{
            MyDialog.ToastMessage("加载商品累加参数错误：" + value_obj.getString("info"), null);
        }
        return false;
    }

    private JSONObject get_or_show_goodsGroupSetting(boolean b){
        JSONObject value_obj = new JSONObject();
        Switch sh = findViewById(R.id.goods_group_show_switch);
        int status = 0;
        if (b){
            if (sh.isChecked()){
                status = 1;
            }
            value_obj.put("s",status);
        }else{
            sh.setChecked(hasShowGroupGoods(value_obj));
        }
        return value_obj;
    }
    public static boolean hasShowGroupGoods(@Nullable JSONObject object){
        if (object == null)object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("goods_group_show",object)){
            return object.getIntValue("s") == 1;
        }else{
            MyDialog.ToastMessage("加载显示组合商品错误：" + object.getString("info"), null);
        }
        return false;
    }

    private JSONObject get_or_show_fast_pay(boolean b) {
        final JSONObject value_obj = new JSONObject();
        Switch sh = findViewById(R.id.fast_pay);
        int status = 0;
        if (b){
            value_obj.put("s",sh.isChecked() ? 1 : 0);
        }else{
            if (SQLiteHelper.getLocalParameter("fast_pay",value_obj)){
                status = Utils.getNotKeyAsNumberDefault(value_obj,"s",1);
                sh.setChecked(status == 1);
            }else{
                MyDialog.ToastMessage("加载开启快捷支付参数错误：" + value_obj.getString("info"), null);
            }
        }
        return value_obj;
    }

    public static boolean hasFastPay(){
        final JSONObject value_obj = new JSONObject();
        if (SQLiteHelper.getLocalParameter("fast_pay",value_obj)){
            return Utils.getNotKeyAsNumberDefault(value_obj,"s",1) == 1;
        }
        return false;
    }

    private JSONObject get_or_show_usable_category(boolean b){
        JSONObject value_obj = new JSONObject();
        if (b){
            value_obj.put("v",JSON.toJSONString(mSelectedCategoryItem));
        }else {
            mSelectedCategoryItem.addAll(loadUsableCategory(value_obj));
            if (!mSelectedCategoryItem.isEmpty()){
                final StringBuilder names = new StringBuilder();
                for (TreeListItem item : mSelectedCategoryItem){
                    if (names.length() != 0){
                        names.append(CATEGORY_SEPARATE);
                    }
                    names.append(item.getItem_name());
                }
                final TextView goods_category_tv = findViewById(R.id.goods_category_tv);
                if (goods_category_tv != null) goods_category_tv.setText(names);
            }
        }
        return value_obj;
    }

    public static List<TreeListItem> loadUsableCategory(JSONObject value_obj){
        if (value_obj == null)value_obj = new JSONObject();
        final List<TreeListItem> items = new ArrayList<>();
        if (SQLiteHelper.getLocalParameter("usable_category",value_obj)){
            items.addAll(JSON.parseObject(Utils.getNullOrEmptyStringAsDefault(value_obj,"v","[]"),new TypeReference<List<TreeListItem>>(){}.getType()));
        }else{
            MyDialog.ToastMessage("加载可用商品分类参数错误：" + value_obj.getString("info"), null);
        }
        return items;
    }
}
