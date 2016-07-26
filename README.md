Defrag
---
Defrag is a simple to use library that allows for Fragment-free Android applications. Why avoid fragments? I couldn't explain it any better than square's article: https://corner.squareup.com/2014/10/advocating-against-android-fragments.html

Download
---

Defrag is available in the jCenter repository:

```java
dependencies {
  compile 'com.solera.defrag:defrag:0.9.0-beta1'
}
```

Usage
-----

Add ViewStack to your layout and push/pop your views to it. View state are saved and restored as the
stack changes, and smooth traversal animations happen automatically. In your Activity's onCreate:

```java
final ViewStack viewStack = (ViewStack) findViewById(R.id.viewstack);
if (savedInstanceState == null) {
  //no previous state, so push the first view
  viewStack.push(R.layout.first_layout);
}
```

To push a new view to the foreground:
```java
viewStack.push(R.layout.second_layout);
```
Or to go back:
```java
viewStack.pop();
```
Or to replace the top:
```java
viewStack.replace(R.layout.third_layout);
```
is easy!

Custom transition animations
---

It's easy to create custom transition animations, all you have to do is implement HasTraversalAnimation:
```java
public class ViewWithTraversalAnimation extends FrameLayout implements HasTraversalAnimation {
...
    /**
   * @param fromView the view that we are traversing from.
   * @return an animation that will be run for the traversal, or null if the default should be run.
   */
  @Override @Nullable Animator createAnimation(@NonNull View fromView) {
    final AnimatorSet set = new AnimatorSet();
    
    final AnimatorSet exitAnim = new AnimatorSet();
    exitAnim.setInterpolator(new AccelerateInterpolator());
    exitAnim.play(ObjectAnimator.ofFloat(fromView, View.ALPHA, 0.0f))
        .with(ObjectAnimator.ofFloat(fromView, View.TRANSLATION_X, 0f, -200));

    setAlpha(0f);
    final AnimatorSet enterAnim = new AnimatorSet();
    enterAnim.setInterpolator(new DecelerateInterpolator());
    enterAnim.setStartDelay(300);
    enterAnim.play(ObjectAnimator.ofFloat(this, View.ALPHA, 0.0f, 1.0f))
        .with(ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 100f, 0));

    set.play(exitAnim).with(enterAnim);
    
    return set;
  }
}
```
Start parameters and results
---
You can push a view with start parameters, these parameters will be saved if the activity is re-created:

```java
  viewStack.pushWithSerializableParameter(R.layout.layout_hello,"World!");
```

Any Class that implemnets Serializable can be used, if you need something more complex, use a Bundle:
```java
  final Bundle parameters = ...
  viewStack.pushWithParameters(R.layout.layout_hello,parameters);
```

then, when the view is inflated & created:
```java
  final String parameters = viewStack.getSerializableParameter(this);
  //or a bundle:
  final Bundle parameterBundle  = viewStack.getParameters(this);
  textView.setText("Hullo "+parameters);
  //save the parameters to something different, this is a way to save state when recreating the stack:
  viewStack.setSerializableParameter(this,"Android!");
```

Likewise, you can return information to another view by calling setResult, this is equivalent to the startActivityForResult & onActivityResult methods:

```java
  //finished with this view, pop back a particular view, with a result:
  viewStack.popWithResult(R.layout.first_layout,"Result");
```

Retrieving the result is similiar to getting Parameters (and is persisted when the Activity is recreated):

```java
  final String result = viewStack.getResult();
```

Take a look at the [sample app](https://github.com/R3PI/Defrag/tree/master/app) for an example implementation.

Thanks to
---
The inspiration of this library was from Square's article on Fragments, and from their library Flow (https://github.com/square/flow).

License:
---

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
