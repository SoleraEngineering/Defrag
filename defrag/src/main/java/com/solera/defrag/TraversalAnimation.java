package com.solera.defrag;

import android.animation.Animator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class TraversalAnimation {
	public static final int ABOVE = 0;
	public static final int BELOW = 1;
	private final Animator mAnimator;
	private final int mDrawOrder;

	@NonNull public static TraversalAnimation newInstance(@NonNull Animator animator,
			@AnimateInDrawOrder int drawOrder) {
		return new TraversalAnimation(animator, drawOrder);
	}

	private TraversalAnimation(@NonNull Animator animator, @AnimateInDrawOrder int drawOrder) {
		mAnimator = animator;
		mDrawOrder = drawOrder;
	}

	@NonNull Animator animator() {
		return mAnimator;
	}

	@AnimateInDrawOrder int drawOrder() {
		return mDrawOrder;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TraversalAnimation that = (TraversalAnimation) o;

		if (mDrawOrder != that.mDrawOrder) return false;
		return mAnimator != null ? mAnimator.equals(that.mAnimator) : that.mAnimator == null;
	}

	@Override public int hashCode() {
		int result = mAnimator != null ? mAnimator.hashCode() : 0;
		result = 31 * result + mDrawOrder;
		return result;
	}

	@Override public String toString() {
		return "TraversalAnimation{" +
				"mAnimator=" + mAnimator +
				", mDrawOrder=" + mDrawOrder +
				'}';
	}

	@Retention(SOURCE) @IntDef({ ABOVE, BELOW }) public @interface AnimateInDrawOrder {
	}
}
