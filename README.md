[![Circle CI](https://circleci.com/gh/doridori/Pilot.svg?style=svg)](https://circleci.com/gh/doridori/Pilot)  [![Maven Central](https://img.shields.io/badge/Maven%20Central-v1.0.2-blue.svg)](http://repo1.maven.org/maven2/com/kodroid/pilot/)

# Pilot

Pilot is a way to model the Application State in a familiar (`android.*` decoupled) `Stack` structure, and provides hooks for `View` based UI Rendering to take place. This facilitates:

- Single `Activity` applications
- A (passive) thin-`View` based architecture 
- `android.*`-less app navigation (i.e. Controller -> Controller)
- An abstract backstack
- Stack-based data scoping
- Use of any kind of MV* approach

![Pilot Mascot](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/pilot_mascot.png)

# Why

- Cleaner, decoupled code
- Avoiding `Fragments` is desired for many reasons, not limited to flexibility of how the backstack is used
- Flexibity of what lives in the backstack enables easy data-scoping for session / screen data as it lives in the stack rather than being passed around or statically based
- Easy testing
- As facilitates MV\* approaches this means not having to think about asynchronous operations in your UI code (i.e. you can ignore `Loaders`, RxLifecycle handling or whatever other approach being used to work around the android lifecycle)

# Components

## Application State

Type                      | SRP 
--------------------------|------------------------------
StateStack                |A `Stack` of `StatStackFrame` objects
StateStackFrame           |Frame that lives in a `StateStack`. May represent a Screen or scoped-data.
@HiddenStateFrame         |Annotation which removes a `StateStackFrame` from all `StackStack` change observer callbacks

## Rendering (Android)

Type                      | SRP 
--------------------------|------------------------------
StateStackRenderer        |Holds the `StateFrameSetRenderer` collection that is queried upon `StateStackFrame` change events
StateFrameSetRenderer     |Interface for an object that can compose a UI for a given set of `StateFrame` classes
StateStackActivityAdapter |Bridge between the hosting Activities lifecycle events and a `StateStack` instance
StateStackFrameBackedFrameLayout |Convenience `FrameLayout` base for `BackedByFrame` backed views

# Seperating Application State from UI Rendering

It can be beneficial to separate the Applications State from the rendering of the UI as really these are separate concerns.

**Application State** is generally concerned with: 

- What is the user currently doing? 
- What was the user previously doing that can be returned to? 
- What happens if the user decides to do something else?
- What data is associated with what the user is currently doing?
- What operations are associated with what the user is currently doing?

**Rendering** is generally concerned with:

- How can the user visualise what they are currently doing?
- How does the user return to what they were previously doing?
- How does the user signify they want to do something else?

The primary win when decoupling in this way is by seperating reponsilbilities testing and refactoring becomes much easier. A secondary advantage is that the Android rendering could be replaced with a terminal, or other type of client. 

One design principle is that the Stack should not change regardless of the screen size or rotation. Any master/detail changes (or other size related rendering logic) should sit in the Rendering layer, which in Pilot is abstracted by the `StateFrameSetRenderer`.

Another design priciple is that the Application State should be pure java, to facilitate JVM testing.

# Supplementary Aims

To make usage of this project as flexible as possible, the following decisions have been made:

- **Not** to tie any consumers to a specific controller type. The reason for this is that all projects have their own approaches in terms of what a Presenter/Controller should be and this is really an orthogonal concern to how the lifecycle and navigation between these controllers is handled. You may prefer to use MVP (Passive View, Supervising Controller, Presentation Model), MVVM or some other variant, this should not impact how the controller layer is managed.

- **Not** to _require_ any 3rd party dependencies (e.g. Dagger or RxJava). These (and other) libraries can be used with Pilot but are not essential. 

# `View` only?

At present Pilot is for use primarily with `View`-only UI's. While this seems restrictive I feel adding `Fragment` support at this point would distract from the main aim of this project. Fragments are great as they are a wrapper for a sub-section of the UI, which have lifecycle callback support and backstack etc. I find when these concepts are pulled out (as they are in Pilot) UI components suddenly become simpler and less bothered about these concepts, and therefore humble `View` becomes the obvious choice for how to render an applications state, as represented by `Pilot`.

You may end up with a single-`Activity` application with nothing but simple `Views`!

# Not the first

Of course I am not the first to think in the way. The guys at Square were talking about [very similar things](https://corner.squareup.com/2014/10/advocating-against-android-fragments.html) (simple Views and abstract backstack management) years ago - this is a slightly different approach for how to realise those concepts.

Nor am I the last! [Conductor](https://github.com/bluelinelabs/Conductor) is a very similar library which has appeared with the same concepts (and better documentation). Good to see others are thinking along the same lines. I will continue with Pilot however as sure we will have slightly differnt take on things.

EDIT: [Moxy](https://github.com/Arello-Mobile/Moxy) and [Triad](https://github.com/nhaarman/Triad) and [Magellan](https://github.com/wealthfront/magellan) and [Flowless](https://github.com/Zhuinden/flowless) and [ThirtyInch](https://github.com/grandcentrix/ThirtyInch) are similar recent repos

# Doc Contents

The rest of this README is split across a few doc files:

- [Example Frame & View](https://github.com/doridori/Pilot/blob/master/docs%2Fexample_frame_and_view.md) **Needs Update**
- [Quick Start](https://github.com/doridori/Pilot/blob/master/docs/quick_start.md) **Needs Update**
- [General Concepts](https://github.com/doridori/Pilot/blob/master/docs/general_concepts.md) **Needs Update**
- [F.A.Q.](https://github.com/doridori/Pilot/blob/master/docs/faq.md) **Needs Update**
- [Example App Architecture](https://github.com/doridori/Pilot/blob/master/docs/app-architecture.md) **Needs Update**

# Usage

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'com.kodroid:pilot:1.0.2'
}
```

# Proguard

```
-keepclassmembers class * extends com.kodroid.pilot.lib.android.frameBacking.PilotFrameLayout{
 public <init>(android.content.Context);
}

-keepclassmembers class * extends com.kodroid.pilot.lib.stack.PilotFrame{
 public <init>(com.kodroid.pilot.lib.stack.Args);
}
```

# WIP

This is a WIP and is currently in development. Some of the supplementary docs also need updating as the project is still undergoing some conceptual refactoring. I am welcome to any input via the Issues page to guide its development. This library is not in use in production yet.

# License

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


