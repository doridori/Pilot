package com.kodroid.pilot.lib.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that can be used for PilotStack integration via composition rather than via Inheritance by
 * using a root PilotActivity.
 *
 * PilotStack is a simple java stack and has no notion of Android, PilotManager is the glue class
 * handling common behaviour between the PilotStack and View/Lifecycle handling
 *
 * This is becoming more like a FragmentManager. Differences are that:
 *
 * - This (mainly) deals with Views instead of Fragments. However any view operation can be triggered by users of this class (fragments / activities etc)
 * - its backstack is more versatile and manipulatable
 * - it is Presenter aware.
 * - Its ;ike FragmentManager crossed with Flow/Morter with a goal of being simpler than each of those projects individually
 * - Don't need to extend any specific Activity to use (unlike FragmentManager)
 * - (see README for more motivations)
 *
 * - Not this classes responsibility to save / restore (integrating Activity can do this is onSaveInstanceState)
 * - Not this classes responsibility to maintain static ref to Stack (integrating Activity can do this)
 * - Not this classes responsibility to add the view to hierarchy - just to pass it back to Activity. However this class will perform checks to look at current view and if matches stack
 *
 * - Is this classes responsibility to make it as simple as poss to start using a pilotStack
 * - Is this classes responsibility to hold the view mappings (static init)
 * - Is this classes responsibility to attach and detach listeners from here->PilotStack
 * - Is this classes responsibility to hold listener for Activity->here for stack / view changes
 * - This class should be supplied with a launch frame
 * - This class should pass on any visible stack element that does not have a view mapping supplied to here to the interating activity
 *
 * This class should be held statically inside any composing Activity. This is because this is a simple
 * way for it to survive config-changes and we can clean it up when we know the composing Activity is
 * being killed for good.
 *
 * This class is intended to hold
 * onto its {@link PilotStack} and therefore any frames on that stack until the composing Activity
 * is destroyed (and onFinish == true).
 *
 * Note as this class is intended to be held around statically forever it should have no mem footprint
 * when destroyed (when finishing == true) and also care should be taken not too leak any Activity /
 * view references to it.
 *
 * If there is an intention to have multiple instances of the same Activity class each with their own
 * stack a static map can be used with keys passed through saved state. However, many apps will not
 * go down this route.
 *
 * Composing Activity needs to delegate through to:
 *
 * - {@link #onCreateDelegate(Bundle, ViewGroup, ActivityDelegate)}
 * - {@link #onDestroyDelegate(Activity)}
 * - {@link #onSaveInstanceStateDelegate(Bundle)}
 * - {@link #onBackPressedDelegate()}
 *
 * //TODO need to be able to specify anims here. Raise github ticket
 */
public class PilotManager implements PilotStack.EventListener
{
    private PilotStack mPilotStack;

    private Map<Class<? extends PilotFrame>, Class<? extends PresenterBasedFrameLayout>> mFrameToViewMappings = new HashMap<>();
    private Class<? extends PilotFrame> mLaunchFrameClass;

    private ViewGroup mRootView;

    private ActivityDelegate mActivityDelegate;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param rootViews All views that are presenter backed and that should be handled by this PilotManager.
     *                  First View in this array will be the launch view and added to the stack on first start.
     */
    public PilotManager(
            Class<? extends PresenterBasedFrameLayout>[] rootViews,
            Class<? extends PilotFrame> launchFrameClass)
    {
        setupRootViewAndPresenterMappings(rootViews);
        mLaunchFrameClass = launchFrameClass;
    }

    //==================================================================//
    // Private
    //==================================================================//

    private void setupRootViewAndPresenterMappings(Class<? extends PresenterBasedFrameLayout>[] rootViews)
    {
        //get view classes that make up the root level of the app
        for(Class<? extends PresenterBasedFrameLayout> viewClass : rootViews)
        {
            if(!PresenterBasedFrameLayout.class.isAssignableFrom(viewClass))
                throw new RuntimeException("Passed class does not extend PresenterBasedFrameLayout:"+viewClass.getCanonicalName());
            Class<? extends PilotFrame> presenterClass = PresenterBasedFrameLayout.getPresenterClass(viewClass);
            mFrameToViewMappings.put(presenterClass, viewClass);
        }
    }

    //==================================================================//
    // Delegate methods
    //==================================================================//

    /**
     * This must be called from your {@link android.app.Activity#onCreate(Bundle)}
     *
     * @param savedInstanceState forward the activity's save state bundle here for auto pilot stack state restoration on process death (only)
     * @param rootView The view that will auto be rendered into for any Presenter-backed-views specified in contructor array
     * @param activityDelegate delegate methods for composing Activity
     */
    public void onCreateDelegate(Bundle savedInstanceState, ViewGroup rootView, ActivityDelegate activityDelegate)
    {
        mRootView = rootView;
        mActivityDelegate = activityDelegate;

        //this should only happen when on first run and if a new instance of this activity is started
        if(mPilotStack == null)
        {
            //check if we need to restore any saves state
            if(savedInstanceState != null && savedInstanceState.containsKey(getStateSaveBundleKey()))
            {
                Log.d(getClass().getCanonicalName(), "Restoring PilotStack!");
                mPilotStack = (PilotStack) savedInstanceState.getSerializable(getStateSaveBundleKey());
            }
            else
            {
                Log.d(getClass().getCanonicalName(), "Creating new PilotStack!");
                mPilotStack = new PilotStack();

                //set launch frame
                try
                {
                    PilotFrame launchFrame = mLaunchFrameClass.getConstructor().newInstance();
                    mPilotStack.pushFrame(launchFrame);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Launch frame cant be instantiated, check this frame has a no-arg constructor: "+mLaunchFrameClass.getCanonicalName(), e);
                }
            }
        }

        //re-hookup event listener
        mPilotStack.setEventListener(this);

        //get the top frame of the stack and visit it - this will ensure that the view displayed matches the top frame.
        final PilotFrame topFrame = mPilotStack.getTopVisibleFrame();
        //manually call the stack listener
        topVisibleFrameUpdated(topFrame, Direction.FORWARD);
    }

    /**
     * This must be called from {@link Activity#onDestroy()}
     *
     * @param activity
     */
    public void onDestroyDelegate(Activity activity)
    {
        //remove listener so callbacks are not triggered when Activity in destroy state
        mPilotStack.deleteEventListener();
        //we do NOT want to keep a ref to this as it will leak the context.
        mRootView = null;
        //nor keep a ref to this
        mActivityDelegate = null;

        //Get rid of the stack if activity is finishing for good. This will not be true if something temp killed in backstack https://github.com/doridori/Pilot/issues/8
        if(activity.isFinishing())
        {
            //todo call clear on stack to allow any explicit data cleanup inside frame callbacks if don't want to wait for JVM
            mPilotStack = null;
        }
    }

    /**
     * This must be called from {@link Activity#onSaveInstanceState(Bundle)}
     */
    public void onSaveInstanceStateDelegate(Bundle outState)
    {
        outState.putSerializable(getStateSaveBundleKey(), mPilotStack);
    }

    /**
     * This must be called from {@link Activity#onBackPressed()}
     */
    public void onBackPressedDelegate()
    {
        mPilotStack.popTopVisibleFrame();
    }

    //==================================================================//
    // Stack Listener methods
    //==================================================================//

    /**
     * Called when {@link PilotStack} events happen in the Stack.
     *
     * If a view is on the top of the stack is for this frame type the new presenter will be set on it
     *
     * @param topVisibleFrame
     * @param direction
     */
    @Override
    @SuppressWarnings("unchecked")
    public final void topVisibleFrameUpdated(PilotFrame topVisibleFrame, PilotStack.EventListener.Direction direction)
    {
        if(mActivityDelegate.interceptTopFrameUpdatedForCategory(topVisibleFrame, direction))
            return;

        if(!mFrameToViewMappings.containsKey(topVisibleFrame.getClass()))
            throw new NullPointerException("No mapping entered for "+ topVisibleFrame.getClass().getCanonicalName()+ ". Have you declared in getRootViewClasses()? Did you mean to intercept this frame event via the ActivityDelegate");
        Class<? extends PresenterBasedFrameLayout> viewClassFrameType = mFrameToViewMappings.get(topVisibleFrame.getClass());
        PresenterBasedFrameLayout view = ensureCurrentViewIsOfType(viewClassFrameType);
        view.setPresenter(view.getPresenterClass().cast(topVisibleFrame));
    }

    @Override
    public final void noVisibleFramesLeft()
    {
        mActivityDelegate.noVisibleFramesLeft();
    }

    //==================================================================//
    // Activity delegate/listener for stack events
    //==================================================================//

    /**
     * Methods that a composing Activity should / can implement
     */
    public interface ActivityDelegate
    {
        /**
         * Composing Activity may want to perform some custom logic here so should pass on this event.
         * This could be showing a fragment / dialog / activity for a particular frame and / or
         * removing a dialog if any other frame becomes top of stack etc. Forward on for flexibility
         * with ability to intercept (i.e. skip the below default view transition code.
         *
         * @param topVisibleFrame
         * @param direction
         * @return return false unless you need to intercept anything
         */
        boolean interceptTopFrameUpdatedForCategory(PilotFrame topVisibleFrame, PilotStack.EventListener.Direction direction);

        /**
         * You probably just want to call {@link Activity#finish()} here.
         */
        void noVisibleFramesLeft();
    }

    //==================================================================//
    // View Handling
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
                T newView = viewClass.getConstructor(Context.class).newInstance(mRootView.getContext());
                setCurrentView(newView);
                return newView;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    //TODO this needs to accept animation input (without falling susceptable to race conditions that can arise from quick succession of stack transitions. Could do this simply by keeping track of the last view that was added and that's the one that is returned in getCurrentView - get with tag?)
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

    //==================================================================//
    // State saving utils
    //==================================================================//

    private String getStateSaveBundleKey()
    {
        return getClass().getCanonicalName();
    }


}
