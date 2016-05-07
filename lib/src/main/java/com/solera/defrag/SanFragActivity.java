package com.solera.defrag;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

/**
 * An Activity that handles ViewStack
 */
public abstract class SanFragActivity extends AppCompatActivity {
  private boolean mDisableUI = false;

  @Override public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
    return mDisableUI || super.dispatchTouchEvent(ev);
  }

  @Override public Object getSystemService(@NonNull String name) {
    if (ViewStack.matchesServiceName(name)) {
      return getViewStack();
    }
    return super.getSystemService(name);
  }

  @NonNull abstract ViewStack getViewStack();

  @Override public void onBackPressed() {
    if (mDisableUI) {
      return;
    }
    if (!getViewStack().onBackPressed()) {
      super.onBackPressed();
    }
  }
}
