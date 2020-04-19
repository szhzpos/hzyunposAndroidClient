package com.wyc.cloudapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseParameterFragment extends BaseFragment {
    private static final String mTitle = "基本参数";
    private View mRootView;
    private Context mContext;
    public BaseParameterFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject laodContent() {
        try {
            get_save_period(false);
            get_dual_view(false);
            get_goods_img_show(false);
            get_sec_level_category_show(false);
            get_auto_mol(false);
        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.ToastMessage(null,e.getMessage(),mContext,null);
        }
        return null;
    }

    @Override
    public boolean saveContent() {
        JSONObject content = new JSONObject();
        JSONArray array = new JSONArray();
        StringBuilder err = new StringBuilder();
        try {
            content.put("parameter_id","d_s_period");
            content.put("parameter_content",get_save_period(true));
            content.put("parameter_desc","本地数据保存周期");
            array.put(content);

            content = new JSONObject();
            content.put("parameter_id","dual_v");
            content.put("parameter_content",get_dual_view(true));
            content.put("parameter_desc","双屏设置");
            array.put(content);

            content = new JSONObject();
            content.put("parameter_id","g_i_show");
            content.put("parameter_content",get_goods_img_show(true));
            content.put("parameter_desc","显示商品图片设置");
            array.put(content);

            content = new JSONObject();
            content.put("parameter_id","sec_l_c_show");
            content.put("parameter_content",get_sec_level_category_show(true));
            content.put("parameter_desc","二级类别显示设置");
            array.put(content);

            content = new JSONObject();
            content.put("parameter_id","auto_mol");
            content.put("parameter_content",get_auto_mol(true));
            content.put("parameter_desc","自动抹零设置");
            array.put(content);
            if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err,1)){
                MyDialog.ToastMessage(null,err.toString(),mContext,null);
            }else{
                MyDialog.ToastMessage(null,"保存成功！",mContext,null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.ToastMessage(null,e.getMessage(),mContext,null);
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
    public void onAttach(Context context) {
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
        laodContent();
    }

    private void set_save_period(){
        if (mRootView != null){
            RadioGroup group = mRootView.findViewById(R.id.save_period);
            group.setOnCheckedChangeListener((group1, checkedId) -> {
                RadioButton rb = group1.findViewById(checkedId);
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
            Switch sh = mRootView.findViewById(R.id._dual_view_switch);
            View dual_v = mRootView.findViewById(R.id.dual_v);
            sh.setOnCheckedChangeListener((buttonView, isChecked) -> dual_v.setVisibility(isChecked?View.VISIBLE:View.GONE));
        }
    }
    private void auto_mol(){
        if (null != mRootView){
            Switch sh = mRootView.findViewById(R.id.auto_mol_switch);
            RadioGroup rg = mRootView.findViewById(R.id._auto_mol_group);

            sh.setOnCheckedChangeListener((buttonView, isChecked) -> rg.setVisibility(isChecked?View.VISIBLE:View.GONE));
        }
    }

    private JSONObject get_save_period(boolean way) throws JSONException {
        //数据保存周期
        JSONObject value_obj = new JSONObject();
        RadioGroup group = mRootView.findViewById(R.id.save_period);
        RadioButton rb_check = group.findViewById(group.getCheckedRadioButtonId());
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
                group.check(value_obj.optInt("id",R.id._a_month));//触发click事件,不需要单独保存value
            }else{
                MyDialog.ToastMessage("加载数据保存周期参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }
        return value_obj;
    }
    private JSONObject get_dual_view(boolean b) throws JSONException {
        //双屏
        JSONObject value_obj = new JSONObject();
        Switch dual_view_sh = mRootView.findViewById(R.id._dual_view_switch);
        EditText show_interval = mRootView.findViewById(R.id.dualview_img_show_interval);
        int status = 0,interval = 0;
        if (b){
            if (dual_view_sh.isChecked()){
                status = 1;
                try {
                    interval = Integer.valueOf(show_interval.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    interval = 3;
                }
            }
            value_obj.put("s",status);
            value_obj.put("v",interval);
        }else{
            if (SQLiteHelper.getLocalParameter("dual_v",value_obj)){

                status = value_obj.optInt("s",0);
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
    private JSONObject get_goods_img_show(boolean b) throws JSONException {
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
                sh.setChecked(value_obj.optInt("s",1) == 1);//默认显示
            }else{
                MyDialog.ToastMessage("加载商品显示图片参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }

        return value_obj;
    }
    private JSONObject get_sec_level_category_show(boolean b) throws JSONException {
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
                sh.setChecked(value_obj.optInt("s",0) == 1);
            }else{
                MyDialog.ToastMessage("加载商品显示图片参数错误：" + value_obj.getString("info"),mContext,null);
            }
        }
        return value_obj;
    }
    private JSONObject get_auto_mol(boolean b) throws JSONException {
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
                status = value_obj.optInt("s",1);
                if (status == 1){
                    id = value_obj.optInt("id",R.id.mol_j);
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
