package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.bean.TreeListItem;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: TreeListAdapterForObj
 * @Description: bean对象树形数据适配器
 * @Author: wyc
 * @CreateDate: 2021-06-22 14:27
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-22 14:27
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TreeListAdapterForObj extends TreeListBaseAdapterWithList<TreeListAdapterForObj.MyViewHolder> {

    public TreeListAdapterForObj(Context context, boolean single) {
        super(context, single);
    }

    @Override
    int getContentLayoutId() {
        return R.id.content;
    }

    @Override
    void bindContent(@NonNull MyViewHolder holder, TreeListItem object) {
        holder.textView.setText(object.getItem_name());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(getView());
    }

    public static class MyViewHolder extends TreeListBaseAdapter.MyViewHolder {
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

    protected int clickItemIndex(){
        return -1;
    }
}
