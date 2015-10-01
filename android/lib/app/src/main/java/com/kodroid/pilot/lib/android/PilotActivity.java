package com.kodroid.pilot.lib.android;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Base Activity which can be used to house a whole application
 *
 * Uses a few unchecked exceptions which are suppressed below. There were compile-time-checked alternatives
 * for some of the design problems but they all resulted in more LOC for any implementing child subclass activities.
 * This should not cause an issue however.
 */
public abstract class PilotActivity extends FragmentActivity implements PilotStack.EventListener
{
    private PilotStack mPilotStack;
    private FrameLayout mRootView;
    private Map<Class<? extends PilotFrame>, Class<? extends PresenterBasedFrameLayout>> mFrameToViewMappings = new HashMap<>();

    //==================================================================//
    // Lifecycle
    //==================================================================//

    @Override
    protected void onStart()
    {
        super.onStart();
        if(mPilotStack == null)
            throw new IllegalStateException("You must call init() in onCreate() after setting the contentView");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //remove listener that would've been added inside onCreate
        mPilotStack.deleteEventListener(this);
    }

    //==================================================================//
    // Abstract methods
    //==================================================================//

    /**
     * @return the first frame that makes up the launch screen of the app
     */
    protected abstract PilotFrame getLaunchPresenterFrame();

    /**
     * @return An array of the root views that make up this application
     */
    protected abstract Class<? extends PresenterBasedFrameLayout>[] getRootViewClasses();

    //==================================================================//
    // Stack init and application definition
    //==================================================================//

    /**
     * Subclasses must call this in onCreate() after setting the contentView
     *
     * @param rootView set the root view the first level presenter backed views to be added to.
     */
    protected final void init(FrameLayout rootView)
    {
        //root view
        if(rootView == null)
            throw new NullPointerException("rootView is null");
        mRootView = rootView;

        setupRootViewAndPresenterMappings();

        //setup the application stack
        boolean freshStart = ensurePilotStackPresent();
        if(freshStart) mPilotStack.pushFrame(getLaunchPresenterFrame());

        //listen for stack UI change events after the first frame has been added so the control flow
        //is the same regardless of if the stack has been dematerialised with content or created new
        mPilotStack.setEventListener(this);

        //get the top frame of the stack and visit it - this will ensure that the view displayed matches the top frame.
        final PilotFrame topFrame = mPilotStack.getTopVisibleFrame();
        //manually call the stack listener for this first frame
        topVisibleFrameUpdated(topFrame, Direction.FORWARD);
    }

    /**
     * This grabs the state stack that survives config changes (but not process death which requires
     * serializing or parceling the stack). The {@link PilotStack} could live in an app wide Singleton
     * but its nice to have it linked to an Activity as it can be GCd when the activity is killed (and no object is retained).
     *
     * @return true if the application is starting fresh
     */
    private boolean ensurePilotStackPresent()
    {
        PilotStack persistedPilotStack = (PilotStack) getLastCustomNonConfigurationInstance();
        boolean freshStart = persistedPilotStack == null;

        if(freshStart)
            mPilotStack = new PilotStack();
        else
            mPilotStack = persistedPilotStack;

        return freshStart;
    }

    private void setupRootViewAndPresenterMappings()
    {
        //get view classes that make up the root level of the app
        for(Class<? extends PresenterBasedFrameLayout> viewClass : getRootViewClasses())
        {
            Class<? extends PilotFrame> presenterClass = PresenterBasedFrameLayout.getPresenterClass(viewClass);
            mFrameToViewMappings.put(presenterClass, viewClass);
        }
    }

    //==================================================================//
    // Retaining
    //==================================================================//

    /**
     * Called between onStop() and onDestroy()
     */
    @Override
    public final PilotStack onRetainCustomNonConfigurationInstance()
    {
        return mPilotStack;
    }

    //==================================================================//
    // Stack Listener methods
    //==================================================================//

    /**
     * Called when {@link PilotStack} events happen in the Stack
     *
     * @param topVisibleFrame
     * @param direction
     */
    @Override
    @SuppressWarnings("unchecked")
    public final void topVisibleFrameUpdated(PilotFrame topVisibleFrame, Direction direction)
    {
        //give subclasses a chance to implement some custom view switching logic for this frame e.. show a fragment or new activity
        if(interceptTopFrameUpdatedForCategory(topVisibleFrame, direction))
            return;
        if(!mFrameToViewMappings.containsKey(topVisibleFrame.getClass()))
            throw new NullPointerException("No mapping entered for "+ topVisibleFrame.getClass().getCanonicalName()+ ". Have you declared in getRootViewClasses()?");
        Class<? extends PresenterBasedFrameLayout> viewClassFrameType = mFrameToViewMappings.get(topVisibleFrame.getClass());
        PresenterBasedFrameLayout view = ensureCurrentViewIsOfType(viewClassFrameType);
        view.setPresenter(view.getPresenterClass().cast(topVisibleFrame));
    }

    /**
     * Allow subclasses to override individual frame changes. Useful as may want to show an external
     * activity / fragment / dialog for a certain frame. Optionally the frame can be passed to this
     * other view type and interacted with as normal.
     *
     * @param topVisibleFrame
     * @param direction
     * @return
     */
    boolean interceptTopFrameUpdatedForCategory(PilotFrame topVisibleFrame, Direction direction)
    {
        return false;
    }

    @Override
    public final void noVisibleFramesLeft()
    {
        finish();
    }

    //==================================================================//
    // On back
    //==================================================================//

    @Override
    public void onBackPressed()
    {
        mPilotStack.popTopVisibleFrame();
    }

    //==================================================================//
    // View adding
    //==================================================================//

    /**
     * Gets the View if its the current view otherwise will make it the current view.
     *
     * @return da view
     */
    @SuppressWarnings("unchecked")
    private <T extends View> T ensureCurrentViewIsOfType(Class<T> viewClass)
    {
        final PresenterBasedFrameLayout currentView = (PresenterBasedFrameLayout) getCurrentView();
        if(currentView != null && currentView.getClass().equals(viewClass))
            return (T) currentView;
        else
        {
            try
            {
                T newView = viewClass.getConstructor(Context.class).newInstance(this);
                setCurrentView(newView);
                return newView;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void setCurrentView(View newView)
    {
        //for now we will do a crude add / remove but could animate etc
        mRootView.removeAllViews();
        mRootView.addView(newView);
    }

    private View getCurrentView()
    {
        //simple as setCurrentView is simple and non-animating (i.e. only ever one view present at a time)
        return mRootView.getChildAt(0);
    }
}
