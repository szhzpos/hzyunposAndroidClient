package com.wyc.cloudapp.CustomizationView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PieView extends View {
    private Paint mPaint;
    private RectF mPieRect;
    private int mViewHeight,mViewWidth,mSweepAngle;
    private JSONArray mDatas;
    private double mSumValue;
    private int mDataSize = 0,mMaxLegendWidth;
    private List<Rect> mLegendTextBounds;
    private Point mCoordinateCenter;
    private boolean mInitFlag = true;
    private JSONObject mSelectedPie;
    private Context mContext;
    private float mTouchX,mTouchY;
    public PieView(Context context) {
        super(context);
        init(context);
    }

    public PieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int w = getMeasuredWidth(),h = getMeasuredHeight();
        int minSize = Math.min(w, h);
        mPieRect.set(-minSize / 2.5f,-minSize / 2.5f,minSize / 2.5f,minSize / 2.5f);
        mCoordinateCenter.set(w / 4, h / 2);
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom){
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.translate(mCoordinateCenter.x,mCoordinateCenter.y);
        if (mInitFlag) {
            drawAnimationPie(canvas);
        }else
            drawSelectedPie(canvas);

        drawCentreCircle(canvas);
        drawBubble(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_HOVER_ENTER:
                IsPointInCircularSector(event.getX() - mCoordinateCenter.x,event.getY() - mCoordinateCenter.y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
        }
        return super.onTouchEvent(event);
    }

    private void init(final Context context){
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPieRect = new RectF();
        mDatas = new JSONArray();
        mLegendTextBounds = new ArrayList<>();
        mCoordinateCenter = new Point();
    }

    private void drawCentreCircle(final Canvas canvas){
        if (mDataSize == 0)return;
        final String centraContent = String.format(Locale.CHINA,"%.2f",mSumValue),sz = "总金额";

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(0,0,mPieRect.width() / 3.5f,mPaint);
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(Utils.sp2px(mContext,14));

        final Rect textBounds = new Rect();
        mPaint.getTextBounds(sz,0,sz.length(),textBounds);

        canvas.drawText(sz,-textBounds.width() >> 1,-Utils.dpToPx(mContext,18),mPaint);
        canvas.drawText(centraContent,-mPaint.measureText(centraContent) / 2,textBounds.bottom + textBounds.height() >> 1,mPaint);
    }

    private void drawAnimationPie(final Canvas canvas){
        JSONObject object;
        float startAngle;
        for (int i = 0;i < mDataSize;i++) {
            object = mDatas.getJSONObject(i);
            startAngle = (float) object.getDoubleValue("startAngle");
            if (mSweepAngle >= startAngle) {
                mPaint.setColor(object.getIntValue("color"));
                canvas.drawArc(mPieRect,startAngle, mSweepAngle - startAngle, true, mPaint);
                drawLegend(canvas,i,object);
            }
        }
    }

    private void drawSelectedPie(final Canvas canvas){
        JSONObject object;
        float startAngle,sweepAngle;
        boolean isAllZero = true;
        for (int i = 0;i < mDataSize;i++) {
            object = mDatas.getJSONObject(i);
            startAngle = (float) object.getDoubleValue("startAngle");
            sweepAngle = (float) object.getDoubleValue("sweepAngle");

            drawLegend(canvas,i,object);

            if (object.getDoubleValue("value") != 0.0) {
                if (isAllZero) isAllZero = false;
            }else{
                continue;
            }

            mPaint.setColor(object.getIntValue("color"));
            if (object.getBooleanValue("isSel")){
                canvas.save();
                canvas.scale(1.1f,1.1f);
                canvas.drawArc(mPieRect,startAngle, sweepAngle, true, mPaint);
                canvas.restore();
            }else
                canvas.drawArc(mPieRect,startAngle, sweepAngle, true, mPaint);
        }
        if (isAllZero)canvas.drawArc(mPieRect,0, 360, true, mPaint);
    }

    public void update(){
        mSweepAngle += 8;
        if (mSweepAngle > 360){
            mSweepAngle = 0;
            mInitFlag = false;
            removeCallbacks(null);
        }else{
            invalidate();
            postDelayed(this::update,10);
        }
    }


    public void setDatas(final JSONArray datas){
        if (datas == null)return;
        resetMember(datas);

        double sum_raio = 0.0,value = 0.0,ratio = 0.0;

        JSONObject object,preObj = null;

        int size = datas.size();
        String legendContent;
        for (int i = 0;i < size;i++){
            object = datas.getJSONObject(i);
            if (i > 0)preObj = datas.getJSONObject(i - 1);
            value = object.getDouble("value");
            if (i + 1 == size && value != 0.0){
                ratio = Utils.formatDoubleDown(1.0 - sum_raio,5);
            }else{
                ratio = Utils.formatDoubleDown(mSumValue != 0.0 ? value / mSumValue : 0.0,5);
                sum_raio += ratio;
            }
            object.put("color",Color.rgb(getRandomNumber(10,218),getRandomNumber(18,228),getRandomNumber(8,248)));
            object.put("ratio",ratio);
            object.put("startAngle",preObj == null ? 0 : preObj.getDouble("sweepAngle") + preObj.getDouble("startAngle"));
            object.put("sweepAngle",ratio * 360);

            mDatas.add(object);
            mDataSize += 1;

            //计算图例内容宽度
            legendContent = String.format(Locale.CHINA,"%s,%f",object.getString("label"),value);
            final Rect textBounds = new Rect();
            float fontSize = mPaint.getTextSize();
            mPaint.setTextSize(Utils.sp2px(mContext,16));
            mPaint.getTextBounds(legendContent,0,legendContent.length(),textBounds);
            mPaint.setTextSize(fontSize);
            mLegendTextBounds.add(textBounds);
        }
        mMaxLegendWidth = getMaxLegendWidth();
        update();
    }

    private void resetMember(final JSONArray datas){
        if (mDataSize != 0){
            mSelectedPie = null;
            mInitFlag = true;
            mDataSize = 0;
            mDatas = new JSONArray();
            mLegendTextBounds = new ArrayList<>();
        }
        mSumValue = getSumValue(datas);
    }

    private int getMaxLegendWidth(){
        int max = 0;
        if (mLegendTextBounds != null){
            Collections.sort(mLegendTextBounds, (o1, o2) -> -Integer.compare(o1.width(), o2.width()));
            max = mLegendTextBounds.get(0).width();
        }
        Logger.d("max_legend_width:%d",max);
        return max;
    }

    private double getSumValue(final JSONArray datas){
        double sum = 0.0;
        for (int i = 0,size = datas.size();i < size;i++){
            final JSONObject obj = datas.getJSONObject(i);
            if (obj != null)sum += obj.getDoubleValue("value");
        }
        return sum;
    }

    private void drawPolyline(final Canvas canvas,int index,final JSONObject object){
        double degree = 0.0,sweepAngle = 0.0,radius = mPieRect.width() / 2;
        float line_size = 180;

        canvas.save();

        sweepAngle = (float) object.getDoubleValue("sweepAngle");
        if (sweepAngle == 0)return;

        final String label = String.format(Locale.CHINA,"%s%s",object.getString("label"),object.getString("value"));
        final Rect fontSize = new Rect();
        mPaint.getTextBounds(label,0,label.length(),fontSize);
        Logger.d("fontSize:%s,width:%d,height:%d",fontSize,fontSize.width(),fontSize.height());

        degree = (float) object.getDoubleValue("startAngle") + sweepAngle / 2;

        double radian = degree * Math.PI / 180;

        float xAxis = (float) (radius * Math.cos(radian)),yAxis = (float) (radius * Math.sin(radian));

        Logger.d("label:%s,dx:%f,dy:%f,Quadrant:%d",object.getString("label"),xAxis,yAxis,whichQuadrant(xAxis,yAxis));

        float stopX = xAxis < 0.0 ? xAxis - line_size : xAxis + line_size;

        canvas.drawLine(xAxis,yAxis, stopX,yAxis,mPaint);
        mPaint.setTextSize(36);
        canvas.drawText(String.format(Locale.CHINA,"%s%s",object.getString("label"),object.getString("value")), stopX,yAxis,mPaint);

        canvas.restore();
    }

    private int whichQuadrant(double xAxis,double yAxis){
        int quadrant = 0;//原点

        if (xAxis > 0 && yAxis > 0){
            quadrant = 4;
        }else if (xAxis > 0 && yAxis < 0){
            quadrant = 1;
        }else if (xAxis < 0 && yAxis < 0){
            quadrant = 2;
        }else if (xAxis < 0 && yAxis > 0){
            quadrant = 3;
        }else if (xAxis == 0){
            if (yAxis > 0)
                quadrant = 4;
            else
                quadrant = 2;
        }else {
            if (xAxis > 0)
                quadrant = 1;
            else
                quadrant = 2;
        }
        return quadrant;
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private void drawLegend(final Canvas canvas,int index,final JSONObject object){
        final Rect fontSize = mLegendTextBounds.get(index);

        int size = mDataSize - 1,max_legend_width = mMaxLegendWidth;
        float fontHeight = fontSize.height();
        float start_space = mViewWidth - mPieRect.width() - max_legend_width - Utils.dpToPx(mContext,18),vertical_space = (mPieRect.height() * 0.8f - (size * fontHeight)) / (size == 0 ? 1 :size),pie_right = mPieRect.right,
                pie_top =  mPieRect.top + (mViewHeight - fontHeight * mDataSize - size * vertical_space - fontHeight) / 2,radius = 12;
        float xStart = pie_right + start_space,yStart  = index * (fontHeight + vertical_space) + pie_top;

        mPaint.setColor(object.getIntValue("color"));
        mPaint.setTextSize(Utils.sp2px(mContext,16));
        canvas.drawCircle(xStart,yStart,radius,mPaint);
        canvas.drawText(String.format(Locale.CHINA,"%s %s",object.getString("label"),object.getString("value")),xStart + radius / 2 + 8,yStart + fontHeight / 3.0f ,mPaint);
        final RectF legend_coordinate = new RectF(xStart ,yStart - fontHeight ,xStart + max_legend_width,yStart + fontHeight);
        object.put("l_coordi",legend_coordinate);
    }

    private void IsPointInCircularSector(float xAxis,float yAxis){
        double d_r = Math.sqrt(xAxis * xAxis + yAxis * yAxis);
        float pie_width = mPieRect.width();
        double radius = pie_width / 2,degree = 0.0;

        for (int i = 0,size = mDataSize;i < size;i ++){
            final JSONObject object = mDatas.getJSONObject(i);
            double startAngle = object.getDoubleValue("startAngle"),sweepAngle = object.getDoubleValue("sweepAngle");
            final RectF legend_coordinate = (RectF) object.get("l_coordi");
            if (null != legend_coordinate){
                if (d_r <= radius && pie_width / 3.5 <= d_r ){
                    double cos_v = xAxis / d_r,radian = Math.acos(cos_v);
                    degree =360 - radian * 180 / Math.PI;
                    int quadrant = whichQuadrant(xAxis,yAxis);
                    if (quadrant == 3 || quadrant == 4)degree = 360 - degree;
                    if (degree > startAngle && degree < (startAngle + sweepAngle)){
                        if (mSelectedPie == null){
                            object.put("isSel",true);
                            mSelectedPie = object;
                        }else{
                            if (mSelectedPie != object){
                                mSelectedPie.put("isSel",false);
                                object.put("isSel",true);
                                mSelectedPie = object;
                            }else {
                                mSelectedPie.put("isSel",!mSelectedPie.getBooleanValue("isSel"));
                            }
                        }
                        mTouchX = xAxis;
                        mTouchY = yAxis;

                        postInvalidate();
                        break;
                    }
                }else if (xAxis >= legend_coordinate.left && xAxis <= legend_coordinate.right && yAxis >= legend_coordinate.top && yAxis <= legend_coordinate.bottom){
                    if (mSelectedPie == null){
                        object.put("isSel",true);
                        mSelectedPie = object;
                    }else{
                        if (mSelectedPie != object){
                            mSelectedPie.put("isSel",false);
                            object.put("isSel",true);
                            mSelectedPie = object;
                        }else {
                            mSelectedPie.put("isSel",!mSelectedPie.getBooleanValue("isSel"));
                        }
                    }

                    degree =(startAngle + sweepAngle / 2);

                    double bubble_radian = degree *  Math.PI / 180;
                    mTouchX = (float) (Math.cos(bubble_radian) * radius);
                    mTouchY  =  (float) (Math.sin(bubble_radian) * radius);

                    postInvalidate();
                    break;
                }
            }
        }

    }

    private void drawBubble(final Canvas canvas){
        final JSONObject object = mSelectedPie;
        if (object != null && object.getBooleanValue("isSel") && object.getDoubleValue("value") != 0.0){
            final String content = String.format(Locale.CHINA,"%s %s (%.2f%s)",object.getString("label"),object.getString("value"),object.getDoubleValue("ratio") * 100,"%");
            final Rect textBounds = new Rect();
            mPaint.setTextSize(Utils.sp2px(mContext,16));
            mPaint.getTextBounds(content,0,content.length(),textBounds);
            float  wBubble = Utils.dpToPx(mContext, 18) + textBounds.width(),hBubble = Utils.dpToPx(mContext,48);
            float xAxis = mTouchX,yAxis = mTouchY;

            int l_edge = Utils.dpToPx(mContext,15),offset_5 = Utils.dpToPx(mContext,5);
            float x1 = (float) (Math.sin(45 * Math.PI / 180) * l_edge) + 1,y1 = (float) (Math.cos(45 * Math.PI / 180) * l_edge) + 1;
            float s_xAxis = 0,s_yAxis = 0,border_radius = Utils.dpToPx(mContext,2);

            final Path path = new Path();
            final LinearGradient linear = new LinearGradient(wBubble,hBubble,0,0,new int[]{Color.BLACK,Color.rgb(120, 120, 120),Color.rgb(0, 0, 0)} ,new float[]{0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP);
            final RectF rectF = new RectF();

            if (Math.abs(yAxis + mCoordinateCenter.y ) + hBubble +  y1 > mViewHeight){
                s_xAxis = xAxis -x1;
                s_yAxis = yAxis  - y1;
                rectF.set(s_xAxis - offset_5,s_yAxis - hBubble,wBubble + s_xAxis - offset_5,s_yAxis);
            }else{
                s_xAxis = xAxis -x1;
                s_yAxis = yAxis  + y1;
                rectF.set(s_xAxis - offset_5,s_yAxis ,wBubble + s_xAxis - offset_5,hBubble + s_yAxis);
            }

            path.moveTo(xAxis,yAxis);
            path.lineTo(s_xAxis,s_yAxis);
            path.lineTo(xAxis + x1 ,s_yAxis);
            path.lineTo(xAxis,yAxis);

            mPaint.setShader(linear);
            mPaint.setAlpha(128);
            canvas.drawPath(path,mPaint);
            canvas.drawRoundRect(rectF,border_radius,border_radius,mPaint);
            mPaint.setShader(null);

            mPaint.setColor(Color.WHITE);
            canvas.drawText(content,rectF.left + (rectF.width() - textBounds.width()) / 2,rectF.top + (hBubble - textBounds.height()),mPaint);
        }
    }

}
