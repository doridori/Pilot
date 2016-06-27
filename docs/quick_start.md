#Quick Start

Have a quick look at [Example Frame & View](https://github.com/doridori/Pilot/blob/master/docs%2Fexample_frame_and_view.md) and then see below for how to implement with Pilot.
    

 
##Integrate `PilotLifecycleManager` into your `Activity`

`PilotLifecycleManager` will ensure there is always a valid `PilotStack` instance available.  This also allows us a simple mechanism of retaining the `PilotStack` on config-change and will handle saving / restoring the `PilotStack` on process death.

Then **delegate** a few `Activity` lifecycle calls to the `PilotLifecycleManager`

```java
    //==================================================================//
    // Lifecycle
    //==================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	//...
	
        //The PilotStack instance in use in this example lives in a Singleton. The manager will ensure this Activity won't leak.
        pilotLifecycleManager = new PilotLifecycleManager(PilotStackHolder.getInstance(), EnterCardPresenter.class);
        pilotLifecycleManager.onCreateDelegate(savedInstanceState, buildPilotSyncer(rootView), this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        pilotLifecycleManager.onDestroyDelegate(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        pilotLifecycleManager.onSaveInstanceStateDelegate(outState);
    }

    @Override
    public void onBackPressed()
    {
        pilotLifecycleManager.onBackPressedDelegate();
    }
```

Note inside `onCreate` we pass the `PilotSyncer` we declared earlier.


##Create some `PilotFrame` subclasses

These represent the **app states** (think _LoginScreen_ or _ContentScreen_) and the **data-scopes** (think _session_ or _domain-driven-process_)  of your application and are the things that will live on the stack.

A `PilotFrame` with the `@InvisibleFrame` annotation represents the **data-scope** frame mentioned above and will not trigger `PilotStack` listener callbacks. These are useful to make stuff available (and un-available) on a DI object graph, or alternatively for classes to pull data out from directly.

All other `PilotFrames` represent an **app state** and are the ones which will have a `UITypeHandler` which handles this `PilotFrame` subclass via the `PilotSyncer` discussed above.

As mentioned elsewhere there is no actual rule for how your `PilotFrame` classes should be structured as everyone has their own approach to controller / presenter / view-model logic and I do not want to force anyone down a particular route. 

For an example check out [Example Frame & View](https://github.com/doridori/Pilot/blob/master/docs%2Fexample_frame_and_view.md).

##`View` should reflect state of corresponding `PilotFrame`

When a `PilotFrameLayout` extending `View` is created is will have the corresponding `PilotFrame` passed to it. This is availble by the time `View.onAttachedToWindow()` is called. View subclasses can override `backingFrameSet(ShowPadPresenter backingPilotFrame)` to do any post-backing frame set initialisation.

State-change listeners are added inside the `PilotFrame` `View.onAttached` and `View.onDetached` methods and will result in `PilotFrame.updated()` being called, which is where you should sync up the UI state with the PilotFrame state.

For an example check out [Example Frame & View](https://github.com/doridori/Pilot/blob/master/docs%2Fexample_frame_and_view.md).

##Declare what _Top Level Views_ exist in the application

Each main view/screen of the application will be represented by a `PilotFrame` subclass, which will hold that screens ViewState and communicate with any asynchronous code. Each of these `PilotState`s need to have a corresponding `View` class that will represent it. In this documentation I am referring to these as _Top Level Views_ (TLV). These can to be declared as so: 

```java
static final Class<? extends PilotFrameBackedFrameLayout>[] TOP_LEVEL_VIEWS = new Class[]
    {
        FirstView.class,
        SecondInSessionView.class
        ...
    };
```

Each of these TLVs are matched to a `PilotFrame` subclass via a `@BackedByFrame` annotation declared in the TLV - more on this in a bit.

##Declare a `PilotSyncer` for these TLVs
 
A `PilotSyncer` is the class that is responsible for ensuring the UI matches the current `PilotStack` state. 

This is declared by creating a new instance with one or more `UITypeHandler`s. The `UITypeHandler` interface consists of one method, `boolean onFrame(PilotFrame frame);` which should return `true` if it handles a specific `PilotFrame` subclass. All that needs to be done is to create a `PilotSyncer` that can handle all possible visible `PilotFrames` that can exist in the stack.

The below example uses a single `UIViewTypeHandler` which will create the TLV view and place it inside the passed `rootView` using the supplied `UIViewTypeHandler.Displayer` class. 

```java
private PilotSyncer buildPilotSyncer(FrameLayout rootView)
{        
    UITypeHandler allViewsUiTypeHandler = new UIViewTypeHandler(
            TOP_LEVEL_VIEWS, 
            new UIViewTypeHandler.SimpleDisplayer(rootView));
        
    return new PilotSyncer(allViewsUiTypeHandler);
}
```

##Launch your app

Following the above on app launch your initial PilotFrame should be pushed on the `PilotStack` and your `PilotSyncer` registered `UIViewTypeHandler` should create and display the corresponding `View`, Voila!
