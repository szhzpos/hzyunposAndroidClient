package com.wyc.cloudapp.adapter.business;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.business.MobileVipCategoryInfoActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.bean.VipGrade;
import com.wyc.cloudapp.dialog.business.EditVipCategoryDialog;

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
public class MobileVipCategoryAdapter extends AbstractDataAdapterForList<VipGrade,MobileVipCategoryAdapter.MyViewHolder> implements View.OnClickListener  {
    private final MobileVipCategoryInfoActivity mContext;

    public MobileVipCategoryAdapter(MobileVipCategoryInfoActivity c){
        mContext = c;
    }

    protected static class MyViewHolder extends AbstractDataAdapterForJson.SuperViewHolder{
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
        final VipGrade object = mData.get(position);
        holder._grade_name_tv.setText(String.format(Locale.CHINA,"%s - %s",object.getGrade_sort(),object.getGrade_name()));
        holder._grade_name_tv.setTag(object.getGrade_id());
        if (!holder._modify.hasOnClickListeners())holder._modify.setOnClickListener(this);
        holder._modify.setTag(object);
    }
    @Override
    public void onClick(View v) {
        EditVipCategoryDialog.start(mContext, (VipGrade) v.getTag(),true);
    }
}
