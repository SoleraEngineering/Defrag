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
import android.widget.FrameLayout;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import auto.parcel.AutoParcel;

/**
 * Handles a stack of views, and animations between these views.
 */
public class ViewStack extends FrameLayout {
	//Explicitly create a new string - as we use this reference as a token
	public static final Bundle USE_EXISTING_SAVED_STATE = new Bundle();
	private static final String SINGLE_PARAMETER_KEY = "view_stack_single_param";
	private final Collection<ViewStackListener> viewStackListeners = new CopyOnWriteArrayList<>();
	private final Deque<ViewStackEntry> viewStack = new ArrayDeque<>();
	private TraversingState traversingState = TraversingState.IDLE;
	private AnimationHandler animationHandler = new DefaultAnimationHandler();
	private Object result;

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

	@NonNull
	public AnimationHandler setAnimationHandler(@NonNull AnimationHandler handler) {
		final AnimationHandler oldHandler = this.animationHandler;
		animationHandler = handler;
		return oldHandler;
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
		final ViewStackEntry peek = viewStack.peek();
		if (peek != null) {
			return peek.getView();
		}
		return null;
	}

	public boolean pop() {
		return popWithResult(1, null);
	}

	public boolean popWithResult(int count, @Nullable Object result) {
		if (viewStack.size() <= count) {
			return false;
		}
		this.result = result;
		setTraversingState(TraversingState.POPPING);
		final View fromView = viewStack.pop().getView();
		while (--count > 0) {
			viewStack.pop();
		}
		final ViewStackEntry peek = viewStack.peek();
		final View toView = peek.getView();
		addView(toView);
		peek.restoreState(toView);
		ViewUtils.waitForMeasure(toView, new ViewUtils.OnMeasuredCallback() {
			@Override
			public void onMeasured(View view, int width, int height) {
				ViewStack.this.runAnimation(fromView, toView, TraversalDirection.BACK);
			}
		});
		return true;
	}

	public void addTraversingListener(@NonNull ViewStackListener listener) {
		ViewUtils.verifyMainThread();
		viewStackListeners.add(listener);
	}

	public void removeTraversingListener(@NonNull ViewStackListener listener) {
		ViewUtils.verifyMainThread();
		viewStackListeners.remove(listener);
	}

	@NonNull
	public TraversingState getTraversingState() {
		return traversingState;
	}

	private void setTraversingState(@NonNull TraversingState traversing) {
		if (traversing != TraversingState.IDLE && traversingState != TraversingState.IDLE) {
			throw new IllegalStateException("ViewStack is currently traversing");
		}

		traversingState = traversing;
		for (ViewStackListener listener : viewStackListeners) {
			listener.onTraversing(traversingState);
		}
	}

	/**
	 * Returns top layout resource reference or 0 if {@link ViewStack} is empty and
	 * no top layout has been found.
	 */
	@LayoutRes public int getTopLayout() {
		final ViewStackEntry peek = viewStack.peek();
		return peek != null ? peek.mLayout : 0;
	}

	public void replace(@LayoutRes int layout) {
		replaceWithParameters(layout, null);
	}

	public void replaceWithParameter(@LayoutRes int layout, @Nullable Serializable parameter) {
		replaceWithParameters(layout, createSimpleBundle(parameter));
	}

	public void replaceWithParameters(@LayoutRes int layout, @Nullable Bundle parameters) {
		setTraversingState(TraversingState.REPLACING);
		final ViewStackEntry viewStackEntry = new ViewStackEntry(layout, parameters, null);
		final View view = viewStackEntry.getView();
		if (viewStack.isEmpty()) {
			throw new IllegalStateException("Replace on an empty stack");
		}

		final ViewStackEntry topEntry = viewStack.peek();
		final View fromView = topEntry.getView();
		viewStack.push(viewStackEntry);
		addView(view);
		ViewUtils.waitForMeasure(view, new ViewUtils.OnMeasuredCallback() {
			@Override
			public void onMeasured(View view, int width, int height) {
				ViewStack.this.runAnimation(fromView, view, TraversalDirection.FORWARD);
				viewStack.remove(topEntry);
			}
		});
	}

	public int getViewCount() {
		return viewStack.size();
	}

	public void push(@LayoutRes int layout) {
		pushWithParameters(layout, null);
	}

	public void pushWithParameter(@LayoutRes int layout, @Nullable Serializable parameter) {
		pushWithParameters(layout, createSimpleBundle(parameter));
	}

