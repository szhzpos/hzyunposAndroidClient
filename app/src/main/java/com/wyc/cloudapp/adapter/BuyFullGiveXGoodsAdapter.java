package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: BuyFullGiveXGoodsAdapter
 * @Description: 买满赠送商品适配器
 * @Author: wyc
 * @CreateDate: 2021/3/22 18:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/22 18:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BuyFullGiveXGoodsAdapter extends TreeListBaseAdapterForJson<BuyFullGiveXGoodsAdapter.MyViewHolder> {
    final SaleActivity mContext;
    public BuyFullGiveXGoodsAdapter(SaleActivity context, boolean single) {
        super(context, single);
        mContext = context;
    }

    static class MyViewHolder extends TreeListBaseAdapterForJson.MyViewHolder {
        TextView _barcode_tv,_name_tv,_unit_name_tv,
                _retail_price_tv,remark_tv,_num_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            _barcode_tv = findViewById(R.id._barcode);
            _name_tv = findViewById(R.id._name);
            _unit_name_tv = findViewById(R.id._unit_name);
            _num_tv = findViewById(R.id._num_tv);
            _retail_price_tv = findViewById(R.id._retail_price);
            remark_tv = findViewById(R.id.remark);
        }
        @Override
        protected int getContentResourceId() {
            return R.layout.buyfull_give_x_goods_layout;
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(getView());
    }

    @Override
    void bindContent(@NonNull MyViewHolder holder, JSONObject object) {
        final JSONObject content = object.getJSONObject("content");
        if (null != content){
            final JSONObject goods = new JSONObject();
            if (findBuyFullGiveGoodsByBarcodeId(goods,content.getString("barcode_id"))){
                if (Utils.lessThan7Inches(mContext))
                    holder._barcode_tv.setVisibility(View.GONE);
                else
                    holder._barcode_tv.setText(goods.getString("barcode"));

                holder._name_tv.setText(goods.getString("goods_title"));
                holder._unit_name_tv.setText(goods.getString("unit_name"));
                holder._num_tv.setText(String.format(Locale.CHINA,"%.2f",content.getDoubleValue("xnum_give")));
                holder._retail_price_tv.setText(String.format(Locale.CHINA,"%.2f",content.getDoubleValue("markup_price")));
                holder.remark_tv.setText(content.getString("remark"));
            }else {
                Toast.makeText(mContext,goods.getString("info"),Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean findBuyFullGiveGoodsByBarcodeId(final JSONObject out,final String barcode_id){
        final String  sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,only_coding,ifnull(type,0) type," +
                "brand_id,gs_id,a.category_id category_id,b.path path,retail_price,retail_price price,tc_rate,tc_mode,tax_rate,ps_price,cost_price,trade_price,buying_price,yh_mode,yh_price," +
                "metering_id,conversion from barcode_info a inner join shop_category b on a.category_id = b.category_id where goods_status = 1 and barcode_status = 1 and  barcode_id = " + barcode_id + " UNION select gp_id ,-1 goods_id,ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name, -1 barcode_id,ifnull(gp_code,'') barcode,-1 only_coding,ifnull(type,0) type," +
                    "'' brand_id,'' gs_id, '' category_id,'' path,gp_price retail_price,gp_price price,0 tc_rate,0 tc_mode,0 tax_rate,0 ps_price,0 cost_price,0 trade_price,gp_price buying_price,0 yh_mode,0 yh_price,1 metering_id,1 conversion from goods_group \n" +
                    "where status = 1 and gp_id = " + barcode_id;
       return SQLiteHelper.execSql(out,sql);
    }


    @Override
    protected int clickItemIndex() {
        return -1;
    }
}
