package com.solera.defrag;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import java.util.List;

/**
 * A collection of utilities for ViewStacks.
 */
public class ViewStackUtils {
	private ViewStackUtils() {
	}

	/**
	 * Safely replace the current view as soon as the {@link ViewStack} will be in {@link
	 * TraversingState#IDLE}. If it is already in the idle state, method is invoked immediately.
	 */
	public static void safeReplace(@NonNull final ViewStack viewStack, @LayoutRes int layout) {
		safeReplaceWithParameters(viewStack, layout, null);
	}

	/**
	 * Safely replace the current view as soon as the {@link ViewStack} will be in {@link
	 * TraversingState#IDLE}. If it is already in the idle state, method is invoked immediately.
	 */
	public static void safeReplaceWithParameters(@NonNull final ViewStack viewStack,
			@LayoutRes final int layout, @Nullable final Bundle parameters) {
		waitForTraversingState(viewStack, TraversingState.IDLE, new Runnable() {
			@Override public void run() {
				viewStack.replaceWithParameters(layout, parameters);
			}
		});
	}

	/**
	 * Safely replace the {@link ViewStack} stack as soon as the {@link ViewStack} will be in {@link
	 * TraversingState#IDLE}. If it is already in the idle state, method is invoked immediately.
	 */
	public static void safeReplaceStack(@NonNull final ViewStack viewStack,
			@NonNull final List<Pair<Integer, Bundle>> views) {
		waitForTraversingState(viewStack, TraversingState.IDLE, new Runnable() {
			@Override public void run() {
				viewStack.replaceStack(views);
			}
		});
	}

	/**
	 * Safely replace the {@link ViewStack} stack as soon as the {@link ViewStack} will be in {@link
	 * TraversingState#IDLE}. If it is already in the idle state, method is invoked immediately.
	 */
	public static void safeReplaceStack(@NonNull final ViewStack viewStack,
			@LayoutRes final Integer layout, @Nullable final Bundle parameters) {
		waitForTraversingState(viewStack, TraversingState.IDLE, new Runnable() {
			@Override public void run() {
				viewStack.replaceStack(layout, parameters);
			}
		});
	}

	/**
	 * Safely push a view (as soon as the {@link ViewStack} will be in {@link TraversingState#IDLE},
	 * if it is already in the idle state, method is invoked immediately).
	 */
	public static void safePush(@NonNull final ViewStack viewStack, @LayoutRes int layout) {
		safePushWithParameters(viewStack, layout, null);
	}

	/**
	 * Safely push a view as soon as the {@link ViewStack} will be in {@link TraversingState#IDLE}.
	 * If it is already in the idle state, method is invoked immediately.
	 */
	public static void safePushWithParameters(@NonNull final ViewStack viewStack,
			@LayoutRes final int layout, @Nullable final Bundle parameters) {
		waitForTraversingState(viewStack, TraversingState.IDLE, new Runnable() {
			@Override public void run() {
				viewStack.pushWithParameters(layout, parameters);
			}
		});
	}

	/**
	 * Safely pop the current view as soon as the {@link ViewStack} will be in {@link
	 * TraversingState#IDLE}. If it is already in the idle state, method is invoked immediately.
	 * <p>
	 * Note: You cannot be sure if the pop operation has been successful.
	 */
	public static void safePop(@NonNull final ViewStack viewStack) {
		safePopWithResult(viewStack, null);
	}

	/**
	 * Safely pop the current view as soon as the {@link ViewStack} will be in {@link
	 * TraversingState#IDLE}. If it is already in the idle state, method is invoked immediately.
	 * <p>
	 * Note: You cannot be sure if the pop operation has been successful.
	 */
	public static void safePopWithResult(@NonNull final ViewStack viewStack,
			@Nullable final Object result) {
		waitForTraversingState(viewStack, TraversingState.IDLE, new Runnable() {
			@Override public void run() {
				viewStack.popWithResult(result);
			}
		});
	}

	/**
	 * Safely pop to the given view layout as soon as the {@link ViewStack} will be in {@link
	 * TraversingState#IDLE}. If it is already in the idle state, method is invoked immediately.
	 * <p>
	 * Note: You cannot be sure if the pop operation has been successful.
	 */
	public static void safePopBackToWithResult(@NonNull final ViewStack viewStack,
			@LayoutRes final int layout, @Nullable final Object result) {
		waitForTraversingState(viewStack, TraversingState.IDLE, new Runnable() {
			@Override public void run() {
				viewStack.popBackToWithResult(layout, result);
			}
		});
	}

	/**
	 * Call the given callback when viewStack will be in the given state (if it is already in the
	 * given state, the callback is immediately invoked).
	 *
	 * @param viewStack the viewStack to wait on
	 * @param desiredState the traversing state to wait on
	 * @param callback the callback to invoke
	 */
	@MainThread private static void waitForTraversingState(@NonNull final ViewStack viewStack,
			@TraversingState final int desiredState, @NonNull final Runnable callback) {
		ViewUtils.verifyMainThread();
		if (desiredState == viewStack.getTraversingState()) {
			callback.run();
		} else {
			viewStack.addTraversingListener(new ViewStackListener() {
				@Override public void onTraversing(@TraversingState int traversingState) {
					if (traversingState == desiredState) {
						viewStack.removeTraversingListener(this);
						callback.run();
					}
				}
			});
		}
	}
}
