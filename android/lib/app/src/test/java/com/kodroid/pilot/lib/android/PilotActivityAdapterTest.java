package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilot.lib.stack.PilotStackTest;

import org.junit.Test;

public class PilotActivityAdapterTest
{
    @Test(expected= IllegalStateException.class)
    public void onCreateDelegate_stackWithNoVisibleFrames_shouldThrow()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(PilotStackTest.TestInvisibleDataFrame.class);
        PilotActivityAdapter pilotActivityAdapter = new PilotActivityAdapter(pilotStack, null, null, null, null);
        pilotActivityAdapter.onCreateDelegate(null);
    }
}