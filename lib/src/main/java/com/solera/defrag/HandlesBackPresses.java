package com.solera.defrag;

/**
 * A view that handles back press events. If a view on the {@link ViewStack} implements this, the
 * event will be handled here, rather than the default behaviour ({@link ViewStack#pop()}.
 */
public interface HandlesBackPresses {
  /**
   * Handle a backpress event.
   *
   * @return true if the view handled this event, false otherwise. If the event is not handled here,
   * the activity should handle it.
   */
  boolean onBackPressed();
}
