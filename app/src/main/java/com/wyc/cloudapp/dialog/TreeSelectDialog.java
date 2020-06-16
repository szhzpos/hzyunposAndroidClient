package com.wyc.cloudapp.dialog;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnContextImp;

public class TreeSelectDialog extends DialogBaseOnContextImp {
    private TreeListAdapter mAdapter;
    public TreeSelectDialog(@NonNull Context context, String title) {
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
        final TreeListAdapter listAdapter = new TreeListAdapter(mContext,false);
        listAdapter.setDatas(SQLiteHelper.getListToJson("SELECT category_id item_id,name item_name FROM shop_category where parent_id = 0",null));
        item_list.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(listAdapter);

        mAdapter = listAdapter;
    }

}
