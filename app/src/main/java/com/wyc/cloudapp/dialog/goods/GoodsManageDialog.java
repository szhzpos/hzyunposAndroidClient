package com.wyc.cloudapp.dialog.goods;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.activity.mobile.business.NormalEditGoodsInfoActivity;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.GoodsManageViewAdapter;
import com.wyc.cloudapp.adapter.TreeListAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.bean.GoodsCategory;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.utils.Utils;

public final class GoodsManageDialog extends AbstractDialogSaleActivity {
    private Spinner mConditionSpinner;
    private JSONObject mCurrentCategory;
    private GoodsManageViewAdapter mViewAdapter;
    public GoodsManageDialog(@NonNull SaleActivity context) {
        super(context, context.getString(R.string.manage_goods_sz));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCategoryList();
        initConditionSpinner();
        initGoodsDetail();
        initQueryBtn();
        initAddBtn();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.goods_manage_dialog_layout;
    }

    private void initAddBtn(){
        final Button btn = findViewById(R.id._add_goods_btn);
        if (btn != null)
            btn.setOnClickListener(v -> {
                if (AddGoodsInfoDialog.verifyGoodsAddPermissions(mContext)){

                    GoodsCategory category = null;
                    if (mCurrentCategory != null){
                        category = new GoodsCategory(mCurrentCategory.getIntValue(TreeListBaseAdapter.COL_ID),mCurrentCategory.getString("code"),mCurrentCategory.getString(TreeListBaseAdapter.COL_NAME));
                    }
                    NormalEditGoodsInfoActivity.start(mContext,null,category);
                    /*final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(mContext);
                    addGoodsInfoDialog.setCurrentCategory(mCurrentCategory);
                    addGoodsInfoDialog.setFinishListener(barcode -> {
                        addGoodsInfoDialog.dismiss();
                    });
                    addGoodsInfoDialog.show();*/
                }
            });
    }

    private void query(){
        if (mViewAdapter != null)mViewAdapter.setDatas(getWhereCondition(),0);
    }
    private void initQueryBtn(){
        final Button btn = findViewById(R.id._query_btn);
        if (btn != null)
            btn.setOnClickListener(v -> query());
    }

    private void initCategoryList(){
        final RecyclerView item_list = findViewById(R.id.goods_category_list);
        final TreeListAdapter listAdapter = new TreeListAdapter(mContext,true);
        final JSONArray array = GoodsCategoryAdapter.getCategoryAsTreeListData();
        final JSONObject all = new JSONObject();
        all.put(TreeListBaseAdapter.COL_ID,0);
        all.put(TreeListBaseAdapter.COL_NAME,"所有类别");
        array.add(0,all);

        listAdapter.setData(array,null);
        item_list.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(listAdapter);
        listAdapter.setItemListener(object -> {
            if (Utils.getNotKeyAsNumberDefault(object, TreeListBaseAdapter.COL_ID,0) != 0)
                mCurrentCategory = object;
            else
                if (mCurrentCategory != null)mCurrentCategory = null;

            query();
        });
    }

    private void initConditionSpinner(){
        final Spinner spinner = findViewById(R.id._condition_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,R.layout.drop_down_style);
        adapter.add(mContext.getString(R.string.barcode_sz));
        adapter.add(mContext.getString(R.string.only_code));
        adapter.add(mContext.getString(R.string.g_name_sz));
        adapter.add("助 记 码");
        spinner.setAdapter(adapter);
        mConditionSpinner = spinner;
    }

    private void initGoodsDetail(){
        final RecyclerView goods_detail = findViewById(R.id.table_body);
        if (null != goods_detail){
            final GoodsManageViewAdapter goodsManageViewAdapter = new GoodsManageViewAdapter(mContext);
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(goodsManageViewAdapter);
            goodsManageViewAdapter.setDatas(getWhereCondition(),0);
            mViewAdapter = goodsManageViewAdapter;
        }
    }

    protected double getWidthRatio(){
        return 0.98;
    }
    protected double getHeightRatio(){
        return 0.98;
    }


    private String getWhereCondition(){
        final String where_sz = " where ",and_sz = " and ",left_bracket = "(",status_sz = "barcode_status=";
        final StringBuilder condition_sb = new StringBuilder(where_sz);
        final EditText _condition_et = findViewById(R.id._condition_et);
        final String condition_sz = _condition_et.getText().toString();

        if (!condition_sz.isEmpty()){
            switch (mConditionSpinner.getSelectedItemPosition()){
                case 0:
                    condition_sb.append("barcode=").append("'").append(condition_sz).append("'");
                    break;
                case 1:
                    condition_sb.append("only_coding=").append("'").append(condition_sz).append("'");
                    break;
                case 2:
                    condition_sb.append("goods_title=").append("'").append(condition_sz).append("'");
                    break;
                default:
                    condition_sb.append("mnemonic_code=").append("'").append(condition_sz.toUpperCase()).append("'");
                    break;
            }
            condition_sb.append(and_sz);
        }
        int categoryid = Utils.getNotKeyAsNumberDefault(mCurrentCategory,TreeListBaseAdapter.COL_ID,0);
        if (categoryid != 0){
            condition_sb.append("category_id in (").append("select category_id from shop_category where path like '%").append(categoryid).append("%'").append(")");
            condition_sb.append(and_sz);
        }else {
            condition_sb.append("category_id in (").append(GoodsCategoryAdapter.getUsableCategoryString()).append(")");
            condition_sb.append(and_sz);
        }
        //状态
        final CheckBox normal_opt = findViewById(R.id.normal_opt),unshelve_opt = findViewById(R.id.unshelve_opt),deleted_opt = findViewById(R.id.deleted_opt);
        if (normal_opt.isChecked()){
            condition_sb.append(left_bracket).append(status_sz).append(1);
        }
        if (unshelve_opt.isChecked()){
            if (normal_opt.isChecked()){
                condition_sb.append(" or ");
            }else
                condition_sb.append(left_bracket);

            condition_sb.append(status_sz).append(2);
        }

        if (deleted_opt.isChecked()){
            if (normal_opt.isChecked() || unshelve_opt.isChecked()){
                condition_sb.append(" or ");
            }else
                condition_sb.append(left_bracket);

            condition_sb.append(status_sz).append(3).append(" or ").append(status_sz).append(4);;
        }
        if (condition_sb.indexOf(status_sz) != -1)condition_sb.append(") ");
        normal_opt.setOnCheckedChangeListener(opt_change_listener);
        unshelve_opt.setOnCheckedChangeListener(opt_change_listener);
        deleted_opt.setOnCheckedChangeListener(opt_change_listener);

        //处理结尾
        if (where_sz.equals(condition_sb.toString())){
            condition_sb.delete(0,condition_sb.length());
        }else{
            if (condition_sb.toString().endsWith(and_sz)){
                int len = condition_sb.length();
                condition_sb.delete(len - and_sz.length(),len);
            }
        }

        return condition_sb.toString();
    }
    private final CompoundButton.OnCheckedChangeListener opt_change_listener = (buttonView, isChecked) -> query();

}
