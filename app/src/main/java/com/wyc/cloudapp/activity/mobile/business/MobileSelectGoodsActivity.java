package com.wyc.cloudapp.activity.mobile.business;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.GoodsInfoItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.goods.AddGoodsInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class MobileSelectGoodsActivity extends AbstractMobileActivity {
    public static final int SELECT_GOODS_CODE = 0x147;
    private GoodsAdapter mGoodsInfoAdapter;
    private CategoryAdapter mGoodsCategoryAdapter;
    private EditText mSearchContentEt;
    private static boolean isSelectMode = false;//true 选择商品模式
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        initTitle();

        initGoodsCategory();
        initGoodsInfo();
        initSearchContent();

        checkSearch();
    }

    private void initTitle(){
        final Intent intent = getIntent();
        if (intent != null){
            setMiddleText(intent.getStringExtra("title"));
            isSelectMode = intent.getBooleanExtra("isSel",false);
        }
        setRightText(getString(R.string.a_goods_sz));
        setRightListener(v -> {
            if (AddGoodsInfoDialog.verifyGoodsAddPermissions(this)){
                final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(this);
                final JSONObject category = mGoodsCategoryAdapter.CategoryObj;
                addGoodsInfoDialog.setCurrentCategory(category);
                addGoodsInfoDialog.setFinishListener(barcode -> {
                    addGoodsInfoDialog.dismiss();
                    loadGoods(category.getString("item_id"));
                });
                addGoodsInfoDialog.show();
            }
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_select_goods;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.business_search_goods);
        search.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                return new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            }

            @Override
            protected char[] getReplacement() {
                return new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            }
        });
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                search(search.getText().toString());
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    search(search.getText().toString());
                }
            }
            return false;
        });

        mSearchContentEt = search;
    }

    private void checkSearch(){
        final Intent intent = getIntent();
        if (intent != null){
            final String barcode = intent.getStringExtra("barcode");
            if (Utils.isNotEmpty(barcode) && mSearchContentEt != null){
                mSearchContentEt.setText(barcode);
                mSearchContentEt.post(()->search(barcode));
            }
        }
    }

    private void search(final String id){
        mGoodsInfoAdapter.fuzzy_search_goods(id,true);
    }

    private void initGoodsInfo(){
        final RecyclerView business_goods_info_list = findViewById(R.id.business_goods_info_list);
        mGoodsInfoAdapter = new GoodsAdapter(this);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this,GoodsInfoViewAdapter.MOBILE_SPAN_COUNT);
        business_goods_info_list.setLayoutManager(gridLayoutManager);
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(business_goods_info_list,getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration());

        mGoodsInfoAdapter.setOnSelectFinish(barcode_id -> {
            if (isSelectMode){
                final Intent intent = new Intent();
                intent.putExtra("barcode_id",barcode_id);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        business_goods_info_list.setAdapter(mGoodsInfoAdapter);
    }

    private void initGoodsCategory(){
        final RecyclerView goods_type_view = findViewById(R.id.business_goods_type_list);
        mGoodsCategoryAdapter = new CategoryAdapter(this);
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        if (isSelectMode)
            mGoodsCategoryAdapter.setDatas(-2);//业务单据商品选择不加载组合商品
        else
            mGoodsCategoryAdapter.setDatas(0);
        goods_type_view.setAdapter(mGoodsCategoryAdapter);
    }

    private void loadGoods(final String id){
        mGoodsInfoAdapter.loadGoodsByCategoryId(id);
    }

    static String getGoodsName(final JSONObject object){
        return Utils.getNullStringAsEmpty(object,"goods_title");
    }

    static private class CategoryAdapter extends AbstractDataAdapter<CategoryAdapter.MyViewHolder> implements View.OnClickListener{
        final MainActivity mContext;
        private View mCurrentItemView;
        private final JSONObject CategoryObj = new JSONObject();
        public CategoryAdapter(MainActivity activity) {
            mContext = activity;
        }

        @Override
        public void onClick(View view) {
            TextView name_tv = view.findViewById(R.id.category_name),category_id = view.findViewById(R.id.category_id);
            if (category_id != null && name_tv != null){
                final Resources resources = mContext.getResources();
                int white = resources.getColor(R.color.white,null),blue = resources.getColor(R.color.blue,null);
                if (null != mCurrentItemView){
                    if (mCurrentItemView != view){
                        mCurrentItemView.setBackgroundColor(white);
                        name_tv = mCurrentItemView.findViewById(R.id.category_name);
                        name_tv.setTextColor(blue);

                        mCurrentItemView = view;
                        mCurrentItemView.setBackgroundColor(blue);
                        name_tv = view.findViewById(R.id.category_name);
                        name_tv.setTextColor(white);
                    }
                }else{
                    view.setBackgroundColor(blue);
                    name_tv.setTextColor(white);
                    mCurrentItemView = view;
                }
                final String id = category_id.getText().toString(),name = name_tv.getText().toString();

                CategoryObj.put("item_id",id);
                CategoryObj.put("item_name",name);

                ((MobileSelectGoodsActivity)mContext).loadGoods(id);
            }
        }

        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder {
            private final TextView category_id;
            private final TextView category_name;
            MyViewHolder(View itemView) {
                super(itemView);
                category_id = itemView.findViewById(R.id.category_id);
                category_name =  itemView.findViewById(R.id.category_name);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View itemView = View.inflate(mContext, R.layout.goods_type_info_layout, null);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.height_50)));
            itemView.setOnClickListener(this);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            if (mDatas != null){
                final JSONObject goods_type_info = mDatas.getJSONObject(position);
                holder.category_id.setText(goods_type_info.getString("category_id"));
                holder.category_name.setText(goods_type_info.getString("name"));
                if (position == 1) holder.itemView.callOnClick();
            }
        }

        public void setDatas(int parent_id){
            final StringBuilder err = new StringBuilder();
            switch (parent_id){
                case 0:
                    mDatas = SQLiteHelper.getListToJson("select category_id,name from shop_category where parent_id='0' and status = 1 union select -1 category_id,'组合商品' name ",0,0,false,err);
                    break;
                case -2:
                    mDatas = SQLiteHelper.getListToJson("select category_id,name from shop_category where parent_id='0' and status = 1",0,0,false,err);
                    break;
                default:
                    mDatas = SQLiteHelper.getListToJson("select category_id,name from shop_category where depth = 2 and status = 1 and parent_id=" + parent_id,0,0,false,err);
            }
            if (mDatas != null){
                this.notifyDataSetChanged();
            }else{
                MyDialog.ToastMessage("加载类别错误：" + err,mContext,null);
            }
        }
    }

    static private class GoodsAdapter extends AbstractDataAdapter<GoodsAdapter.MyViewHolder> implements View.OnClickListener{
        final MainActivity mContext;
        private OnSelectFinish onSelectFinish;
        public GoodsAdapter(MainActivity activity) {
            mContext = activity;
        }

        @Override
        public void onClick(View v) {
            final TextView tv = v.findViewById(R.id.barcode_id);
            if (null != tv && onSelectFinish != null)onSelectFinish.onFinish(tv.getText().toString());
        }

        public interface OnSelectFinish{
            void onFinish(final String barcode_id);
        }

        public void setOnSelectFinish(OnSelectFinish listener) {
            this.onSelectFinish = listener;
        }

        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder {
            TextView gp_id,goods_id,goods_title,unit_name,barcode_id,barcode,price;
            ImageView goods_img;
            MyViewHolder(View itemView) {
                super(itemView);
                goods_img = itemView.findViewById(R.id.g_img);
                goods_id = itemView.findViewById(R.id.goods_id);
                gp_id = itemView.findViewById(R.id.gp_id);
                goods_title =  itemView.findViewById(R.id.goods_title);
                unit_name =  itemView.findViewById(R.id.unit_name);
                barcode_id =  itemView.findViewById(R.id.barcode_id);
                barcode =  itemView.findViewById(R.id.barcode);
                price =  itemView.findViewById(R.id.sale_price);
            }
        }

        @NonNull
        @Override
        public GoodsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View itemView = View.inflate(mContext, R.layout.goods_info_content_layout, null);
            final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.goods_height));
            itemView.setLayoutParams(lp);
            itemView.setOnClickListener(this);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {
            if (mDatas != null){
                final JSONObject goods_info = mDatas.getJSONObject(position);
                final TextView goods_title = myViewHolder.goods_title;
                myViewHolder.goods_img.setVisibility(View.GONE);
                myViewHolder.goods_id.setText(goods_info.getString("goods_id"));
                myViewHolder.gp_id.setText(goods_info.getString("gp_id"));
                myViewHolder.unit_name.setText(goods_info.getString("unit_name"));

                myViewHolder.barcode_id.setText(goods_info.getString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.getString("barcode"));

                if (isSelectMode)
                    myViewHolder.price.setText(goods_info.getString("price"));
                else {
                    myViewHolder.price.setText(goods_info.getString("retail_price"));
                }

                goods_title.setText(goods_info.getString("goods_title"));
                if(goods_title.getCurrentTextColor() != mContext.getResources().getColor(R.color.good_name_color,null)){
                    goods_title.setTextColor(mContext.getColor(R.color.good_name_color));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
                }
            }
        }

        public void loadGoodsByCategoryId(final String id){
            try {
                setDatas(Integer.parseInt(id));
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        private void setDatas(int id){

            final StringBuilder err = new StringBuilder();
            final String sql;
            if (-1 == id){
                sql = "select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                        " -1  barcode_id,ifnull(gp_code,'') barcode,type,gp_price price,gp_price retail_price,ifnull(img_url,'') img_url from goods_group \n" +
                        "where status = 1";
            }else{
                sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(case type when 2 then only_coding else barcode end,'') barcode," +
                        "type,buying_price price,retail_price,ifnull(img_url,'') img_url from barcode_info where (goods_status = 1 and barcode_status = 1) and category_id in (select category_id from shop_category where path like '%" + id +"%')";
            }

            mDatas = SQLiteHelper.getListToJson(sql,0,0,false,err);
            if (mDatas != null){
                this.notifyDataSetChanged();
            }else{
                MyDialog.ToastMessage("加载商品错误：" + err,mContext,null);
            }
        }

        public void fuzzy_search_goods(@NonNull final String search_content,boolean autoSelect){
            final StringBuilder err = new StringBuilder();
            final ContentValues barcodeRuleObj = new ContentValues();
            String sql_where,full_sql,sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(case type when 2 then only_coding else barcode end,'') barcode,only_coding,type,retail_price price\n" +
                    ",ifnull(img_url,'') img_url from barcode_info where (goods_status = 1 and barcode_status = 1) and %1";


            sql_where = "(barcode like '%" + search_content + "%' or only_coding like '%" + search_content +"%' or mnemonic_code like '%" + search_content +"%')";
            full_sql = sql.replace("%1",sql_where) + " UNION select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    "-1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,type,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = '1' and " + sql_where;

            final JSONArray array  = SQLiteHelper.getListToJson(full_sql,0,0,false,err);

            Logger.d("full_sql:%s",full_sql);

            if (array != null){
                if (isSelectMode && autoSelect && array.size() == 1){
                    if (onSelectFinish != null)onSelectFinish.onFinish(array.getJSONObject(0).getString("barcode_id"));
                }else {
                    mDatas = array;
                    notifyDataSetChanged();
                }
            }else{
                MyDialog.ToastMessage("搜索商品错误：" + err,mContext,null);
            }
        }
    }
}