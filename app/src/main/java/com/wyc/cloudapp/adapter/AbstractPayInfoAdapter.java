package com.wyc.cloudapp.adapter;

import com.wyc.cloudapp.activity.base.MainActivity;

public abstract class AbstractPayInfoAdapter<T extends AbstractTableDataAdapter.SuperViewHolder>  extends AbstractTableDataAdapter<T> {
    public AbstractPayInfoAdapter(MainActivity activity) {
        super(activity);
    }

    public abstract boolean isPaySuccess();
}
