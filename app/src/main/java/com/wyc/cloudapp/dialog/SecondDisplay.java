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

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.print.PrintUtilsToBitbmp;
import com.wyc.cloudapp.print.PrinterCommands;
import com.wyc.cloudapp.utils.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SecondDisplay extends Presentation implements SurfaceHolder.Callback {
    private final String mAdFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/ad_img/";
    private SaleGoodsViewAdapter mSaleGoodsAdapter;
    private RecyclerView mSaleGoodsView;
    private Context mContext;
    private JSONObject mStoreinfo;
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
    private SecondDisplay(Context outerContext, Display display) {
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

        mPaint = new Paint();
        mPaint.setTextSize(18);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initSaleGoodsView(){
        mSaleGoodsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                JSONArray datas = mSaleGoodsAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0;
                if (datas.length() == 0){
                    if (!mShowBannerImg) mShowBannerImg = true;
                    for (int i = 0,length = datas.length();i < length;i ++){
                        JSONObject jsonObject = datas.optJSONObject(i);
                        sale_sum_num += jsonObject.optDouble("xnum");
                        sale_sum_amount += jsonObject.optDouble("sale_amt");
                    }
                    mSaleSumNum.setText(String.format(Locale.CANADA,"%.3f",sale_sum_num));
                    mSaleSumAmount.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                    mSaleGoodsView.scrollToPosition(mSaleGoodsAdapter.getCurrentItemIndex());
                }else{
                    if (mShowBannerImg) mShowBannerImg = false;
                }
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
            int barcode_id = sale.optInt("barcode_id");
            if ( 0 != barcode_id && barcode_id != mCurrentBarcodeId){//当前显示的图片和即将要显示的图片不相同时再显示
                CustomApplication.execute(()->{
                    final String sql = "select ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and barcode_id = " + barcode_id +
                            " UNION\n" +
                            "select ifnull(img_url,'') img_url from goods_group where status = '1' and gp_id =" + barcode_id;
                    JSONObject object = new JSONObject();
                    if (SQLiteHelper.execSql(object,sql)){
                        String img_url = object.optString("img_url");
                        if (!"".equals(img_url)){
                            final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                            mBannerBitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                            mCurrentBarcodeId = barcode_id;
                        }
                    }else{
                        mSurface.post(()->MyDialog.ToastMessage(object.optString("info"), mContext,getWindow()));
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
    private void setShowInterval(int interval){
        if (interval > 5){
            mShowInterval = interval;
        }
    }
    public void notifyChange(int index){
        mSaleGoodsAdapter.setCurrentItemIndex(index).notifyDataSetChanged();
    }

    public void loadAdImg(final String url,final String appid,final String appScret){
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
            HttpRequest httpRequest = new HttpRequest();
            JSONObject object = new JSONObject();
            final String err = "获取广告图片错误：";

            Activity activity = null;
            if (mContext instanceof Activity){
                activity = (Activity)mContext;
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
                                        if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(err + img_obj.optString("info"),mContext,getWindow()));
                                    }
                                }
                            }
                        }
                        mAdFileNames = img_dir.list();
                    }else{
                        if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(err + retJson.optString("info"),mContext,getWindow()));
                    }
                }else{
                    if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(err + retJson.optString("info"), mContext,getWindow()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (activity != null)activity.runOnUiThread(()->MyDialog.ToastMessage(err + e.getMessage(), mContext,getWindow()));
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
            }
        }
    }
    private void showAdImg(){
        mBottomRect = new Rect(0,mSurface.getHeight() - 32,mSurface.getWidth(),mSurface.getBottom());
        mShowAdImgFut = CustomApplication.scheduleAtFixedRate(showAdImgRunnable,0,50,TimeUnit.MILLISECONDS);
    }



    private Runnable showAdImgRunnable = ()->{
        if (mAdFileNames != null) {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            Rect rect = new Rect(0,0,mSurface.getWidth(),mSurface.getHeight() - 32);
            if (System.currentTimeMillis() - loseTime >= mShowInterval * 1000 && mShowBannerImg){
                mBannerBitmap = BitmapFactory.decodeFile(mAdFilePath + mAdFileNames[mShowAdImgTimes++ % mAdFileNames.length]);
                loseTime = System.currentTimeMillis();
            }
            if (mBannerBitmap != null){
                canvas.drawBitmap(mBannerBitmap,new Rect(0,0,mBannerBitmap.getWidth(),mBannerBitmap.getHeight()),rect,null);
            }


            //图片边框
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1);
            mPaint.setColor(mContext.getColor(R.color.blue));
            canvas.drawRect(rect,mPaint);
            //底部区域
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mContext.getColor(R.color.white));
            canvas.drawRect(mBottomRect,mPaint);
            //底部文字
            mPaint.setColor(mContext.getColor(R.color.blue));
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            canvas.drawText("欢迎光临！",mBannerTextX,mBottomRect.top + fontMetrics.bottom - fontMetrics.top,mPaint);

            if ((mBannerTextX +=1) > mSurface.getWidth())mBannerTextX = 0;

            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    };

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

    public static SecondDisplay getInstantiate(Context context){
        JSONObject object = new JSONObject();
        SecondDisplay secondDisplay = null;
        if (SQLiteHelper.getLocalParameter("dual_v",object)){
            if (object.optInt("s") == 1){
                Display presentationDisplay = getDisplayFromService(context);
                if (presentationDisplay != null) {
                    secondDisplay = new SecondDisplay(context, presentationDisplay);
                    secondDisplay.setShowInterval(object.optInt("v"));
                }
            }
        }else{
            MyDialog.ToastMessage("初始化双屏错误：" + object.optString("info"),context,null);
        }
        return secondDisplay;
    }
}
