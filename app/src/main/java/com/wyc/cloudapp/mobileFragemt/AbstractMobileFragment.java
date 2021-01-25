package com.wyc.cloudapp.mobileFragemt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.fragment.app.Fragment;

import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.logger.Logger;

/*
 * 负责加载功能布局，getRootLayoutId返回根布局id，如果根布局不是功能布局，则根据getMainViewId返回的id在根布局查找。
 * 每个功能布局的叶节点为TextView或其子类，叶节点除了功能布局外最多可以再嵌套一层ViewGroup。
 * 叶节点的点击事件可以调用triggerItemClick，子类可实现此方法实现业务逻辑。
 * Activity 的全类名保存在叶节点的tag属性中。
 * */
public abstract class AbstractMobileFragment extends Fragment {
    protected MainActivity mContext;
    public AbstractMobileFragment(final MainActivity activity){
        mContext = activity;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }

    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final int function_layout_id = getMainViewId(),rood_layout_id = getRootLayout();

        final View root = inflater.inflate(rood_layout_id, container, false);
        ViewGroup function_linearLayout;

        if (rood_layout_id != function_layout_id){
            function_linearLayout = root.findViewById(function_layout_id);
        }else
            function_linearLayout = (ViewGroup) root;

        if (function_linearLayout != null){

            int _count = function_linearLayout.getChildCount(),child_count;
            for (int i = 0;i < _count;i ++){
                final View child = function_linearLayout.getChildAt(i);
                if (child instanceof ViewGroup){
                    final ViewGroup viewGroup = (ViewGroup)child;
                    child_count = viewGroup.getChildCount();
                    for (int j = 0; j < child_count;j ++){
                        final View view = viewGroup.getChildAt(j);
                        view.setOnClickListener(mClickListener);
                    }
                }else if (child instanceof TextView){
                    child.setOnClickListener(mClickListener);
                }
            }
        }
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    abstract protected int getRootLayout();
    abstract protected int getMainViewId();
    abstract protected void triggerItemClick(final View v);

    private final View.OnClickListener mClickListener = this::triggerItemClick;
}
