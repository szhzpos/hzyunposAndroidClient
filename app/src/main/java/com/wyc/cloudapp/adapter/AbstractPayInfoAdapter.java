package com.wyc.cloudapp.adapter;

public abstract class AbstractPayInfoAdapter<T extends AbstractTableDataAdapter.SuperViewHolder>  extends AbstractTableDataAdapter<T> {
    public abstract boolean isPaySuccess();
}
