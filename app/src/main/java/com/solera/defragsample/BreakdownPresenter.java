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

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.solera.defrag.ViewStack;

public class BreakdownPresenter extends Presenter<BreakdownPresenter.View> {
  private static final String TOTAL_COST = "totalCost";
  private static final String TOTAL_PEOPLE = "totalPeople";

  public static void push(@NonNull ViewStack viewStack, int totalCost, int totalPeople) {
    final Bundle parameters = new Bundle();
    parameters.putInt(TOTAL_COST, totalCost);
    parameters.putInt(TOTAL_PEOPLE, totalPeople);
    viewStack.pushWithParameters(R.layout.breakdown, parameters);
  }

  @Override protected void onTakeView() {
    super.onTakeView();

    final ViewStack viewStack = ViewStackHelper.getViewStack(getContext());
    final Bundle parameters = viewStack.getParameters(getView());
    if (parameters == null) {
      throw new IllegalStateException("Parameters is null");
    }

    final int totalCost = parameters.getInt(TOTAL_COST);
    final int totalPeople = parameters.getInt(TOTAL_PEOPLE);
    final int result = totalCost / totalPeople;

    getView().setUi(Integer.toString(totalCost), Integer.toString(totalPeople),
        Integer.toString(result));
  }

  interface View extends PresenterView {
    void setUi(@NonNull String totalCost, @NonNull String totalPeople, @NonNull String perPerson);
  }
}
