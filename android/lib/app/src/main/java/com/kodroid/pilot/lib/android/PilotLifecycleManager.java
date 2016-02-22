package com.kodroid.pilot.lib.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilot.lib.sync.PilotSyncer;

/**
 * This classes SRP is to bridge between the hosting Activities lifecycle events (and death / recreation) and a constant PilotStack instance.
 *
 * All reactions to the contained {@link PilotStack} events (between the delegated onCreate() and onDestroy() lifecycle methods) are
 * handled by a passed {@link PilotStack.TopFrameChangedListener}.
 */
public class PilotLifecycleManager
{
    private final Class<? extends PilotFrame> launchFrameClass;
    private PilotStack pilotStack;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param pilotStack the PilotStack instance to be managed by this class.
     * @param launchFrameClass The launch frame for the handled PilotStack. Will only be created on first creation of the stack.
     */
    public PilotLifecycleManager(PilotStack pilotStack, Class<? extends PilotFrame> launchFrameClass)
    {
        this.pilotStack = pilotStack;
        this.launchFrameClass = launchFrameClass;
    }

    //==================================================================//
    // Delegate methods
    //==================================================================//

    /**
     * This must be called from your {@link Activity#onCreate(Bundle)}.
     *
     * This has to be called *after* setContentView otherwise any Fragments which may be backed by
     * Pilot will not have had a chance to be recreated and therefore will be duplicated.
     *
     * @param savedInstanceState forward the activity's save state bundle here for auto pilot stack state restoration on process death (only)
     * @param pilotSyncer will be added to the backing PilotStack and removed in onDestory() (and reference nulled). This will be updated with the current stack state inside this method.
     * @param stackEmptyListener to be notified when the stack becomes empty. Integrators will typically want to exit the current Activity at this point.
     */
    public void onCreateDelegate(
            Bundle savedInstanceState,
            PilotSyncer pilotSyncer,
            PilotStack.StackEmptyListener stackEmptyListener)
    {
        if(pilotStack.isEmpty())
            initializePilotStack(savedInstanceState, launchFrameClass);
        else if(!pilotStack.doesContainVisibleFrame())
            throw new IllegalStateException("Trying to initiate UI with a stack that contains no visible frames!");

        //re-hookup event listener for view mngr
        pilotStack.setTopFrameChangedListener(pilotSyncer);
        pilotStack.setStackEmptyListener(stackEmptyListener);

        //TODO need to delegate to the Rebuilder at this point https://github.com/doridori/Pilot/issues/5
        //get the top frame of the stack and visit it - this will ensure that the view displayed matches the top frame.
        final PilotFrame topFrame = pilotStack.getTopVisibleFrame();
        //manually call the stack listener
        pilotSyncer.topVisibleFrameUpdated(topFrame, PilotStack.TopFrameChangedListener.Direction.FORWARD);
    }

    /**
     * This must be called from {@link Activity#onDestroy()}
     *
     * @param activity
     */
    public void onDestroyDelegate(Activity activity)
    {
        //remove listener so callbacks are not triggered when Activity in destroy state
        pilotStack.deleteListeners();

        //Get rid of the stack if activity is finishing for good. This will not be true if something temp killed in the backstack https://github.com/doridori/Pilot/issues/8
        if(activity.isFinishing())
        {
            //TODO call clear on stack to allow any explicit data cleanup inside frame callbacks if don't want to wait for JVM
            pilotStack = null;
        }
    }

    /**
     * This must be called from {@link Activity#onSaveInstanceState(Bundle)}
     */
    public void onSaveInstanceStateDelegate(Bundle outState)
    {
        outState.putSerializable(getStateSaveBundleKey(), pilotStack);
    }

    /**
     * This must be called from {@link Activity#onBackPressed()}
     */
    public void onBackPressedDelegate()
    {
        pilotStack.popToNextVisibleFrame();
    }

    //==================================================================//
    // State saving utils
    //==================================================================//

    private String getStateSaveBundleKey()
    {
        return getClass().getCanonicalName();
    }

    //==================================================================//
    // Private
    //==================================================================//

    /**
     * Ensure a PilotStack instance is up and running. This will either be a newly created one (initialised
     * with the passed in <code>launchFrameClass</code> or a restored one via the <code>savedInstanceState</code>
     *
     * @param savedInstanceState
     * @param launchFrameClass
     */
    private void initializePilotStack(Bundle savedInstanceState, final Class<? extends PilotFrame> launchFrameClass)
    {
        if(!pilotStack.isEmpty())
            throw new IllegalStateException("PilotStack already exists!");

        //check if we need to restore any saves state
        if(savedInstanceState != null && savedInstanceState.containsKey(getStateSaveBundleKey()))
        {
            throw new IllegalStateException("Not impl!");
            //Log.d(getClass().getCanonicalName(), "Restoring PilotStack!");
            //pilotStack = (PilotStack) savedInstanceState.getSerializable(getStateSaveBundleKey());
        }
        else
        {
            Log.d(getClass().getCanonicalName(), "Creating new PilotStack!");

            //set launch frame
            try
            {
                pilotStack.pushFrame(launchFrameClass);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Launch frame cant be instantiated, check this frame has a no-arg constructor: "+launchFrameClass.getCanonicalName(), e);
            }
        }
    }



}
