package com.solera.defrag;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public interface AnimationHandler {
	@Nullable
	TraversalAnimation createAnimation(@NonNull View from, @NonNull View to, @TraversingOperation int operation);
}
