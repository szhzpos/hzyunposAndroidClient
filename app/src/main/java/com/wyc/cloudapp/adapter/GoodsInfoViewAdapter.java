package com.wyc.cloudapp.adapter;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.goods.GoodsPriceAdjustDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public final class GoodsInfoViewAdapter extends RecyclerView.Adapter<GoodsInfoViewAdapter.MyViewHolder> implements View.OnClickListener {
    public static final int SPAN_COUNT = 5,MOBILE_SPAN_COUNT = 1;
    public static final String W_G_MARK = "IWG";//计重、计份并且通过扫条码选择的商品标志
    private final SaleActivity mContext;
    private JSONArray mDatas;
    private OnGoodsSelectListener mSelectListener;
    private boolean mShowPic = true;
    private View mCurrentItemView;
    private boolean mPriceAdjustMode;
    public GoodsInfoViewAdapter(final SaleActivity context){
        this.mContext = context;
        final JSONObject jsonObject = new JSONObject();
        if (SQLiteHelper.getLocalParameter("g_i_show",jsonObject)){
            mShowPic = (Utils.getNotKeyAsNumberDefault(jsonObject,"s",1) == 1);
        }else{
            MyDialog.ToastMessage("加载是否显示商品图片参数错误：" + jsonObject.getString("info"),mContext,null);
        }
    }

    @Override
    public void onClick(View v) {
        set_selected_status(v);
        Utils.disableView(v,300);
        if (mSelectListener != null)mSelectListener.onSelect(getSelectGoodsByIndex());
    }

    final static class SALE_TYPE{
        static final int COMMON = 0,SPECIAL_PROMOTION = 1;
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
                if (!"".equals(img_url)){
                    final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                    CustomApplication.execute(()->{
                        final Bitmap bitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                        goods_img.post(()-> myViewHolder.goods_img.setImageBitmap(bitmap));
                    });
                }else{
                    goods_img.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                }
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
            if(goods_title.getCurrentTextColor() != mContext.getResources().getColor(R.color.good_name_color,null)){
                goods_title.setTextColor(mContext.getColor(R.color.good_name_color));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
            }

            myViewHolder.itemView.setOnClickListener(this);
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
        try {
            setDatas(Integer.valueOf(id));
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    private void setDatas(int id){

        final StringBuilder err = new StringBuilder();
        final String sql;
        if (-1 == id){
            sql = "select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    " -1  barcode_id,ifnull(gp_code,'') barcode,type,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = 1";
        }else{
            sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(case type when 2 then only_coding else barcode end,'') barcode," +
                    "type,retail_price price,ifnull(img_url,'') img_url from barcode_info where (goods_status = 1 and barcode_status = 1) and category_id in (select category_id from shop_category where path like '%" + id +"%')";
        }

        mDatas = SQLiteHelper.getListToJson(sql,0,0,false,err);
        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载商品错误：" + err,mContext,null);
        }
    }

    public boolean fuzzy_search_goods(@NonNull final String search_content,boolean autoSelect){
        boolean code = false;
        final StringBuilder err = new StringBuilder();
        final ContentValues barcodeRuleObj = new ContentValues();
        String sql_where,full_sql,sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(case type when 2 then only_coding else barcode end,'') barcode,only_coding,type,retail_price price\n" +
                ",ifnull(img_url,'') img_url from barcode_info where (goods_status = 1 and barcode_status = 1) and %1";

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
            sql_where = "(barcode like '%" + search_content + "%' or only_coding like '%" + search_content +"%' or mnemonic_code like '%" + search_content +"%')";
            full_sql = sql.replace("%1",sql_where) + " UNION select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    "-1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,type,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = '1' and " + sql_where;

            array = SQLiteHelper.getListToJson(full_sql,0,0,false,err);
        }

        Logger.d("full_sql:%s",full_sql);

        if (array != null){
            if (code = !array.isEmpty()){
                if (autoSelect && array.size() == 1){
                    if (mSelectListener != null)mSelectListener.onSelect(array.getJSONObject(0));
                }else {
                    mDatas = array;
                    notifyDataSetChanged();
                }
            }
        }else{
            MyDialog.ToastMessage("搜索商品错误：" + err,mContext,null);
        }
        return code;
    }

    public interface OnGoodsSelectListener {
        void onSelect(final JSONObject object);
    }

    public void setOnGoodsSelectListener(OnGoodsSelectListener listener) {
        mSelectListener = listener;
    }

    public boolean getSingleGoods(@NonNull JSONObject object, final String weigh_barcode_info, final String id){
        final String full_sql,sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,only_coding,ifnull(type,0) type," +
                "retail_price,retail_price price,tc_rate,tc_mode,tax_rate,ps_price,cost_price,trade_price,buying_price,yh_mode,yh_price,metering_id,conversion from barcode_info where goods_status = 1 and barcode_status = 1 and ";

        boolean isWeighBarcode = weigh_barcode_info != null && weigh_barcode_info.length() != 0;
        if (isWeighBarcode){
            full_sql = sql + "only_coding = '" + id + "'";
        }else
            full_sql = sql + "barcode_id = " + id + " UNION select gp_id ,-1 goods_id,ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name, -1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,ifnull(type,0) type," +
                    "gp_price retail_price,gp_price price,0 tc_rate,0 tc_mode,0 tax_rate,0 ps_price,0 cost_price,0 trade_price,gp_price buying_price,0 yh_mode,0 yh_price,1 metering_id,1 conversion from goods_group \n" +
                    "where status = 1 and gp_id = " + id;
        boolean code =  SQLiteHelper.execSql(object,full_sql);
        if (code){
            if (mPriceAdjustMode){
                code = false;
                final GoodsPriceAdjustDialog priceAdjustDialog = new GoodsPriceAdjustDialog(mContext,object);
                priceAdjustDialog.show();
            }else {
                if (isWeighBarcode){
                    code = parseElectronicBarcode(object,weigh_barcode_info);
                }else {
                    final JSONObject promotion_obj = new JSONObject();
                    if (code = getPromotionGoods(promotion_obj,Utils.getNotKeyAsNumberDefault(object,"barcode_id",-1),mContext.getStoreId())){
                        if (!promotion_obj.isEmpty()){
                            object.put("sale_type",SALE_TYPE.SPECIAL_PROMOTION);//1 零售特价促销
                            object.put("limit_xnum",promotion_obj.getDoubleValue("limit_xnum"));

                            int way = promotion_obj.getIntValue("way");
                            switch (way){
                                case 1://定价
                                    object.put("price",promotion_obj.getDoubleValue("promotion_price"));
                                    break;
                                case 2://折扣
                                    object.put("price",promotion_obj.getDoubleValue("promotion_price") / 10 * object.getDoubleValue("retail_price"));
                                    break;
                            }
                        }
                    }else {
                        object.clear();
                        object.put("info",promotion_obj.getString("info"));
                    }
                }
            }
        }
       return code;
    }

    public boolean isPriceAdjustMode(){
        return mPriceAdjustMode;
    }

    public static boolean getPromotionGoods(final JSONObject object,int barcode_id,final String stores_id){
        final String sql = "select way,limit_xnum,promotion_price from promotion_info where barcode_id = '" + barcode_id +"' and status = 1 and " +
                "stores_id = " + stores_id + " and date(start_date, 'unixepoch', 'localtime') || ' ' ||begin_time  <= datetime('now', 'localtime') \n" +
                " and datetime('now', 'localtime') <= date(end_date, 'unixepoch', 'localtime') || ' ' ||end_time and \n" +
                "promotion_week like '%' ||case strftime('%w','now' ) when 0 then 7 else strftime('%w','now' ) end||'%' order by tlp_id desc";

        Logger.d("PromotionGoodsSQL：%s",sql);

        return SQLiteHelper.execSql(object,sql);
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
                price = object.getDoubleValue("price");
                if (metering_id == 0){//计重
                    xnum = (Utils.equalDouble(price,0.0) ? 0.0 : amt / price);
                }else{//计份
                    xnum = 1;
                }
            }
            Logger.d("price：%f,xnum:%f,sale_amt:%f",price,xnum,amt);

            final JSONObject promotion_obj = new JSONObject();
            if (code = getPromotionGoods(promotion_obj,Utils.getNotKeyAsNumberDefault(object,"barcode_id",-1),mContext.getStoreId())){
                double discount = 1.0,ori_price = object.getDoubleValue("retail_price");
                if (!promotion_obj.isEmpty()){
                    object.put("sale_type",SALE_TYPE.SPECIAL_PROMOTION);//1 零售特价促销
                    object.put("limit_xnum",promotion_obj.getDoubleValue("limit_xnum"));

                    int way = promotion_obj.getIntValue("way");
                    switch (way){
                        case 1://定价
                            if (!Utils.equalDouble(ori_price,0.0))discount = promotion_obj.getDoubleValue("promotion_price") / ori_price;
                            break;
                        case 2://折扣
                            discount = promotion_obj.getDoubleValue("promotion_price") / 10;
                            break;
                    }
                }

                Logger.d("discount:%f,xnum:%f,amt:%f",discount,xnum,amt);

                object.put("price",price * discount);
                object.put("xnum",String.format(Locale.CHINA,"%.4f",xnum));
                object.put("sale_amt",amt);
                object.put(W_G_MARK,weigh_barcode_info);

            }else {
                object.put("info",promotion_obj.getString("info"));
            }
        }else {
            object.put("info",barcodeRuleObj.getAsString("info"));
        }
        return code;
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
        return MyDialog.ToastMessage(null,"加载条码秤参数错误：" + scaleSetting.getString("info"),mContext,null,SQLiteHelper.getLocalParameter("scale_setting",scaleSetting));
    }
}