	public void pushWithParameters(@LayoutRes int layout, @Nullable Bundle parameters) {
		final ViewStackEntry viewStackEntry = new ViewStackEntry(layout, parameters, null);
		final View view = viewStackEntry.getView();

		setTraversingState(TraversingState.PUSHING);
		if (viewStack.isEmpty()) {
			viewStack.push(viewStackEntry);
			addView(view);
			ViewUtils.waitForMeasure(view, new ViewUtils.OnMeasuredCallback() {
				@Override
				public void onMeasured(View view, int width, int height) {
					setTraversingState(TraversingState.IDLE);
				}
			});
			return;
		}

		final ViewStackEntry peek = viewStack.peek();
		final View fromView = peek.getView();
		peek.saveState(fromView);
		viewStack.push(viewStackEntry);
		addView(view);

		ViewUtils.waitForMeasure(view, new ViewUtils.OnMeasuredCallback() {
			@Override
			public void onMeasured(View view, int width, int height) {
				ViewStack.this.runAnimation(fromView, view, TraversalDirection.FORWARD);
			}
		});
	}

	/**
	 * Replace the current stack with the given views, if the Bundle component
	 * is the USE_EXISTING_SAVED_STATE tag, then we will use that saved state for that
	 * view (if it exists, and is at the right location in the stack) otherwise this will be null.
	 */
	public void replaceStack(@NonNull List<Pair<Integer, Bundle>> views) {
		if (viewStack.isEmpty()) {
			throw new IllegalStateException("replaceStack called on empty stack.");
		}
		setTraversingState(TraversingState.REPLACING);

		final ViewStackEntry fromEntry = viewStack.peek();

		//take a copy of the view stack:
		Deque<ViewStackEntry> copy = new ArrayDeque<>(viewStack);

		viewStack.clear();
		viewStack.push(fromEntry);

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
			viewStack.push(new ViewStackEntry(view.first, savedParameter, viewState));
		}

		final ViewStackEntry toEntry = viewStack.peek();

		final View toView = toEntry.getView();

		if (fromEntry.mLayout == toEntry.mLayout) {
			//if current topEntry layout is equal to the next proposed topEntry layout
			//we cannot do a transition animation
			viewStack.remove(fromEntry);
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
					ViewStack.this.runAnimation(fromView, toView, TraversalDirection.FORWARD);
					viewStack.remove(fromEntry);
				}
			});
		}
	}

	/**
	 * Replace the current stack with the given view, if the Bundle component
	 * is the USE_EXISTING_SAVED_STATE tag, then we will use that saved state for that
	 * view (if it exists, and is at the right location in the stack) otherwise this will be null.
	 */
	public void replaceStack(@LayoutRes Integer layout, @Nullable Bundle parameters) {
		replaceStack(Collections.singletonList(Pair.create(layout, parameters)));
	}

	/**
	 * @return the result (if any) of the last popped view, and clears this result.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> T getResult() {
		final T result = (T) this.result;
		this.result = null;
		return result;
	}

	/**
	 * @param view the view to retrieve the parameters for.
	 * @return the parameters, or null if none found.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Serializable> T getParameter(@NonNull Object view) {
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
	 */
	public void setParameter(@NonNull Object view, @Nullable Serializable parameter) {
		setParameters(view, createSimpleBundle(parameter));
	}

	/**
	 * @return the start parameters of the view/presenter
	 */
	@Nullable
	public Bundle getParameters(@NonNull Object view) {
		final Iterator<ViewStackEntry> viewStackEntryIterator = viewStack.descendingIterator();
		while (viewStackEntryIterator.hasNext()) {
			final ViewStackEntry viewStackEntry = viewStackEntryIterator.next();
			if (view == viewStackEntry.mViewReference.get()) {
				return viewStackEntry.mParameters;
			}
		}
		return null;
	}

	public void setParameters(@NonNull Object view, @Nullable Bundle parameters) {
		final Iterator<ViewStackEntry> viewStackEntryIterator = viewStack.descendingIterator();
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
		final Iterator<ViewStackEntry> viewStackEntryIterator = viewStack.iterator();
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
			viewStack.add(new ViewStackEntry(entry.layout(), entry.parameters(), entry.viewState()));
		}
		if (!viewStack.isEmpty()) {
			addView(viewStack.peek().getView());
		}
		super.onRestoreInstanceState(parcelable.superState());
	}

	@NonNull
	private Animator createAnimation(@NonNull View from, @NonNull View to,
									@NonNull TraversalDirection direction) {
		Animator animation = null;
		if (to instanceof HasTraversalAnimation) {
			animation = ((HasTraversalAnimation) to).createAnimation(from);
		}

		if (animation == null) {
			return animationHandler.createAnimation(from, to, direction);
		} else {
			return animation;
		}
	}

	private void runAnimation(final View from, final View to,
							  TraversalDirection direction) {
		final Animator animator = createAnimation(from, to, direction);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				removeView(from);
				setTraversingState(TraversingState.IDLE);
			}
		});
		animator.start();
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

	@AutoParcel
	static abstract class SaveState implements Parcelable {
		static SaveState newInstance(@NonNull ViewStack viewstack, @NonNull Parcelable superState) {
			List<SaveStateEntry> stack = new ArrayList<>(viewstack.getViewCount());
			for (ViewStackEntry entry : viewstack.viewStack) {
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
