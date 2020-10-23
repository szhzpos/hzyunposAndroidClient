package com.wyc.cloudapp.dialog.CustomizationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class ScaleView extends View {
    private Context mContext;
    private int  mCapacity = 15000;//capacity 单位g;
    private Paint mPaint;
    private Point mCoordinateCenter;
    private float mOutRadius;
    private float mCurrentValue = 0.000f;
    private float mStartPointerAngle, mEndPointerAngle;
    private float mNumberWidth;
    private List<Path> mNumPathList;
    public ScaleView(Context context) {
        this(context,null);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context,attrs,defStyleAttr,0);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mCoordinateCenter = new Point();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int w = getMeasuredWidth(),h = getMeasuredHeight();
        mOutRadius = Math.min(w, h) / 2.1f;
        mCoordinateCenter.set(w / 5, h / 2);
        mNumPathList = generateCurrentValuePath();
    }

    @Override
    public void onLayout(boolean changed,int l,int t,int r, int b){
        super.onLayout(changed,l,t,r,b);
    }

    @Override
    public void onDraw(Canvas canvas){
        drawDrawable(canvas);
        drawContent(canvas);
    }

    private void drawDrawable(final Canvas canvas){
        final Drawable bg = getBackground();
        if (bg != null)bg.draw(canvas);
    }

    public void setCurrentValue(final float v){
        mCurrentValue = v;
        mStartPointerAngle = mCurrentValue == 0.0f ? 0.0f : 360.0f / mCapacity * mCurrentValue ;
        updatePointerAngle();
    }

    public void updatePointerAngle(){
        boolean isExist = false;

        if (mStartPointerAngle != 0.0){
            float step = 8.0f;
            float diff = mStartPointerAngle - mEndPointerAngle;

            if (diff == 0.0)return;

            if (diff > 0){
                mEndPointerAngle += step;
                if (mEndPointerAngle > mStartPointerAngle){
                    if (Math.abs(mStartPointerAngle) % step > 0){
                        mEndPointerAngle = mStartPointerAngle;
                    }else
                        isExist =true;
                }
            }else{
                mEndPointerAngle -= step;
                if (mEndPointerAngle < mStartPointerAngle ){
                    if (Math.abs(mStartPointerAngle) % step > 0){
                        mEndPointerAngle = mStartPointerAngle;
                    }else
                        isExist =true;
                }
            }

        }else{
            isExist =true;
        }

        if (isExist){
            mEndPointerAngle = mStartPointerAngle;
            mStartPointerAngle = 0;
            removeCallbacks(update);
        }else{
            postDelayed(update,5);
        }
        invalidate();
    }

    private final Runnable update = this::updatePointerAngle;

    private void drawContent(final Canvas canvas){
        canvas.translate(mCoordinateCenter.x,mCoordinateCenter.y);
        drawGraduation(canvas);
        drawInCircle(canvas);
        drawPointer(canvas);
        drawCurrentValue(canvas);
    }

    private void drawCurrentValue(final Canvas canvas){
        float xAxis = mOutRadius + dpToPx(mContext,18),yAxis = -mOutRadius / 1.8f,num_width = mNumberWidth,space_8 = mOutRadius / 18,xAxis_spacing = num_width + space_8;
        float current_value = mCurrentValue / 1000;

        final String _value = String.format(Locale.CHINA,"%.3f",current_value);

        mPaint.setStyle(Paint.Style.STROKE);
        canvas.save();
        canvas.translate(xAxis,yAxis);

        mPaint.setColor(Color.RED);
        for (int i = 0,size = _value.length();i < size; i++){
            final char c = _value.charAt(i);
            if (c == '.'){
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.BLUE);
                canvas.drawCircle(num_width - space_8,- yAxis * 2,mOutRadius / 20.0f,mPaint);
                mPaint.setColor(Color.RED);
                mPaint.setStyle(Paint.Style.STROKE);
            }else{
                if (i > 0)canvas.translate(xAxis_spacing,0 );
                drawNumber(canvas,c);
            }

        }
        canvas.restore();

    }

    private void drawNumber(final Canvas canvas,char num){
        final List<Path> paths = mNumPathList;
        final int[] ints = new int[]{0,1,2,3,4,5,6};
        switch (num){
            case '0':
                ints[6] = -1;
                break;
            case '1':
                ints[0] = -1;
                ints[1] = -1;
                ints[2] = -1;
                ints[3] = -1;
                ints[6] = -1;
                break;
            case '2':
                ints[1] = -1;
                ints[4] = -1;
                break;
            case '3':
                ints[1] = -1;
                ints[2] = -1;
                break;
            case '4':
                ints[0] = -1;
                ints[2] = -1;
                ints[3] = -1;
                break;
            case '5':
                ints[2] = -1;
                ints[5] = -1;
                break;
            case '6':
                ints[5] = -1;
                break;
            case '7':
                ints[1] = -1;
                ints[2] = -1;
                ints[3] = -1;
                ints[6] = -1;
                break;
            case '9':
                ints[2] = -1;
                break;
            default:
                break;
        }
        for (int j : ints){
            if (j == -1)continue;
            canvas.drawPath(paths.get(j),mPaint);
        }
    }

    private List<Path> generateCurrentValuePath(){
        final List<Path> paths = new Vector<>();
        final float hypotenuse = mOutRadius / 10,radian = (float) (45 * Math.PI / 180),side_len = hypotenuse * 4f;
        float dxAxis = 0.0f,dyAxis = 0.0f;
        final Matrix matrix = new Matrix();
        final Path path1 = new Path();

        dxAxis = (float) (hypotenuse * Math.cos(radian));
        dyAxis = (float) (hypotenuse * Math.sin(radian));

        final float border_len = side_len + dyAxis * 2;

        mNumberWidth = side_len + dxAxis * 4;

        path1.moveTo(0,0);
        path1.lineTo(dxAxis,- dyAxis);
        path1.lineTo(dxAxis + side_len,- dyAxis);
        path1.lineTo(dxAxis * 2 + side_len,0);
        path1.lineTo(dxAxis + side_len,dyAxis);
        path1.lineTo(dxAxis ,dyAxis);
        path1.lineTo(0,0);

        final Path path2 = new Path();
        matrix.postRotate(90);
        path1.transform(matrix,path2);
        matrix.reset();

        final Path path3 = new Path();
        matrix.postTranslate(0, border_len);
        path2.transform(matrix,path3);
        matrix.reset();

        final Path path4 = new Path();
        matrix.postTranslate(0, border_len * 2);
        path1.transform(matrix,path4);
        matrix.reset();

        final Path path5 = new Path();
        matrix.postTranslate(border_len, 0);
        path3.transform(matrix,path5);
        matrix.reset();

        final Path path6 = new Path();
        matrix.postTranslate(0, -border_len);
        path5.transform(matrix,path6);
        matrix.reset();

        final Path path7 = new Path();
        matrix.postTranslate(0,border_len);
        path1.transform(matrix,path7);

        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        paths.add(path7);

        return paths;
    }

    private void drawGraduation(final Canvas canvas){
        float radius = mOutRadius;
        int degree = 3,step = 360 / degree,per_step_v = mCapacity / step;
        double graduationRadian = 0.0,big = 0.0;
        float xAxis = 0.0f,yAxis = 0.0f,e_xAxis = 0.0f,e_yAxis = 0.0f,text_xAxis,text_yAxis,space = mOutRadius / 40,lineSize_5 = mOutRadius / 20,lineSize_16 = mOutRadius / 7,textlineSize = mOutRadius / 4,
                e_radius = radius - lineSize_5,e_radius2 = radius - lineSize_16,text_radius = radius - textlineSize,font_size_9 = mOutRadius / 18,font_size_16 = mOutRadius / 10;
        String value;

        final Rect rect = new Rect();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(0,0, radius,mPaint);

        for (int i = 1; i <= step + 1;i++){
            graduationRadian = (i*degree - 90) * Math.PI / 180.0;
            xAxis = (float) (radius * Math.cos(graduationRadian));
            yAxis = (float) (radius * Math.sin(graduationRadian));

            big += per_step_v;

            if (i % 10 == 0){
                mPaint.setColor(Color.BLACK);
                e_xAxis = (float) (e_radius2 * Math.cos(graduationRadian));
                e_yAxis = (float) (e_radius2 * Math.sin(graduationRadian));

                text_xAxis = (float) (text_radius * Math.cos(graduationRadian));
                text_yAxis = (float) (text_radius * Math.sin(graduationRadian));

                mPaint.setTextSize(font_size_16);
                value = String.valueOf(big / 1000);
                mPaint.getTextBounds(value,0,value.length(),rect);
                if (text_xAxis < 0.0){
                    text_xAxis += space;
                }else{
                    text_xAxis -= space;
                }
                canvas.drawText(value,text_xAxis - rect.width() / 2.0f,text_yAxis + rect.height() / 2.0f,mPaint);
            }else  if(i % 5 == 0){
                e_xAxis = (float) ((e_radius2 + space) * Math.cos(graduationRadian));
                e_yAxis = (float) ((e_radius2 + space) * Math.sin(graduationRadian));

                mPaint.setTextSize(font_size_9);
                value = String.valueOf(big / 1000);
                mPaint.getTextBounds(value,0,value.length(),rect);
                text_xAxis = (float) ((e_radius2 - space) * Math.cos(graduationRadian));
                text_yAxis = (float) ((e_radius2 - space)  * Math.sin(graduationRadian));

                if (text_xAxis < 0.0){
                    text_xAxis += space;
                }
                canvas.drawText(value,text_xAxis - rect.width() / 2.0f,text_yAxis + rect.height() / 2.0f,mPaint);
            }else{
                mPaint.setColor(Color.RED);
                e_xAxis = (float) (e_radius * Math.cos(graduationRadian));
                e_yAxis = (float) (e_radius * Math.sin(graduationRadian));
            }
            canvas.drawLine(xAxis,yAxis,e_xAxis,e_yAxis,mPaint);

        }
    }

    private void drawInCircle(final Canvas canvas){
        float inRadius = mOutRadius / 2.5f;
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(0,0, inRadius,mPaint);
        final String unit = "单位：KG";
        canvas.drawText(unit,-mPaint.measureText(unit) / 2,- inRadius / 2,mPaint);
    }

    private void drawPointer(Canvas canvas){
        float pointer_head_len = mOutRadius / 1.05f,pointer_tail_len = pointer_head_len / 3f;
        float radian_15 = (float) (7 * Math.PI / 180);

        float xAxis = 0,yAxis = -pointer_head_len;
        float hypotenuse = (float) Math.sqrt(xAxis * xAxis + yAxis * yAxis);
        float h_xAxis = (float) (Math.sin(radian_15) * hypotenuse),h_yAxis = (float) (Math.cos(radian_15) * hypotenuse);

        float tail_top_yAxis = (float) (Math.cos(radian_15) * pointer_tail_len);

        canvas.save();

        canvas.rotate(mEndPointerAngle);

        final Path path = new Path();
        path.moveTo(xAxis,yAxis);
        path.lineTo(xAxis - h_xAxis,yAxis + h_yAxis);
        path.lineTo(xAxis,tail_top_yAxis);
        path.lineTo(h_xAxis - xAxis ,yAxis + h_yAxis);
        path.close();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(108);
        canvas.drawPath(path,mPaint);
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(0,0, mOutRadius / 20.0f,mPaint);

        canvas.restore();
    }

    public static int dpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int sp2px(final Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
