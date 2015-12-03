#Show me the code!

Quick setup in your applications Root Activity (I find I only use one Activity per app) by specifing an array of `@Presenter` backed `View` classes and the app launch state.

```java
public class ExampleRootActivity extends Activity
{
    private static PilotManager sPilotManager;

    static
    {
        Class<? extends PresenterBasedFrameLayout>[] rootViews = new Class[]{
                FirstView.class,
                SecondInSessionView.class
        };

        sPilotManager = new PilotManager(rootViews, FirstViewPresenter.class);
    }
    ...
}
```

_Im sure you may be thinking uh-oh when you see the `static`s above. This is explained elsewhere in this README._ 

Delegate some `Activity` lifecycle calls to the `PilotManager`

```java
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FrameLayout rootView = new FrameLayout(this);
        setContentView(rootView);
        sPilotManager.onCreateDelegate(savedInstanceState, rootView, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        sPilotManager.onDestroyDelegate(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        sPilotManager.onSaveInstanceStateDelegate(outState);
    }

    @Override
    public void onBackPressed()
    {
        sPilotManager.onBackPressedDelegate();
    }
```

The first time `Activity.onCreate()` is called it will triggers the `FirstView` to be added to the rootView (declared above), with access to its Presenter (as defined by `@Presenter`) which is pulled (and itself has access to) from the Activitys `PilotStack`.

```java
@Presenter(FirstViewPresenter.class)
public class FirstView extends PresenterBasedFrameLayout<FirstViewPresenter>
{
    ...
    public void someButtonPressed()
    {
        getPresenter().someButtonPressed();
    }
}
```

Example of view action causing a presenter method to be called, which in turn manipulates the application stack directly. This could just as easily have originated from internal logic to the presenter i.e. as the result of some asyncronous operation. This examples pushes a data-frame on the stack also (more on that later). Note how **no** `android.*` classes are involved in this app navigation / state transition.

```java
/**
 * Implement the Presenter/Controller aspect of this whatever way you want
 */ 
public class FirstViewPresenter extends PilotFrame
{
    ...
    /**
     * This represents some app nav action which happens via the parent FatStack.
     * Called by UI in production or called directly in tests. At this point you will generally want to
     *
     * 1) push another UI frame on the stack
     * 2) push a scoped-data frame on the stack and then a UI frame.
     */
    public void someButtonPressed()
    {
        //example of pushing data frame then presenter frame *directly* from presenter
        getParentStack().pushFrame(new SessionScopedData("RandomSessionKey"));
        getParentStack().pushFrame(new SecondInSessionViewPresenter());
    }
}
```

The corresponding view that utilised the newly added Presenter will auto be added to the main rootView (and all other Views removed).

```java
@Presenter(SecondInSessionViewPresenter.class)
public class SecondInSessionView extends PresenterBasedFrameLayout<SecondInSessionViewPresenter>
{
    public void someFuntionality()
    {
        getPresenter().someMethodCall...
    }
}
```

Pretty simple but quite powerful!
