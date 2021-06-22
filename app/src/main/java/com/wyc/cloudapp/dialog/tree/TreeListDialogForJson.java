package com.wyc.cloudapp.dialog.tree;

import android.content.Context;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListAdapter;

public class TreeListDialogForJson extends AbstractTreeListDialog<JSONArray,JSONObject> {
     public TreeListDialogForJson(@NonNull Context context, String title) {
        super(context, title);
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.tree_list_dialog_layout;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TreeListAdapter getAdapter() {
        return new TreeListAdapter(mContext,isSingleSelection());
    }

}
