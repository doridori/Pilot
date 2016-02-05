#ExampleFrame

```java
public class ExamplePresenter extends PilotFrame
{
    private ViewState viewState = new ViewState();
    
    //=============//
    // Constructor
    //=============//

    public EnterCardPresenter(Args args)
    {
        super(args); 
        ...//pull something out of args
    }
    
    //=======================//
    // Inputs / User Actions
    //=======================//
    
    public void userDidSomething...()
    {
       ...//do something i.e. perform an async call that changes the ViewState object
       notifyObservers();
    }
    
    //=======================//
    // Current State of View
    //=======================//

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
