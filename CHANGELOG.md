Change Log
==========

Version 1.1.1
----------------------------
 * ViewStack#replaceStack with an empty stack is now supported.
 * Updated AutoValue to 1.3
 * Minor javadoc fixes

Version 1.1.0
----------------------------
 * Added AnimationHandler a simple api to handle generic transition animations.
 * Moved enums to IntDef for library size optimization.
 * Changed nested class access to private methods & attributes to package private as part of library size optimization.
 * Migrated AutoParcel to AutoValue.
 * Deprecated HandlesBackPresses - this can be handled in a custom callback from onBackPress in you activity.
 * Deprecated HasTraversalAnimation - use AnimationHandler.
 * Call to replace with 0 views will result in a  call to push.
 * Added replaceStack with one view method.

Version 1.0.0
----------------------------
 * Added safe operations helper class.
 * Fixed issue where ConcurrentModificationException would occur when removing listeners while traversing the listener list.

Version 0.9.0
----------------------------
 * Removed deprecated code.

Version 0.9.0-beta2
----------------------------
 * Renamed parameter methods from xxSerializedParameter to xxParameter, now use xxParameter to set simple parameter, and xxParameters to use Bundle.
 * Fixed UI issue with Defrag sample app.

Version 0.9.0-beta1
----------------------------

 * ViewStack is now view - no need to initialize with a view, or call onStop/onStart.
 * Added save/restore of view state support (for pushed/popped views).
 * Added parameter support for bundles.
 * Deprecated Viewstack.get(Context) - use DI or a helper class.
 * Deprecated methods to be removed in next non-beta release.

Version 0.8.2
----------------------------
* Simplified library AndroidManifest.

Version 0.8.1
----------------------------
* Fix potential NullPointerException crash when calling ViewStack#getTopView().

Version 0.8.0
----------------------------

* Initial public release.
