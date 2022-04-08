package com.wyc.cloudapp.dialog;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.wyc.cloudapp.R;

public class TakePhotoPopWin extends PopupWindow {
    public TakePhotoPopWin(Context context,View.OnClickListener itemsOnClick) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.take_photo_pop, null);
        Button btn_take_photo = view.findViewById(R.id.btn_take_photo);
        Button btn_pick_photo = view.findViewById(R.id.btn_pick_photo);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> dismiss());
        btn_pick_photo.setOnClickListener(v -> {
            dismiss();
            itemsOnClick.onClick(v);
        });
        btn_take_photo.setOnClickListener(v -> {
            dismiss();
            itemsOnClick.onClick(v);
        });
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setOutsideTouchable(true);
        this.setAnimationStyle(R.style.take_photo_anim);
        this.setContentView(view);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
    }
}