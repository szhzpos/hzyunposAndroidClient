package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class GoodsCategoryAdapter extends RecyclerView.Adapter<GoodsCategoryAdapter.MyViewHolder> implements View.OnClickListener{
    private final SaleActivity mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;//当前选择的类别item
    private GoodsCategoryAdapter mChildGoodsCategoryAdapter;
    private final RecyclerView mSecLevelGoodsCategoryView;
    private boolean mChildShow = false,mFirstLoad = true;
    private String mCategoryId;
    public GoodsCategoryAdapter(SaleActivity context, RecyclerView v){
        mContext = context;
        mSecLevelGoodsCategoryView = v;
        loadChildShow();
    }

    @Override
    public void onClick(View view) {
        TextView category_id;
        if (null != mCurrentItemView){
            if (mCurrentItemView != view){
                setViewBackgroundColor(mCurrentItemView,false);
                setViewBackgroundColor(view,true);
            }
        }else{
            setViewBackgroundColor(view,true);
        }

        category_id = view.findViewById(R.id.category_id);
        if (category_id != null){
            mCategoryId = category_id.getText().toString();
            mContext.loadGoods(mCategoryId);
            showSecGoodsType();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView category_id;
        private final TextView category_name;
        MyViewHolder(View itemView) {
            super(itemView);
            category_id = itemView.findViewById(R.id.category_id);
            category_name =  itemView.findViewById(R.id.category_name);
        }
    }

    @Override
    public void onViewRecycled (MyViewHolder holder){
        if (holder.itemView == mCurrentItemView){
            setViewBackgroundColor(mCurrentItemView,false);
        }
    }

    protected void setViewBackgroundColor(final View view, boolean s){
        TextView name;
        final Resources resources = mContext.getResources();
        int white = resources.getColor(R.color.white,null),blue = resources.getColor(R.color.blue,null);
        int text_color,backgroundColor;
        if (s){
            text_color = white;
            backgroundColor = blue;
        }else {
            text_color = blue;
            backgroundColor = white;
        }
        view.setBackgroundColor(backgroundColor);
        name = view.findViewById(R.id.category_name);
        name.setTextColor(text_color);
        if (mCurrentItemView != view)mCurrentItemView = view;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.goods_type_info_layout, null);
        if (null == mSecLevelGoodsCategoryView){
             itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.height_50)));
        }else{
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT));
        }
        itemView.setOnClickListener(this);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            final JSONObject goods_type_info = mDatas.getJSONObject(i);

            final String category_id =Utils.getNullStringAsEmpty(goods_type_info,"category_id");
            myViewHolder.category_id.setText(category_id);

            if (category_id.equals(mCategoryId)){
                setViewBackgroundColor(myViewHolder.itemView,true);
            }

            myViewHolder.category_name.setText(String.format(Locale.CHINA,"%s-%s",goods_type_info.getString("category_code"),goods_type_info.getString("name")));
            if (i == 1 && mFirstLoad && !mContext.containGoods() && (Utils.lessThan7Inches(mContext) || mSecLevelGoodsCategoryView != null)){//一级分类触发第二个类别查询
                mFirstLoad = false;
                myViewHolder.itemView.callOnClick();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public void setDatas(int parent_id){
        final StringBuilder err = new StringBuilder();
        if (0 == parent_id)
            mDatas = SQLiteHelper.getListToJson("select category_id,category_code,name from shop_category where parent_id='0' and status = 1 union select -1 category_id,-2 category_code,'组合商品' name ",0,0,false,err);
        else
            mDatas = SQLiteHelper.getListToJson("select category_id,category_code,name from shop_category where depth = 2 and status = 1 and parent_id=" + parent_id,0,0,false,err);

        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载类别错误：" + err,mContext,null);
        }
    }

    private void showSecGoodsType(){
            if (mCurrentItemView != null && mSecLevelGoodsCategoryView != null && mChildShow){
                final TextView tv = mCurrentItemView.findViewById(R.id.category_id);
                try{
                    if (mChildGoodsCategoryAdapter == null){
                        mChildGoodsCategoryAdapter = new GoodsCategoryAdapter(mContext,null);
                        mSecLevelGoodsCategoryView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
                        mSecLevelGoodsCategoryView.setAdapter(mChildGoodsCategoryAdapter);
                    }
                    mChildGoodsCategoryAdapter.clearCurrentItemView();
                    mChildGoodsCategoryAdapter.setDatas(Integer.parseInt(tv.getText().toString()));
                    if (mChildGoodsCategoryAdapter.getItemCount() == 0){
                        mSecLevelGoodsCategoryView.setVisibility(View.GONE);
                    }else{
                        mSecLevelGoodsCategoryView.setVisibility(View.VISIBLE);
                    }
                    mChildGoodsCategoryAdapter.notifyDataSetChanged();
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
    }
    private void clearCurrentItemView(){
        if (mCurrentItemView != null){
            mCurrentItemView.setBackgroundColor(mContext.getResources().getColor(R.color.white,null));
            TextView name = mCurrentItemView.findViewById(R.id.category_name);
            name.setTextColor(mContext.getResources().getColor(R.color.blue,null));
            mCurrentItemView = null;
        }
    }
    private void loadChildShow(){
        final JSONObject jsonObject = new JSONObject();
        if (SQLiteHelper.getLocalParameter("sec_l_c_show",jsonObject)){
            mChildShow = jsonObject.getIntValue("s") == 1;
        }else{
            MyDialog.ToastMessage("加载是否显示商品二级类别参数错误：" + jsonObject.getString("info"),mContext,null);
        }
    }

    public void trigger_preView(){
        Logger.d("trigger_preView");
        if (mCurrentItemView != null){
            final String id;
            if (mChildShow && mSecLevelGoodsCategoryView != null && mChildGoodsCategoryAdapter.mCurrentItemView != null){
                id = ((TextView) mChildGoodsCategoryAdapter.mCurrentItemView.findViewById(R.id.category_id)).getText().toString();
            }else{
                id = ((TextView) mCurrentItemView.findViewById(R.id.category_id)).getText().toString();
            }
            mContext.loadGoods(id);
        }
    }
    public static JSONArray getCategoryAsTreeListData(final Context context){
        final JSONArray categorys = new JSONArray();
        final StringBuilder err = new StringBuilder();
        generateDatas(null,categorys,err);
        if (err.length() != 0)MyDialog.ToastMessage(err.toString(),context,null);
        return categorys;
    }
    private static void generateDatas(final JSONObject parent,final JSONArray categorys,final StringBuilder err){
        final JSONArray array = SQLiteHelper.getListToJson("SELECT  depth -1 level,category_id item_id,category_code code, name item_name FROM shop_category where status = 1 and parent_id = " + Utils.getNullOrEmptyStringAsDefault(parent,"item_id","0") + " order by category_code",err);
        if (array != null){
            JSONObject item_json;
            JSONArray kids;
            for (int i = 0,size = array.size();i < size;i++){
                item_json = array.getJSONObject(i);
                item_json.put("unfold",false);
                item_json.put("isSel",false);
                item_json.put("kids",new JSONArray());
                if (parent != null){
                    item_json.put("p_ref",parent);
                    kids = parent.getJSONArray("kids");
                    kids.add(item_json);
                }
                generateDatas(item_json,null,err);

                if (categorys != null)categorys.add(item_json);
            }
        }
    }

    public static boolean getCategoryPath(final JSONObject out,final String id){
        return SQLiteHelper.execSql(out,"SELECT path FROM shop_category where category_id = '"+ id +"'");
    }

}
