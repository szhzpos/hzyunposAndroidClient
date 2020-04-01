package com.wyc.cloudapp.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {
    public BaseFragment() {}
    public abstract String getTitle();
    public abstract JSONObject laodContent();
    public abstract boolean saveContent();
}
