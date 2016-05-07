package com.solera.defrag;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * A collection of utilities for views.
 */
public class ViewUtils {
  private ViewUtils() {
  }

  /**
   * Call the given callback when the view has been measured (if it has already been measured, the
   * callback is immediately invoked.
   *
   * @param view the view to measure
   * @param callback the callback to invoke
   */
  @MainThread public static void waitForMeasure(@NonNull final View view,
      @NonNull final OnMeasuredCallback callback) {
    int width = view.getWidth();
    int height = view.getHeight();

    if (width > 0 && height > 0) {
      callback.onMeasured(view, width, height);
      return;
    }

    view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override public boolean onPreDraw() {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        if (observer.isAlive()) {
          observer.removeOnPreDrawListener(this);
        }

        callback.onMeasured(view, view.getWidth(), view.getHeight());

        return true;
      }
    });
  }

  public interface OnMeasuredCallback {
    void onMeasured(View view, int width, int height);
  }
}
