package com.wyc.cloudapp.dialog;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListAdapter;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnContextImp;

public class TreeListDialog extends DialogBaseOnContextImp {
    private TreeListAdapter mAdapter;
    private JSONArray mDatas;
    private boolean mSingle;
    public TreeListDialog(@NonNull Context context, String title) {
        super(context, title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initList();
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.tree_select_dialog_layout;
    }

    private void initList(){
        final RecyclerView item_list = findViewById(R.id.item_list);
        final TreeListAdapter listAdapter = new TreeListAdapter(mContext,mSingle);
        listAdapter.setDatas(mDatas);
        item_list.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(listAdapter);

        mAdapter = listAdapter;
    }

    public TreeListDialog setDatas(final JSONArray obj,boolean b){
        mSingle = b;
        mDatas = obj;
        return this;
    }
}
