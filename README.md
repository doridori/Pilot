[![Circle CI](https://circleci.com/gh/doridori/Pilot.svg?style=svg)](https://circleci.com/gh/doridori/Pilot)  [![Maven Central](https://img.shields.io/badge/Maven%20Central%20SNAPSHOT-v0.10.0-blue.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/kodroid/pilot/0.10.0-SNAPSHOT/)

#Pilot

An `android.*` decoupled application navigation stack which facilitates:

- Single `Activity` applications
- A (passive) thin-`View` based architecture 
- `android.*`-less app navigation (i.e. Presenter -> Presenter)
- An abstract backstack
- Stack-based data scoping
- Use of any kind of MV* approach

![Pilot Mascot](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/pilot_mascot.png)

##Why?

- Cleaner, decoupled code
- Avoiding `Fragments` is desired for many reasons, not limited to flexibility of how the backstack is used
- Flexibity of what lives in the backstack enables easy data-scoping for session / screen data as it lives in the stack rather than being passed around or statically based
- Easy testing
- As facilitates MV* approaches this means not having to think about asynchronous operations in your UI code (i.e. you can ignore `Loaders`, RxLifecycle handling or whatever other approach being used to work around the android lifecycle)

##How?

Type                 | SRP 
---------------------|------------------------------
PilotStack           |A `Stack` of `PilotFrame` objects
PilotFrame           |Frame that lives in a `PilotStack`. May represent a Screen or scoped-data
PilotSyncer          |Holds the `UITypeHandler` collection that is queried upon `PilotStack` change events
UITypeHandler        |Interface for an object that can compose a UI for a given set of `PilotFrame` classes
PilotLifecycleManager|Bridge between the hosting Activities lifecycle events and a `PilotStack` instance
PilotFrameLayout     |Convenience `FrameLayout` base for `BackedByFrame` backed views
@BackedByFrame       |Annotation to link a `PilotFrameLayout` to a `PilotFrame` instance

#Intro

**Pilot** is an effort to abstract a simple Android Application-Navigation-Stack. 

Android development blurs the line between UI declaration and application flow. `Activity` and `Fragment` Lifecycle methods coupled with asynchronous operations can be a headache. These problems are well known and have various solutions represented by various projects on GitHub.

A popular approach is to use an MV* based architecture to separate some form of Controller / Presenter variant from the UI. This is great and has many benefits. However, there are not many projects out there that facilitate UI-less controller -> controller flow. This is the hole that Pilot is aiming to fill.

There are multiple benefits of allowing such a flow. If you have not already I recommend you read the [Motivations post](http://doridori.github.io/Android-Architecture-Pilot/).

**_21/07/16 Note:_ this is a WIP and is currently in development. Some of the supplementary docs also need updating as the project is still undergoing some conceptual refactoring. I am welcome to any input via the Issues page to guide its development. This library is not in use in production yet.**

#Supplementary Aims

To make usage of this project as flexible as possible, the following decisions have been made:

- **Not** to tie any consumers to a specific controller type. The reason for this is that all projects have their own approaches in terms of what a Presenter/Controller should be and this is really an orthogonal concern to how the lifecycle and navigation between these controllers is handled. You may prefer to use MVP (Passive View, Supervising Controller, Presentation Model), MVVM or some other variant, this should not impact how the controller layer is managed.

- **Not** to _require_ any 3rd party dependencies (e.g. Dagger or RxJava). These (and other) libraries can be used with Pilot but are not essential. 

#`View` only?

At present Pilot is for use primarily with `View`-only UI's. While this seems restrictive I feel adding `Fragment` support at this point would distract from the main aim of this project. Fragments are great as they are a wrapper for a sub-section of the UI, which have lifecycle callback support and backstack etc. I find when these concepts are pulled out (as they are in Pilot) UI components suddenly become simpler and less bothered about these concepts, and therefore humble `View` becomes the obvious choice for how to render an applications state, as represented by `Pilot`.

You may end up with a single-`Activity` application with nothing but simple `Views`!

#Not the first

Of course I am not the first to think in the way. The guys at Square were talking about [very similar things](https://corner.squareup.com/2014/10/advocating-against-android-fragments.html) (simple Views and abstract backstack management) years ago - this is a slightly different approach for how to realise those concepts.

Nor am I the last! [Conductor](https://github.com/bluelinelabs/Conductor) is a very similar library which has appeared with the same concepts (and better documentation). Good to see others are thinking along the same lines. I will continue with Pilot however as sure we will have slightly differnt take on things.

EDIT: [Moxy](https://github.com/Arello-Mobile/Moxy) now looks very similar also.

#Doc Contents

The rest of this README is split across a few doc files:

- [Example Frame & View](https://github.com/doridori/Pilot/blob/master/docs%2Fexample_frame_and_view.md)
- [Quick Start](https://github.com/doridori/Pilot/blob/master/docs/quick_start.md)
- [General Concepts](https://github.com/doridori/Pilot/blob/master/docs/general_concepts.md) **WIP**
- [F.A.Q.](https://github.com/doridori/Pilot/blob/master/docs/faq.md)
- [Example App Architecture](https://github.com/doridori/Pilot/blob/master/docs/app-architecture.md)

#Usage

```gradle
repositories {
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots"
    }
}

dependencies {
    compile 'com.kodroid:pilot:0.9.0-SNAPSHOT'
}
```

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


