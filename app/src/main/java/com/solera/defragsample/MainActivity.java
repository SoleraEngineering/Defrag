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
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.solera.defrag.ViewStackListener;

public class MainActivity extends AppCompatActivity {
  private ViewStack mViewStack;
  private boolean mDisableUI = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final FrameLayout mainLayout = (FrameLayout) findViewById(android.R.id.content);
    mViewStack = new ViewStack(mainLayout, savedInstanceState);

    mViewStack.addTraversingListener(new ViewStackListener() {
      @Override public void onTraversing(@NonNull TraversingState traversingState) {
        mDisableUI = traversingState != TraversingState.IDLE;
      }
    });


    mViewStack.startWith(R.layout.totalcost);
  }

  @Override protected void onStop() {
    super.onStop();
    mViewStack.onStop();
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mViewStack.onSaveInstanceState(outState);
  }

  @Override public void onBackPressed() {
    if (mDisableUI) {
      return;
    }
    if (!mViewStack.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override protected void onStart() {
    super.onStart();
    mViewStack.onStart();
  }

  @Override public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
    return mDisableUI || super.dispatchTouchEvent(ev);
  }

  @Override public Object getSystemService(@NonNull String name) {
    if (ViewStack.matchesServiceName(name)) {
      return mViewStack;
    }
    return super.getSystemService(name);
  }
}
