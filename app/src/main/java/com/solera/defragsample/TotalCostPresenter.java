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
