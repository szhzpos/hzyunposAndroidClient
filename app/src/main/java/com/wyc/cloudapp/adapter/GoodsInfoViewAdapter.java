package com.wyc.cloudapp.adapter;

import android.content.ContentValues;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class GoodsInfoViewAdapter extends RecyclerView.Adapter<GoodsInfoViewAdapter.MyViewHolder> {
    public static String W_G_MARK = "IWG";//计重、计份并且通过扫条码选择的商品标志
    private Context mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;
    private boolean mSearchLoad = false;//是否按搜索框条件加载
    private boolean mShowPic = true;
    public GoodsInfoViewAdapter(Context context){
        this.mContext = context;
        JSONObject jsonObject = new JSONObject();
        if (SQLiteHelper.getLocalParameter("g_i_show",jsonObject)){
            mShowPic = (Utils.getNotKeyAsNumberDefault(jsonObject,"s",1) == 1);
        }else{
            MyDialog.ToastMessage("加载是否显示商品图片参数错误：" + jsonObject.getString("info"),mContext,null);
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView gp_id,goods_id,goods_title,unit_id,unit_name,barcode_id,barcode,price;
        ImageView goods_img;
        View mCurrentItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentItemView = itemView;

            goods_img = itemView.findViewById(R.id.g_img);
            goods_id = itemView.findViewById(R.id.goods_id);
            gp_id = itemView.findViewById(R.id.gp_id);
            goods_title =  itemView.findViewById(R.id.goods_title);
            unit_id =  itemView.findViewById(R.id.unit_id);
            unit_name =  itemView.findViewById(R.id.unit_name);
            barcode_id =  itemView.findViewById(R.id.barcode_id);
            barcode =  itemView.findViewById(R.id.barcode);
            price =  itemView.findViewById(R.id.sale_price);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.goods_info_content_layout, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.goods_height));
        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject goods_info = mDatas.getJSONObject(i);
            if (goods_info != null){
                final String img_url = Utils.getNullStringAsEmpty(goods_info,"img_url");
                if (mShowPic){
                    if (!"".equals(img_url)){
                        final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                        CustomApplication.execute(()->{
                            final Bitmap bitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                            myViewHolder.goods_img.post(()-> myViewHolder.goods_img.setImageBitmap(bitmap));
                        });
                    }else{
                        myViewHolder.goods_img.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                    }
                }else{
                    myViewHolder.goods_img.setVisibility(View.GONE);
                }

                myViewHolder.goods_id.setText(goods_info.getString("goods_id"));
                myViewHolder.gp_id.setText(goods_info.getString("gp_id"));
                myViewHolder.goods_title.setText(goods_info.getString("goods_title"));
                myViewHolder.unit_id.setText(goods_info.getString("unit_id"));
                myViewHolder.unit_name.setText(goods_info.getString("unit_name"));
                myViewHolder.barcode_id.setText(goods_info.getString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.getString("barcode"));
                myViewHolder.price.setText(goods_info.getString("price"));

                if(myViewHolder.goods_title.getCurrentTextColor() == mContext.getResources().getColor(R.color.blue,null)){
                    myViewHolder.goods_title.setTextColor(mContext.getColor(R.color.good_name_color));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
                }

                if (mOnItemClickListener != null){
                    myViewHolder.mCurrentItemView.setOnClickListener((View v)->{
                        mOnItemClickListener.onClick(v,i);
                    });
                }

                if (mSearchLoad && mDatas.size() == 1 && null != myViewHolder.mCurrentItemView){//搜索只有一个的时候自动选择
                        myViewHolder.mCurrentItemView.callOnClick();
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public JSONObject getItem(int i){
        return mDatas == null ? null : mDatas.getJSONObject(i);
    }

    void setDatas(int id){

        StringBuilder err = new StringBuilder();
        String sql = "",category_id;
        if (-1 == id){
            sql = "select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    " -1  barcode_id,ifnull(gp_code,'') barcode,type,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = 1";
        }else{
            category_id = SQLiteHelper.getString("select category_id from shop_category where path like '%" + id +"%'",err);
            if (null == category_id){
                MyDialog.ToastMessage("加载商品错误：" + err,mContext,null);
                return;
            }
            category_id = category_id.replace("\r\n",",");
            sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,type,retail_price price,ifnull(img_url,'') img_url from barcode_info where (goods_status = 1 and barcode_status = 1) and category_id in (" + category_id + ")";
        }

        mDatas = SQLiteHelper.getListToJson(sql,0,0,false,err);
        if (mDatas != null){
            if (mSearchLoad)mSearchLoad = false;
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载商品错误：" + err,mContext,null);
        }
    }

    public void fuzzy_search_goods(@NonNull final EditText search){
        final StringBuilder err = new StringBuilder();
        final ContentValues barcodeRuleObj = new ContentValues();
        final String search_content = search.getText().toString(),sql_where,full_sql,
                sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,only_coding,type,retail_price price\n" +
                ",ifnull(img_url,'') img_url from barcode_info where (goods_status = 1 and barcode_status = 1) and %1";

        if (isBarcodeWeighingGoods(search_content,barcodeRuleObj)){
            sql_where = "only_coding = " + barcodeRuleObj.getAsString("item_id");
            full_sql = sql.replace("%1",sql_where);
            final JSONObject object = new JSONObject();
            if (SQLiteHelper.execSql(object,full_sql)){
                object.put(W_G_MARK,search_content);
                mDatas.fluentClear();
                mDatas.add(object);
            }else {
                mDatas = null;
                err.append(Utils.getNullStringAsEmpty(object,"info"));
            }
        }else {
            sql_where = "(barcode like '" + search_content + "%' or mnemonic_code like '" + search_content +"%')";
            full_sql = sql.replace("%1",sql_where) + " UNION select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    "-1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,type,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = '1' and " + sql_where;

            mDatas = SQLiteHelper.getListToJson(full_sql,0,0,false,err);
        }
        if (mDatas != null){
            if(mDatas.size() != 0){
                if (!mSearchLoad)mSearchLoad = true;
            }else{
                search.selectAll();
                MyDialog.ToastMessage("无此商品！",mContext,null);
            }
            this.notifyDataSetChanged();
        }else{
            search.selectAll();
            MyDialog.ToastMessage("搜索商品错误：" + err,mContext,null);
        }
    }

    public boolean getSingleGoods(@NonNull JSONObject object, final String weigh_barcode_info, int id){
        final String full_sql,sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,only_coding,ifnull(type,0) type," +
                "retail_price,retail_price price,tc_rate,tc_mode,tax_rate,ps_price,cost_price,trade_price,buying_price,yh_mode,yh_price,metering_id,conversion from barcode_info where goods_status = 1 and barcode_status = 1 and ";

        if (weigh_barcode_info != null && weigh_barcode_info.length() != 0){
            full_sql = sql + "only_coding = " + id;
            return parseElectronicBarcode(full_sql,object,weigh_barcode_info);
        }

        full_sql = sql + "barcode_id = " + id + " UNION select gp_id ,-1 goods_id,ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name, -1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,ifnull(type,0) type," +
                "gp_price retail_price,gp_price price,0 tc_rate,0 tc_mode,0 tax_rate,0 ps_price,0 cost_price,0 trade_price,gp_price buying_price,0 yh_mode,0 yh_price,1 metering_id,1 conversion from goods_group \n" +
                "where status = 1 and gp_id = " + id;

        //Logger.d("full_sql:%s",full_sql);

       return SQLiteHelper.execSql(object,full_sql);
    }

    private boolean parseElectronicBarcode(@NonNull String full_sql,@NonNull final JSONObject object,@NonNull final String weigh_barcode_info){
        boolean code = true;
        ContentValues barcodeRuleObj = new ContentValues();
        if (code = SQLiteHelper.execSql(object,full_sql)){
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
                    price = amt / (weight == 0 ? 1 : weight);
                }else{
                    price = object.getDoubleValue("price");
                    if (metering_id == 0){//计重
                        xnum = amt / (price == 0 ? 1 : price);
                    }else{//计份
                        xnum = 1;
                    }
                }
                Logger.d("price：%f,xnum:%f,sale_amt:%f",price,(amt / (price == 0 ? 1 : price)),amt);

                object.put("price",price);
                object.put("xnum",String.format(Locale.CHINA,"%.3f",xnum));
                object.put("sale_amt",amt);
                object.put(W_G_MARK,weigh_barcode_info);

                Logger.d("计重商品：%s",object.toJSONString());
            }
        }
        return code;
    }

    public interface OnItemClickListener{
        void onClick(View v,int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public int getGoodsId(final JSONObject jsonObject){
        int id = -1;
        if (!"".equals(Utils.getNullStringAsEmpty(jsonObject,W_G_MARK))){//计重、计份并且通过扫条码选择的商品标志
            id = jsonObject.getIntValue("only_coding");
        }else{
            id = jsonObject.getIntValue("barcode_id");
            if (-1 == id){//组合商品
                id = jsonObject.getIntValue("gp_id");
            }
        }
        return id;
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
                        if (end <= barcode_len){
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
            if (MyDialog.ToastMessage(null,"输入条码长度与参数设置的不一致",mContext,null,(code = barcode.length() == barcodeRule.length()))){
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
            }
        }
        return code;
    }

    private boolean loadScaleSetting(final JSONObject scaleSetting){
        return MyDialog.ToastMessage(null,"加载条码秤参数错误：" + scaleSetting.getString("info"),mContext,null,SQLiteHelper.getLocalParameter("scale_setting",scaleSetting));
    }
}
