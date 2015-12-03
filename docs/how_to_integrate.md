#How To Integrate

##Import .aar

Check the github releases for this at present. Jcenter coming soon :)

##Integrate `PilotManager` into your `Activity`

1. Implement a `static` initialiser for the `Views` that should be auto-handled by the `PilotManager` i.e. when a `PilotFrame` appears on the backing `PilotStack` the corresponding `View` will be shown.
2. Delegate a few `Activity` lifecycle methods to the `PilotManager`
3. Implement the `PilotManager.ActivityDelegate` interface.

See the [ExampleRootActivity](https://github.com/doridori/Pilot/blob/master/android/PilotExample/app/src/main/java/com/kodroid/pilotexample/android/ExampleRootActivity.java) for an example.

##Create some `PilotFrame` subclasses

These represent the individual states and data-scopes of your application and are the things that will live on the stack.

`PilotFrames` that appear on the stack that have a corresponding `@Presenter` annotated `PresenterBasedFrameLayout` extending `View` class in the initialised `PilotManager` will have their `View` auto displayed when that frame hits the top of the `PilotStack`. These `Views` will have access to their `PilotFrame` backed `Presenter` and the parent `PilotStack`. The stack can be used for `Presenter` to `Presenter` [navigation](https://github.com/doridori/Pilot/blob/master/android/PilotExample/app/src/main/java/com/kodroid/pilotexample/android/frames/presenter/FirstViewPresenter.java#L46).

`PilotFrames` that dont have a corresponding `View` setup will need to be handled by `ActivityDelegate.interceptTopFrameUpdatedForCategory` otherwise an [Exception will be thrown](https://github.com/doridori/Pilot/blob/master/android/lib/app/src/main/java/com/kodroid/pilot/lib/android/PilotManager.java#L221). This may be because you want to display a Dialog / Fragment / switch Activity.

`PilotFrames` that are annotated with `@InvisibleFrame` will not trigger stack events when pushed. These are useful for data-scoping (as [shown in the Example app](https://github.com/doridori/Pilot/blob/master/android/PilotExample/app/src/main/java/com/kodroid/pilotexample/android/frames/presenter/SecondInSessionViewPresenter.java#L26))
