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

/**
 * A view that handles back press events. If a view on the {@link ViewStack} implements this, the
 * event will be handled here, rather than the default behaviour ({@link ViewStack#pop()}.
 *
 * @deprecated call {@link ViewStack#pop()} directly when receiving the back press event.
 */
@Deprecated public interface HandlesBackPresses {
  /**
   * Handle a back press event.
   *
   * @return true if the view handled this event, false otherwise. If the event is not handled here,
   * the activity should handle it.
   */
  boolean onBackPressed();
}
