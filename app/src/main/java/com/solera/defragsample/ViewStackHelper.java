package com.solera.defragsample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.solera.defrag.ViewStack;

/**
 * Returns the view stack given the context.
 */
public class ViewStackHelper {
	private static final String VIEW_STACK_SERVICE_NAME = "view_stack";

	private ViewStackHelper() {
	}

	@Nullable public static ViewStack getViewStack(@NonNull View view) {
		return getViewStack(view.getContext());
	}

	@Nullable public static ViewStack getViewStack(@NonNull Context context) {
		//noinspection WrongConstant
		return (ViewStack) context.getSystemService(VIEW_STACK_SERVICE_NAME);
	}

	public static boolean matchesServiceName(String serviceName) {
		return VIEW_STACK_SERVICE_NAME.equals(serviceName);
	}
}
