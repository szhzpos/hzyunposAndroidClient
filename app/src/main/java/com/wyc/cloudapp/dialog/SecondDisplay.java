package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SecondDisplay extends Presentation {
    private final String mAdFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/ad_img/";
    private SaleGoodsViewAdapter mSaleGoodsAdapter;
    private RecyclerView mSaleGoodsView;
    private Context mContext;
    private ImageView mImageView;
    private JSONObject mStoreinfo;
    private TextView mSaleSumNum,mSaleSumAmount;
    private int mShowAdImgTimes = 0,mShowInterval = 5;//mShowAdImgTimes显示图片次数
    private String[] mAdFileNames;
    private ScheduledFuture<?> mShowAdImgFut;
    private TextView mBottomRollTv;
    public SecondDisplay(Context outerContext, Display display) {
        super(outerContext, display);
        mContext = outerContext;

        mSaleGoodsAdapter = new SaleGoodsViewAdapter(mContext);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_disp_content_layout);

        mSaleSumNum = findViewById(R.id.sale_sum_num);
        mSaleSumAmount = findViewById(R.id.sale_sum_amt);
        mImageView = findViewById(R.id.banner_img);
        mBottomRollTv = findViewById(R.id.rolltv);

        //初始化商品信息
        initSaleGoodsView();
        //初始化导航信息
        initNavigationInfo();
        //c初始化底部动画
        initBottomRollTv();
    }
    @Override
    public void onAttachedToWindow (){
        super.onAttachedToWindow();
        showAdImg();
    }
    @Override
    public void  onDetachedFromWindow(){
        super.onDetachedFromWindow();
        stopShowImg();
    }

    private void initSaleGoodsView(){
        mSaleGoodsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                JSONArray datas = mSaleGoodsAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0,dis_sum_amt = 0.0;
                for (int i = 0,length = datas.length();i < length;i ++){
                    JSONObject jsonObject = datas.optJSONObject(i);
                    sale_sum_num += jsonObject.optDouble("xnum");
                    sale_sum_amount += jsonObject.optDouble("sale_amt");
                }
                mSaleSumNum.setText(String.format(Locale.CANADA,"%.3f",sale_sum_num));
                mSaleSumAmount.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                mSaleGoodsView.scrollToPosition(mSaleGoodsAdapter.getCurrentItemIndex());
                displayGoodsImg();
            }
        });
        mSaleGoodsView = findViewById(R.id.sec_sale_goods_list);
        mSaleGoodsView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsView.setAdapter(mSaleGoodsAdapter);
    }
    private void displayGoodsImg(){
        final JSONObject sale = mSaleGoodsAdapter.getCurrentContent();
        if (sale != null){
            final String barcode_id = sale.optString("barcode_id");
            if (!"".equals(barcode_id)){
                CustomApplication.execute(()->{
                    final String sql = "select ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and barcode_id = " + barcode_id +
                            " UNION\n" +
                            "select ifnull(img_url,'') img_url from goods_group where status = '1' and gp_id =" + barcode_id;
                    JSONObject object = new JSONObject();
                    Activity activity = null;
                    if (mContext instanceof Activity){
                        activity = (Activity) mContext;
                    }
                    if (SQLiteHelper.execSql(object,sql)){
                        String img_url = object.optString("img_url");
                        if (!"".equals(img_url)){
                            final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                            final Bitmap bitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                            if (null != activity)
                                activity.runOnUiThread(()->{
                                mImageView.setImageBitmap(bitmap);
                            });
                        }
                    }else{
                        if (null != activity)
                            activity.runOnUiThread(()->{
                                MyDialog.ToastMessage(object.optString("info"), mContext,getWindow());
                            });
                    }
                });
            }
        }
    }
    private void initNavigationInfo(){
        if (mStoreinfo != null){
            TextView stores_name = findViewById(R.id.sec_store_name),stores_hotline = findViewById(R.id.sec_stores_hotline),
            stores_addr = findViewById(R.id.sec_stores_addr);
            stores_name.setText(mStoreinfo.optString("stores_name"));
            stores_hotline.setText(mStoreinfo.optString("telphone"));
            stores_addr.setText(mStoreinfo.optString("region"));
        }
    }
    public SecondDisplay setNavigationInfo(JSONObject object){
        mStoreinfo = object;
        return this;
    }
    public SecondDisplay setDatas(JSONArray array){
        if (array == null){
            array = new JSONArray();
        }
        mSaleGoodsAdapter.setDatas(array);
        notifyChange(0);
        return this;
    }
    public SecondDisplay setShowInterval(int interval){
        if (interval > 5){
            mShowInterval = interval;
        }
        return this;
    }
    public void notifyChange(int index){
        mSaleGoodsAdapter.setCurrentItemIndex(index);
        mSaleGoodsAdapter.notifyDataSetChanged();
    }

    public void loadAdImg(final String url,final String appid,final String appScret){
        CustomApplication.execute(()->{
            HttpRequest httpRequest = new HttpRequest();
            JSONObject object = new JSONObject();
            Activity activity = null;
            final String err = "获取广告图片错误：";
            if (mContext instanceof Activity){
                activity = (Activity) mContext;
            }
            File img_dir = new File(mAdFilePath);
            if (!img_dir.exists()){
                if (!img_dir.mkdir()){
                    if (null != activity){
                        activity.runOnUiThread(()->{
                            MyDialog.ToastMessage("初始化广告图片目录错误！",mContext,getWindow());
                        });
                    }
                    return;
                }
            }
            try {
                object.put("appid",appid);
                final JSONObject retJson = httpRequest.setConnTimeOut(10000).sendPost(url  + "/api/get_config/get_sc_ad",HttpRequest.generate_request_parm(object,appScret),true);
                if (retJson.getInt("flag") == 1){
                    object = new JSONObject(retJson.getString("info"));
                    if ("y".equals(object.getString("status"))){
                        String img_url_info,img_file_name;
                        JSONArray imgs = new JSONArray(object.getString("sc_logo_list"));
                        for(int i = 0,size = imgs.length();i < size;i++){
                            img_url_info = imgs.getString(i);
                            if (!img_url_info.equals("")){
                                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1);
                                File file = new File(mAdFilePath + img_file_name);
                                if (!file.exists()){
                                    final JSONObject img_obj = httpRequest.getFile(file,img_url_info);
                                    if (img_obj.optInt("flag") == 0){
                                        if (null != activity){
                                            activity.runOnUiThread(()->{
                                                MyDialog.ToastMessage(err + img_obj.optString("info"),mContext,getWindow());
                                            });
                                        }
                                    }
                                }
                            }
                        }
                        mAdFileNames = img_dir.list();
                    }else{
                        if (null != activity){
                            activity.runOnUiThread(()->{
                                MyDialog.ToastMessage(err + retJson.optString("info"),mContext,getWindow());
                            });
                        }
                    }
                }else{
                    if (null != activity){
                        activity.runOnUiThread(()->{
                            MyDialog.ToastMessage(err + retJson.optString("info"), mContext,getWindow());
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (null != activity){
                    activity.runOnUiThread(()->{
                        MyDialog.ToastMessage(err + e.getMessage(), mContext,getWindow());
                    });
                }
            }
        });
    }

    private void stopShowImg(){
        if (!CustomApplication.removeTask(showAdImgRunnable)){
            mShowAdImgFut.cancel(true);
            try {
                mShowAdImgFut.get(3,TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | CancellationException | TimeoutException e) {
                e.printStackTrace();
                Logger.d("停止广告图片：%s",e.getMessage());
            }
        }
    }
    private void showAdImg(){
        mShowAdImgFut = CustomApplication.scheduleAtFixedRate(showAdImgRunnable,0,mShowInterval * 1000, TimeUnit.MILLISECONDS);
    }
    private Runnable showAdImgRunnable = ()->{
        if (mAdFileNames != null){
            if (mContext instanceof Activity){
                Activity activity = (Activity)mContext;
                final Bitmap bitmap = BitmapFactory.decodeFile(mAdFilePath + mAdFileNames[mShowAdImgTimes++ % mAdFileNames.length]);
                activity.runOnUiThread(()->{
                    mImageView.setImageBitmap(bitmap);
                });
            }
        }
    };
    private void initBottomRollTv(){
        final Animation transtoLeftAnim = AnimationUtils.loadAnimation(mContext, R.anim.tv_move);
        transtoLeftAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mBottomRollTv.startAnimation(animation);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mBottomRollTv.startAnimation(transtoLeftAnim);
    }
}
