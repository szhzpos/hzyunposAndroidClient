package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: BuyFullGiveXRuleAdapter
 * @Description: 买满赠送规则适配器
 * @Author: wyc
 * @CreateDate: 2021/3/22 17:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/22 17:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BuyFullGiveXRuleAdapter extends TreeListBaseAdapterForJson<BuyFullGiveXRuleAdapter.MyViewHolder> {

    public BuyFullGiveXRuleAdapter(Context context, boolean single) {
        super(context, single);
    }
    static class MyViewHolder extends TreeListBaseAdapterForJson.MyViewHolder {
        TextView textView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = findViewById(R.id.content);
        }
        @Override
        protected int getContentResourceId() {
            return R.layout.single_text_layout;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(getView());
    }

    @Override
    void bindContent(@NonNull MyViewHolder holder, JSONObject object) {
        holder.textView.setText(object.getString(TreeListBaseAdapter.COL_NAME));
    }

    @Override
    protected int clickItemIndex() {
        return 0;
    }
}
