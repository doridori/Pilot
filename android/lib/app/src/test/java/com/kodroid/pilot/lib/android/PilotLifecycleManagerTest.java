package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilot.lib.stack.PilotStackTest;

import org.junit.Test;

public class PilotLifecycleManagerTest
{
    @Test(expected= IllegalStateException.class)
    public void onCreateDelegate_stackWithNoVisibleFrames_shouldThrow()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(PilotStackTest.TestInvisibleDataFrame.class);
        PilotLifecycleManager pilotLifecycleManager = new PilotLifecycleManager(pilotStack, null);
        pilotLifecycleManager.onCreateDelegate(null, null, null);
    }
}