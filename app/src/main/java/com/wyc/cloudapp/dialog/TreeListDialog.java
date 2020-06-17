package com.wyc.cloudapp.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListAdapter;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnContextImp;
import com.wyc.cloudapp.logger.Logger;

public class TreeListDialog extends DialogBaseOnContextImp {
    private TreeListAdapter mAdapter;
    private JSONArray mDatas,mSelectedItems;
    private boolean mSingle;

    public TreeListDialog(@NonNull Context context, String title) {
        super(context, title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initList();
        initBtn();
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.tree_list_dialog_layout;
    }

    private void initList(){
        final RecyclerView item_list = findViewById(R.id.item_list);
        final TreeListAdapter listAdapter = new TreeListAdapter(mContext,mSingle);
        listAdapter.setDatas(mDatas,mSelectedItems);
        item_list.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(listAdapter);
        mAdapter = listAdapter;
    }

    public TreeListDialog setDatas(final JSONArray obj,final JSONArray selectItems,boolean b){
        mSingle = b;
        mDatas = obj;
        mSelectedItems = selectItems;
        return this;
    }

    public JSONArray getMultipleContent(){
        if (mAdapter != null)return mAdapter.getMultipleSelectedContent();
        return new JSONArray();
    }

    public JSONObject getSingleContent(){
        if (mAdapter != null)return mAdapter.getSingleSelectedContent();
        return new JSONObject();
    }

    private void initBtn(){
        final Button ok = findViewById(R.id.t_ok),cancel = findViewById(R.id.t_cancel);
        if (ok != null && cancel != null){
            ok.setOnClickListener(v -> setCodeAndExit(1));
            cancel.setOnClickListener(v -> setCodeAndExit(0));
        }
    }

}
