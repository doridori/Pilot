package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.statestack.StateStack;
import com.kodroid.pilot.lib.statestack.StateStackTest;

import org.junit.Test;

public class PilotActivityAdapterTest
{
    @Test(expected= IllegalStateException.class)
    public void onCreateDelegate_stackWithNoVisibleFrames_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new StateStackTest.TestHiddenDataFrame());
        PilotActivityAdapter pilotActivityAdapter = new PilotActivityAdapter(stateStack, null, null, null);
        pilotActivityAdapter.onCreateDelegate();
    }
}