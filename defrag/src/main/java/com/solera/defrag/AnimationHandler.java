package com.solera.defrag;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.view.View;

public interface AnimationHandler {
	@NonNull
	Animator createAnimation(@NonNull View from, @NonNull View to, @NonNull TraversalDirection direction);
}
