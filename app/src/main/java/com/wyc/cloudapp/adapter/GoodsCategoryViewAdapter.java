package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class GoodsCategoryViewAdapter extends RecyclerView.Adapter<GoodsCategoryViewAdapter.MyViewHolder> implements View.OnClickListener{
    private MainActivity mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;//当前选择的类别item
    private GoodsCategoryViewAdapter mChildGoodsCategoryAdpter;
    private RecyclerView mChildGoodsCategoryView;
    private boolean mChildShow = false,mFirstLoad = true;
    public GoodsCategoryViewAdapter(MainActivity context, RecyclerView v){
        this.mContext = context;
        mChildGoodsCategoryView = v;
        laodChildShow();
    }

    @Override
    public void onClick(View view) {
        TextView name,category_id;
        if (null != mCurrentItemView){
            if (mCurrentItemView != view){
                mCurrentItemView.setBackgroundColor(mContext.getResources().getColor(R.color.white,null));
                name = mCurrentItemView.findViewById(R.id.category_name);
                name.setTextColor(mContext.getResources().getColor(R.color.blue,null));

                mCurrentItemView = view;
                mCurrentItemView.setBackgroundColor(mContext.getResources().getColor(R.color.blue,null));
                name = mCurrentItemView.findViewById(R.id.category_name);
                name.setTextColor(mContext.getResources().getColor(R.color.white,null));
            }
        }else{
            view.setBackgroundColor(mContext.getResources().getColor(R.color.blue,null));
            name = view.findViewById(R.id.category_name);
            name.setTextColor(mContext.getResources().getColor(R.color.white,null));
            mCurrentItemView = view;
        }

        category_id = view.findViewById(R.id.category_id);
        if (category_id != null){
            mContext.loadGoods(category_id.getText().toString());
            showSecGoodsType();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView category_id,category_name;
        private View mCurrentLayoutItemView;//当前布局的item
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            category_id = itemView.findViewById(R.id.category_id);
            category_name =  itemView.findViewById(R.id.category_name);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.goods_type_info_content_layout, null);
         if (null == mChildGoodsCategoryView){
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
            if (goods_type_info != null){
                myViewHolder.category_id.setText(goods_type_info.getString("category_id"));
                myViewHolder.category_name.setText(goods_type_info.getString("name"));
                if (i == 1 && mFirstLoad){//一级分类触发第二个类别查询
                    mFirstLoad = false;
                    if (mChildGoodsCategoryView != null){
                        myViewHolder.mCurrentLayoutItemView.callOnClick();
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public void setDatas(int parent_id){
        StringBuilder err = new StringBuilder();
        if (0 == parent_id)
            mDatas = SQLiteHelper.getListToJson("select category_id,name from shop_category where parent_id='0' union select -1 category_id,'组合商品' name ",0,0,false,err);
        else
            mDatas = SQLiteHelper.getListToJson("select category_id,name from shop_category where depth = 2 and parent_id=" + parent_id,0,0,false,err);

        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载类别错误：" + err,mContext,null);
        }
    }

    private void showSecGoodsType(){
            if (mCurrentItemView != null && mChildGoodsCategoryView != null && mChildShow){
                final TextView tv = mCurrentItemView.findViewById(R.id.category_id);
                try{
                    if (mChildGoodsCategoryAdpter == null){
                        mChildGoodsCategoryAdpter = new GoodsCategoryViewAdapter(mContext,null);
                        mChildGoodsCategoryView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
                        mChildGoodsCategoryView.setAdapter(mChildGoodsCategoryAdpter);
                    }
                    mChildGoodsCategoryAdpter.clearCurrentItemView();
                    mChildGoodsCategoryAdpter.setDatas(Integer.valueOf(tv.getText().toString()));
                    if (mChildGoodsCategoryAdpter.getItemCount() == 0){
                        mChildGoodsCategoryView.setVisibility(View.GONE);
                    }else{
                        mChildGoodsCategoryView.setVisibility(View.VISIBLE);
                    }
                    mChildGoodsCategoryAdpter.notifyDataSetChanged();
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
    private void laodChildShow(){
        JSONObject jsonObject = new JSONObject();
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
            if (mChildShow && mChildGoodsCategoryView != null && mChildGoodsCategoryAdpter.mCurrentItemView != null){
                id = ((TextView) mChildGoodsCategoryAdpter.mCurrentItemView.findViewById(R.id.category_id)).getText().toString();
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
        final JSONArray array = SQLiteHelper.getListToJson("SELECT  depth -1 level,category_id item_id, name item_name FROM shop_category where parent_id = " + Utils.getNullOrEmptyStringAsDefault(parent,"item_id","0"),err);
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

}
