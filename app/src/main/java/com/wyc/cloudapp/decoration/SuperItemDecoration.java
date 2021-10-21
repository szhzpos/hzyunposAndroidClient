package com.wyc.cloudapp.decoration;

import android.graphics.Canvas;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class SuperItemDecoration extends RecyclerView.ItemDecoration {
    protected int mSpace = -1;
    public SuperItemDecoration(){
    }
    @Override
    public void onDraw(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }

    private void getVerSpacing(int viewHeight,int m_height){
        double vertical_space ,vertical_counts,per_vertical_space;
        vertical_space = viewHeight % m_height;
        vertical_counts = viewHeight / m_height;
        per_vertical_space = vertical_space / (vertical_counts != 0 ? vertical_counts:1);
        mSpace = (int) Utils.formatDouble(per_vertical_space,0);
    }

    public static void registerGlobalLayoutToRecyclerView(@NonNull final RecyclerView recyclerView,final float size,final SuperItemDecoration decoration){
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = recyclerView.getMeasuredHeight(),counts = recyclerView.getItemDecorationCount();
                if (null != decoration){
                    if (counts > 0)recyclerView.removeItemDecorationAt(0);
                    decoration.getVerSpacing(height, (int) size);
                    recyclerView.addItemDecoration(decoration);
                }else {
                    if (counts > 0){
                        final RecyclerView.ItemDecoration itemDecoration = recyclerView.getItemDecorationAt(0);
                        if (itemDecoration instanceof SuperItemDecoration){
                            ((SuperItemDecoration) itemDecoration).getVerSpacing(height, (int) size);
                            recyclerView.invalidateItemDecorations();
                        }
                    }
                }
            }
        });
    }
}
