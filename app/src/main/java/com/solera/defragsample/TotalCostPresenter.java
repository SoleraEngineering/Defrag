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
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class TotalCostPresenter extends Presenter<TotalCostPresenter.View> {

  @Override protected void onTakeView() {
    super.onTakeView();

    addViewSubscription(onSubmit());
    addViewSubscription(onTotalCostChanged());
  }

  @NonNull private Subscription onSubmit() {
    final View view = getView();
    return view.onSubmit().flatMap(new Func1<Object, Observable<Integer>>() {
      @Override public Observable<Integer> call(Object ignore) {
        return view.onTotalCostChanged().map(new Func1<CharSequence, Integer>() {
          @Override public Integer call(CharSequence charSequence) {
            return Integer.parseInt(charSequence.toString());
          }
        });
      }
    }).take(1).subscribe(new Action1<Integer>() {
      @Override public void call(Integer totalCost) {
        view.showTotalPeople(totalCost);
      }
    });
  }

  @NonNull private Subscription onTotalCostChanged() {
    final View view = getView();
    return view.onTotalCostChanged().map(new Func1<CharSequence, Boolean>() {
      @Override public Boolean call(CharSequence charSequence) {
        return charSequence.length() != 0;
      }
    }).distinctUntilChanged().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean isValid) {
        view.enableSubmit(isValid);
      }
    });
  }

  public interface View extends PresenterView {
    @NonNull Observable<CharSequence> onTotalCostChanged();

    @NonNull Observable<?> onSubmit();

    void enableSubmit(boolean enable);

    void showTotalPeople(int totalCost);
  }
}
