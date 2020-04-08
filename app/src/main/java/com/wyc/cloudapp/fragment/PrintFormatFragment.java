package com.wyc.cloudapp.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrintFormatFragment extends BaseFragment {
    private static final String mTitle = "打印格式";
    private View mRootView;
    private Context mContext;
    public PrintFormatFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject laodContent() {
        try {
            get_print_format_content(false);
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
            content.put("parameter_id", "print_f_info");
            content.put("parameter_content", get_print_format_content(true));
            content.put("parameter_desc", "打印格式信息");
            array.put(content);
            if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err)){
                MyDialog.ToastMessage(null,err.toString(),mContext,null);
            }else{
                MyDialog.ToastMessage(null,"保存成功！",mContext,null);
            }
        }catch (JSONException e){
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
        return inflater.inflate(R.layout.print_format_content_layout,container);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        //保存参数
        mRootView.findViewById(R.id.save).setOnClickListener(v->saveContent());
        //加载参数
        laodContent();
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

    private JSONObject get_print_format_content(boolean way) throws JSONException {
        JSONObject object = new JSONObject();
        if (mRootView != null){
            RadioGroup frg = mRootView.findViewById(R.id.format_rg),fzrg = mRootView.findViewById(R.id.format_size_rg);
            EditText stores_name = mRootView.findViewById(R.id.stores_name),footer_c = mRootView.findViewById(R.id.footer_c),
                    p_count = mRootView.findViewById(R.id.p_count),footer_space = mRootView.findViewById(R.id.footer_space);
             if (way){
                 object.put("f",frg.getCheckedRadioButtonId());
                 object.put("f_z",fzrg.getCheckedRadioButtonId());
                 object.put("s_n",stores_name.getText().toString());
                 object.put("f_c",footer_c.getText().toString());
                 object.put("p_c",p_count.getText().toString());
                 object.put("f_s",footer_space.getText().toString());
             }else{
                 if (SQLiteHelper.getLocalParameter("print_f_info",object)){
                     frg.check(object.optInt("f"));
                     fzrg.check(object.optInt("f_z"));
                     stores_name.setText(object.optString("s_n"));
                     footer_c.setText(object.optString("f_c"));
                     p_count.setText(object.optString("p_c"));
                     footer_space.setText(object.optString("f_s"));
                 }else{
                     MyDialog.ToastMessage("加载打印格式参数错误：" + object.getString("info"),mContext,null);
                 }
             }
        }
        return object;
    }


}
