package com.wyc.cloudapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class PrintFormatFragment extends BaseFragment {
    private static final String mTitle = "打印格式";
    private View mRootView;
    private Context mContext;
    private int mCurrentFormatId = R.id.checkout_format;
    public PrintFormatFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject laodContent() {

        get_print_format_content(false);

        return null;
    }

    @Override
    public boolean saveContent() {
        final JSONArray array = new JSONArray();
        final StringBuilder err = new StringBuilder();
        array.add(get_print_format_content(true));
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
        return inflater.inflate(R.layout.print_format_content_layout,container);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        final Button save_btn = mRootView.findViewById(R.id.save);

        //保存参数
        save_btn.setOnClickListener(v->saveContent());


        final RadioGroup rg = mRootView.findViewById(R.id.format_rg);
        rg.setOnCheckedChangeListener((group, checkedId) -> laodContent());
        rg.check(mCurrentFormatId);
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

    private JSONObject get_print_format_content(boolean way){
        final JSONObject object = new JSONObject(),content = new JSONObject();

        if (mRootView != null){
            final RadioGroup frg = mRootView.findViewById(R.id.format_rg),fzrg = mRootView.findViewById(R.id.format_size_rg);
            final EditText stores_name = mRootView.findViewById(R.id.stores_name),footer_c = mRootView.findViewById(R.id.footer_c),
                    p_count = mRootView.findViewById(R.id.p_count),footer_space = mRootView.findViewById(R.id.footer_space);
            String parameter_id = "",parameter_desc = "";
            int id = -1;
            switch (frg.getCheckedRadioButtonId()){
                case R.id.checkout_format:
                    parameter_id = "c_f_info";
                    parameter_desc = "结账小票打印格式信息";
                    break;
                case R.id.vip_c_format:
                    parameter_id = "v_f_info";
                    parameter_desc = "充值小票打印格式信息";
                    break;
                case R.id.refund_format:
                    parameter_id = "r_f_info";
                    parameter_desc = "退货小票打印格式信息";
                    break;
                case R.id.transfer_format:
                    parameter_id = "t_f_info";
                    parameter_desc = "交班小票打印格式信息";
                    break;
            }
             if (way){
                 object.put("f",frg.getCheckedRadioButtonId());
                 object.put("f_z",fzrg.getCheckedRadioButtonId());
                 object.put("s_n",stores_name.getText().toString());
                 object.put("f_c",footer_c.getText().toString());
                 object.put("p_c",p_count.getText().toString());
                 object.put("f_s",footer_space.getText().toString());

                 content.put("parameter_id", parameter_id);
                 content.put("parameter_content",object);
                 content.put("parameter_desc",parameter_desc);
             }else{
                 if (SQLiteHelper.getLocalParameter(parameter_id,object)){
                     id = Utils.getNotKeyAsNumberDefault(object,"f",-1);
                     if (id != -1){
                         frg.check(id);
                         fzrg.check(object.getIntValue("f_z"));
                         stores_name.setText(object.getString("s_n"));
                         footer_c.setText(object.getString("f_c"));
                         p_count.setText(Utils.getNullOrEmptyStringAsDefault(object,"p_c","1"));
                         footer_space.setText(Utils.getNullOrEmptyStringAsDefault(object,"f_s","5"));
                     }
                 }else{
                     MyDialog.ToastMessage("加载打印格式参数错误：" + object.getString("info"),mContext,null);
                 }
             }
        }
        return content;
    }


}
