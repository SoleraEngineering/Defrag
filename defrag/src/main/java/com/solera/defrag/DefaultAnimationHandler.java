package com.solera.defrag;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * An implementation of the AnimationHandler. A view bounces in/out when being added/removed
 * from the stack.
 */
class DefaultAnimationHandler implements AnimationHandler {
	private static final int DEFAULT_ANIMATION_DURATION_IN_MS = 300;

	@NonNull
	@Override
	public Animator createAnimation(@NonNull View from, @NonNull View to,
									@NonNull TraversalDirection direction) {
		boolean backward = direction == TraversalDirection.BACK;

		AnimatorSet set = new AnimatorSet();

		set.setInterpolator(new OvershootInterpolator());
		set.setDuration(DEFAULT_ANIMATION_DURATION_IN_MS);

		set.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0.0f));
		set.play(ObjectAnimator.ofFloat(to, View.ALPHA, 0.5f, 1.0f));

		if (backward) {
			set.play(ObjectAnimator.ofFloat(from, View.SCALE_X, 1.1f));
			set.play(ObjectAnimator.ofFloat(from, View.SCALE_Y, 1.1f));
			set.play(ObjectAnimator.ofFloat(to, View.SCALE_X, 0.9f, 1.0f));
			set.play(ObjectAnimator.ofFloat(to, View.SCALE_Y, 0.9f, 1.0f));
		} else {
			set.play(ObjectAnimator.ofFloat(from, View.SCALE_X, 0.9f));
			set.play(ObjectAnimator.ofFloat(from, View.SCALE_Y, 0.9f));
			set.play(ObjectAnimator.ofFloat(to, View.SCALE_X, 1.1f, 1.0f));
			set.play(ObjectAnimator.ofFloat(to, View.SCALE_Y, 1.1f, 1.0f));
		}

		return set;
	}
}
