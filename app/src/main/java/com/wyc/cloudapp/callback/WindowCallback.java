package com.wyc.cloudapp.callback;

import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.wyc.cloudapp.logger.Logger;

public class WindowCallback implements Window.Callback {
    private final Window mWin;
    private Object mObj;
    private Window.Callback  mOri;
    public WindowCallback(@NonNull final Window window,final Object obj){
        mOri = window.getCallback();
        mWin = window;
        mObj = obj;
    }
    @Override
    protected void finalize(){
        Logger.d("WindowCallback finalized");
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        dismiss();
        clear();
        return mWin.superDispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        clear();
        return mWin.superDispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP){
            dismiss();
        }
        clear();
        return mWin.superDispatchTouchEvent(event);
    }

    private void clear(){
        if (mOri != null)mWin.setCallback(mOri);
        mOri = null;
        mObj = null;//断开引用
    }
    private void dismiss(){
        if (mObj instanceof Toast){
            ((Toast)mObj).cancel();
        }else if (mObj instanceof Snackbar){
            ((Snackbar)mObj).dismiss();
        }
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        clear();
        return mWin.superDispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        clear();
        return mWin.superDispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        clear();
        return false;
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        clear();
        return null;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        clear();
        return false;
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
        clear();
        return false;
    }

    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
        clear();
        return false;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        clear();
        return false;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        clear();
    }

    @Override
    public void onContentChanged() {
        clear();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        clear();
    }

    @Override
    public void onAttachedToWindow() {
        clear();
    }

    @Override
    public void onDetachedFromWindow() {
        clear();
    }

    @Override
    public void onPanelClosed(int featureId, @NonNull Menu menu) {
        clear();
    }

    @Override
    public boolean onSearchRequested() {
        clear();
        return false;
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        clear();
        return false;
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        clear();
        return null;
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        clear();
        return null;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        clear();
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        clear();
    }
}
