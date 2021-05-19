package com.wyc.cloudapp.adapter.business;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.business.MobileVipCategoryInfoActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.bean.VipGrade;
import com.wyc.cloudapp.dialog.business.EditVipCategoryDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileVipCategoryAdapter
 * @Description: 会员类别适配器
 * @Author: wyc
 * @CreateDate: 2021/5/19 16:46
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/19 16:46
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileVipCategoryAdapter extends AbstractDataAdapter<MobileVipCategoryAdapter.MyViewHolder> implements View.OnClickListener  {
    private final MobileVipCategoryInfoActivity mContext;

    public MobileVipCategoryAdapter(MobileVipCategoryInfoActivity c){
        mContext = c;
    }

    protected static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
        TextView _grade_name_tv;
        Button _modify;
        public MyViewHolder(View itemView) {
            super(itemView);
            _grade_name_tv = findViewById(R.id._grade_name_tv);
            _modify = findViewById(R.id._modify);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = View.inflate(mContext, R.layout.mobile_vip_category_info_content_layout,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject object = mDatas.getJSONObject(position);
        holder._grade_name_tv.setText(String.format(Locale.CHINA,"%s - %s",object.getString("grade_sort"),object.getString("grade_name")));
        holder._grade_name_tv.setTag(object.getString("grade_id"));
        if (!holder._modify.hasOnClickListeners())holder._modify.setOnClickListener(this);
        holder._modify.setTag(object);
    }
    @Override
    public void onClick(View v) {
        final JSONObject object = Utils.getViewTagValue(v);
        Logger.d_json(object);
        final VipGrade vipGrade = object.toJavaObject(VipGrade.class);
        EditVipCategoryDialog.start(mContext,vipGrade,true);
    }
}
