package com.wyc.cloudapp.listener;

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

public class WindowCallback implements Window.Callback {
    private Window mWin;
    private Object mObj;
    public WindowCallback(@NonNull final Window window,final Object obj){
        mWin = window;
        mObj = obj;
    }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {

            return mWin.superDispatchKeyEvent(event);
        }

        @Override
        public boolean dispatchKeyShortcutEvent(KeyEvent event) {
            return mWin.superDispatchKeyShortcutEvent(event);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN){
                if (mObj instanceof Toast){
                    ((Toast)mObj).cancel();
                }else if (mObj instanceof Snackbar){
                    ((Snackbar)mObj).dismiss();
                }
                mObj = null;//断开引用
            }
            return mWin.superDispatchTouchEvent(event);
        }

        @Override
        public boolean dispatchTrackballEvent(MotionEvent event) {
            return mWin.superDispatchTrackballEvent(event);
        }

        @Override
        public boolean dispatchGenericMotionEvent(MotionEvent event) {
            return mWin.superDispatchGenericMotionEvent(event);
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            return false;
        }

        @Nullable
        @Override
        public View onCreatePanelView(int featureId) {
            return null;
        }

        @Override
        public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
            return false;
        }

        @Override
        public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
            return false;
        }

        @Override
        public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
            return false;
        }

        @Override
        public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
            return false;
        }

        @Override
        public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {

        }

        @Override
        public void onContentChanged() {

        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {

        }

        @Override
        public void onAttachedToWindow() {

        }

        @Override
        public void onDetachedFromWindow() {

        }

        @Override
        public void onPanelClosed(int featureId, @NonNull Menu menu) {

        }

        @Override
        public boolean onSearchRequested() {
            return false;
        }

        @Override
        public boolean onSearchRequested(SearchEvent searchEvent) {
            return false;
        }

        @Nullable
        @Override
        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
            return null;
        }

        @Nullable
        @Override
        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
            return null;
        }

        @Override
        public void onActionModeStarted(ActionMode mode) {

        }

        @Override
        public void onActionModeFinished(ActionMode mode) {

        }
}
