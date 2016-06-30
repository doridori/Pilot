#ExampleFrame

```java
public class ExamplePresenter extends PilotFrame
{
    private ViewState viewState = new ViewState();
    
    //==================================================================//
    // Constructor
    //==================================================================//

    public ExamplePresenter(Args args)
    {
        super(args); 
        ...//pull something out of args
    }
    
    //==================================================================//
    // Lifecycle
    //==================================================================//
    
    @Override
    public void pushed()
    {
        //perform some actions here that may interact with getParentStack() i.e. check something and maybe transition app state
    }
    
    //==================================================================//
    // Inputs / User Actions
    //==================================================================//
    
    public void userDidSomething...()
    {
       ...//do something i.e. perform an async call that changes the ViewState object
       notifyObservers();
    }
    
    public void userDone()
    {
        //in case this frame has been removed from the stack i.e. the forwarding view may 
        //be animating out and still have an active 'done' button
        if(getParentStack() == null) 
            return;
    
        //push another PilotFrame on the stack. This will trigger a new View to be rendered.
        getParentStack().pushFrame(new AnotherExamplePresenter());
    }
    
    //==================================================================//
    // Current State of View
    //==================================================================//

    public ViewState getViewState()
    {
        return viewState;
    }

    public static class ViewState
    {
        //some view state i.e.
        public String someMessage = "Oh Hai!";
    }
}
```
    
#Example View

```java
@BackedByFrame(ExamplePresenter.class)
public class ExampleView extends PilotFrameLayout<ExamplePresenter>
{
    //==================================================================//
    // Constructor
    //==================================================================//

    public ExampleView(Context context)
    {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.view_example, this, true);
    }
    
    //==================================================================//
    // Lifecycle
    //==================================================================//
    
    @Override
    public void backingFrameSet(PinFrame backingPilotFrame)
    {
        //do something that may interact with the backing frame
    }

    //==================================================================//
    // Presenter Updates
    //==================================================================//
                
    @Override
    public void updated()
    {
        ExamplePresenter.ViewState viewState = getBackingPilotFrame().getViewState();
        ...//use ViewState to change visible UI state
    }
}
```
