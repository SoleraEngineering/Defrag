/*
 * Copyright 2016 Tom Hall.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.solera.defrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import auto.parcel.AutoParcel;

/**
 * Handles a stack of views, and animations between these views.
 */
public class ViewStack extends FrameLayout {
	//Explicitly create a new string - as we use this reference as a token
	public static final Bundle USE_EXISTING_SAVED_STATE = new Bundle();
	private static final String SERVICE_NAME = "view_stack";
	private static final int DEFAULT_ANIMATION_DURATION_IN_MS = 300;
	private static final String SINGLE_PARAMETER_KEY = "view_stack_single_param";
	private final Collection<ViewStackListener> mViewStackListeners = new ArrayList<>();
	private Deque<ViewStackEntry> mViewStack = new ArrayDeque<>();
	private TraversingState mTraversingState = TraversingState.IDLE;
	private Object mResult;

	public ViewStack(Context context) {
		super(context);
	}

	public ViewStack(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ViewStack(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ViewStack(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * @deprecated Use a DI library or have an interface for your containing Activity to return your ViewStack
	 */
	@Deprecated
	public static boolean matchesServiceName(String serviceName) {
		return SERVICE_NAME.equals(serviceName);
	}

	/**
	 * @deprecated Use a DI library or have an interface for your containing Activity to return your ViewStack
	 */
	@Deprecated
	public static ViewStack get(@NonNull View view) {
		return ViewStack.get(view.getContext());
	}

	/**
	 * @deprecated Use a DI library or have an interface for your containing Activity to return your ViewStack
	 */
	@Deprecated
	@SuppressLint("WrongConstant")
	public static ViewStack get(@NonNull Context context) {
		//noinspection ResourceType
		return (ViewStack) context.getSystemService(SERVICE_NAME);
	}

	/**
	 * It should be called in the {@link Activity#onBackPressed()} in order to handle the backpress
	 * events correctly.
	 *
	 * @return true if the back press event was handled by the viewstack, false otherwise (and so the
	 * activity should handle this event).
	 */
	public boolean onBackPressed() {
		final View topView = getTopView();
		if (topView != null && topView instanceof HandlesBackPresses) {
			return ((HandlesBackPresses) topView).onBackPressed();
		}
		return pop();
	}

	@Nullable
	public View getTopView() {
		final ViewStackEntry peek = mViewStack.peek();
		if (peek != null) {
			return peek.getView();
		}
		return null;
	}

	public boolean pop() {
		return popWithResult(1, null);
	}

	public boolean popWithResult(int count, @Nullable Object result) {
		if (mViewStack.size() <= count) {
			return false;
		}
		mResult = result;
		setTraversingState(TraversingState.POPPING);
		final View fromView = mViewStack.pop().getView();
		while (--count > 0) {
			mViewStack.pop();
		}
		final ViewStackEntry peek = mViewStack.peek();
		final View toView = peek.getView();
		addView(toView);
		peek.restoreState(toView);
		ViewUtils.waitForMeasure(toView, new ViewUtils.OnMeasuredCallback() {
			@Override
			public void onMeasured(View view, int width, int height) {
				ViewStack.this.runAnimation(fromView, toView, Direction.BACK);
			}
		});
		return true;
	}

	public void addTraversingListener(@NonNull ViewStackListener listener) {
		mViewStackListeners.add(listener);
	}

	public void removeTraversingListener(@NonNull ViewStackListener listener) {
		mViewStackListeners.remove(listener);
	}

	@NonNull
	public TraversingState getTraversingState() {
		return mTraversingState;
	}

	private void setTraversingState(@NonNull TraversingState traversing) {
		if (traversing != TraversingState.IDLE && mTraversingState != TraversingState.IDLE) {
			throw new IllegalStateException("ViewStack is currently traversing");
		}

		mTraversingState = traversing;
		for (ViewStackListener listener : mViewStackListeners) {
			listener.onTraversing(mTraversingState);
		}
	}

	@LayoutRes
	public int getTopLayout() {
		return mViewStack.peek().mLayout;
	}

	public void replace(@LayoutRes int layout) {
		replaceWithParameters(layout, null);
	}

	/**
	 * @deprecated Replaced by {@link #replaceWithSerializableParameter(int, Serializable)}
	 */
	@Deprecated
	public void replace(@LayoutRes int layout, @Nullable Serializable parameter) {
		replaceWithParameters(layout, createSimpleBundle(parameter));
	}

	public void replaceWithSerializableParameter(@LayoutRes int layout, @Nullable Serializable parameter) {
		replaceWithParameters(layout, createSimpleBundle(parameter));
	}

	public void replaceWithParameters(@LayoutRes int layout, @Nullable Bundle parameters) {
		setTraversingState(TraversingState.REPLACING);
		final ViewStackEntry viewStackEntry = new ViewStackEntry(layout, parameters, null);
		final View view = viewStackEntry.getView();
		if (mViewStack.isEmpty()) {
			throw new IllegalStateException("Replace on an empty stack");
		}

		final ViewStackEntry topEntry = mViewStack.peek();
		final View fromView = topEntry.getView();
		mViewStack.push(viewStackEntry);
		addView(view);
		ViewUtils.waitForMeasure(view, new ViewUtils.OnMeasuredCallback() {
			@Override
			public void onMeasured(View view, int width, int height) {
				ViewStack.this.runAnimation(fromView, view, Direction.FORWARD);
				mViewStack.remove(topEntry);
			}
		});
	}

	public int getViewCount() {
		return mViewStack.size();
	}

	public void push(@LayoutRes int layout) {
		pushWithParameters(layout, null);
	}

	/**
	 * @param layout    the layout file to push.
	 * @param parameter the parameters of the layout file.
	 * @deprecated Use {@link #pushWithSerializableParameter(int, Serializable)}
	 */
	@Deprecated
	public void push(@LayoutRes int layout, @Nullable Serializable parameter) {
		pushWithParameters(layout, createSimpleBundle(parameter));
	}

	public void pushWithSerializableParameter(@LayoutRes int layout, @Nullable Serializable parameter) {
		pushWithParameters(layout, createSimpleBundle(parameter));
	}

	public void pushWithParameters(@LayoutRes int layout, @Nullable Bundle parameters) {
		final ViewStackEntry viewStackEntry = new ViewStackEntry(layout, parameters, null);
		final View view = viewStackEntry.getView();

		setTraversingState(TraversingState.PUSHING);
		if (mViewStack.isEmpty()) {
			mViewStack.push(viewStackEntry);
			addView(view);
			ViewUtils.waitForMeasure(view, new ViewUtils.OnMeasuredCallback() {
				@Override
				public void onMeasured(View view, int width, int height) {
					setTraversingState(TraversingState.IDLE);
				}
			});
			return;
		}

		final ViewStackEntry peek = mViewStack.peek();
		final View fromView = peek.getView();
		peek.saveState(fromView);
		mViewStack.push(viewStackEntry);
		addView(view);

		ViewUtils.waitForMeasure(view, new ViewUtils.OnMeasuredCallback() {
			@Override
			public void onMeasured(View view, int width, int height) {
				ViewStack.this.runAnimation(fromView, view, Direction.FORWARD);
			}
		});
	}

	/**
	 * Replace the current stack with the given views, if the Serializable component
	 * is the USE_EXISTING_SAVED_STATE tag, then we will use that saved state for that
	 * view (if it exists, and is at the right location in the stack) otherwise this will be null.
	 */
	public void replaceStack(@NonNull List<Pair<Integer, Bundle>> views) {
		if (mViewStack.isEmpty()) {
			throw new IllegalStateException("replaceStack called on empty stack.");
		}
		setTraversingState(TraversingState.REPLACING);

		final ViewStackEntry fromEntry = mViewStack.peek();

		//take a copy of the view stack:
		Deque<ViewStackEntry> copy = new ArrayDeque<>(mViewStack);

		mViewStack.clear();
		mViewStack.push(fromEntry);

		Iterator<ViewStackEntry> iterator = copy.iterator();
		for (Pair<Integer, Bundle> view : views) {
			Bundle savedParameter = view.second;
			SparseArray<Parcelable> viewState = null;
			if (view.second == USE_EXISTING_SAVED_STATE) {
				savedParameter = null;
				if (iterator != null && iterator.hasNext()) {
					final ViewStackEntry next = iterator.next();
					if (next.mLayout == view.first) {
						savedParameter = next.mParameters;
						viewState = next.mViewState;
					} else {
						iterator = null;
					}
				}
			}
			mViewStack.push(new ViewStackEntry(view.first, savedParameter, viewState));
		}

		final ViewStackEntry toEntry = mViewStack.peek();

		final View toView = toEntry.getView();

		if (fromEntry.mLayout == toEntry.mLayout) {
			//if current topEntry layout is equal to the next proposed topEntry layout
			//we cannot do a transition animation
			mViewStack.remove(fromEntry);
			removeAllViews();
			addView(toView);
			ViewUtils.waitForMeasure(toView, new ViewUtils.OnMeasuredCallback() {
				@Override
				public void onMeasured(View view, int width, int height) {
					setTraversingState(TraversingState.IDLE);
				}
			});
		} else {
			final View fromView = fromEntry.getView();
			addView(toView);

			ViewUtils.waitForMeasure(toView, new ViewUtils.OnMeasuredCallback() {
				@Override
				public void onMeasured(View view, int width, int height) {
					ViewStack.this.runAnimation(fromView, toView, Direction.FORWARD);
					mViewStack.remove(fromEntry);
				}
			});
		}
	}

	/**
	 * @return the result (if any) of the last popped view, and clears this result.
	 */
	@Nullable
	public <T> T getResult() {
		final T result = (T) mResult;
		mResult = null;
		return result;
	}

	/**
	 * @param view the view to retrieve the parameters for.
	 * @return the parameters, or null if none found.
	 * @deprecated Use {@link #getSerializableParameter(Object)}
	 */
	@Deprecated
	@Nullable
	public <T extends Serializable> T getParameter(@NonNull Object view) {
		return getSerializableParameter(view);
	}

	@Nullable
	public <T extends Serializable> T getSerializableParameter(@NonNull Object view) {
		final Bundle parameters = getParameters(view);
		if (parameters == null) {
			return null;
		} else {
			return (T) parameters.getSerializable(SINGLE_PARAMETER_KEY);
		}
	}

	/**
	 * @param view      the view to set the parameter for.
	 * @param parameter the parameter to set.
	 * @deprecated Use {@link #setSerializableParameter(Object, Serializable)}
	 */
	@Deprecated
	public void setParameter(@NonNull Object view, @Nullable Serializable parameter) {
		setSerializableParameter(view, parameter);
	}

	public void setSerializableParameter(@NonNull Object view, @Nullable Serializable parameter) {
		setParameters(view, createSimpleBundle(parameter));
	}

	/**
	 * @return the start parameters of the view/presenter
	 */
	@Nullable
	public Bundle getParameters(@NonNull Object view) {
		final Iterator<ViewStackEntry> viewStackEntryIterator = mViewStack.descendingIterator();
		while (viewStackEntryIterator.hasNext()) {
			final ViewStackEntry viewStackEntry = viewStackEntryIterator.next();
			if (view == viewStackEntry.mViewReference.get()) {
				return viewStackEntry.mParameters;
			}
		}
		return null;
	}

	public void setParameters(@NonNull Object view, @Nullable Bundle parameters) {
		final Iterator<ViewStackEntry> viewStackEntryIterator = mViewStack.descendingIterator();
		while (viewStackEntryIterator.hasNext()) {
			final ViewStackEntry viewStackEntry = viewStackEntryIterator.next();
			if (view == viewStackEntry.mViewReference.get()) {
				viewStackEntry.setParameters(parameters);
				return;
			}
		}
	}

	/**
	 * Pop off the stack, with the given result.
	 *
	 * @param result the result.
	 * @return true if the pop operation has been successful, false otherwise.
	 */
	public boolean popWithResult(@Nullable Object result) {
		return popWithResult(1, result);
	}

	/**
	 * Pop back to the given layout is on top.
	 *
	 * @param layout the layout to be on the top.
	 * @param result the result to return to the (new) top view.
	 * @return true if the pop operation has been successful, false otherwise.
	 */
	public boolean popBackToWithResult(@LayoutRes int layout, @Nullable Object result) {
		final Iterator<ViewStackEntry> viewStackEntryIterator = mViewStack.iterator();
		int popCount = 0;
		while (viewStackEntryIterator.hasNext()) {
			final ViewStackEntry next = viewStackEntryIterator.next();
			if (next.mLayout == layout) {
				return popWithResult(popCount, result);
			}
			popCount++;
		}
		return false;
	}

	public boolean pop(int count) {
		return popWithResult(count, null);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable parcelable = super.onSaveInstanceState();
		return SaveState.newInstance(this, parcelable);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		final SaveState parcelable = (SaveState) state;
		for (SaveStateEntry entry : parcelable.stack()) {
			mViewStack.add(new ViewStackEntry(entry.layout(), entry.parameters(), entry.viewState()));
		}
		if (!mViewStack.isEmpty()) {
			addView(mViewStack.peek().getView());
		}
		super.onRestoreInstanceState(parcelable.superState());
	}

	private void runAnimation(final View from, final View to,
							  Direction direction) {
		Animator animator = createAnimation(from, to, direction);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				removeView(from);
				setTraversingState(TraversingState.IDLE);
			}
		});
		animator.start();
	}

