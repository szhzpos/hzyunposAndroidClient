package com.wyc.cloudapp.activity.mobile;

import android.os.Bundle;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.FragmentPagerAdapter;
import com.wyc.cloudapp.mobileFragemt.AbstractMobileFragment;
import com.wyc.cloudapp.mobileFragemt.TimeCardSaleQueryFragment;
import com.wyc.cloudapp.mobileFragemt.TimeCardUseQueryFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeCardQueryActivity extends AbstractMobileActivity {
    @BindView(R.id._fragment_tab)
    TabLayout _tab;
    @BindView(R.id.view_pager)
    ViewPager2 view_pager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getString(R.string.once_card_query_sz));
        ButterKnife.bind(this);

        init();
    }

    private void init(){
        final List<AbstractMobileFragment> fragments = new ArrayList<>();
        fragments.add(new TimeCardSaleQueryFragment());
        fragments.add(new TimeCardUseQueryFragment());

        final FragmentPagerAdapter<AbstractMobileFragment> adapter =  new FragmentPagerAdapter<>(fragments,this);
        view_pager.setAdapter(adapter);

        new TabLayoutMediator(_tab, view_pager,(tab, position) -> tab.setText(adapter.getItem(position).getTitle())).attach();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_pager_container;
    }
}