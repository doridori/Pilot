# Example App Architecture

As Pilot concerns itself with the UI & Presenter/Controller layer only it needs to be used as part of a wider app architecture. There is flexibility in this, but an example _Clean_-style architecture is shown below.

The below architecture embodies [Clean Architecture](http://fernandocejas.com/2014/09/03/architecting-android-the-clean-way/) principles, and is similar to the [Viper](http://mutualmobile.github.io/blog/2013/12/04/viper-introduction/) iOS architecture. 

## Overview

![App Arch with Pilot](/gfx/app_arch.png)

## Layers

### View Layer

- Plain-Old-Android-Views! These forward input events to and render state from their corresponding Presenter `PilotFrames`.
- Interacts with Presenter layer by forwarding input-events, and rendering the Presenters `State` object when changed.

### Presenter Layer

- The PilotStack, which holds mostly Presenter `PilotFrames` with some data only frames. 
- Presentation Model styles Presenters
- This layer interacts with the Use-Case layer mostly via asynchronous callbacks. This is preferred over `Observables` at this stage as the returned data may be complicated (i.e could be success with some data, or multiple errors with differnt meta data).
- Use-case dependencies should be DI'd into the Presenter layer, as opposed to constructor passed, as the `PilotFrames` have fixed constructors.

### Use-Case (Interactor) Layer

- Embodies Use-Cases i.e. `Login`, `GetDataX` etc.
- Interacts with model layer via `Observables` for easy composition inside the Use-Case layer.
- Model dependencies should be passed via constructor or DI'd to this layer.

### Model Layer

- Represents all in-memory-data and all data-sources (i.e. remote API, local DB etc)





