/*
 * Copyright 2016 Tom Hall.
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
 */
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
