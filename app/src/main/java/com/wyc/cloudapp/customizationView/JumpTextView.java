package com.wyc.cloudapp.customizationView;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.CustomizationView
 * @ClassName: JumpTextView
 * @Description: Fragment点击跳转叶节点
 * @Author: wyc
 * @CreateDate: 2021/1/27 15:09
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/1/27 15:09
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class JumpTextView extends androidx.appcompat.widget.AppCompatTextView {
    public JumpTextView(Context context) {
        this(context,null);
    }

    public JumpTextView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public JumpTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
