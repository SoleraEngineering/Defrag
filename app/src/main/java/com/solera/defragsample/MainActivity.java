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
    //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    //setSupportActionBar(toolbar);

    //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    //fab.setOnClickListener(new View.OnClickListener() {
    //  @Override public void onClick(View view) {
    //    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
    //        .setAction("Action", null)
    //        .show();
    //  }
    //});

    final FrameLayout mainLayout = (FrameLayout) findViewById(android.R.id.content);
    mViewStack = new ViewStack(mainLayout, savedInstanceState);

    mViewStack.addTraversingListener(new ViewStackListener() {
      @Override public void onTraversing(@NonNull TraversingState traversingState) {
        mDisableUI = traversingState != TraversingState.IDLE;
      }
    });
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
