package com.wyc.cloudapp.CustomizationView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.CustomizationView
 * @ClassName: AdaptiveTextView
 * @Description: 内容自适应控件尺寸
 * @Author: wyc
 * @CreateDate: 2021/3/30 11:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/30 11:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class AdaptiveTextView extends androidx.appcompat.widget.AppCompatTextView {
    private CharSequence mText;
    private int w,h;
    private final Paint paint = new Paint();
    public AdaptiveTextView(Context context) {
        this(context,null);
    }

    public AdaptiveTextView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AdaptiveTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed){
            w = getMeasuredWidth();
            h = getMeasuredHeight();
            super.setText(formatText(mText),BufferType.NORMAL);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mText = text;
        super.setText(text, type);
    }
    private CharSequence formatText(CharSequence text){

        final String sz = text.toString();
        final Rect bound = new Rect();
        paint.setTextSize(getTextSize());
        paint.getTextBounds(sz,0,sz.length(),bound);

        int b_w = bound.width();

        int available_w = w - getPaddingLeft() - getPaddingRight(),available_h = h - getPaddingTop() - getPaddingBottom();

        if (available_w < b_w){
            int t_w = 0, t_h = Math.abs((int)(paint.getFontMetrics().top - paint.getFontMetrics().bottom));
            final StringBuilder sb = new StringBuilder();
            CharSequence cs;
            int times = available_h / t_h;
            int len = sz.length(),s_len;
            for (int i = 0;i < len;i++){
                cs = sz.subSequence(i,i +1);
                paint.getTextBounds(cs.toString(),0,1,bound);
                t_w += bound.width();
                if (t_w > available_w){
                    if (--times <= 0){
                        s_len = sb.length();
                        sb.replace(s_len-1,s_len,"...");
                        return sb;
                    }else {
                        sb.append("\n");
                        sb.append(cs);
                        t_w = 0;
                    }
                }else {
                    sb.append(cs);
                }
            }
            s_len = sb.length();
            if (s_len> 0){
                if (s_len < len){
                    sb.replace(s_len-1,s_len,"...");
                }
                return sb;
            }
        }
        return text;
    }


    @Override
    public CharSequence getText() {
        return mText;
    }
}
