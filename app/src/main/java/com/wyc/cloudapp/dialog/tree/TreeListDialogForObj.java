package com.wyc.cloudapp.dialog.tree;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.adapter.TreeListAdapterForObj;
import com.wyc.cloudapp.adapter.bean.TreeListItem;

import java.util.List;

public class TreeListDialogForObj extends AbstractTreeListDialog<List<TreeListItem>, TreeListItem> {
    public TreeListDialogForObj(@NonNull Context context, String title) {
        super(context, title);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TreeListAdapterForObj getAdapter() {
        return new TreeListAdapterForObj(mContext,isSingleSelection());
    }
}
