#Core Components

##The `PilotStack`

The `PilotStack` is the main core of this mini library which has the following properties:

- Holds an internal collection of `PilotFrame` objects that can be manipulated in various ways (Push, Pop, Clear at frame, clear all).
- Stack events can be listened to
- Implements `Serializable`

##The `PilotFrame`

- Has lifecycle methods
- Holds reference to parent stack
- Implements `Serializable`

**The `PilotFrame` can also be a controller itself or hold a reference to one.**

##The `@InvisibleFrame`

Annotation that can be applied to a `PilotFrame` subclass which signifies that it should be ignored for all stack callback operations. Very useful for handling scoped data within the stack

##The `PresenterBasedFrameLayout`

Base [class](https://github.com/doridori/Pilot/blob/master/android/lib/app/src/main/java/com/kodroid/pilot/lib/android/PresenterBasedFrameLayout.java) that can be extended for RichViews which accepts Presenter setting. Needs a [`@Presenter`](https://github.com/doridori/Pilot/blob/master/android/lib/app/src/main/java/com/kodroid/pilot/lib/android/Presenter.java) annotation to be present.

##The `PilotManager`

Handles a `PilotStack` for you and handles core `View` changes based upon stack transitions (if you are using Views). A `PilotManager` is intended to be used one-per-Activity and is designed to be referenced and setup **statically** as it handles its own memory releasing inside the Activity lifecycle delegate calls. This allows for easy persistence accross config-changes and back-stack-memory-saving-death and will clear all memory used when the Activity is destroyed for good. It will also handle saving and restoring itself on process-death. For a any discussion or questions about this please see [this related ticket](https://github.com/doridori/Pilot/issues/8). **Think of the `PilotManager` as the glue code between Android `Activity`s lifecycle peculiarities / View handling and the simple `PilotStack` functionality and usefullness.**

The `PilotManager` allows you to _compose_ your Activity with Pilot functionality as opposed to _inherit_ it from some form of PilotRootActivity, hence the need for delegation methods.