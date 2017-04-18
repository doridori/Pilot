# General Concepts

## Single-Activity Application

I find a lot of the applications I have build in the last couple of years sit inside a single `Activity` and just manipulate `Fragments` and/or `View`. You can use a single `Activity` which holds a single `PilotStack` for the simplest use case. This however does not mean you cant jump out to seperate activitys, for example when integrating with 3rd party libs which require this.

Advantage: Simple

## Frame-to-Frame control flow

As mentioned in the `Motivations` post above this kind of control flow (which bypasses `android.*` classes completly has its advantages) and is made very simple with Pilot. An example below

```java
public class FirstViewFrame extends PilotFrame
{
    ...
    public void somethingHappened()
    {        
        getParentStack().pushFrame(new SecondViewPresenter());
    }
}
```

Advantage: This means for each and quick JVM testing and any interested renderer could just listen out for `PilotStack` changes and work out how to render them. You could imagine it would be trivial to replace the whole interface on an application with a command line and you would not have to change a line of controller logic (ok so you may never do this but it highlights a good degree of decoupling!).

## Presenter lifecycle management

As is the root goal of most frameworks that interact with controller lifecycle management is an important point. Using the 'PilotStack' the 'PilotFrames' (and therefore any controllers they may be composed of and provide) live just as long as they need to without worrying about config-changes _or_ the lifecycle of any attached view/activity component. This happens for **free** as all app navigation happens via the `PilotStack` in the first place. For example see below

![Presenter Scope Example Diagram](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/presenter_scope_example.png)

This may seem overly simple - but in my mind thats a good thing! There are many approaches to this singular issue that can easily get over-complicated when controllers and liveness are tied too much into an Activity or view components particular lifecycle.

## How Presenters are attached to Views and displayed.

//todo

### What about after a config-change?

After a config-change the PilotStack has been preserved via the Activity lifecycle delegate methods - which will auto show the view at the top of the PilotStack. 

### What about re-inflated Views that are now presenter-less?

These will either have their Presenter pushed back to them or will be removed if they represent a PilotFrame that is not longer top of the PilotStack. You would probably only encounter this case in you were using a PilotStack inside a `Fragment` that had been re-infalted. Not the recommended use-case anyhow.

### What about child views that also have Presenters?!

There are many forms these can take but a simple way to handle these are to maintain a collection of child-Presenter objects inside the PilotFrame and push / pull them out depending on their own individual lifecycle. 

For example you may have a RecyclerView which contains child Views which are each backed by their own Presenter. These could be pulled from the main PilotFrame backed Presenter by some meta data i.e. id.

## Scoped Data

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

## Controller Lifecycle Events

See [Issue](https://github.com/doridori/Pilot/issues/7) representing this task.

## onFrameResult

//todo

## Handling Process Death

//todo

## Opaqueness 

A frame can represent a View that is opaque or partially transparent. This infomation is stored obtained via a `UITypeHandlers.isOpaque()` method (see [#20](https://github.com/doridori/Pilot/issues/20)). This information is important as it allows UITypeHanders to rebuild the entire visible UI at any point.


