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
import auto.parcel.AutoParcel;
import com.solera.defrag.ViewStack;
import java.io.Serializable;

public class BreakdownPresenter extends Presenter<BreakdownPresenter.View> {
  public static void push(@NonNull ViewStack viewStack, int totalCost, int totalPeople) {
    viewStack.push(R.layout.breakdown, Parameters.newInstance(totalCost, totalPeople));
  }

  @Override protected void onTakeView() {
    super.onTakeView();

    final Parameters parameters = ViewStack.get(getContext()).getParameters(getView());
    if (parameters == null) {
      throw new IllegalStateException("Parameters is null");
    }

    getView().setUi(Integer.toString(parameters.totalCost()),
        Integer.toString(parameters.totalPeople()),
        Integer.toString(parameters.totalCost() / parameters.totalPeople()));
  }

  public interface View extends PresenterView {
    void setUi(@NonNull String totalCost, @NonNull String totalPeople, @NonNull String perPerson);
  }

  @AutoParcel static abstract class Parameters implements Serializable {
    static Parameters newInstance(int totalCost, int totalPeople) {
      return new AutoParcel_BreakdownPresenter_Parameters(totalCost, totalPeople);
    }

    abstract int totalCost();

    abstract int totalPeople();
  }
}
