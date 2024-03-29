package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;


public class TreeListAdapter extends TreeListBaseAdapterForJson<TreeListAdapter.MyViewHolder> {
    public TreeListAdapter(Context context, boolean single) {
        super(context, single);
    }

    public static class MyViewHolder extends TreeListBaseAdapterForJson.MyViewHolder {
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
        holder.textView.setText(object.getString("item_name"));
    }

    @Override
    protected int clickItemIndex() {
        return -1;
    }
}
