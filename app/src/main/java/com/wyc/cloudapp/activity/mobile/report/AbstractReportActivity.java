package com.wyc.cloudapp.activity.mobile.report;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.utils.Utils;

import java.util.Calendar;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile.report
 * @ClassName: SuperReportActivity
 * @Description: 报表Activity父类
 * @Author: wyc
 * @CreateDate: 2021/2/20 10:02
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/20 10:02
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractReportActivity extends AbstractMobileActivity {
    protected JSONObject mQueryConditionObj;
    protected View mCurrentDateView;
    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMenu();
        mQueryConditionObj = new JSONObject();
        mQueryConditionObj.put("stores_id",getStoreId());
    }
    protected void showDatePickerDialog(final Context context, final TextView tv, Calendar calendar) {
        Utils.showDatePickerDialog(context,mCurrentDateView,tv,calendar);
    }

    protected void setStartTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
    }
    protected void setEndTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
    }

    private void initMenu() {
        setLeftText(getIntent().getStringExtra("title"));
        setMiddleText(getStoreName());
    }
}
