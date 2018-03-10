package com.kodroid.pilot.lib.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.kodroid.pilot.lib.statestack.StateFrame;
import com.kodroid.pilot.lib.statestack.StateStack;

/**
 * This classes SRP is to bridge between the hosting Activities lifecycle events (and death / recreation) and a longer-lived StateStack instance.
 *
 * onCreate will instantiate the passed launch frame class if the stack is empty.
 *
 * onStart and onStop will route visibility events to the {@link StateStackUISyncer}
 *
 * onDestroy will remove listeners attached by this instance
 *
 * Back-press will pop visible stack frames until the stack is empty, which would result in a call to
 * {@link StateStack.StackEmptyListener}.
 *
 * All reactions to the contained {@link StateStack} events (between the delegated onCreate() and onDestroy() lifecycle methods) are
 * handled by a passed {@link StateStack.TopFrameChangedListener}.
 */
public class StateStackActivityAdapter
{
    private StateStack stateStack;
    private StateStackUISyncer stateStackUISyncer;
    private StateFrame launchState;
    private final StateStack.StackEmptyListener stackEmptyListener;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param stateStack the StateStack instance to be managed by this class.
     * @param stateStackUISyncer will be added to the backing StateStack and removed in onDestory() (and reference nulled). This will be updated with the current stack state inside this method.
     * @param launchState
     * @param stackEmptyListener to be notified when the stack becomes empty. Integrators will typically want to exit the current Activity at this point.
     */
    public StateStackActivityAdapter(
            StateStack stateStack,
            StateStackUISyncer stateStackUISyncer,
            StateFrame launchState,
            StateStack.StackEmptyListener stackEmptyListener)
    {
        this.stateStack = stateStack;
        this.stateStackUISyncer = stateStackUISyncer;
        this.launchState = launchState;
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
     */
    public void onCreateDelegate()
    {
        if(stateStack.isEmpty())
            initializePilotStack(launchState);
        else if(!stateStack.doesContainVisibleFrame())
            throw new IllegalStateException("Trying to initiate UI with a stack that contains no visible frames!");

        //hookup all event listeners to stack
        stateStack.addTopFrameChangedListener(stateStackUISyncer);
        stateStack.setStackEmptyListener(stackEmptyListener);

        //render everything that should be currently seen on screen
        stateStackUISyncer.renderAllCurrentlyVisibleFrames(stateStack);
    }

    /**
     * Activity must call
     */
    public void onStartDelegate()
    {
        stateStackUISyncer.hostActivityOnStarted();
    }

    /**
     * Activity must call
     */
    public void onStopDelegate()
    {
        stateStackUISyncer.hostActivityOnStopped();
    }

    /**
     * This must be called from {@link Activity#onDestroy()}
     *
     * @param activity
     */
    public void onDestroyDelegate(Activity activity)
    {
        //remove listeners so callbacks are not triggered when Activity in destroy state
        stateStack.deleteListeners(stateStackUISyncer, stackEmptyListener);
    }

    /**
     * This must be called from {@link Activity#onBackPressed()}
     */
    public void onBackPressedDelegate()
    {
        stateStack.popToNextVisibleFrame();
    }

    //==================================================================//
    // Private
    //==================================================================//

    /**
     * Ensure a StateStack instance is up and running. This will either be a newly created one (initialised
     * with the passed in <code>launchFrameClass</code> or a restored one via the <code>savedInstanceState</code>
     */
    private void initializePilotStack(StateFrame launchState)
    {
        if(!stateStack.isEmpty())
            throw new IllegalStateException("StateStack already initialized");

        Log.d(getClass().getCanonicalName(), "StateStack is empty - push launch frame:"+launchState.getClass().getName());
        stateStack.pushFrame(launchState);
    }
}
