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
