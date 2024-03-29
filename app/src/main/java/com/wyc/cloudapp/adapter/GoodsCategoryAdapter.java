package com.wyc.cloudapp.adapter;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.fragment.BaseParameter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GoodsCategoryAdapter extends RecyclerView.Adapter<GoodsCategoryAdapter.MyViewHolder> implements View.OnClickListener{
    private final SaleActivity mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;//当前选择的类别item
    private GoodsCategoryAdapter mChildGoodsCategoryAdapter;
    private final RecyclerView mSecLevelGoodsCategoryView;
    private boolean mChildShow = false;
    private boolean mFirstLoad = true;
    private final boolean hasShowGroupGoods = BaseParameter.hasShowGroupGoods(null);
    private String mCategoryId;

    private final static List<String> mUsableCategory = new CategoryArrayList();
    private static String mUsableCategoryString = "";

    public GoodsCategoryAdapter(SaleActivity context, RecyclerView v){
        mContext = context;
        mSecLevelGoodsCategoryView = v;
        loadChildShow();
        initUsableCategory();
    }

    public static List<String> getUsableCategory(){
        return new CategoryArrayList(mUsableCategory);
    }

    public static class CategoryArrayList extends ArrayList<String>{
        public CategoryArrayList(List<String> c){
            super(c);
        }
        public CategoryArrayList(){
            super();
        }
        @Override
        public boolean contains(@Nullable @org.jetbrains.annotations.Nullable Object o) {
            if (isEmpty())return true;
            return super.contains(o);
        }
    }

    private void initUsableCategory(){
        CustomApplication.execute(()->{
            final List<TreeListItem>  items = BaseParameter.loadUsableCategory(null);
            JSONArray array;
            String id;
            for (TreeListItem item : items){
                id = item.getItem_id();

                mUsableCategory.add(id);

                array = SQLiteHelper.getListToValue("select category_id from shop_category where path like '%"+ id +"%' and parent_id <> 0 and status = 1",null);
                if (array != null){
                    for (int i = 0,size = array.size();i < size;i ++){
                        mUsableCategory.add(array.getString(i));
                    }
                }
            }

            final StringBuilder ids = new StringBuilder();
            for (String cid : mUsableCategory){
                if (ids.length() != 0){
                    ids.append(",");
                }
                ids.append(cid);
            }
            mUsableCategoryString = ids.toString();

            setData(0);
            Logger.d("usable category:%s", Arrays.toString(mUsableCategory.toArray()));
        });
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


    protected void setViewBackgroundColor(final View view, boolean s){
        TextView name;
        final Resources resources = mContext.getResources();
        int white = resources.getColor(R.color.white,null),blue = resources.getColor(R.color.blue,null);
        int text_color,backgroundColor;
        if (s){
            text_color = white;
            backgroundColor = blue;
            if (mCurrentItemView != view)mCurrentItemView = view;
        }else {
            text_color = blue;
            backgroundColor = white;
        }
        view.setBackgroundColor(backgroundColor);
        name = view.findViewById(R.id.category_name);
        name.setTextColor(text_color);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.goods_type_info_layout, null);
        if (null == mSecLevelGoodsCategoryView){
             itemView.setLayoutParams(new RecyclerView.LayoutParams( (int) mContext.getResources().getDimension(mContext.lessThan7Inches() ? R.dimen.goods_type_width : R.dimen.sec_goods_type_width), (int) mContext.getResources().getDimension(R.dimen.height_50)));
        }else{
            itemView.setLayoutParams(new RecyclerView.LayoutParams( (int) mContext.getResources().getDimension(R.dimen.goods_type_width),ViewGroup.LayoutParams.MATCH_PARENT));
        }
        itemView.setOnClickListener(this);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null && !mDatas.isEmpty()){
            final JSONObject goods_type_info = mDatas.getJSONObject(i);

            final String category_id =Utils.getNullStringAsEmpty(goods_type_info,"category_id");
            myViewHolder.category_id.setText(category_id);

            setViewBackgroundColor(myViewHolder.itemView, category_id.equals(mCategoryId));

            myViewHolder.category_name.setText(String.format(Locale.CHINA,"%s-%s",goods_type_info.getString("category_code"),goods_type_info.getString("name")));
            if (i == (hasShowGroupGoods ? 1 : 0) && mFirstLoad && !mContext.containGoods() && (Utils.lessThan7Inches(mContext) || mSecLevelGoodsCategoryView != null)){//一级分类触发第二个类别查询
                mFirstLoad = false;
                myViewHolder.itemView.callOnClick();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public static String getUsableCategoryString(){
        return mUsableCategoryString;
    }

    private void setData(int parent_id){
        final StringBuilder err = new StringBuilder();
        if (0 == parent_id) {
            String sql = "select category_id,category_code,name from shop_category where parent_id='0' and status = 1";
            if (!mUsableCategory.isEmpty()){
                sql = "select category_id,category_code,name from shop_category where category_id in ("+ getUsableCategoryString() +") and parent_id='0' and status = 1";
            }
            if (hasShowGroupGoods){
                sql = sql + " union select -1 category_id,'' category_code,'组合商品' name " ;
            }

            sql = sql + " order by category_code ";

            mDatas = SQLiteHelper.getListToJson(sql, 0, 0, false, err);
        }else
            mDatas = SQLiteHelper.getListToJson("select category_id,category_code,name from shop_category where depth = 2 and status = 1 and parent_id='" + parent_id +"' order by category_code",0,0,false,err);

        if (mDatas != null){
            notifyItemRangeChanged(0,mDatas.size());
        }else{
            MyDialog.toastMessage(mContext.getString(R.string.load_category_err,err));
        }
    }

    private void showSecGoodsType(){
            if (mCurrentItemView != null && mSecLevelGoodsCategoryView != null && mChildShow){
                final TextView tv = mCurrentItemView.findViewById(R.id.category_id);
                try{
                    if (mChildGoodsCategoryAdapter == null){
                        mChildGoodsCategoryAdapter = new GoodsCategoryAdapter(mContext,null);
                        mSecLevelGoodsCategoryView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
                        mSecLevelGoodsCategoryView.addItemDecoration(new LinearItemDecoration(mContext.getColor(R.color.lightBlue),1));
                        mSecLevelGoodsCategoryView.setAdapter(mChildGoodsCategoryAdapter);
                    }
                    mChildGoodsCategoryAdapter.clearCurrentItemView();
                    mChildGoodsCategoryAdapter.setData(Integer.parseInt(tv.getText().toString()));
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
            MyDialog.ToastMessage("加载是否显示商品二级类别参数错误：" + jsonObject.getString("info"), null);
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

    public static List<TreeListItem> getTopLevelCategory(){
        final List<TreeListItem> items = new ArrayList<>();
        final StringBuilder err = new StringBuilder();
        final JSONArray array = SQLiteHelper.getListToJson("SELECT  depth -1 level,category_id item_id,category_code code, name item_name FROM shop_category where status = 1 and parent_id = 0  order by category_code",err);
        if (array != null){
            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = array.getJSONObject(i);
                final TreeListItem item = new TreeListItem();
                item.setItem_id(object.getString("item_id"));
                item.setCode(object.getString("code"));
                item.setItem_name(object.getString("item_name"));
                items.add(item);
            }
        }else MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.load_category_err,err));
        return items;
    }

    public static JSONArray getCategoryAsTreeListData(){
        final JSONArray categorys = new JSONArray();
        final StringBuilder err = new StringBuilder();
        generateDatas(null,categorys,err);
        if (err.length() != 0)MyDialog.ToastMessage(err.toString(), null);
        return categorys;
    }
    private static void generateDatas(final JSONObject parent,final JSONArray categorys,final StringBuilder err){
        final JSONArray array = SQLiteHelper.getListToJson("SELECT  depth -1 level,category_id item_id,category_code code, name item_name FROM shop_category where "+ (mUsableCategoryString.isEmpty()?"":" category_id in ("+ mUsableCategoryString +") and") +" status = 1 and parent_id = " + Utils.getNullOrEmptyStringAsDefault(parent,"item_id","0") + " order by category_code",err);
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
