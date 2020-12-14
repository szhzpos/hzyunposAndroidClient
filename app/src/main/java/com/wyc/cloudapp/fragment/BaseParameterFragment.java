package com.wyc.cloudapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.DigitKeyboardPopup;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

public class BaseParameterFragment extends AbstractBaseFragment {
    private static final String mTitle = "基本参数";
    public BaseParameterFragment() {
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
        get_or_show_autoMol(false);
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
        content.put(p_id_key,"auto_mol");
        content.put(p_c_key, get_or_show_autoMol(true));
        content.put(p_desc_key,"自动抹零设置");
        array.add(content);
        if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err,1)){
            MyDialog.ToastMessage(null,err.toString(),mContext,null);
        }else{
            MyDialog.ToastMessage(null,"保存成功！",mContext,null);
        }
        return false;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        //初始化事件
        set_save_period();//数据保存周期
        _dual_view();//双屏设置
        auto_mol();//自动抹零

        mRootView.findViewById(R.id.save).setOnClickListener(v->saveContent());
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

    private void set_save_period(){
        if (mRootView != null){
            final RadioGroup group = mRootView.findViewById(R.id.save_period);
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
    }
    private void _dual_view(){
        if (mRootView != null){
            final Switch sh = mRootView.findViewById(R.id._dual_view_switch);
            final View dual_v = mRootView.findViewById(R.id.dual_v);
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
    }
    private void auto_mol(){
        if (null != mRootView){
            final Switch sh = mRootView.findViewById(R.id.auto_mol_switch);
            final RadioGroup rg = mRootView.findViewById(R.id._auto_mol_group);

            sh.setOnCheckedChangeListener((buttonView, isChecked) -> rg.setVisibility(isChecked?View.VISIBLE:View.GONE));
        }
    }

    private JSONObject get_or_show_saveDataPeriod(boolean way){
        //数据保存周期
        final JSONObject value_obj = new JSONObject();
        final RadioGroup group = mRootView.findViewById(R.id.save_period);
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
                MyDialog.ToastMessage("加载数据保存周期参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }
        return value_obj;
    }
    private JSONObject get_or_show_dual_view(boolean b){
        //双屏
        final JSONObject value_obj = new JSONObject();
        final Switch dual_view_sh = mRootView.findViewById(R.id._dual_view_switch);
        final EditText show_interval = mRootView.findViewById(R.id.dualview_img_show_interval);
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
                View dual_v = mRootView.findViewById(R.id.dual_v);
                if (status == 1){
                    dual_v.setVisibility(View.VISIBLE);
                    show_interval.setText(value_obj.getString("v"));
                }else{
                    dual_v.setVisibility(View.GONE);
                }
            }else{
                MyDialog.ToastMessage("加载双屏设置参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }

        return value_obj;
    }
    private JSONObject get_or_show_goodsImgSetting(boolean b){
        JSONObject value_obj = new JSONObject();
        Switch sh = mRootView.findViewById(R.id._goods_img_show_switch);
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
                MyDialog.ToastMessage("加载商品显示图片参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }

        return value_obj;
    }
    private JSONObject get_or_show_secLevelCategorySetting(boolean b){
        JSONObject value_obj = new JSONObject();
        Switch sh = mRootView.findViewById(R.id._sec_level_category_show);
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
                MyDialog.ToastMessage("加载商品显示图片参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }
        return value_obj;
    }
    private JSONObject get_or_show_autoMol(boolean b) {
        JSONObject value_obj = new JSONObject();
        Switch sh = mRootView.findViewById(R.id.auto_mol_switch);
        int status = 0,value = 0,id = -1;
        if (b){
            if (sh.isChecked()){
                status = 1;
                RadioGroup rg = mRootView.findViewById(R.id._auto_mol_group);
                switch (rg.getCheckedRadioButtonId()){
                    case R.id.mol_y:
                        id = R.id.mol_y;
                        value = 1;
                        break;
                    case R.id.mol_j:
                        id = R.id.mol_j;
                        value = 2;
                        break;
                }
            }
            value_obj.put("id",id);
            value_obj.put("s",status);
            value_obj.put("v",value);
        }else{
            if (SQLiteHelper.getLocalParameter("auto_mol",value_obj)){
                RadioGroup rg = mRootView.findViewById(R.id._auto_mol_group);
                status = Utils.getNotKeyAsNumberDefault(value_obj,"s",0);
                if (status == 1){
                    id =Utils.getNotKeyAsNumberDefault(value_obj,"id",R.id.mol_j);
                    if (id == -1)id = R.id.mol_j;
                    rg.check(id);
                }
                sh.setChecked(status == 1);
                rg.setVisibility(status == 1 ? View.VISIBLE :View.GONE);
            }else{
                MyDialog.ToastMessage("加载自动抹零参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }

        return value_obj;
    }
}
