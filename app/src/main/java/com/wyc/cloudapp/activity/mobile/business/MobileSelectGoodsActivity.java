package com.wyc.cloudapp.activity.mobile.business;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.constants.WholesalePriceType;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.GoodsInfoItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.goods.AddGoodsInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class MobileSelectGoodsActivity extends AbstractMobileBaseArchiveActivity {
    public static final String TITLE_KEY = "title",IS_SEL_KEY = "isSel",PRICE_TYPE_KEY = "price_type",TASK_CATEGORY_KEY = "taskCategory",SEARCH_KEY = "barcode";

    public static final int SELECT_GOODS_CODE = 0x147;
    private GoodsAdapter mGoodsInfoAdapter;
    private CategoryAdapter mGoodsCategoryAdapter;
    private EditText mSearchContentEt;
    /*true 商品选择模式*/
    private static boolean isSelectMode = false;
    private static int mPriceType = WholesalePriceType.BUYING_PRICE;
    /*需要过滤商品类别*/
    private String mGoodsCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initExtra();

        initGoodsCategory();
        initGoodsInfo();
        initSearchContent();

        checkSearch();
    }
    private void initExtra(){
        final Intent intent = getIntent();
        if (intent != null){
            isSelectMode = intent.getBooleanExtra(IS_SEL_KEY,false);
            mPriceType = intent.getIntExtra(PRICE_TYPE_KEY,WholesalePriceType.BUYING_PRICE);
            mGoodsCategory = intent.getStringExtra(TASK_CATEGORY_KEY);

            Logger.d("priceType:%d,taskCategory：%s",mPriceType, mGoodsCategory);
        }
    }

    @Override
    protected void add() {
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
    }

    @Override
    protected String title() {
        return getIntent().getStringExtra(TITLE_KEY);
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
            final String barcode = intent.getStringExtra(SEARCH_KEY);
            if (Utils.isNotEmpty(barcode) && mSearchContentEt != null){
                mSearchContentEt.setText(barcode);
                CustomApplication.runInMainThread(()->search(barcode));
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
        final MobileSelectGoodsActivity mContext;
        private View mCurrentItemView;
        private final JSONObject CategoryObj = new JSONObject();
        public CategoryAdapter(MobileSelectGoodsActivity activity) {
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

                mContext.loadGoods(id);
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
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.height_40)));
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
                    if (Utils.isNotEmpty(mContext.mGoodsCategory)){
                        mDatas = SQLiteHelper.getListToJson("select category_id,name from shop_category where status = 1 and category_id in("+ mContext.mGoodsCategory +")",0,0,false,err);
                    }else
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
        final MobileSelectGoodsActivity mContext;
        private OnSelectFinish onSelectFinish;
        public GoodsAdapter(MobileSelectGoodsActivity activity) {
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

                final String img_url = Utils.getNullStringAsEmpty(goods_info,"img_url");
                if (!"".equals(img_url)){
                    final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                    CustomApplication.execute(()->{
                        final Bitmap bitmap = BitmapFactory.decodeFile(CustomApplication.getGoodsImgSavePath() + szImage);
                        CustomApplication.runInMainThread(()-> myViewHolder.goods_img.setImageBitmap(bitmap));
                    });
                }else{
                    myViewHolder.goods_img.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                }

                myViewHolder.goods_id.setText(goods_info.getString("goods_id"));
                myViewHolder.gp_id.setText(goods_info.getString("gp_id"));
                myViewHolder.unit_name.setText(goods_info.getString("unit_name"));

                myViewHolder.barcode_id.setText(goods_info.getString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.getString("barcode"));

                if (isSelectMode) {
                    String key = "price";
                    switch (mPriceType){//1零售价，2优惠价，3配送价，4批发价，5参考进货价
                        case WholesalePriceType.RETAIL_PRICE:
                            key = "retail_price";
                            break;
                        case WholesalePriceType.COST_PRICE:
                            key = "cost_price";
                            break;
                        case WholesalePriceType.PS_PRICE:
                            key = "ps_price";
                            break;
                        case WholesalePriceType.TRADE_PRICE:
                            key = "trade_price";
                            break;
                        default:
                    }
                    myViewHolder.price.setText(goods_info.getString(key));
                }else {
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

        private final String common_sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(case type when 2 then only_coding else barcode end,'') barcode," +
                "type,buying_price price,retail_price,cost_price,ps_price,trade_price,ifnull(img_url,'') img_url from barcode_info " +
                "where (goods_status = 1 and barcode_status = 1) and ";

        private void setDatas(int id){

            final StringBuilder err = new StringBuilder();
            final String sql;
            if (-1 == id){
                sql = "select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                        " -1  barcode_id,ifnull(gp_code,'') barcode,type,gp_price price,gp_price retail_price,0 cost_price,0 ps_price,0 trade_price,ifnull(img_url,'') img_url from goods_group \n" +
                        "where status = 1";
            }else{
                sql = common_sql + " category_id in (select category_id from shop_category where path like '%" + id +"%')";
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
            String sql_where,full_sql;

            sql_where = "(barcode like '%" + search_content + "%' or only_coding like '%" + search_content +"%' or mnemonic_code like '%" + search_content +"%')";
            if (Utils.isNotEmpty(mContext.mGoodsCategory)){
                sql_where = sql_where.concat(" and category_id in (" + mContext.mGoodsCategory +")");
            }
            full_sql = common_sql + sql_where + " UNION select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    "-1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,type,gp_price price,0 cost_price,0 ps_price,0 trade_price,ifnull(img_url,'') img_url from goods_group \n" +
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