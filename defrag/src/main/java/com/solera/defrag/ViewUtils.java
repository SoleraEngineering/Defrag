/*
 * Copyright 2013 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Taken from Flow sample app, found at https://github.com/square/flow/flow-sample/src/main/java/com/example/flow/util/Utils.java
 */
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

  /**
   * Call the given callback when viewStack will be in the given state (if it is already in the
   * given state, the callback is immediately invoked).
   *
   * @param viewStack the viewStack to wait on
   * @param desiredState the traversing state to wait on
   * @param callback the callback to invoke
   */
  @MainThread public static void waitForTraversingState(@NonNull final ViewStack viewStack,
      @NonNull final TraversingState desiredState, @NonNull final ViewStackListener callback) {
    if (desiredState == viewStack.getTraversingState()) {
      callback.onTraversing(desiredState);
    } else {
      viewStack.addTraversingListener(new ViewStackListener() {
        @Override public void onTraversing(@NonNull TraversingState traversingState) {
          if (traversingState == desiredState) {
            viewStack.removeTraversingListener(this);
            callback.onTraversing(desiredState);
          }
        }
      });
    }
  }

  public interface OnMeasuredCallback {
    void onMeasured(View view, int width, int height);
  }
}
