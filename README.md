Defrag
---
Defrag is a simple to use library that allows for Fragment-free Android applications. Why avoid fragments? I couldn't explain it any better than square's article: https://corner.squareup.com/2014/10/advocating-against-android-fragments.html

Download
---

Defrag is available in the jCenter repository:

```java
dependencies {
  compile 'com.solera.defrag:defrag:1.2.0'
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

It's easy to create custom transition animations, just call setAnimationHandler with your own AnimationHandler:
```java
public class CustomAnimationHandler implements AnimationHandler {
	@Nullable TraversalAnimation createAnimation(@NonNull View from, @NonNull View to,
			@TraversingOperation int operation) {
      boolean forward = operation != TraversingOperation.POP;

      AnimatorSet set = new AnimatorSet();

      set.setInterpolator(new DecelerateInterpolator());

      final int width = from.getWidth();

      if (forward) {
        to.setTranslationX(width);
        set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, 0 - (width / 3)));
        set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
      } else {
        to.setTranslationX(0 - (width / 3));
        set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, width));
        set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
      }

      return TraversalAnimation.newInstance(set,
          forward ? TraversalAnimation.ABOVE : TraversalAnimation.BELOW);
    }
  }
}
```
Start parameters and results
---
You can push a view with start parameters, these parameters will be saved if the activity is re-created:

```java
  viewStack.pushWithParameter(R.layout.layout_hello,"World!");
```

Any Class that implements Serializable can be used, if you need something more complex, use a Bundle:
```java
  final Bundle parameters = ...
  viewStack.pushWithParameters(R.layout.layout_hello,parameters);
```

then, when the view is inflated & created:
```java
  final String parameters = viewStack.getParameter(this);
  //or a bundle:
  final Bundle parameterBundle  = viewStack.getParameters(this);
  textView.setText("Hullo "+parameters);
  //save the parameters to something different, this is a way to save state when recreating the stack:
  viewStack.setParameter(this,"Android!");
```

Likewise, you can return information to another view by calling setResult, this is equivalent to the startActivityForResult & onActivityResult methods:

```java
  //finished with this view, pop back a particular view, with a result:
  viewStack.popWithResult(R.layout.first_layout,"Result");
```

Retrieving the result is similar to getting Parameters (and is persisted when the Activity is recreated):

```java
  final String result = viewStack.getResult();
```

Take a look at the [sample app](https://github.com/R3PI/Defrag/tree/master/app) for an example implementation.

Thanks to
---
The inspiration of this library was from Square's article on Fragments, and from their library Flow (https://github.com/square/flow).

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Defrag-green.svg?style=true)](https://android-arsenal.com/details/1/3693)

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
