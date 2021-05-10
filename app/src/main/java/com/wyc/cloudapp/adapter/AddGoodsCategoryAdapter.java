package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.bean.TreeList;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: AddGoodsCategoryAdapter
 * @Description: 商品分类管理适配器
 * @Author: wyc
 * @CreateDate: 2021/5/10 18:21
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/10 18:21
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class AddGoodsCategoryAdapter extends TreeListBaseAdapterWithList<AddGoodsCategoryAdapter.MyViewHolder>{
    public AddGoodsCategoryAdapter(Context context, boolean single) {
        super(context, single);
    }
    static class MyViewHolder extends TreeListBaseAdapterWithList.MyViewHolder {
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
    void bindContent(@NonNull MyViewHolder holder, TreeList object) {
        holder.textView.setText(object.getItem_name());
    }

    @Override
    protected int clickItemIndex() {
        return -1;
    }
}
