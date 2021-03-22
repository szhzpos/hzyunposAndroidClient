package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
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
public class BuyFullGiveXGoodsAdapter extends TreeListBaseAdapter<BuyFullGiveXGoodsAdapter.MyViewHolder> {
    final SaleActivity mContext;
    public BuyFullGiveXGoodsAdapter(SaleActivity context, boolean single) {
        super(context, single);
        mContext = context;
    }

    static class MyViewHolder extends TreeListBaseAdapter.MyViewHolder {
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
            if (mContext.findGoodsByBarcodeId(goods,content.getString("barcode_id"))){
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



    @Override
    protected int clickItemIndex() {
        return -1;
    }
    public static JSONArray formatPresentGoods(final JSONArray array){
        final JSONArray data = new JSONArray();
        if (null != array){
            JSONObject object;
            for (int i = 0,size = array.size();i < size;i++){
                final JSONObject tmp = array.getJSONObject(i);
                final String id = Utils.getNullStringAsEmpty(tmp,"barcode_id");

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",id);
                object.put("content",tmp);
                data.add(object);
            }
        }
        return data;
    }
}
