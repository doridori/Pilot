package com.kodroid.pilot.lib.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.kodroid.pilot.lib.statestack.Args;
import com.kodroid.pilot.lib.statestack.StateFrame;
import com.kodroid.pilot.lib.statestack.StateStack;

/**
 * This classes SRP is to bridge between the hosting Activities lifecycle events (and death / recreation) and a longer-lived StateStack instance.
 *
 * onCreate will instantiate the passed launch frame class if the stack is empty.
 *
 * onStart and onStop will route visibility events to the {@link PilotUISyncer}
 *
 * onDestroy will remove listeners attached by this instance
 *
 * Back-press will pop visible stack frames until the stack is empty, which would result in a call to
 * {@link StateStack.StackEmptyListener}.
 *
 * All reactions to the contained {@link StateStack} events (between the delegated onCreate() and onDestroy() lifecycle methods) are
 * handled by a passed {@link StateStack.TopFrameChangedListener}.
 */
public class PilotActivityAdapter
{
    private StateStack stateStack;
    private PilotUISyncer pilotUISyncer;
    private final Class<? extends StateFrame> launchFrameClass;
    private final Args launchFrameArgs;
    private final StateStack.StackEmptyListener stackEmptyListener;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param stateStack the StateStack instance to be managed by this class.
     * @param pilotUISyncer will be added to the backing StateStack and removed in onDestory() (and reference nulled). This will be updated with the current stack state inside this method.
     * @param launchFrameClass The launch frame for the handled StateStack. Will only be created on first creation of the stack. Will be ignored if a stack exists in the savedInstanceState
     * @param launchFrameArgs Args to be passed to the launch frame. Can be null.
     * @param stackEmptyListener to be notified when the stack becomes empty. Integrators will typically want to exit the current Activity at this point.
     */
    public PilotActivityAdapter(
            StateStack stateStack,
            PilotUISyncer pilotUISyncer,
            Class<? extends StateFrame> launchFrameClass,
            Args launchFrameArgs,
            StateStack.StackEmptyListener stackEmptyListener)
    {
        this.stateStack = stateStack;
        this.pilotUISyncer = pilotUISyncer;
        this.launchFrameClass = launchFrameClass;
        this.launchFrameArgs = launchFrameArgs;
        this.stackEmptyListener = stackEmptyListener;
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

     */
    public void onCreateDelegate(Bundle savedInstanceState)
    {
        if(stateStack.isEmpty())
            initializePilotStack(savedInstanceState, launchFrameClass, launchFrameArgs);
        else if(!stateStack.doesContainVisibleFrame())
            throw new IllegalStateException("Trying to initiate UI with a stack that contains no visible frames!");

        //hookup all event listeners to stack
        stateStack.addTopFrameChangedListener(pilotUISyncer);
        stateStack.setStackEmptyListener(stackEmptyListener);

        //render everything that should be currently seen on screen
        pilotUISyncer.renderAllCurrentlyVisibleFrames(stateStack);
    }

    /**
     * Activity must call
     */
    public void onStartDelegate()
    {
        pilotUISyncer.hostActivityOnStarted();
    }

    /**
     * Activity must call
     */
    public void onStopDelegate()
    {
        pilotUISyncer.hostActivityOnStopped();
    }

    /**
     * This must be called from {@link Activity#onDestroy()}
     *
     * @param activity
     */
    public void onDestroyDelegate(Activity activity)
    {
        //remove listeners so callbacks are not triggered when Activity in destroy state
        stateStack.deleteListeners(pilotUISyncer, stackEmptyListener);
    }

    /**
     * This must be called from {@link Activity#onSaveInstanceState(Bundle)}
     */
    public void onSaveInstanceStateDelegate(Bundle outState)
    {
        //TODO #26
    }

    /**
     * This must be called from {@link Activity#onBackPressed()}
     */
    public void onBackPressedDelegate()
    {
        stateStack.popToNextVisibleFrame();
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
     * Ensure a StateStack instance is up and running. This will either be a newly created one (initialised
     * with the passed in <code>launchFrameClass</code> or a restored one via the <code>savedInstanceState</code>
     *
     * @param savedInstanceState
     * @param launchFrameClass
     * @param launchFrameArgs can be null
     */
    private void initializePilotStack(Bundle savedInstanceState, final Class<? extends StateFrame> launchFrameClass, Args launchFrameArgs)
    {
        if(!stateStack.isEmpty())
            throw new IllegalStateException("StateStack already initialized");

        //check if we need to restore any saves state
        if(savedInstanceState != null && savedInstanceState.containsKey(getStateSaveBundleKey()))
        {
            throw new IllegalStateException("Not impl!");
            //Log.d(getClass().getCanonicalName(), "Restoring StateStack!");
            //stateStack = (StateStack) savedInstanceState.getSerializable(getStateSaveBundleKey());
        }
        else
        {
            Log.d(getClass().getCanonicalName(), "StateStack is empty - push launch frame:"+launchFrameClass.getName());

            //set launch frame
            try
            {
                stateStack.pushFrame(launchFrameClass, launchFrameArgs);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Launch frame cant be instantiated, check this frame has a no-arg constructor which calls super(null): "+launchFrameClass.getCanonicalName(), e);
            }
        }
    }



}
