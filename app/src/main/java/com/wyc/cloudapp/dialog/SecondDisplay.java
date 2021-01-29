package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.adapter.NormalSaleGoodsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SecondDisplay extends Presentation implements SurfaceHolder.Callback {
    private final String mAdFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/ad_img/";
    private final NormalSaleGoodsAdapter mNormalSaleGoodsAdapter;
    private RecyclerView mSaleGoodsView;
    private SaleActivity mContext;
    private JSONObject mStoreInfo;
    private TextView mSaleSumNum,mSaleSumAmount;
    private int mShowAdImgTimes = 0,mShowInterval = 5;//mShowAdImgTimes显示图片次数
    private volatile String[] mAdFileNames;
    private Future<?> mShowAdImgFut;
    private int mCurrentBarcodeId;
    private SurfaceView mSurface;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private long loseTime = 0;
    private int mBannerTextX = 0;
    private Bitmap mBannerBitmap = null;
    private Rect mBottomRect;
    private volatile boolean mShowBannerImg = true;
    private SecondDisplay(SaleActivity outerContext, Display display) {
        super(outerContext, display);
        mContext = outerContext;

        mNormalSaleGoodsAdapter = new NormalSaleGoodsAdapter(mContext);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_disp_content_layout);
        mSaleSumNum = findViewById(R.id.sale_sum_num);
        mSaleSumAmount = findViewById(R.id.sale_sum_amt);
        mSurface = findViewById(R.id.surfaceView);

        //初始化商品信息
        initSaleGoodsView();
        //初始化导航信息
        initNavigationInfo();
        //初始化surface
        initSurfaceView();
    }
    @Override
    public void onAttachedToWindow (){
        super.onAttachedToWindow();
    }
    @Override
    public void  onDetachedFromWindow(){
        super.onDetachedFromWindow();

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        showAdImg();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopShowImg();
    }

    private void initSurfaceView(){
        mSurface.setZOrderOnTop(true);
        mSurface.setBackgroundColor(mContext.getColor(R.color.white));
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(this);

        final Paint paint =  new Paint();
        paint.setTextSize(18);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setTextAlign(Paint.Align.CENTER);
        mPaint = paint;
    }

    private void initSaleGoodsView(){
        mNormalSaleGoodsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                JSONArray datas = mNormalSaleGoodsAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0;
                if (datas.size() != 0){
                    if (mShowBannerImg) mShowBannerImg = false;
                    for (int i = 0,length = datas.size();i < length;i ++){
                        JSONObject jsonObject = datas.getJSONObject(i);
                        sale_sum_num += jsonObject.getDoubleValue("xnum");
                        sale_sum_amount += jsonObject.getDoubleValue("sale_amt");
                    }
                    mSaleSumNum.setText(String.format(Locale.CANADA,"%.3f",sale_sum_num));
                    mSaleSumAmount.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                    mSaleGoodsView.scrollToPosition(mNormalSaleGoodsAdapter.getCurrentItemIndex());
                }else{
                    mCurrentBarcodeId = 0;
                    if (!mShowBannerImg) mShowBannerImg = true;
                }
                displayGoodsImg();
            }
        });
        mSaleGoodsView = findViewById(R.id.sec_sale_goods_list);
        mSaleGoodsView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsView.setAdapter(mNormalSaleGoodsAdapter);
    }
    private void displayGoodsImg(){
        final JSONObject sale = mNormalSaleGoodsAdapter.getCurrentContent();
        if (sale != null){
            int barcode_id = sale.getIntValue("barcode_id");
            if ( 0 != barcode_id && barcode_id != mCurrentBarcodeId){//当前显示的图片和即将要显示的图片不相同时再显示
                CustomApplication.execute(()->{
                    final String sql = "select ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and barcode_id = " + barcode_id +
                            " UNION select ifnull(img_url,'') img_url from goods_group where status = '1' and gp_id =" + barcode_id;
                    final JSONObject object = new JSONObject();
                    if (SQLiteHelper.execSql(object,sql)){
                        String img_url = object.getString("img_url");
                        if (!"".equals(img_url)){
                            final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                            mBannerBitmap = BitmapFactory.decodeFile(CustomApplication.IMG_PATH + szImage);
                            mCurrentBarcodeId = barcode_id;
                        }
                    }else{
                        mSurface.post(()->MyDialog.ToastMessage(object.getString("info"), mContext,getWindow()));
                    }
                });
            }
        }
    }
    private void initNavigationInfo(){
        if (mStoreInfo != null){
            final TextView stores_name = findViewById(R.id.store_name),stores_hotline = findViewById(R.id.sec_stores_hotline),
            stores_addr = findViewById(R.id.sec_stores_addr);
            stores_name.setText(mStoreInfo.getString("stores_name"));
            stores_hotline.setText(mStoreInfo.getString("telphone"));
            stores_addr.setText(mStoreInfo.getString("region"));
        }
    }
    public SecondDisplay setNavigationInfo(JSONObject object){
        mStoreInfo = object;
        return this;
    }
    public SecondDisplay setDatas(JSONArray array){
        if (array == null){
            array = new JSONArray();
        }
        mNormalSaleGoodsAdapter.setDatas(array);
        notifyChange(0);
        return this;
    }
    private void setShowInterval(int interval){
        if (interval > 5){
            mShowInterval = interval;
        }
    }
    public void notifyChange(int index){
        mNormalSaleGoodsAdapter.setCurrentItemIndex(index).notifyDataSetChanged();
    }

    public void loadAdImg(final String url,final String appid,final String appSecret){
        final File img_dir = new File(mAdFilePath);
        if (!img_dir.exists()){
            if (!img_dir.mkdir()){
                MyDialog.ToastMessage("初始化广告图片目录错误！",mContext,getWindow());
                return;
            }
        }else{
            mAdFileNames = img_dir.list();
        }
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            JSONObject object = new JSONObject();
            final String err = "获取广告图片错误：";

            final Activity activity = mContext;

            try {
                object.put("appid",appid);
                object.put("pos_num",mContext.getPosNum());
                object.put("stores_id", mContext.getStoreId());
                final JSONObject retJson = httpRequest.setConnTimeOut(10000).sendPost(url  + "/api/pos_img/index",HttpRequest.generate_request_parm(object,appSecret),true);
                if (retJson.getIntValue("flag") == 1){
                    object = JSON.parseObject(retJson.getString("info"));
                    if ("y".equals(object.getString("status"))){
                        String img_url_info,img_file_name,file_hash;

                        final String data_sz = object.getString("data");
                        final JSONArray imgs;
                        if (data_sz.startsWith("[")){
                            imgs = JSON.parseArray(data_sz);
                        }else {
                            final JSONObject obj = JSON.parseObject(data_sz);
                            imgs = Utils.getNullObjectAsEmptyJsonArray(obj,"sc_ad_yr");
                        }

                        for(int i = 0,size = imgs.size();i < size;i++){
                            object = imgs.getJSONObject(i);
                            img_url_info = object.getString("url");
                            file_hash = object.getString("hash");
                            if (!img_url_info.equals("")){
                                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1);
                                if (img_file_name.contains(".")){
                                    img_file_name = String.format(Locale.CHINA,"%d_%s%s",i,file_hash,img_file_name.substring(img_file_name.indexOf(".")));
                                }else {
                                    img_file_name = String.format(Locale.CHINA,"%d_%s",i,file_hash);
                                }

                                final File file = new File(mAdFilePath + img_file_name);
                                if (!file.exists()){
                                    deleteAdImgFromIndex(i);

                                    final JSONObject img_obj = httpRequest.getFile(file,img_url_info);
                                    if (img_obj.getIntValue("flag") == 0){
                                        final String error = err.concat(img_obj.getString("info"));
                                        Logger.d("load_img_err:%s",error);
                                        if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(error,mContext,getWindow()));
                                    }
                                }
                            }else {
                                deleteAdImgFromIndex(i);
                            }
                        }
                        mAdFileNames = img_dir.list();
                    }else{
                        if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(err + retJson.getString("info"),mContext,getWindow()));
                    }
                }else{
                    if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(err + retJson.getString("info"), mContext,getWindow()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(err + e.getMessage(), mContext,getWindow()));
            }
        });
    }

    private void deleteAdImgFromIndex(int index){
        if (mAdFileNames != null){
            for (String name : mAdFileNames){
                if (name.startsWith(String.valueOf(index))){
                    final File file = new File(mAdFilePath + name);
                    @SuppressWarnings("unused")
                    final boolean delete = file.delete();
                    break;
                }
            }
        }
    }

    private void stopShowImg(){
        if (!CustomApplication.removeTask(showAdImgRunnable)){
            mShowAdImgFut.cancel(true);
            try {
                mShowAdImgFut.get(3,TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | CancellationException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
    private void showAdImg(){
        mBottomRect = new Rect(0,mSurface.getHeight() - 32,mSurface.getWidth(),mSurface.getBottom());
        mShowAdImgFut = CustomApplication.scheduleAtFixedRate(showAdImgRunnable,0,50,TimeUnit.MILLISECONDS);
    }



    private final Runnable showAdImgRunnable = ()->{
        if (mAdFileNames != null) {
            final Canvas canvas = mSurfaceHolder.lockCanvas();
            final Rect rect = new Rect(0,0,mSurface.getWidth(),mSurface.getHeight() - 32);
            if (System.currentTimeMillis() - loseTime >= mShowInterval * 1000 && mShowBannerImg){
                mBannerBitmap = BitmapFactory.decodeFile(mAdFilePath + mAdFileNames[mShowAdImgTimes++ % mAdFileNames.length]);
                loseTime = System.currentTimeMillis();
            }
            if (mBannerBitmap != null){
                canvas.drawBitmap(mBannerBitmap,new Rect(0,0,mBannerBitmap.getWidth(),mBannerBitmap.getHeight()),rect,null);
            }
            final Paint paint = mPaint;

            //图片边框
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            paint.setColor(mContext.getColor(R.color.blue));
            canvas.drawRect(rect,paint);
            //底部区域
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(mContext.getColor(R.color.white));
            canvas.drawRect(mBottomRect,paint);
            //底部文字
            paint.setColor(mContext.getColor(R.color.blue));
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            canvas.drawText("欢迎光临！",mBannerTextX,mBottomRect.top + fontMetrics.bottom - fontMetrics.top,paint);

            if ((mBannerTextX +=1) > mSurface.getWidth())mBannerTextX = 0;

            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    };

    @SuppressWarnings("unused")
    private static Display getDisplayFromMediaRouter(Context context){
        Display presentationDisplay = null;
        MediaRouter mediaRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        if (null != mediaRouter){
            MediaRouter.RouteInfo route = mediaRouter.getDefaultRoute();
            if (route != null) {
                presentationDisplay = route.getPresentationDisplay();
            }
        }
        return presentationDisplay;
    }
    private static Display getDisplayFromService(Context context){
        Display presentationDisplay = null;
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        if (null != displayManager){
            Display[] displays = displayManager.getDisplays();
            if (displays != null && displays.length > 1) {
                presentationDisplay = displays[1];
            }
        }
        return presentationDisplay;
    }

    public static SecondDisplay getInstantiate(final SaleActivity context){
        final JSONObject object = new JSONObject();
        SecondDisplay secondDisplay = null;
        if (SQLiteHelper.getLocalParameter("dual_v",object)){
            if (object.getIntValue("s") == 1){
                Display presentationDisplay = getDisplayFromService(context);
                if (presentationDisplay != null) {
                    secondDisplay = new SecondDisplay(context, presentationDisplay);
                    secondDisplay.setShowInterval(object.getIntValue("v"));
                }
            }
        }else{
            MyDialog.ToastMessage("初始化双屏错误：" + object.getString("info"),context,null);
        }
        return secondDisplay;
    }
}
