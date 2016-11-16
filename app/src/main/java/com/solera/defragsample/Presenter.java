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
package com.solera.defragsample;

import android.content.Context;
import android.support.annotation.NonNull;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Based presenter.
 */
public abstract class Presenter<T extends PresenterView> {
	private T view;
	private CompositeSubscription viewSubscriptions;

	public final void takeView(@NonNull T view) {
		if (this.view != null) {
			throw new IllegalStateException(
					"Presenter already has the view or the dropview isn't called");
		}

		this.view = view;
		viewSubscriptions = new CompositeSubscription();
		onTakeView();
	}

	public final void dropView() {
		onDropView();
		view = null;
		viewSubscriptions.clear();
	}

	public final void addViewSubscription(@NonNull Subscription subscription) {
		viewSubscriptions.add(subscription);
	}

	public final void removeViewSubscription(@NonNull Subscription subscription) {
		viewSubscriptions.remove(subscription);
	}

	protected void onTakeView() {
	}

	protected void onDropView() {
	}

	protected final Context getContext() {
		return getView().getContext();
	}

	/**
	 * Call this method from within your subscriptions, and this method
	 * will always return an object. Outside and it could potentially
	 * return null, which you should check. (We do not flag this as @Nullable,
	 * since we would have to put ignores everywhere in our lint.)
	 *
	 * @return view that this presenter is attached to.
	 */
	protected final T getView() {
		return view;
	}

	/**
	 * Returns default error action, as per Dan Lew advice from common RxJava mistakes.
	 * https://speakerdeck.com/dlew/common-rxjava-mistakes
	 */
	protected Action1<Throwable> getDefaultErrorAction() {
		return new Action1<Throwable>() {
			@Override public void call(Throwable throwable) {
				throw new OnErrorNotImplementedException("RxError source", throwable);
			}
		};
	}
}
