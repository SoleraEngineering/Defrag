package com.solera.defrag;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public interface AnimationHandler {
	/**
	 * Creates a TraversalAnimation for the given views.
	 *
	 * @param from the view that is transitioning out.
	 * @param to the view that is transitioning in.
	 * @param operation the type of operation.
	 * @return the animation, or null if there is no animation.
	 */
	@Nullable TraversalAnimation createAnimation(@NonNull View from, @NonNull View to,
			@TraversingOperation int operation);
}
