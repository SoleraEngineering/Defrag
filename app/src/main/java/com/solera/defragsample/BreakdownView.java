package com.solera.defragsample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;

public class BreakdownView extends FrameLayout implements BreakdownPresenter.View {
  private final BreakdownPresenter mPresenter = new BreakdownPresenter();
  @Bind(R.id.textview_costvalue) TextView mCostTextView;
  @Bind(R.id.textview_peoplevalue) TextView mPeopleTextView;
  @Bind(R.id.textview_perpersonvalue) TextView mPerPersonTextView;

  public BreakdownView(Context context, AttributeSet attrs) {
    super(context, attrs);
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
    mPresenter.takeView(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    mPresenter.dropView();
  }

  @Override public void setUi(@NonNull String totalCost, @NonNull String totalPeople,
      @NonNull String perPerson) {
    mCostTextView.setText(totalCost);
    mPeopleTextView.setText(totalPeople);
    mPerPersonTextView.setText(perPerson);
  }
}
