Android Architecture: Introducing Pilot

#Motivation

There are some common questions that keep popping up on the interwebs around the current collective thought on Android Architecture. Some of these questions are:

1. What approach can I use to handle my Presenters Lifecycle?
2. How can Presenters control the flow of an application?
3. How can I scope data within my application?
4. What architecture chould I use to support a View only based architecture (or a mix of Fragments / Views) ?

The above questions are not easy ones to address and by no means an I suggesting the below is the only solution to these common issues. I have been thinking about solutions to the above and have started using a simple abstract application stack which does offer solutions. 

This post and supporting repo is definitely a work-in-progress but Im hoping it will spark some discussion which can be fed back into the project so it's something that is useful to others also. I have been tempted to not write anything about this until I have answered all the pending questions I have myself but I feel more may be gained by publishing early and often.

In this post I will briefly expand on the above questions and introduce the project.


#Preliminaries

##A Note on terminology

**Presenters** are all the rage in the Android world at present. Some call them Presenters, Controllers, ViewModels, PassiveView etc and they are used as part of MVC, MVP, MVVM (with DataBinding), MVA etc and all these terms overlap and have differnt meanings and implementations depending on who you talk to. The one thing they all have in common is pulling out from the view all the interesting stuff thats not to do with direct rendering of pixels and more about the application workings, states and asyncronous functionality. These architectures are great as they enable seperation of concerns and therefore easy testability and can be read about elsewhere. Throughout this post I will use these terms interchangably as one of the ideas of Pilot is not to force one kind of view/logic seperation.

**View** vs **view**

If I mean a subclass of `android.view.View` I will use an upper-case **View** else if I mean the view layer (could be view or fragment) then a lower-case **view**.

##A Note on dependencies

I have tried to keep dependency use to a minimum when thinking about this solution as in my mind a lib works best when it does not force the use of any approach outside of its primary concern. By all means Pilot will work with Dagger and RxJava but just as easily without.

##A Note on Morter and Flow

Morter and Flow is definitely a big part of the inspiration for Pilot and is created by developers a lot cleverer than me by a mile. The reason I did not use those two complementry libs to address the issues that are listed at the top of the post is down to one thing - I felt they were too complicated. Whenever I started to read though the docs, example projects and some excellent blog posts on it (like [Using Flow & Mortar](https://realm.io/news/using-flow-mortar/) and [An Investigation into Flow and Mortar](https://www.bignerdranch.com/blog/an-investigation-into-flow-and-mortar/)) I always ended up feeling like there was too much congitive load to hook it all together. Plus, I like trying to solve a problem myself :p

##A Note on RxJava

As hinted at above Pilots aims are orthagonal to RxJava so its not baked into this lib anywhere. It can be used inside your controllers or even as the bridge between controllers and your view objects.


#Motivations Expanded

##1. What approach should I use to handle my Presenters Lifecycle?

With the above in mind there is always a lot of discussion around how to handle the lifecycle of all these controller logic objects. Solutions are usually having them as part of a singleton so they live forever, manually creating and removing them based upon per view / fragment / activity logic so they survive config changes but not other events, recreating on config changes and just relying on GC or using 3rd party libs like Morter and Flow. For me all of these approaches have a drawback which may be poor memory managment (Singletons) or too complicated (Morter & flow). Also libs I have seen which partly address this issue always seem to force other contraints on the integrator like using RxJava or Dagger.

Ideally a solution here would allow us to control the lifecycle of our application controllers as a **distinct entity** and no longer have the lifecycle logic distributed thoughout the mire of view classes. I want to be able to implement custom lifecycle logic on an abstracted stack. 

##2. How can Presenters control the flow of an application?

Good question! A common one I have been seeing asked online and one that is often left unanswed. The default approach to this is something like:

1. view starts
2. view creates presenter
3. _something happens_
4. Presenter tells view needs to change to another view
5. view uses some Context to change view
6. GOTO 1

This is ok, but not great and often results in transitions being hooked into a Context somewhere and therefore harder to test some cases. For the same reason that Controllers are used in most apps for decoupling code from `android.*` classes (and therefore easy to test on a JVM) I want the same to be true of the application stack and navigation. Decouple FTW!

Ideally a solution for this would allow us to have Presenter-to-Presenter instantiation and leave the test-restraining views out of the question (plus this may save of some plumbing callbacks). For me a Controller should handle the States of an application and the view should just reflect the current state. This concept makes sense in a controller-to-view relationship just as much as an app-to-controller-stack relationship.

##3. How can I scope data within my application?

Another good one!

Many talks around Dagger (and especially Dagger 2) seem to raise this question. Dagger 2 introduces `@Scope`annotations of which the only one that does anything out of the box is the `@Singleton`. The talk of `@ActivityScope` always comes up [everywhere](http://stackoverflow.com/questions/29923376/dagger2-custom-scopes-how-do-custom-scopes-activityscope-actually-work) and the answer is that for any custom scopes they have to be implemented manually. In practise this can suffer from the same problem above around Presenter lifecycle management techniques or relies on Morter & Flow.

But also we of course have a notion of data and scoping outside of Dagger and DI frameworks. For example regardless of using Dagger we can easily think about an application that on logic makes some data available to the application and on logout removes this data (and also editing tasks / shopping / forms etc could all have small related scoped data sets).

There is also a security based motivation here as some secure applications need to be able to handle scoped data explicitly and succintly. Its not hard to image an application that collects some sensitve data which needs to be wiped as soon as any one of a number of conditions are met. If this sensitive data has been passed around and/or lives in a singleton somewhere cleanup can become spagetti like and error prone. 

Ideally the solution would be that regardless of the data handling technique we use (DI or no-DI) we had an easy way to add / remove / access / cleanup scoped data. Also regarding the security concern that data is automatically cleared based upon certain app specific state transitions, so a missed clear call wouldnt leak any data.

##4. What architecture chould I use to support a View only based architecture (or a mix of Fragments / Views)

View only approaches are also in fashion at present (as they were before Fragments even existed) and any solution we use needs to be able to work with `Fragments` &  `View` as there is no point in a solution that only works for a subset of possible view implementatons as its an unneeded limitation.

However there are some special concerns when it comes to View only arhcitecture, and for me this mainly lies around lifecycle callbacks. 

Ideally the proposed solution would work with Views or Fragments and allow the presenter to have some notion of a simplified application lifecycle. This in turn would allow Views to react to simple lifecycle events without using EventBuses, but you may find the View suddenly does not care about the lifecycle if the presenter already knows about the lifecycle change.

So, hope thats enough motavations before the main event.

#Introducing Pilot

![](https://raw.githubusercontent.com/doridori/Pilot/master/gfx/pilot_mascot.png)

Please continue reading via the repos README.
