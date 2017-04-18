# F.A.Q.

## Q. Is this a `FragmentManager` for `Views?`

At first glance this may look like a `FragmentManager` for `Views`, but this would be misleading.

A `FragmentManager` (together with `FragmentActivity`) takes ownership of a stack of fragments, the display of those fragments, the state restoration and Fragments lifecycle calls. This is all fine and of course works well for many. 

A problem with`FragmentManager` is that it really violates the `SRP` principle and as a result can be cumbersome to use (code smell: 2300 LOCs!). As it bundles the above responsibilities together it is inflexible. Also, there is no simple way to attach state to these constructs without falling foul of some edge case of lifecycle handling.

This issue has led to an explosion of MVP libraries and approaches to try and move domain logic outside of these constructs, which is fantastic. A problem is that many documented MV* approaches still rely on a `FragmentManager` held backstack to represent view navigation, which ends up in application-state-changes and transitions having to be routed through a UI component. This is un-needed and results in harder to test code and is a direct-result of the original design decisions of the bloated `Fragment` concept. 

**Pilot attempts to split up the responsibilities of the `FragmentManager` into distinct SRP components.** Once this point is reached `Fragment`s lose their appeal.

Pilot is also **Presenter-aware**, has plumbing to handle `View` creation and **Presenter mapping** out of the box (which is optional and can be used to trigger Fragment/Activity/Dialog creation also), **survives config-changes and process-death** and has a **queryable** app stack **which can hold scoped data** as well as view-backing-presenters. 

## Q. How can I use this as part of a wider architecture?

See [Example App Architecture](https://github.com/doridori/Pilot/blob/master/docs/app-architecture.md)


## Q. So where does one put context related android and google play services code in this architecture?

If your interacting with any system service or something that requires an application context it would make sense to apply some form of DI to make this object available to your consumers anyway. If you had a frame in the stack that represented grabbing a location and doing something with it this could be done via your DId location obtainer.

If its something that has some provided UI you would need to register a `UITypeHandler` that performs the appropriate calls when the related `PilotFrame` appears on the stack.
