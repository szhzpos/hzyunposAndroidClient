package com.wyc.cloudapp.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.bean.PrintLabelGoods;
import com.wyc.cloudapp.customizationView.SwipeLayout;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: LabelGoodsAdapter
 * @Description: 标签打印商品适配器
 * @Author: wyc
 * @CreateDate: 2022/4/2 16:34
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/4/2 16:34
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class LabelGoodsAdapter extends AbstractSelectAdapter<PrintLabelGoods,LabelGoodsAdapter.MyViewHolder> implements View.OnClickListener {

    public LabelGoodsAdapter(){
        super();
        mData = new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        final Object obj = v.getTag();
        if (obj instanceof PrintLabelGoods){
            final PrintLabelGoods goods = (PrintLabelGoods) obj;
            int id = v.getId();
            if (id == R.id.minus){
                goods.setNum(goods.getNum() - 1);
                notifyItemChanged(mData.indexOf(goods));
            }else if (id == R.id.plus){
                goods.setNum(goods.getNum() + 1);
                notifyItemChanged(mData.indexOf(goods));
            }else if (id == R.id.container){
                invoke(goods);
            }
        }
    }

    static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder {
        @BindView(R.id.rowId)
        TextView rowId;
        @BindView(R.id.goods_title)
        TextView goods_title;

        @BindView(R.id.barcode)
        TextView barcode;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.spec)
        TextView spec;

        @BindView(R.id.print_num_tv)
        TextView num;

        @BindView(R.id.minus)
        Button minus;

        @BindView(R.id.plus)
        Button plus;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final SwipeLayout itemView = (SwipeLayout)View.inflate(parent.getContext(), R.layout.label_print_swipe_container, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.addMenuItem(parent.getContext().getString(R.string.delete_sz), v -> {
            mData.remove((PrintLabelGoods)itemView.getTag());
            itemView.setTag(null);
            itemView.closeRightMenu();
            notifyDataSetChanged();
        }, Color.RED);
        itemView.setLayoutParams(lp);
        itemView.setOnClickListener(this);

        final Button minus = itemView.findViewById(R.id.minus),plus = itemView.findViewById(R.id.plus);
        final EditText num = itemView.findViewById(R.id.print_num_tv);
        if (minus != null && plus != null && num !=null){
            minus.setOnClickListener(this);
            plus.setOnClickListener(this);

            num.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    final Object obj = num.getTag();
                    if (obj instanceof PrintLabelGoods){
                        try {
                            ((PrintLabelGoods)obj).setNum(Integer.parseInt(s.toString()));
                        }catch (NumberFormatException ignore){
                        }
                    }
                }
            });
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final PrintLabelGoods goods = mData.get(position);
        holder.rowId.setText(String.format(Locale.CHINA,"%d、",position + 1));
        holder.goods_title.setText(goods.getGoodsTitle());
        holder.barcode.setText(goods.getBarcode());
        holder.price.setText(String.format(Locale.CHINA,"￥%.2f",goods.getRetail_price()));
        holder.spec.setText(String.format(Locale.CHINA,"规格:%s",goods.getSpec()));

        holder.num.setTag(goods);
        holder.num.setText(String.valueOf(goods.getNum()));
        
        holder.minus.setTag(goods);
        holder.plus.setTag(goods);
        holder.itemView.setTag(goods);
    }

    public void addData(final PrintLabelGoods object){
        if (object != null){
            mData.add(object);
            notifyItemChanged(mData.size() - 1);
        }
    }

    public void addDataById(String barcodeId){
        final PrintLabelGoods object = getGoodsDataById(barcodeId);
        if (object != null){
            mData.add(object);
            notifyItemChanged(mData.size() - 1);
        }
    }
    public int getPrintTotalNum(){
        int num = 0;
        for (PrintLabelGoods goods : mData){
            num += goods.getNum();
        }
        return num;
    }

    private final String  fields = "barcode_id barcodeId,goods_title goodsTitle, barcode,unit_name unit,origin,spec_str spec,yh_price,retail_price";
    private PrintLabelGoods getGoodsDataById(String barcodeId ){
        String sql = "select "+ fields +" from barcode_info where barcode_id = '"+ barcodeId +"' and (goods_status = 1 and barcode_status = 1)";
        if (!Utils.isNotEmpty(barcodeId)) {
            sql =  "select "+ fields +" from barcode_info where goods_status = 1 and barcode_status = 1 limit 1";
        }
        final JSONObject goods = new JSONObject();
        if (!SQLiteHelper.execSql(goods,sql)){
            MyDialog.toastMessage(goods.getString("info"));
            return null;
        }
        if (goods.isEmpty())return null;
        return goods.toJavaObject(PrintLabelGoods.class);
    }

    public List<PrintLabelGoods> getGoodsDataByBarcode(String barcode){
        final String sql = "select "+ fields +" from barcode_info where barcode = '" + barcode +"' and (goods_status = 1 and barcode_status = 1)";
        final StringBuilder sb = new StringBuilder();
        final JSONArray goods = SQLiteHelper.getListToJson(sql,sb);
        if (goods != null){
            return goods.toJavaList(PrintLabelGoods.class);
        }else{
            MyDialog.toastMessage(sb.toString());
        }
        return new ArrayList<>();
    }

}