	@NonNull
	private Animator createAnimation(@NonNull View from, @NonNull View to,
									 @NonNull Direction direction) {
		Animator animation = null;
		if (to instanceof HasTraversalAnimation) {
			animation = ((HasTraversalAnimation) to).createAnimation(from);
		}

		if (animation == null) {
			return createDefaultAnimation(from, to, direction);
		} else {
			return animation;
		}
	}

	private Animator createDefaultAnimation(View from, View to, Direction direction) {
		boolean backward = direction == Direction.BACK;

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

	@Nullable
	private Bundle createSimpleBundle(@Nullable Serializable parameter) {
		final Bundle parameterBundle;
		if (parameter == null) {
			parameterBundle = null;
		} else {
			parameterBundle = new Bundle(1);
			parameterBundle.putSerializable(SINGLE_PARAMETER_KEY, parameter);
		}

		return parameterBundle;
	}

	private enum Direction {
		BACK,
		FORWARD
	}

	@AutoParcel
	static abstract class SaveState implements Parcelable {
		static SaveState newInstance(@NonNull ViewStack viewstack, @NonNull Parcelable superState) {
			List<SaveStateEntry> stack = new ArrayList<>(viewstack.getViewCount());
			for (ViewStackEntry entry : viewstack.mViewStack) {
				stack.add(SaveStateEntry.newInstance(entry.mLayout, entry.mParameters, entry.mViewState));
			}
			return new AutoParcel_ViewStack_SaveState(stack, superState);
		}

		@NonNull
		abstract List<SaveStateEntry> stack();

		@NonNull
		abstract Parcelable superState();
	}

	@AutoParcel
	static abstract class SaveStateEntry implements Parcelable {
		static SaveStateEntry newInstance(int layout, @Nullable Bundle parameters, @Nullable SparseArray<Parcelable> viewState) {
			return new AutoParcel_ViewStack_SaveStateEntry(layout, parameters, viewState);
		}

		@LayoutRes
		abstract int layout();

		@Nullable
		abstract Bundle parameters();

		@Nullable
		abstract SparseArray<Parcelable> viewState();
	}

	private class ViewStackEntry {
		@LayoutRes
		private final int mLayout;
		@Nullable
		private Bundle mParameters;
		@Nullable
		private SparseArray<Parcelable> mViewState;
		private WeakReference<View> mViewReference = new WeakReference<>(null);

		ViewStackEntry(@LayoutRes int layout, @Nullable Bundle parameters, @Nullable SparseArray<Parcelable> viewState) {
			mLayout = layout;
			mParameters = parameters;
			mViewState = viewState;
		}

		void setParameters(@Nullable Bundle parameters) {
			mParameters = parameters;
		}

		private void saveState(@NonNull View view) {
			final SparseArray<Parcelable> parcelableSparseArray = new SparseArray<>();
			view.saveHierarchyState(parcelableSparseArray);
			mViewState = parcelableSparseArray;
		}

		private void restoreState(@NonNull View view) {
			if (mViewState != null) {
				view.restoreHierarchyState(mViewState);
			}
		}

		private View getView() {
			View view = mViewReference.get();
			if (view == null) {
				view = LayoutInflater.from(getContext()).inflate(mLayout, ViewStack.this, false);
				mViewReference = new WeakReference<>(view);
			}
			return view;
		}
	}
}
