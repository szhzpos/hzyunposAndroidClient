package com.wyc.cloudapp.dialog.business;

import android.os.Bundle;
import android.view.ViewStub;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.business
 * @ClassName: AddGoodsCategoryDialog
 * @Description: 新增商品分类
 * @Author: wyc
 * @CreateDate: 2021/5/11 16:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/11 16:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class AddGoodsCategoryDialog extends AbstractAddArchiveDialog {
    public AddGoodsCategoryDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.new_category_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayout() {
        return R.layout.add_goods_category_dialog;
    }

    @Override
    protected void sure() {

    }

}
