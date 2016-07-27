As Pilot concerns itself with the UI & Presenter/Controller layer only it needs to be used as part of a wider app architecture. There is flexibility in this but an example architecture is shown below.

The below architecture embodies Clean Architecture principles, and is similar to the [Viper](http://mutualmobile.github.io/blog/2013/12/04/viper-introduction/) iOS architecture. 

#Overview

![App Arch with Pilot](/gfx/app_arch.png)

#Layers

##View Layer

Plain-Old-Android-Views! These forward input events to and render state from their corresponding Presenter `PilotFrames`.

##Presenter Layer

The PilotStack, which holds mostly Presenter `PilotFrames` with some data only frames. 

##Use-Case Layer

Embodies Use-Cases i.e. `Login`, `GetDataX` etc.




