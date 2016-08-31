Change Log
==========

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
