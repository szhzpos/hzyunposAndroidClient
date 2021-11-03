package com.wyc.cloudapp.activity.mobile.cashierDesk;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.FragmentPagerAdapter;
import com.wyc.cloudapp.mobileFragemt.AbstractMobileFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.activity.mobile.cashierDesk
 * @ClassName: FragmentContainerActivity
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021-10-27 11:44
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-10-27 11:44
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class FragmentContainerActivity<T extends AbstractMobileFragment> extends AbstractDefinedTitleActivity {
    @BindView(R.id._fragment_tab)
    TabLayout _tab;
    @BindView(R.id.view_pager)
    ViewPager2 view_pager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        init();
    }

    private void init(){
        final FragmentPagerAdapter<T> adapter =  new FragmentPagerAdapter<>(createFragments(),this);
        view_pager.setAdapter(adapter);
        new TabLayoutMediator(_tab, view_pager,(tab, position) -> tab.setText(adapter.getItem(position).getTitle())).attach();
    }

    protected abstract @NonNull List<T> createFragments();

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_pager_container;
    }
}
