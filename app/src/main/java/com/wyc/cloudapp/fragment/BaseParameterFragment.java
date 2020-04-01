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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseParameterFragment extends BaseFragment {
    private static final String mTitle = "基本参数";
    private View mRootView;
    public BaseParameterFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject laodContent() {

        return null;
    }

    @Override
    public boolean saveContent() {
        String tag;
        JSONObject content = new JSONObject(),value = new JSONObject();

        RadioButton rb = mRootView.findViewById(R.id._a_week);
        try {
            if (rb.isChecked()){
                value.put(rb.getTag().toString(),1);
            }else{
                value.put("v",0);
            }


            content.put("save_period",value);
        } catch (JSONException e) {
            e.printStackTrace();
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
            Logger.d("BaseParameterFragment");
        mRootView = view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
