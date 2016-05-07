package com.solera.defrag;

import android.support.annotation.NonNull;

/**
 * Interface definition of a callback for when the traversal state has changed in the ViewStack.
 */
public interface ViewStackListener {
  /**
   * Called when the ViewStack is changing state.
   */
  void onTraversing(@NonNull TraversingState traversingState);
}
