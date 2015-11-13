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
 * This classes SRP is to bridge between the hosting Activities lifecycle events (and death / recreation) and a constant PilotStack instance.
 *
 * All reactions to the contained {@link PilotStack} events (between the delegated onCreate() and onDestroy() lifecycle methods) are
 * handled by a passed {@link com.kodroid.pilot.lib.stack.PilotStack.EventListener}.
 */
public class PilotLifecycleManager
{
    private PilotStack mPilotStack;
    private PilotStack.EventListener mStackEventListener;

    //==================================================================//
    // Delegate methods
    //==================================================================//

    /**
     * This must be called from your {@link Activity#onCreate(Bundle)}
     *
     * @param savedInstanceState forward the activity's save state bundle here for auto pilot stack state restoration on process death (only)
     * @param stackEventListener will be added to the backing PilotStack and removed in onDestory() (and reference nulled). This will be updated with the current stack state inside this method.
     * @param launchFrameClass The launch frame for the handled PilotStack. Will only be created on first creation of the stack.
     */
    public void onCreateDelegate(Bundle savedInstanceState, PilotStack.EventListener stackEventListener, Class<? extends PilotFrame> launchFrameClass)
    {
        //This PilotLifecycleManager instance is designed to be held statically which means the
        //PilotStack should only be null when the Activity is first created or when its been recreated
        //with saved state after process death
        if(mPilotStack == null)
            initializePilotStack(savedInstanceState, launchFrameClass);

        mStackEventListener = stackEventListener;
        //re-hookup event listener for view mngr
        mPilotStack.setEventListener(mStackEventListener);

        //get the top frame of the stack and visit it - this will ensure that the view displayed matches the top frame.
        final PilotFrame topFrame = mPilotStack.getTopVisibleFrame();
        //manually call the stack listener
        mStackEventListener.topVisibleFrameUpdated(topFrame, PilotStack.EventListener.Direction.FORWARD);
    }

    /**
     * This must be called from {@link Activity#onDestroy()}
     *
     * @param activity
     */
    public void onDestroyDelegate(Activity activity)
    {
        //remove listener so callbacks are not triggered when Activity in destroy state
        mStackEventListener = null;
        mPilotStack.deleteEventListener();

        //Get rid of the stack if activity is finishing for good. This will not be true if something temp killed in the backstack https://github.com/doridori/Pilot/issues/8
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
        if(mPilotStack == null)
            throw new IllegalStateException("PilotStack already exists!");

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
                PilotFrame launchFrame = launchFrameClass.getConstructor().newInstance();
                mPilotStack.pushFrame(launchFrame);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Launch frame cant be instantiated, check this frame has a no-arg constructor: "+launchFrameClass.getCanonicalName(), e);
            }
        }
    }



}
