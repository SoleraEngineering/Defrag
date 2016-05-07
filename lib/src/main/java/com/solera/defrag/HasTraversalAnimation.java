package com.solera.defrag;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * A view that defines it's own traversal animation. If a view on the {@link ViewStack} implements
 * this, the animation returned by createAnimation will be run rather than the default.
 */
public interface HasTraversalAnimation {
  /**
   * @param fromView the view that we are traversing from.
   * @return an animation that will be run for the traversal, or null if the default should be run.
   */
  @Nullable Animator createAnimation(@NonNull View fromView);
}
