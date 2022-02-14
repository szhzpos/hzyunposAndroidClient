package com.wyc.cloudapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.callback.ClickListener;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

public class PayDetailViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER = -1;
    private static final int CONTENT = -2;
    private final Context mContext;
    private final JSONArray mDatas;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    private OnDeleteItem mDelItemListener = (id,code) -> true;

    public PayDetailViewAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
    }
    static class ContentHolder extends RecyclerView.ViewHolder {
        private final TextView row_id,pay_method_id,pay_method_name,pay_detail_amt,pay_detail_zl,pay_detail_v_num;
        ContentHolder(View itemView) {
            super(itemView);
            row_id = itemView.findViewById(R.id.row_id);
            pay_method_id =  itemView.findViewById(R.id.pay_method_id);
            pay_method_name =  itemView.findViewById(R.id.pay_method_name);
            pay_detail_amt =  itemView.findViewById(R.id.pay_detail_amt);
            pay_detail_zl =  itemView.findViewById(R.id.pay_detail_zl);
            pay_detail_v_num =  itemView.findViewById(R.id.pay_detail_v_num);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView;
        RecyclerView.ViewHolder holder;
        if (i == HEADER){
            itemView = View.inflate(mContext, R.layout.pay_details_header_layout, null);
            holder = new HeaderHolder(itemView);
        }else{
            itemView = View.inflate(mContext, R.layout.pay_detail_content_layout, null);
            holder = new ContentHolder(itemView);
        }
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.pay_detail_row_height)));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder myViewHolder, int i) {
        if (myViewHolder instanceof ContentHolder){
            final JSONObject pay_detail = mDatas.getJSONObject(i - 1);
            if (pay_detail != null){
                ContentHolder contentHolder = (ContentHolder)myViewHolder;
                contentHolder.row_id.setText(String.valueOf(i));
                contentHolder.pay_method_name.setText(pay_detail.getString("name"));
                contentHolder.pay_detail_amt.setText(pay_detail.getString("pamt"));
                contentHolder.pay_detail_zl.setText(pay_detail.getString("pzl"));


                final String pay_method_id = pay_detail.getString("pay_method_id"),
                        v_num = Utils.getNullStringAsEmpty(pay_detail,"v_num");

                contentHolder.pay_detail_v_num.setText(v_num);
                contentHolder.pay_method_id.setText(pay_method_id);
                contentHolder.itemView.setOnTouchListener(new ClickListener(v -> {
                    setCurrentItemIndexAndItemView(v);
                    if (mDelItemListener.onDel(pay_method_id,v_num)){
                        deletePayDetail(mCurrentItemIndex);
                    }
                }, this::setSelectStatus));

                if (mCurrentItemIndex == i){
                    setSelectStatus(contentHolder.itemView);
                    mCurrentItemView = contentHolder.itemView;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    @Override
    public int getItemViewType(int position){
        if (0 == position){
            return HEADER;
        }
        return CONTENT;
    }

    private void setCurrentItemIndexAndItemView(View v){
        final TextView tv_id;
        mCurrentItemView = v;
        if (null != mCurrentItemView && (tv_id = mCurrentItemView.findViewById(R.id.pay_method_id)) != null){
            final String id = tv_id.getText().toString();
            for (int i = 0,length = mDatas.size();i < length;i ++){
                final JSONObject json = mDatas.getJSONObject(i);
                if (id.equals(json.getString("pay_method_id"))){
                    mCurrentItemIndex = i;
                    return;
                }
            }
        }
        mCurrentItemIndex = -1;
    }

    public @NonNull JSONArray getDatas(){
        return mDatas;
    }

    /**
     * 根据付款方式id以及凭证号增加支付明细
     * */
    @SuppressLint("NotifyDataSetChanged")
    public void addPayDetail(@NonNull JSONObject pay_detail_info){
        double amt = pay_detail_info.getDouble("pamt");
        final JSONObject object = findPayDetailById(pay_detail_info.getString("pay_method_id"),Utils.getNullStringAsEmpty(pay_detail_info,"v_num"));
        if (object != null){
            double payed_amt = object.getDouble("pamt"),
                    zl_amt = object.getDouble("pzl");

            amt += payed_amt;

            object.put("pamt",Utils.formatDouble(amt,2));
            object.put("pzl",Utils.formatDouble(zl_amt + pay_detail_info.getDouble("pzl"),2));
        }else{
            mDatas.add(pay_detail_info);
        }
        notifyDataSetChanged();
    }

    private void deletePayDetail(int index){
        int size = mDatas.size();
        if (0 <= index && index < size){
            mDatas.remove(index);
            if (mCurrentItemIndex == index){//如果删除的是当前选择的item则重置当前index以及View
                if (index == size - 1){
                    mCurrentItemIndex--;
                }else{
                    mCurrentItemIndex++;
                }
                mCurrentItemView = null;
            }
            notifyDataSetChanged();
        }
    }

    private void setSelectStatus(View v){
        if(null != mCurrentItemView){
            mCurrentItemView.setBackgroundColor(mContext.getResources().getColor(R.color.white,null));
        }
        v.setBackgroundColor(mContext.getResources().getColor(R.color.pink,null));
        setCurrentItemIndexAndItemView(v);
    }

    public JSONObject findPayDetailById(final String id){
        if (id != null){
            for (int i = 0,length = mDatas.size();i < length;i ++){
                final JSONObject jsonObject = mDatas.getJSONObject(i);
                if (id.equals(jsonObject.getString("pay_method_id"))){
                    return jsonObject;
                }
            }
        }
        return null;
    }
    /**
     * 根据付款方式id以及凭证号查找支付明细
     * @param id 付款方式id
     * @param code 凭证号
     * */
    public JSONObject findPayDetailById(final String id,final String code){
        JSONObject detail;
        for (int i = 0,length = mDatas.size();i < length;i ++){
            detail = mDatas.getJSONObject(i);
            if (Utils.getNullStringAsEmpty(detail,"pay_method_id").equals(id)
                    && Utils.getNullStringAsEmpty(detail,"v_num").equals(code)){
                return detail;
            }
        }
        return null;
    }
    /**
     * 根据付款方式id以及凭证号删除支付明细
     * @param id 付款方式id
     * @param code 凭证号
     * */
    @SuppressLint("NotifyDataSetChanged")
    public void delDetailWithIdAndVoucherNum(final String id, final String code){
        JSONObject detail;
        for (int i = mDatas.size() - 1;i >= 0;i --){
            detail = mDatas.getJSONObject(i);
            if (Utils.getNullStringAsEmpty(detail,"pay_method_id").equals(id)
                    && Utils.getNullStringAsEmpty(detail,"v_num").equals(code)){
                mDatas.remove(i);
                notifyDataSetChanged();
                return;
            }
        }
    }

    /**
     * 从数据库删除支付明细
     * @param orderCode 业务订单号
     * @param payMethodId 支付方式id
     * @param code 支付方式凭证号
     * */
    public static boolean delPayDetailFromDatabase(final String orderCode,final String payMethodId,final String code){
        final StringBuilder err = new StringBuilder();
        if (isPayed(orderCode,payMethodId,code)){
            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.forbid_del_hints));
            return false;
        }
        if (SQLiteHelper.execDelete("retail_order_pays","order_code=? and pay_method=? and ifnull(v_num,'')=?",new String[]{orderCode,payMethodId,code},err) < 0){
            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.del_pay_detail_err,err));
            return false;
        }
        return true;
    }
    /**
     * 检查请求api的支付方式是否已经完成支付
     * */
    private static boolean isPayed(final String orderCode,final String payMethodId,final String code){
        final JSONObject pay_status = new JSONObject();
        if (!SQLiteHelper.execSql(pay_status,"select pay_status from retail_order_pays where is_check = 1 and order_code='" + orderCode +"' and pay_method='" + payMethodId +"' and ifnull(v_num,'')='" + code +"'")){
            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.del_pay_detail_err,pay_status.getString("info")));
            return false;
        }
        return pay_status.getIntValue("pay_status") == 2;
    }

    public double getPayingSumAmt(){
        double amt = 0.0;
        for (int i = 0,size = mDatas.size();i < size;i++){
            final JSONObject object = mDatas.getJSONObject(i);
            if (null != object)
                amt += object.getDoubleValue("pamt") - object.getDoubleValue("pzl");
        }
        return amt;
    }

    public boolean hasPayMethodPayed(){
        for (int i = 0,size = mDatas.size();i < size;i++){
            if (Utils.getNotKeyAsNumberDefault(mDatas.getJSONObject(i),"pay_status",1) == 2)return true;
        }
        return false;
    }

    public boolean isEmpty(){
        return getItemCount() - 1 == 0;
    }
    public void clear(){
        mDatas.clear();
        notifyDataSetChanged();
    }

    public interface OnDeleteItem{
        /**
        * @param id 支付方式Id
         * @param code 凭证号
        * @return true 可以删除明细，false不删除明细
        * */
        boolean onDel(@NonNull String id,final String code);
    }
    public void setDelItemListener(OnDeleteItem delItemListener){
        if (delItemListener == null)
            mDelItemListener = (id,code)-> true;
        else
            mDelItemListener = delItemListener;
    }

}
