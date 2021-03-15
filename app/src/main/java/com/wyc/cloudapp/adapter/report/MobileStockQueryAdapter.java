package com.wyc.cloudapp.adapter.report;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.report.MobileStockQueryActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: MobileStockDetailsAdapter
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021/2/3 10:57
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/3 10:57
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileStockQueryAdapter extends AbstractDataAdapter<MobileStockQueryAdapter.MyViewHolder> {
    final MainActivity mContext;
    private int mDataSize = 0, mOffset = 0,mLimit = 50,mTotalRows = 0;
    private JSONObject mPreQueryCondition;

    public MobileStockQueryAdapter(final MainActivity activity){
        mContext = activity;
        mDataSize = getItemCount();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_stock_query_content_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mContext,58));
        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mDatas != null) {
            final JSONObject object = mDatas.getJSONObject(position);
            double stock_num = object.getDoubleValue("stock_num");
            if (stock_num < 0) {
                holder.goods_name_tv.setTextColor(Color.RED);
                holder.stock_amt_tv.setTextColor(Color.RED);
                holder.stock_num_tv.setTextColor(Color.RED);
            } else {
                holder.goods_name_tv.setTextColor(mContext.getColor(R.color.lightBlue));
                holder.stock_amt_tv.setTextColor(mContext.getColor(R.color.lightBlue));
                holder.stock_num_tv.setTextColor(mContext.getColor(R.color.lightBlue));
            }

            holder.goods_name_tv.setText(object.getString("goods_title"));
            holder.stock_amt_tv.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("stock_money")));
            holder.stock_num_tv.setText(String.format(Locale.CHINA, "%.3f", object.getDoubleValue("stock_num")));

            if (mDataSize < mTotalRows && mTotalRows > mLimit && position + 5 == mDataSize) {
                holder.stock_num_tv.postDelayed(this::reQuery, 100);
            }
        }
    }

    public void setDatas(final JSONObject condition){
        if (mOffset != 0)mOffset = 0;
        condition.put("limit",mLimit);
        condition.put("offset",mOffset);
        mPreQueryCondition = condition;
        getDatas(false);
    }

    private void reQuery(){
        mOffset++;
        getDatas(true);
    }
    private void getDatas(boolean append){
        final ProgressDialog progressDialog = ProgressDialog.show(mContext,"",mContext.getString(R.string.hints_query_data_sz),true);
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();
        final JSONObject object = mPreQueryCondition;
        CustomApplication.execute(()->{
            try {
                Logger.d_json(object.toString());

                object.put("offset",mOffset);
                object.put("limit",mLimit);
                object.put("appid",mContext.getAppId());

                final JSONObject retJson = HttpUtils.sendPost(mContext.getUrl() + "/api/goods_set/stock_query", HttpRequest.generate_request_parm(object, mContext.getAppSecret()),true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                        if ("y".equals(info.getString("status"))){
                            mContext.runOnUiThread(()-> ((MobileStockQueryActivity)mContext).showNumAndAmt(String.format(Locale.CHINA,"%.2f",info.getDoubleValue("total_stock_num")),String.format(Locale.CHINA,"%.2f",info.getDoubleValue("total_stock_money"))));
                            final JSONArray datas = info.getJSONArray("data");
                            if (datas.size() != 0){
                                mTotalRows = info.getIntValue("total");
                                loop.done(1);
                            }else{
                                err.append(info.getString("info"));
                                loop.done(0);
                            }
                            if (append){
                                if (mDatas != null){
                                    for (int i = 0,size = datas.size();i < size;i++){
                                        mDatas.add(datas.getJSONObject(i));
                                    }
                                }
                            }else{
                                mDatas = datas;
                            }
                        }else{
                            err.append(info.getString("info"));
                            loop.done(0);
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                err.append(e.getMessage());
                loop.done(0);
            }
        });
        final int code = loop.exec();
        if (code != 1) Toast.makeText(mContext,err.toString(),Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        mDataSize = getItemCount();
        notifyDataSetChanged();
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView goods_name_tv,stock_num_tv,stock_amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            goods_name_tv = itemView.findViewById(R.id.goods_name);
            stock_num_tv =  itemView.findViewById(R.id.stock_num);
            stock_amt_tv = itemView.findViewById(R.id.stock_amt);
        }
    }
}
