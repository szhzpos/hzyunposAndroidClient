package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.EditGoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.dialog.business.EditGoodsCategoryDialog;

import static com.wyc.cloudapp.activity.mobile.business.MobileSelectGoodsActivity.TITLE_KEY;

public class MobileEditGoodsCategoryActivity extends AbstractMobileBaseArchiveActivity {
    private TreeListItem mCurrentCategory;
    private EditGoodsCategoryAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCategoryList();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_add_goods_category;
    }

    @Override
    protected void add() {
        mCurrentCategory = null;
        editCategory(false);
    }
    public void editCategory(boolean modify){
        final EditGoodsCategoryDialog dialog = new EditGoodsCategoryDialog(this,modify);
        dialog.setCategory(mCurrentCategory);
        dialog.show();
    }

    @Override
    protected String title() {
        return getIntent().getStringExtra(TITLE_KEY);
    }

    private void initCategoryList(){
        final RecyclerView item_list = findViewById(R.id.goods_category_list);
        mAdapter = new EditGoodsCategoryAdapter(this,true);
        final JSONArray array = GoodsCategoryAdapter.getCategoryAsTreeListData(this);
        mAdapter.setData(array,null);
        item_list.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(mAdapter);
        mAdapter.setItemListener(object -> mCurrentCategory = object);
    }
    public void addCategory(final String id,final String code,final String name,int level){
        mAdapter.addCategory(id,code,name,level);
    }
    public void updateCategory(final String name){
        mAdapter.updateCurrentItem(name);
    }
}