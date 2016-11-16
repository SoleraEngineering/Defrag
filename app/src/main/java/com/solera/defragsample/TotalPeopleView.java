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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class TotalPeopleView extends FrameLayout implements TotalPeoplePresenter.View {
	private final TotalPeoplePresenter presenter = new TotalPeoplePresenter();
	@Bind(R.id.button) FloatingActionButton floatingActionButton;
	@Bind(R.id.seekbar) AppCompatSeekBar seekBar;
	@Bind(R.id.textview_number) TextView textView;

	public TotalPeopleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@NonNull @Override public Observable<Integer> onTotalPeopleChanged() {
		return RxSeekBar.changes(seekBar).map(new Func1<Integer, Integer>() {
			@Override public Integer call(Integer integer) {
				return integer / 12;
			}
		}).doOnNext(new Action1<Integer>() {
			@Override public void call(Integer integer) {
				textView.setText(Integer.toString(integer));
			}
		});
	}

	@NonNull @Override public Observable<?> onSubmit() {
		return RxView.clicks(floatingActionButton);
	}

	@Override public void enableSubmit(boolean enable) {
		floatingActionButton.setEnabled(enable);
		final float scaleTo = enable ? 1.0f : 0.0f;
		floatingActionButton.animate().scaleX(scaleTo).scaleY(scaleTo);
		textView.animate().scaleX(scaleTo).scaleY(scaleTo);
	}

	@Override public void showBreakdown(int totalCost, int totalPeople) {
		BreakdownPresenter.push(ViewStackHelper.getViewStack(this), totalCost, totalPeople);
	}

	@Override protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
	}

	@Override protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (isInEditMode()) {
			return;
		}
		presenter.takeView(this);
	}

	@Override protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		presenter.dropView();
	}
}
