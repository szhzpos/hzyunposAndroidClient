package com.wyc.cloudapp.adapter.business;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.business.EditConsumerActivity;
import com.wyc.cloudapp.activity.mobile.business.MobileConsumerInfoActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.adapter.bean.Consumer;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileConsumerAdapter
 * @Description: 客户信息适配器
 * @Author: wyc
 * @CreateDate: 2021/5/19 10:11
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/19 10:11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileConsumerAdapter extends AbstractDataAdapterForList<Consumer,MobileConsumerAdapter.MyViewHolder> implements View.OnClickListener  {
    private final MobileConsumerInfoActivity mContext;

    public MobileConsumerAdapter(MobileConsumerInfoActivity c){
        mContext = c;
    }

    protected static class MyViewHolder extends AbstractDataAdapterForJson.SuperViewHolder{
        TextView _consumer_tv,contacts_name_tv,phone_tv;
        Button _modify;
        public MyViewHolder(View itemView) {
            super(itemView);
            _consumer_tv = findViewById(R.id._consumer_tv);
            contacts_name_tv = findViewById(R.id.contacts_name_tv);
            phone_tv = findViewById(R.id.phone_tv);
            _modify = findViewById(R.id._modify);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = View.inflate(mContext, R.layout.mobile_consumer_content_layout,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Consumer object = mData.get(position);
        holder._consumer_tv.setText(String.format(Locale.CHINA,"%s-%s",object.getCs_code(),object.getCs_name()));
        holder.contacts_name_tv.setText(object.getName());
        holder.phone_tv.setText(object.getMobile());
        if (!holder._modify.hasOnClickListeners())holder._modify.setOnClickListener(this);
        holder._modify.setTag(object);
    }
    @Override
    public void onClick(View v) {
        EditConsumerActivity.start(mContext,true,(Consumer)v.getTag());
    }
}
