package com.solera.defrag;


import android.animation.Animator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@AutoValue
public abstract class TraversalAnimation {
	public static final int ABOVE = 0;
	public static final int BELOW = 1;

	@Retention(SOURCE)
	@IntDef({ABOVE, BELOW})
	public @interface AnimateInDrawOrder {
	}

	@NonNull
	public static TraversalAnimation newInstance(@NonNull Animator animator, @AnimateInDrawOrder int drawOrder) {
		return new AutoValue_TraversalAnimation(animator, drawOrder);
	}

	@NonNull
	abstract Animator animator();

	@AnimateInDrawOrder
	abstract int drawOrder();
}
