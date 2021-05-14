package com.wyc.cloudapp.adapter.business;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.business.EditSupplierActivity;
import com.wyc.cloudapp.activity.mobile.business.MobileSupplierInfoActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileSupplierAdapter
 * @Description: 供应商信息适配器
 * @Author: wyc
 * @CreateDate: 2021/5/14 11:14
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/14 11:14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileSupplierAdapter extends AbstractDataAdapter<MobileSupplierAdapter.MyViewHolder> implements View.OnClickListener {
    private final MobileSupplierInfoActivity mContext;

    public MobileSupplierAdapter(MobileSupplierInfoActivity c){
        mContext = c;
    }

    protected static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
        TextView _supplier_tv,contacts_name_tv,phone_tv;
        Button _modify;
        public MyViewHolder(View itemView) {
            super(itemView);
            _supplier_tv = findViewById(R.id._supplier_tv);
            contacts_name_tv = findViewById(R.id.contacts_name_tv);
            phone_tv = findViewById(R.id.phone_tv);
            _modify = findViewById(R.id._modify);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = View.inflate(mContext, R.layout.mobile_supplier_content_layout,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject object = mDatas.getJSONObject(position);
        holder._supplier_tv.setText(String.format(Locale.CHINA,"%s-%s",object.getString("gs_code"),object.getString("gs_name")));
        holder.contacts_name_tv.setText(object.getString("name"));
        holder.phone_tv.setText(object.getString("mobile"));
        if (!holder._modify.hasOnClickListeners())holder._modify.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        EditSupplierActivity.start(mContext,true);
    }
}
