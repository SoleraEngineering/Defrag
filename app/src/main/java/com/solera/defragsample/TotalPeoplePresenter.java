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

import android.support.annotation.NonNull;
import com.solera.defrag.ViewStack;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class TotalPeoplePresenter extends Presenter<TotalPeoplePresenter.View> {
  public static void push(@NonNull ViewStack viewStack, int totalCost) {
    viewStack.push(R.layout.totalpeople, Integer.valueOf(totalCost));
  }

  @Override protected void onTakeView() {
    super.onTakeView();

    final Integer totalCost = ViewStack.get(getContext()).getParameters(getView());
    if (totalCost == null) {
      throw new IllegalStateException("Parameter is null");
    }

    addViewSubscription(onSubmit(totalCost));
    addViewSubscription(onTotalPeopleChanged());
  }

  @NonNull private Subscription onSubmit(final int totalCost) {
    return getView().onSubmit().flatMap(new Func1<Object, Observable<Integer>>() {
      @Override public Observable<Integer> call(Object ignore) {
        return getView().onTotalPeopleChanged();
      }
    }).subscribe(new Action1<Integer>() {
      @Override public void call(Integer totalPeople) {
        getView().showBreakdown(totalCost,totalPeople);
      }
    });
  }

  @NonNull private Subscription onTotalPeopleChanged() {
    return getView().onTotalPeopleChanged().map(new Func1<Integer, Boolean>() {
      @Override public Boolean call(Integer integer) {
        return integer != 0;
      }
    }).distinctUntilChanged().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean isValid) {
        getView().enableSubmit(isValid);
      }
    });
  }

  public interface View extends PresenterView {
    @NonNull Observable<Integer> onTotalPeopleChanged();

    @NonNull Observable<?> onSubmit();

    void enableSubmit(boolean enable);

    void showBreakdown(int totalCost, int totalPeople);
  }
}
