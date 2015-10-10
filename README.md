#Pilot

An abstract application stack which facilitates:

- Presenter lifecycle management
- Presenter -> Presenter control flow
- Fine grained data scope management
- Presenter lifecycle events
- A `View` based architecture 

![Pilot Mascot](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/pilot_mascot.png)

**Note this is a WIP**

If you have not already I **highly recommened** you read the `Motivations` post found [here](http://doridori.github.io/Android-Architecture-Pilot/) as they lay a foundation for the solutions presented in this README.

This README will have the following structure:

1. Quick Intro to the project
2. Some short usage examples
3. Outline the core library components
4. Outline the general concepts
5. How to integrate
6. Highlight what still needs to be worked on
7. Mention any current known limitations

#Intro

**Pilot** is an effort to abstract a simple Application Stack from the common approaches to Controller/Presenter (from now on I will just use these terms interchangeably) based Android application architecture. An aim is to **not** tie any implementations to any specifc controller type or 3rd party dependencies i.e. to be as flexible as possible. I often use State-based Presenter (as outlined by [Dynamo](https://github.com/doridori/Dynamo)) but this is by no mean a requirement.

This is _somewhat like_ a `FragmentManager` for `Views` but it is also much more than that. It is **Presenter-aware**, simple in implementation, has plumbing to handle `View` creation and **Presenter mapping** out of the box (which is optional and can be used to trigger Fragment/Activity transitions also), **survives config-changes and process-death** and has a **queryable** app stack **which can hold scoped data** as well as view-backing-presenters. 

You may read the below and think _'thats what `FragmentManager` is for'_ or _'I use the Activity backstack for that'_ and if you find those solutions are working for you thats great. I find that there is often cases where these two api concepts either over-complicate or restrict the things I need to do and feel there is a good case for some applications to use an abstracted stack instead.

This approach is something like a micro-framework as the application navigation flows through and is handled by it. It also is an approach to building apps that makes the problems outlined by the discussed motivations easier to handle but is not a library that has been created to solve a specific individual issue.

Pilot can be used in single-Activity applications (recommened!) or one-per Activity if need be.

#Quick Usage Examples

Quick setup in your applications Root Activity (I find I only use one Activity per app) by specifing an array of `@Presenter` backed `View` classes and the app launch state.

```java
public class ExampleRootActivity extends Activity
{
    private static PilotManager sPilotManager;

    static
    {
        Class<? extends PresenterBasedFrameLayout>[] rootViews = new Class[]{
                FirstView.class,
                SecondInSessionView.class
        };

        sPilotManager = new PilotManager(rootViews, FirstViewPresenter.class);
    }
    ...
}
```

_Im sure you may be thinking uh-oh when you see the `static`s above. This is explained elsewhere in this README._ 

Delegate some `Activity` lifecycle calls to the `PilotManager`

```java
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FrameLayout rootView = new FrameLayout(this);
        setContentView(rootView);
        sPilotManager.onCreateDelegate(savedInstanceState, rootView, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        sPilotManager.onDestroyDelegate(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        sPilotManager.onSaveInstanceStateDelegate(outState);
    }

    @Override
    public void onBackPressed()
    {
        sPilotManager.onBackPressedDelegate();
    }
```

The first time `Activity.onCreate()` is called it will triggers the `FirstView` to be added to the rootView (declared above), with access to its Presenter (as defined by `@Presenter`) which is pulled (and itself has access to) from the Activitys `PilotStack`.

```java
@Presenter(FirstViewPresenter.class)
public class FirstView extends PresenterBasedFrameLayout<FirstViewPresenter>
{
    ...
    public void someButtonPressed()
    {
        getPresenter().someButtonPressed();
    }
}
```

Example of view action causing a presenter method to be called, which in turn manipulates the application stack directly. This could just as easily have originated from internal logic to the presenter i.e. as the result of some asyncronous operation. This examples pushes a data-frame on the stack also (more on that later). Note how **no** `android.*` classes are involved in this app navigation / state transition.

```java
/**
 * Implement the Presenter/Controller aspect of this whatever way you want
 */ 
public class FirstViewPresenter extends PilotFrame
{
    ...
    /**
     * This represents some app nav action which happens via the parent FatStack.
     * Called by UI in production or called directly in tests. At this point you will generally want to
     *
     * 1) push another UI frame on the stack
     * 2) push a scoped-data frame on the stack and then a UI frame.
     */
    public void someButtonPressed()
    {
        //example of pushing data frame then presenter frame *directly* from presenter
        getParentStack().pushFrame(new SessionScopedData("RandomSessionKey"));
        getParentStack().pushFrame(new SecondInSessionViewPresenter());
    }
}
```

The corresponding view that utilised the newly added Presenter will auto be added to the main rootView (and all other Views removed).

```java
@Presenter(SecondInSessionViewPresenter.class)
public class SecondInSessionView extends PresenterBasedFrameLayout<SecondInSessionViewPresenter>
{
    public void someFuntionality()
    {
        getPresenter().someMethodCall...
    }
}
```

Pretty simple but quite powerful!

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

Handles a `PilotStack` for you and handles core `View` changes based upon stack transitions (if you are using Views). A `PilotManager` is intended to be used one-per-Activity and is designed to be referenced and setup statically as it handles its own memory releasing inside the Activity lifecycle delegate calls. This allows for easy persistence accross config-changes and back-stack-memory-saving-death and will clear all memory used when the Activity is destroyed for good. It will also handle saving and restoring itself on process-death. For a any discussion or questions about this please see [this related ticket](https://github.com/doridori/Pilot/issues/8). **Think of the `PilotManager` as the glue code between Android `Activity`s lifecycle peculiarities / View handling and the simple `PilotStack` functionality and usefullness.**

The `PilotManager` allows you to _compose_ your Activity with Pilot functionality as opposed to _inherit_ it from some form of PilotRootActivity, hence the need for delegation methods.

#General Concepts

##Single-Activity Application

I find a lot of the applications I have build in the last couple of years sit inside a single `Activity` and just manipulate `Fragments` and/or `View`. You can use a single `Activity` which holds a single `PilotStack` for the simplest use case. This however does not mean you cant jump out to seperate activitys, for example when integrating with 3rd party libs which require this.

Advantage: Simple

##Presenter-to-Presenter control flow

As mentioned in the `Motivations` post above this kind of control flow (which bypasses `android.*` classes completly has its advantages) and is made very simple with Pilot. An example below

```java
public class FirstViewPresenter extends PilotFrame
{
    ...
    public void somethingHappened()
    {        
        getParentStack().pushFrame(new SecondViewPresenter());
    }
}
```

Advantage: This means for each and quick JVM testing and any interested renderer could just listen out for `PilotStack` changes and work out how to render them. You could imagine it would be trivial to replace the whole interface on an application with a command line and you would not have to change a line of controller logic (ok so you may never do this but it highlights a good degree of decoupling!).

##Presenter lifecycle management

As is the root goal of most frameworks that interact with controller lifecycle management is an important point. Using the 'PilotStack' the 'PilotFrames' (and therefore any controllers they may be composed of and provide) live just as long as they need to without worrying about config-changes _or_ the lifecycle of any attached view/activity component. This happens for **free** as all app navigation happens via the `PilotStack` in the first place. For example see below

![Presenter Scope Example Diagram](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/presenter_scope_example.png)

This may seem overly simple - but in my mind thats a good thing! There are many approaches to this singular issue that can easily get over-complicated when controllers and liveness are tied too much into an Activity or view components particular lifecycle.

##How Presenters are attached to Views and displayed.

This is probably most easily illustrated with a Sequence diagram, see below.

![Main sequence](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/init_sequence.png)

###What about after a config-change?

After a config-change the PilotStack has been preserved via the Activity lifecycle delegate methods - which will auto show the view at the top of the PilotStack. 

###What about re-inflated Views that are now presenter-less?

These will either have their Presenter pushed back to them or will be removed if they represent a PilotFrame that is not longer top of the PilotStack. You would probably only encounter this case in you were using a PilotStack inside a `Fragment` that had been re-infalted. Not the recommended use-case anyhow.

###What about child views that also have Presenters?!

There are many forms these can take but a simple way to handle these are to maintain a collection of child-Presenter objects inside the PilotFrame and push / pull them out depending on their own individual lifecycle. 

For example you may have a RecyclerView which contains child Views which are each backed by their own Presenter. These could be pulled from the main PilotFrame backed Presenter by some meta data i.e. id.

##Scoped Data

This is an interesting one. As mentioned in the `Motivations` post linked above, scoped data will make people think of differnt implementations depending on if any DI frameworks (think Dagger) are in use. I will restate here that this solution is not to replace Dagger (and also does not require it) but is a way of (optionally) handling data scopes (and the method by which the scoped data could be added and removed from any DI frameworks at runtime).

The notion is very simple really. Is it that there is the idea of a PilotFrame that can just represent data only (as opposed to the default, which is a PilotFrame that represents an app-state which is typically used to back a view-state). Operations can be performed upon these data-frames (e.g. clear this data-frame and all above it) just like any other frame. The one difference is that data-frames do **not** get passed through any `PilotStack` `Listeners`. These data-frames are currently signified by applying the `@InvisibleFrame` annotation to a `PilotFrame` subclass.

An example may help here. A common kind of scoped data in most apps is some kind of `Session` data (say data that exists while a user is logged in).

![Data Scope Example Diagram](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/data_scope_example.png)

In the above diagram we show the SessionDataFrame being popped from the stack along with any in-session frames above (in this example just the one). This could be from user log-out or session timeout (timeout counter could live in the SessionDataFrame itself) and would auto fire a `PilotStack` `Listener` callback containing the top `PilotFrame` in the stack, which may be a login screen in this case.

Inside the SessionDataFrame the data can be made available to the stack by either
- If using Dagger the PilotFrame lifecycle methods (push/pop) couple optionally add / remove dependencies from any Dagger ObjectGraph (i.e. some LoggedInUser object)
- If not using Dagger (as per the current repo example) the SessionData can be pulled from the PilotStack directly (see below code snippit)

```java
public class SecondPilotFrame extends PilotFrame
{
    ...
    public String getSessionDataToDisplay()
    {
        SessionDataFrame sessionScopedData = getParentStack().getFrameOfType(SessionDataFrame.class);
        return sessionScopedData.getSomeSessionData();
    }
}
```

This has benefits of scoped data being able to handle its own lifecycle and also being cleared by default when the stack is cleared but also if clearing the stack on `Activity.finish()` you get all your sensitive data cleaned up for free. Happy days.

##Controller Lifecycle Events

WIP see [Issue](https://github.com/doridori/Pilot/issues/2) representing this task.

##Callbacks

One advantage of using presenter-to-presenter comms is that we can just pass operation callbacks directly. This is a lot simpler that using things like `startActivityForResult()` and having to convert your operations to ints and bundles. You can just pass a callback class / anon class / closure (if using retroLambda) directly to the instantianted Presenter.

"But what about PilotStack state persistence and callbacks?" I hear you ask. Yes this is a common issue when passing callback objects to `Fragment` and `View` implementations as on rotation these view objects are reinflated and your callbacks are lost resulting in a fun NPE. As the PilotStack is persisted on config change we dont have to worry about this. Regardless callbacks being retained on process death, this is quite interesting. If we are Serializing the stack (as talked about elsewhere in this readme) and we have a callback like

```java
	getParentStack().pushFrame(new SecondViewPresenter(new SomeCallback
	{
		public void doSomething()
		{
			//doing something!
	    }
	}
	}));
```

This relationship will be preserved on Deserialization! This however does require callbacks are Serializable. Alpternativly you could fall back to using a version of result codes and just pass primitives around to trigger callback behaviour.

##Handling Process Death

Some times you want your apps state to survive process death. This happens for free inside the `PilotManager` class via the `Activity` lifecycle delegation methods.

Some may complain that serialization is slow and is not ideal. You are right! A pending improvement is to use Parcelable or @AutoParcel in place of Serialization. See [related issue](https://github.com/doridori/Pilot/issues/7).

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

##Example App

See the simple example app for an idea how this stuff works in practise.

#WIP

Please see the projects Issues list for what still needs to be worked on. The main thing at present is the PilotFrame lifecycle callbacks and will do this ASAP. Interested to hear if any input on the thoughts I have had on it.

##Limitations

There are some things that I never really end up using in the 5-6 years I have professionally building Android apps. These include:

- 2 pane tablet layouts
- Tasks. By this I mean the ability to maintain a branching stack where each branch contains a backstack and share a common ancestor. Would suit specific app UX which is not that common in the Android world. Not a big deal its not included!

For this reason I have not spent much time thinking about how to work with them. That said they are represented on the Issues list and I do have ideas how these can work with Pilot. Personally I will wait till I need them or they are requested before spending many thought-cycles on them.

##F.A.Q.

No one has asked me any questions about this as yet but if they do I will create an F.A.Q. :)

#License

    Copyright 2015 Dorian Cussen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


