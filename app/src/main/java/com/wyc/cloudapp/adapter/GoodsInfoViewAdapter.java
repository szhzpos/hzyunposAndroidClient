package com.wyc.cloudapp.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.customizationView.IndicatorRecyclerView;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.viewModel.GoodsViewModel;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.goods.CurPriceDialog;
import com.wyc.cloudapp.dialog.goods.GoodsPriceAdjustDialog;
import com.wyc.cloudapp.fragment.BaseParameter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public final class GoodsInfoViewAdapter extends RecyclerView.Adapter<GoodsInfoViewAdapter.MyViewHolder> implements View.OnClickListener, IndicatorRecyclerView.OnLoad {
    public static final int SPAN_COUNT = BaseParameter.hasPic() ? 5 : 7,MOBILE_SPAN_COUNT = 1;
    /**
    * W_G_MARK 计重、计份并且通过扫条码选择的商品标志
    * */
    public static final String W_G_MARK = "IWG";
    public static final String  SALE_TYPE = "ST";
    private final SaleActivity mContext;
    private JSONArray mDatas;
    private OnGoodsSelectListener mSelectListener;
    private final boolean mShowPic = SPAN_COUNT == 5;
    private View mCurrentItemView;
    private boolean mPriceAdjustMode;
    private static final String NOBARCODEGOODS = "9999999999999";

    private int mPageIndex = 0;
    private static final int mPageNum = 50;
    private int mCategoryId = -1;
    private boolean mLoadMore = true;
    private String mGoodsImgPath = "";

    @SuppressLint("NotifyDataSetChanged")
    public GoodsInfoViewAdapter(final SaleActivity context){
        this.mContext = context;
        if (mShowPic){
            mGoodsImgPath = CustomApplication.getGoodsImgSavePath();
        }
        new ViewModelProvider(context).get(GoodsViewModel.class).init().observe(mContext, array -> {
            mDatas = array;
            updateLoadFlag(array);
            notifyDataSetChanged();
        });
    }

    @Override
    public void onClick(View v) {
        set_selected_status(v);
        invokeListener(getSelectGoodsByIndex());
    }

    @Override
    public void onLoad(@NonNull LOADMODE loadMode) {
        Logger.d("loadMode:%s",loadMode);
        loadMore(loadMode);
    }

    @Override
    public boolean continueLoad() {
        return mLoadMore;
    }

    @Override
    public void onAbort() {
        MyDialog.toastMessage(R.string.cancel_load);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.goods_info_content_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.goods_height));
        itemView.setLayoutParams(lp);
        itemView.setOnClickListener(this);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            final JSONObject goods_info = mDatas.getJSONObject(i);
            final ImageView goods_img = myViewHolder.goods_img;
            final TextView goods_title = myViewHolder.goods_title;
            if (mShowPic){
                final String img_url = Utils.getNullStringAsEmpty(goods_info,"img_url");
                if (Utils.isNotEmpty(img_url)) {
                    Glide.with(myViewHolder.goods_img).load(mGoodsImgPath + img_url.substring(img_url.lastIndexOf("/") + 1)).into(myViewHolder.goods_img);
                } else Glide.with(myViewHolder.goods_img).load(R.drawable.nodish).into(myViewHolder.goods_img);
            }else{
                 if (goods_img.getVisibility() != View.GONE)goods_img.setVisibility(View.GONE);
            }
            myViewHolder.goods_id.setText(goods_info.getString("goods_id"));
            myViewHolder.gp_id.setText(goods_info.getString("gp_id"));
            myViewHolder.unit_name.setText(goods_info.getString("unit_name"));

            myViewHolder.barcode_id.setTag(i);
            myViewHolder.barcode_id.setText(goods_info.getString("barcode_id"));
            myViewHolder.barcode.setText(goods_info.getString("barcode"));
            myViewHolder.price.setText(goods_info.getString("price"));

            goods_title.setText(goods_info.getString("goods_title"));
            goods_title.setTextColor(mContext.getColor(R.color.good_name_color));
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    private void set_selected_status(View v){
        TextView goods_name;
        if(null != mCurrentItemView){
            goods_name = mCurrentItemView.findViewById(R.id.goods_title);
            goods_name.clearAnimation();
            goods_name.setTextColor(mContext.getColor(R.color.good_name_color));
        }
        goods_name = v.findViewById(R.id.goods_title);
        goods_name.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
        goods_name.setTextColor(mContext.getColor(R.color.blue));

        if (mCurrentItemView != v)mCurrentItemView = v;
    }

    private JSONObject getSelectGoodsByIndex(){
        if (mCurrentItemView != null){
            final TextView barcode_id_tv = mCurrentItemView.findViewById(R.id.barcode_id);
            int index = Utils.getViewTagValue(barcode_id_tv,-1);
            if (0 <= index && index < mDatas.size()){
                return mDatas.getJSONObject(index);
            }
        }
        return null;
    }
    public void showAdjustPriceDialog(@NonNull View anchor){
        mPriceAdjustMode = true;

        final Snackbar snackbar = Snackbar.make(anchor,R.string.price_adjust_sz, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAnchorView(anchor);
        final View snackbar_view = snackbar.getView();
        snackbar_view.setTranslationX(Utils.dpToPx(mContext,121));
        snackbar_view.setBackgroundResource(R.drawable.snackbar_background);
        final Button btn = snackbar_view.findViewById(R.id.snackbar_action);
        final TextView tvSnackbarText = snackbar_view.findViewById(R.id.snackbar_text);
        tvSnackbarText.setTextSize(22);
        if (null != btn)btn.setTextSize(20);
        snackbar.setActionTextColor(mContext.getColor(R.color.orange_1));
        snackbar.setAction("点击退出调价模式!", v -> mPriceAdjustMode = false);
        snackbar.show();
    }

    public void loadGoodsByCategoryId(final String id){
        if ("-1".equals(id) || GoodsCategoryAdapter.getUsableCategory().contains(id)){
            try {
                resetLoadMoreParam();
                mCategoryId = Integer.parseInt(id);
                setData(mCategoryId);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }else {
            if (mDatas != null){
                mDatas.clear();
                notifyDataSetChanged();
            }
        }
    }
    private void resetLoadMoreParam(){
        mPageIndex = 0;
        mLoadMore = true;
    }
    private void setData(int id){
        new ViewModelProvider(mContext).get(GoodsViewModel.class).refresh(getSql(id));
    }
    @SuppressLint("NotifyDataSetChanged")
    private void loadMore(LOADMODE orientation){
        mPageIndex++;
        final JSONArray array = GoodsViewModel.load(getSql(mCategoryId));
        updateLoadFlag(array);
        if (array != null && !array.isEmpty()){
            if (orientation == LOADMODE.BEHIND)
                mDatas.addAll(array);
            else mDatas.addAll(1,array);
            CustomApplication.postAtFrontOfQueue(this::notifyDataSetChanged);
        }
    }
    private void updateLoadFlag(final JSONArray array){
        mLoadMore = array != null && array.size() == mPageNum;
    }

    private String getSql(int id){
        final String sql;
        if (-1 == id){
            final String usableCategory = GoodsCategoryAdapter.getUsableCategoryString();
            sql = "select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    " -1  barcode_id,ifnull(gp_code,'') barcode,type,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = 1 and barcode_status = 1" + (usableCategory.isEmpty()?"":" and category_id in (" + usableCategory +")");
        }else{
            final String cols = "-1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(case type when 2 then only_coding else barcode end,'') barcode, " +
                    "type,retail_price price,ifnull(img_url,'') img_url ";

            sql = "select "+ cols +" from barcode_info where goods_status = 1 and barcode_status = 1 and barcode='"+NOBARCODEGOODS+"' and category_id = '"+ id +"'  UNION " +
                    "select "+ cols +" from barcode_info where (goods_status = 1 and barcode_status = 1 and barcode<>'"+NOBARCODEGOODS+"') and category_id in (select category_id from shop_category where path like '%" + id +"%') limit "+ mPageIndex * mPageNum + "," + mPageNum;
        }
        return sql;
    }

    public boolean fuzzy_search_goods(@NonNull final String search_content,boolean autoSelect){
        boolean code = false;
        final StringBuilder err = new StringBuilder();
        final ContentValues barcodeRuleObj = new ContentValues();
        final String usableCategory = GoodsCategoryAdapter.getUsableCategoryString();
        String sql_where,full_sql,sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(case type when 2 then only_coding else barcode end,'') barcode,only_coding,type,retail_price price\n" +
                ",ifnull(img_url,'') img_url from barcode_info where (goods_status = 1 and barcode_status = 1) "+ (usableCategory.isEmpty()?"":" and category_id in (" + usableCategory +")") + " and %1";

        JSONArray array = null;
        if (isBarcodeWeighingGoods(search_content,barcodeRuleObj)){
            sql_where = "only_coding = '" + barcodeRuleObj.getAsString("item_id") + "'";
            full_sql = sql.replace("%1",sql_where);
            final JSONObject object = new JSONObject();
            if (SQLiteHelper.execSql(object,full_sql)){
                array = new JSONArray();
                if (!object.isEmpty()){
                    object.put(W_G_MARK,search_content);
                    array.add(object);
                }
            }else {
                err.append(Utils.getNullStringAsEmpty(object,"info"));
            }
        }else {
            sql_where = "(barcode like '%" + search_content + "%' or only_coding like '%" + search_content +"%' or mnemonic_code like '%" + search_content +"%' or " +
                    "barcode_id in (select barcode_id from auxiliary_barcode_info where status = 1 and fuzhu_barcode = '" + search_content + "'))";
            full_sql = sql.replace("%1",sql_where) + " UNION select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    "-1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,type,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = '1' and " + sql_where;

            array = SQLiteHelper.getListToJson(full_sql,0,0,false,err);
        }

        Logger.d("full_sql:%s",full_sql);

        if (array != null){
            if (code = !array.isEmpty()){
                if (autoSelect && array.size() == 1){
                    invokeListener(array.getJSONObject(0));
                }else {
                    mDatas = array;
                    notifyDataSetChanged();
                }
            }
        }else{
            MyDialog.ToastMessage("搜索商品错误：" + err, null);
        }
        return code;
    }

    private void invokeListener(final JSONObject goods){
        if (goods != null){
            final JSONObject content = new JSONObject();
            if (getSingleSaleGoods(content,goods.getString(GoodsInfoViewAdapter.W_G_MARK),getGoodsId(goods))){
                boolean noBarcode = GoodsInfoViewAdapter.isNoBarcodeGoods(content);
                if (noBarcode || GoodsInfoViewAdapter.isCurPriceGoods(content)){
                    final CurPriceDialog dialog = new CurPriceDialog(mContext,content.getString("goods_title"),noBarcode ? 0 : 1);
                    if (dialog.exec() == 1){
                        content.put("retail_price",dialog.getPrice());
                        content.put("price",dialog.getPrice());
                        content.put("xnum",dialog.getNum());
                    }else {
                        return;
                    }
                }

                if (mPriceAdjustMode){
                    final GoodsPriceAdjustDialog priceAdjustDialog = new GoodsPriceAdjustDialog(mContext,content);
                    priceAdjustDialog.show();
                }else {
                    if (mSelectListener != null)mSelectListener.onSelect(content);
                }
            }
        }
    }

    public interface OnGoodsSelectListener {
        void onSelect(final JSONObject object);
    }

    public void setOnGoodsSelectListener(OnGoodsSelectListener listener) {
        mSelectListener = listener;
    }
    /**
     * @param object 查询商品输出参数。需要判断是否为空来判断查询内容是否存在
     * @param weigh_barcode_info 称重条码信息。如果不为空则使用货号为id查询商品
     * @param id 查询商品id。通过@linkgetGoodsId()获取
     * */
    public boolean getSingleSaleGoods(@NonNull JSONObject object, final String weigh_barcode_info, final String id){
        if (!object.isEmpty())object.clear();

        final String full_sql,sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,only_coding,ifnull(type,0) type," +
                "brand_id,gs_id,a.category_id category_id,b.path path,retail_price,retail_price price,tc_rate,tc_mode,tax_rate,ps_price,cost_price,trade_price,buying_price,yh_mode,yh_price," +
                "metering_id,current_goods,conversion from barcode_info a inner join shop_category b on a.category_id = b.category_id where goods_status = 1 and barcode_status = 1 and ";

        boolean isWeighBarcode = Utils.isNotEmpty(weigh_barcode_info);
        if (isWeighBarcode){
            full_sql = sql + "only_coding = '" + id + "'";
        }else
            full_sql = sql + "barcode_id = " + id + " UNION select gp_id ,-1 goods_id,ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name, -1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,ifnull(type,0) type," +
                    "'' brand_id,'' gs_id, '' category_id,'' path,gp_price retail_price,gp_price price,0 tc_rate,0 tc_mode,0 tax_rate,0 ps_price,0 cost_price,0 trade_price,gp_price buying_price,0 yh_mode,0 yh_price,1 metering_id,0 current_goods,1 conversion from goods_group \n" +
                    "where status = 1 and gp_id = " + id;
        boolean code =  SQLiteHelper.execSql(object,full_sql);
        if (code){
            if (object.isEmpty()){
                MyDialog.toastMessage(R.string.not_found_goods_hint);
                return false;
            }
            final String category_id = Utils.getNullStringAsEmpty(object,"category_id");
            if (!category_id.isEmpty() && !GoodsCategoryAdapter.getUsableCategory().contains(category_id)){
                MyDialog.toastMessage(R.string.not_found_goods_hint);
                return false;
            }

            makeCommonSaleType(object);

            if (isWeighBarcode){
                code = parseElectronicBarcode(object,weigh_barcode_info);
            }else {
                code = getPromotionInfo(object,mContext.getStoreId(),mContext.getVipGradeId());
            }
        }else {
            MyDialog.toastMessage(object.getString("info"));
        }
       return code;
    }

    public boolean isPriceAdjustMode(){
        return mPriceAdjustMode;
    }

    public static boolean getPromotionInfo(final JSONObject goods, final String stores_id,final String grade_id){
        final StringBuilder err = new StringBuilder();

        final String brand_id  = goods.getString("brand_id"),gs_id = goods.getString("gs_id"),category_id = goods.getString("path"),barcode_id = goods.getString("barcode_id");

        final String where_sql = "where  status = 1 and ((type_detail_id = '"+ barcode_id +"' and promotion_type=1 ) or " +
                "(instr('" + category_id +"' ,type_detail_id||'@') > 0 and promotion_type=2 )" +
                "  or (type_detail_id = '"+ gs_id +"' and promotion_type=3 )  or (type_detail_id = '" + brand_id +"' and promotion_type= 4)) and " +
                "(promotion_object = 0 or ((promotion_object = 2 and "+ grade_id +" > 0) or promotion_grade_id = "+ grade_id +")) and " +
                "stores_id = " + stores_id + " and date(start_date, 'unixepoch', 'localtime') || ' ' ||begin_time  <= datetime('now', 'localtime') \n" +
                " and datetime('now', 'localtime') <= date(end_date, 'unixepoch', 'localtime') || ' ' ||end_time and \n" +
                "promotion_week like '%' ||case strftime('%w','now' ) when 0 then 7 else strftime('%w','now' ) end||'%'";

        double price = goods.getDoubleValue("retail_price");

        final String sql = "select tlp_id,1 tj_type,type_detail_id,promotion_type,way,-1.0 xnum_one,-1.0 limit_xnum_one,0.0 promotion_price_one,-1.0 xnum_two,0.0 limit_xnum_two,0.0 promotion_price_two,-1.0 xnum_three,0.0 limit_xnum_three,0.0 promotion_price_three," +
                "-1.0 xnum_four,0.0 limit_xnum_four,0.0 promotion_price_four," +
                "-1.0 xnum_five,limit_xnum limit_xnum_five,case way when 1 then case when promotion_price < "+ price + " then promotion_price else " + price +" end "+
                "when 2 then promotion_price/10.0 *"+ price +" end promotion_price_five from promotion_info " + where_sql +

                " union all " +

                "select tlp_id,2 tj_type,type_detail_id,promotion_type,way,xnum_one,0.0 limit_xnum_one,case way when 1 then case when promotion_price_one < "+ price + " then promotion_price_one else " + price +" end " +
                "when 2 then promotion_price_one/10.0 *"+ price +" end promotion_price_one," +
                "xnum_two,0.0 limit_xnum_two,case way when 1 then case when promotion_price_two < "+ price + " then promotion_price_two else " + price +" end " +
                "when 2 then promotion_price_two/10.0 *"+ price +" end promotion_price_two," +
                "xnum_three,0.0 limit_xnum_three,case way when 1 then case when promotion_price_three < "+ price + " then promotion_price_three else " + price +" end " +
                "when 2 then promotion_price_three/10.0 *"+ price +" end promotion_price_three," +
                "xnum_four,0.0 limit_xnum_four,case way when 1 then case when promotion_price_four < "+ price + " then promotion_price_four else " + price +" end " +
                "when 2 then promotion_price_four/10.0 *"+ price +" end promotion_price_four," +
                "xnum_five,0.0 limit_xnum_five, case way when 1 then case when promotion_price_five < "+ price + " then promotion_price_five else " + price +" end " +
                "when 2 then promotion_price_five/10.0 *"+ price +" end promotion_price_five from step_promotion_info "+ where_sql;

        boolean code;
        JSONArray array;
        if (code = (array = SQLiteHelper.getListToJson(sql,err)) != null){
            if (!array.isEmpty()){
                makeSpecialPromotionSaleType(goods);
                goods.put("promotion_rules",array);
            }
        }else {
            goods.put("info",err);
        }
        Logger.d("PromotionGoodsSQL：%s",sql);
        return code;
    }

    public static boolean isNoBarcodeGoods(final JSONObject goods){
        return NOBARCODEGOODS.equals(Utils.getNullStringAsEmpty(goods,"barcode"));
    }
    public static boolean isCurPriceGoods(final JSONObject goods){
        return Utils.getNotKeyAsNumberDefault(goods,"current_goods",-1) == 1;
    }

    public static int getSaleType(final JSONObject goods){
        return Utils.getNotKeyAsNumberDefault(goods,SALE_TYPE,0);
    }

    public static boolean isSpecialPromotion(final JSONObject goods){
        return  0 != (getSaleType(goods) & 0x2);
    }
    public static void makeSpecialPromotionSaleType(final JSONObject goods){
        /*零售特价与正常销售不兼容*/
        if (null != goods)goods.put(SALE_TYPE,((getSaleType(goods) & 0xFFFFFFFE) | 0x2));
    }
    public static void clearSpecialPromotionSaleType(final JSONObject goods){//清除零售特价标志
        if (null != goods)goods.put(SALE_TYPE,getSaleType(goods) & 0xFFFFFFFD);
    }

    public static void makeCommonSaleType(final JSONObject goods){
        /*正常销售与零售特价、买X送X不兼容*/
        if (null != goods)goods.put(SALE_TYPE,0x1);
    }
    public static boolean isCommon(final JSONObject goods){
        return  0 != (getSaleType(goods) & 0x1);
    }

    public static void makeBuyXGiveX(final JSONObject goods){
        /*买X送X与正常销售不兼容*/
        if (null != goods)goods.put(SALE_TYPE,((getSaleType(goods) & 0xFFFFFFFE) | 0x4));
    }
    public static boolean isBuyXGiveX(final JSONObject goods){
        return  0 != (getSaleType(goods) & 0x4);
    }
    public static void makeBuyFullGiveX(final JSONObject goods){
        /*买满送X与正常销售不兼容*/
        if (null != goods)goods.put(SALE_TYPE,((getSaleType(goods) & 0xFFFFFFFE) | 0x8));
    }
    public static boolean isBuyFullGiveX(final JSONObject goods){
        return  0 != (getSaleType(goods) & 0x8);
    }

    private boolean parseElectronicBarcode(@NonNull final JSONObject object,@NonNull final String weigh_barcode_info){
        boolean code = true;
        ContentValues barcodeRuleObj = new ContentValues();
        if (code = parseBarcodeRule(weigh_barcode_info,barcodeRuleObj)){
            double xnum = 0.0,price = 0.0;
            int metering_id = object.getIntValue("metering_id");
            int amt_point = barcodeRuleObj.getAsInteger("moneyLen");
            double amt = barcodeRuleObj.getAsDouble("amt") / Math.pow(10,amt_point);

            if (barcodeRuleObj.containsKey("weight") && barcodeRuleObj.containsKey("amt")){
                int w_point = barcodeRuleObj.getAsInteger("weightLen");
                double weight = barcodeRuleObj.getAsDouble("weight") / Math.pow(10,w_point);
                if (metering_id == 0){//计重
                    xnum = weight;
                }else{//计份
                    xnum = 1;
                }
                price = (Utils.equalDouble(weight,0.0) ? 0 : amt / weight);
            }else{
                price = object.getDoubleValue("retail_price");
                if (metering_id == 0){//计重
                    xnum = (Utils.equalDouble(price,0.0) ? 0.0 : amt / price);
                }else{//计份
                    xnum = 1;
                }
            }
            Logger.d("price：%f,xnum:%f,sale_amt:%f",price,xnum,amt);

            object.put("price",price);
            object.put("xnum",String.format(Locale.CHINA,"%.4f",xnum));
            object.put("sale_amt",amt);
            object.put(W_G_MARK,weigh_barcode_info);

            code = getPromotionInfo(object,mContext.getStoreId(),mContext.getVipGradeId());
        }else {
            object.put("info",barcodeRuleObj.getAsString("info"));
        }
        return code;
    }
    public static boolean isBarcodeWeighingGoods(final JSONObject goods){
        return Utils.isNotEmpty(goods.getString(GoodsInfoViewAdapter.W_G_MARK));
    }
    public static boolean isWeighingGoods(final JSONObject goods){//type 1 普通 2散装称重 3鞋帽
        return Utils.getNotKeyAsNumberDefault(goods,"type",-1) == 2;
    }

    public String getGoodsId(final JSONObject jsonObject){
        String id;
        if (!"".equals(Utils.getNullStringAsEmpty(jsonObject,W_G_MARK))){//计重、计份并且通过扫条码选择的商品标志
            id = jsonObject.getString("only_coding");
        }else{
            id = jsonObject.getString("barcode_id");
            if ("-1".equals(id)){//组合商品
                id = jsonObject.getString("gp_id");
            }
        }
        return id;
    }

    public void updateGoodsInfo(final @NonNull JSONArray array){
        if (!array.isEmpty()){
            final JSONObject object = getSelectGoodsByIndex(),new_object = array.getJSONObject(0);
            if (object != null && null != new_object && object.getIntValue("barcode_id") == new_object.getIntValue("barcode_id")){
                for(final String key : new_object.keySet()){
                    object.put(key,new_object.getString(key));
                }
                try {
                    notifyItemChanged(Utils.getViewTagValue(mCurrentItemView.findViewById(R.id.barcode_id),0));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isBarcodeWeighingGoods(final String barcode,final ContentValues object){
        boolean code = true;
        final JSONObject scale_setting = new JSONObject();
        if (code = loadScaleSetting(scale_setting)){
            if (code = !barcode.isEmpty()){
                final String prefix = Utils.getNullStringAsEmpty(scale_setting,"prefix");
                final String barcodeRule = scale_setting.getString("barcodeRule");
                int length = prefix.length(),barcode_len = barcode.length();
                if ((code = barcode_len >= length)){
                    final String tmp = barcode.substring(0,length);
                    if ((code = prefix.equals(tmp))){
                        int start = barcodeRule.indexOf('W'),end = barcodeRule.lastIndexOf('W');
                        if (end < barcode_len){
                            object.put("item_id",barcode.substring(start,end + 1));
                        }
                    }
                    Logger.d("barcode:%s,prefix:%s,tmp:%s,object:%s",barcode,prefix,tmp,object.toString());
                }
            }
        }
        return code;
    }
    private boolean parseBarcodeRule(@NonNull final String barcode,@NonNull final ContentValues object){
        boolean code = true;
        final JSONObject scale_setting = new JSONObject();
        if (code = loadScaleSetting(scale_setting)){
            final String barcodeRule = scale_setting.getString("barcodeRule");
            if (code = barcode.length() == barcodeRule.length()){
                String sub_sz;
                StringBuilder sb = new StringBuilder();
                char tmp;
                for (int i = 0,size = barcodeRule.length();i < size;i++){
                    tmp = barcodeRule.charAt(i);
                    if (sb.length() == 0){
                        sb.append(tmp);
                    }else{
                        if (tmp == sb.charAt(0)){
                            sb.append(tmp);
                        }else{
                            //*F=电子秤前缀 W=货号 E=金额 N=重量 C=核验 0=反校验
                            int length = sb.length(),start = i - length;
                            sub_sz = barcode.substring(start,i);
                            Logger.d("barcode:%s,parse_str:%s,tmp:%s,sub_sz:%s",barcode,sb,tmp,sub_sz);
                            switch (sb.charAt(0)){
                                case 'F':
                                    object.put("prefix",sub_sz);
                                    break;
                                case 'W':
                                    object.put("item_id",sub_sz);
                                    break;
                                case 'E':
                                    object.put("amt",sub_sz);
                                    break;
                                case 'N':
                                    object.put("weight",sub_sz);
                                    break;
                                case 'C':
                                    object.put("check",sub_sz);
                                    break;
                                case '0':
                                    object.put("uncheck",sub_sz);
                                    break;

                            }
                            sb.delete(0,length).append(tmp);
                        }
                    }
                }
                object.put("moneyLen",scale_setting.getIntValue("moneyLen"));
                object.put("weightLen",scale_setting.getIntValue("weightLen"));

                Logger.d("条码解析内容：%s",object.toString());
            }else {
                object.put("info","输入条码长度与参数设置的不一致");
            }
        }
        return code;
    }

    private boolean loadScaleSetting(final JSONObject scaleSetting){
        return MyDialog.ToastMessage(null,"加载条码秤参数错误：" + scaleSetting.getString("info"), null,SQLiteHelper.getLocalParameter("scale_setting",scaleSetting));
    }
}
