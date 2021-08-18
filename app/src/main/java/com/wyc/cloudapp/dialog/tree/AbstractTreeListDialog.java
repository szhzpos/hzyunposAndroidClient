package com.wyc.cloudapp.dialog.tree;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogContext;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.tree
 * @ClassName: AbstractTreeList
 * @Description: 树形对话框基类
 * @Author: wyc
 * @CreateDate: 2021-06-22 11:55
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-22 11:55
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractTreeListDialog<D,S> extends AbstractDialogContext {
    private TreeListBaseAdapter<D,S,AbstractDataAdapter.SuperViewHolder> mAdapter;
    private D mData,mSelectedItems;
    private boolean mSingleSelection;

    public AbstractTreeListDialog(@NonNull Context context, String title) {
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
        mAdapter = getAdapter();
        mAdapter.setData(mData,mSelectedItems);
        item_list.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(mAdapter);

    }

    @Override
    protected double getWidthRatio() {
        return 0.98;
    }

    public AbstractTreeListDialog<D,S> setData(final D obj, final D selectItems, boolean singleSelection){
        mSingleSelection = singleSelection;
        mData = obj;
        mSelectedItems = selectItems;
        return this;
    }

    public D getMultipleContent(){
        return mAdapter.getMultipleSelectedContent();
    }

    public S getSingleContent(){
        return mAdapter.getSingleSelectedContent();
    }

    protected abstract <T extends TreeListBaseAdapter<D,S,AbstractDataAdapter.SuperViewHolder>> T getAdapter();

    private void initBtn(){
        final Button ok = findViewById(R.id.t_ok),cancel = findViewById(R.id.t_cancel);
        if (ok != null && cancel != null){
            ok.setOnClickListener(v -> setCodeAndExit(1));
            cancel.setOnClickListener(v -> setCodeAndExit(0));
        }
    }
    protected boolean isSingleSelection(){
        return mSingleSelection;
    }
}
