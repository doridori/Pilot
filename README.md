[![Stories in Ready](https://badge.waffle.io/doridori/Pilot.png?label=ready&title=Ready)](https://waffle.io/doridori/Pilot)
#Pilot

An `android.*` decoupled application stack which facilitates:

- Presenter -> Presenter control flow
- Stack-based Presenter lifecycle management
- A `View` based architecture 
- Fine grained data scope management

![Pilot Mascot](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/pilot_mascot.png)

**Note this is a WIP**

#Intro

**Pilot** is an effort to abstract a simple Android Application-Navigation-Stack. 

Android development blurs the line between UI declaration and application flow. Activity and Fragment Lifecycle methods coupled with asynchronous operations can be a headache. These problems are well known and have various solutions represented by various projects on GitHub.

A popular approach is to use an MV* based architecture to separate some form of Controller / Presenter variant from the UI. This is great and has many benefits. However, there are not many projects out there that facilitate UI-less controller -> controller flow. This is the hole that Pilot is aiming to fill.

There are multiple benefits of allowing such a flow. If you have not already I recommend you read the [Motivations post](http://doridori.github.io/Android-Architecture-Pilot/).

#Supplementary Aims

To make usage of this project as flexible as possible, the following decisions have been made:

- **Not** to tie any consumers to a specific controller type. The reason for this is that all projects have their own approaches in terms of what a Presenter/Controller should be and this is really an orthogonal concern to how the lifecycle and navigation between these controllers is handled. You may prefer to use MVP (Passive View, Supervising Controller, Presentation Model), MVVM or some other variant, this should not impact how the controller layer is managed.

- **Not** to require any 3rd party dependencies (e.g. Dagger or RxJava). These (and other) libraries can be used with Pilot but are not essential. 

#`View` only?

At present Pilot is for use primarily with `View`-only UI's. While this seems restrictive I feel adding `Fragment` support at this point would distract from the main aim of this project. Fragments are great as they are a wrapper for a sub-section of the UI, which have lifecycle callback support and backstack etc. I find when these concepts are pulled out (as they are in Pilot) UI components suddenly become simpler and less bothered about these concepts, and therefore humble `View` becomes the obvious choice for how to render an applications state, as represented by `Pilot`.

You may end up with a single-`Activity` application with nothing but simple `Views`!

#Not the first

Of course I am not the first to think in the way. The guys at Square were talking about [very similar things](https://corner.squareup.com/2014/10/advocating-against-android-fragments.html) (simple Views and abstract backstack management) years ago - this is a slightly different approach for how to realise those concepts.

#`FragmentManager` for `Views?`

At first glance this may look like a `FragmentManager` for `Views`, but this would be misleading.

A `FragmentManager` (together with `FragmentActivity`) takes ownership of a stack of fragments, the display of those fragments, the state restoration and Fragments lifecycle calls. This is all fine and of course works well for many. 

A problem with`FragmentManager` is that it really violates the `SRP` principle and as a result can be cumbersome to use (code smell: 2300 LOCs!). As it bundles the above responsibilities together it is inflexible. Also, there is no simple way to attach state to these constructs without falling foul of some edge case of lifecycle handling.

This issue has led to an explosion of MVP libraries and approaches to try and move domain logic outside of these constructs, which is fantastic. A problem is that many documented MV* approaches still rely on a `FragmentManager` held backstack to represent view navigation, which ends up in application-state-changes and transitions having to be routed through a UI component. This is un-needed and results in harder to test code and is a direct-result of the original design decisions of the bloated `Fragment` concept. 

**Pilot attempts to split up the responsibilities of the `FragmentManager` into distinct SRP components.** Once this point is reached `Fragment`s lose their appeal.

Pilot is also **Presenter-aware**, has plumbing to handle `View` creation and **Presenter mapping** out of the box (which is optional and can be used to trigger Fragment/Activity/Dialog creation also), **survives config-changes and process-death** and has a **queryable** app stack **which can hold scoped data** as well as view-backing-presenters. 

#Doc Contents

The rest of this README is split across a few doc files:

- [Quick Start](https://github.com/doridori/Pilot/blob/master/docs/quick_start.md)
- [One approach to a View backing `PilotFrame` design](#) **WIP**
- [General Concepts](https://github.com/doridori/Pilot/blob/master/docs/general_concepts.md) **WIP**

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


