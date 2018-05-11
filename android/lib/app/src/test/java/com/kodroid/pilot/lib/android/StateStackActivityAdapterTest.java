package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.statestack.StateStack;
import com.kodroid.pilot.lib.statestack.StateStackTest;

import org.junit.Test;

public class StateStackActivityAdapterTest
{
    @Test(expected= IllegalStateException.class)
    public void onCreateDelegate_stackWithNoVisibleFrames_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new StateStackTest.TestHiddenDataStackFrame());
        StateStackActivityAdapter stateStackActivityAdapter = new StateStackActivityAdapter(stateStack, null, null, null);
        stateStackActivityAdapter.onCreateDelegate();
    }
}