package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoodsCategoryViewAdapter extends RecyclerView.Adapter<GoodsCategoryViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;//当前选择的类别item
    private GoodsInfoViewAdapter mGoodsInfoAdapter;
    private GoodsCategoryViewAdapter mChildGoodsCategoryAdpter;
    private RecyclerView mChildGoodsCategoryView;
    private boolean mChildShow = false;
    public GoodsCategoryViewAdapter(Context context, GoodsInfoViewAdapter adapter, RecyclerView v){
        this.mContext = context;
        this.mGoodsInfoAdapter = adapter;
        mChildGoodsCategoryView = v;

        laodChildShow();
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
             itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,50));
        }else{
            itemView.setLayoutParams(new RecyclerView.LayoutParams(88,ViewGroup.LayoutParams.MATCH_PARENT));
        }

        itemView.setOnClickListener(view -> {
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
            if (null != mGoodsInfoAdapter && category_id != null){
                mGoodsInfoAdapter.setDatas(Integer.valueOf(category_id.getText().toString()));
                showSecGoodsType();
            }
        });
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject goods_type_info = mDatas.optJSONObject(i);
            if (goods_type_info != null){
                myViewHolder.category_id.setText(goods_type_info.optString("category_id"));
                myViewHolder.category_name.setText(goods_type_info.optString("name"));
                if (i == 1){//一级分类触发第二个类别查询
                        if (mChildGoodsCategoryView != null){
                            myViewHolder.mCurrentLayoutItemView.callOnClick();
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.length();
    }

    public JSONObject getItem(int i){
       return mDatas == null ? null : mDatas.optJSONObject(i);
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
                TextView tv = mCurrentItemView.findViewById(R.id.category_id);
                try{
                    if (mChildGoodsCategoryAdpter == null){
                        mChildGoodsCategoryAdpter = new GoodsCategoryViewAdapter(mContext,mGoodsInfoAdapter,null);
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
            mChildShow = jsonObject.optInt("s",0) == 1;
        }else{
            MyDialog.ToastMessage("加载是否显示商品二级类别参数错误：" + jsonObject.optString("info"),mContext,null);
        }
    }

    public void trigger_preView(){
        int id = 0;
        if (mCurrentItemView != null){
            if (mChildGoodsCategoryView != null && mChildGoodsCategoryAdpter.mCurrentItemView != null){
                id = Integer.valueOf(((TextView) mChildGoodsCategoryAdpter.mCurrentItemView.findViewById(R.id.category_id)).getText().toString());
            }else{
                id = Integer.valueOf(((TextView) mCurrentItemView.findViewById(R.id.category_id)).getText().toString());
            }
            if (null != mGoodsInfoAdapter)mGoodsInfoAdapter.setDatas(id);
        }
    }

}
