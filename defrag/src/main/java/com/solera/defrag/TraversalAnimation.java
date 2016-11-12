package com.solera.defrag;


import android.animation.Animator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;

import auto.parcel.AutoParcel;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@AutoParcel
public abstract class TraversalAnimation {
	public static final int ABOVE = 0;
	public static final int BELOW = 1;

	@Retention(SOURCE)
	@IntDef({ABOVE, BELOW})
	public @interface AnimateInDrawOrder {
	}

	@NonNull
	public static TraversalAnimation newInstance(@NonNull Animator animator, @AnimateInDrawOrder int drawOrder) {
		return new AutoParcel_TraversalAnimation(animator, drawOrder);
	}

	@NonNull
	abstract Animator animator();

	@AnimateInDrawOrder
	abstract int drawOrder();
}
