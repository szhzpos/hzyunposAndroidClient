package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AddGoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.bean.TreeList;
import com.wyc.cloudapp.logger.Logger;

import static com.wyc.cloudapp.activity.mobile.business.MobileSelectGoodsActivity.TITLE_KEY;

public class MobileAddGoodsCategoryActivity extends AbstractMobileBaseArchiveActivity {
    private TreeList mCurrentCategory;
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

    }

    @Override
    protected String title() {
        return getIntent().getStringExtra(TITLE_KEY);
    }

    private void initCategoryList(){
        final RecyclerView item_list = findViewById(R.id.goods_category_list);
        final AddGoodsCategoryAdapter listAdapter = new AddGoodsCategoryAdapter(this,true);
        final JSONArray array = GoodsCategoryAdapter.getCategoryAsTreeListData(this);
        listAdapter.setData(array,null);
        item_list.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(listAdapter);
        listAdapter.setItemListener(object -> {
            mCurrentCategory = object;
            Logger.d(object);
        });
    }

}