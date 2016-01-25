#F.A.Q.

##Q. So where does one put context related android and google play services code in this architecture?

If your interacting with any system service or something that requires an application context it would make sense to apply some form of DI to make this object available to your consumers anyway. If you had a frame in the stack that represented grabbing a location and doing something with it this could be done via your DId location obtainer.

If its something that has some provided UI you would need to register a `UITypeHandler` that performs the appropriate calls when the related `PilotFrame` appears on the stack.
