package com.solera.defrag;

import android.animation.Animator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class TraversalAnimation {
	public static final int ABOVE = 0;
	public static final int BELOW = 1;
	private final Animator animator;
	private final int drawOrder;

	@NonNull public static TraversalAnimation newInstance(@NonNull Animator animator,
			@AnimateInDrawOrder int drawOrder) {
		return new TraversalAnimation(animator, drawOrder);
	}

	private TraversalAnimation(@NonNull Animator animator, @AnimateInDrawOrder int drawOrder) {
		this.animator = animator;
		this.drawOrder = drawOrder;
	}

	@NonNull Animator animator() {
		return animator;
	}

	@AnimateInDrawOrder int drawOrder() {
		return drawOrder;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TraversalAnimation that = (TraversalAnimation) o;

		if (drawOrder != that.drawOrder) return false;
		return animator != null ? animator.equals(that.animator) : that.animator == null;
	}

	@Override public int hashCode() {
		int result = animator != null ? animator.hashCode() : 0;
		result = 31 * result + drawOrder;
		return result;
	}

	@Override public String toString() {
		return "TraversalAnimation{" +
				"animator=" + animator +
				", drawOrder=" + drawOrder +
				'}';
	}

	@Retention(SOURCE) @IntDef({ ABOVE, BELOW }) public @interface AnimateInDrawOrder {
	}
}
