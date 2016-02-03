package com.kodroid.pilot.lib.sync;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class PilotSyncerTest
{
    //==================================================================//
    // Opaque frame tests
    //==================================================================//

    @Test
    public void pilotSyncer_newOpaqueFrameHandledBy2ndTypeHandler_shouldClear1stTypeHandler()
    {
        UITypeHandler stubHandler = new UITypeHandler() {
            @Override
            public boolean onFrame(PilotFrame frame) {
                return false;
            }

            @Override
            public boolean isFrameOpaque(PilotFrame frame) {
                return true;
            }

            @Override
            public void clearAllUI() {

            }
        };

        UITypeHandler opaqueUiTypeHandler = new UITypeHandler() {
            @Override
            public boolean onFrame(PilotFrame frame) {
                return frame instanceof TestUIFrame1;
            }

            @Override
            public boolean isFrameOpaque(PilotFrame frame) {
                return true;
            }

            @Override
            public void clearAllUI() {

            }
        };

        UITypeHandler spyHandler = Mockito.spy(stubHandler);
        PilotSyncer pilotSyncer = new PilotSyncer(spyHandler, opaqueUiTypeHandler);
        pilotSyncer.topVisibleFrameUpdated(new TestUIFrame1(), PilotStack.TopFrameChangedListener.Direction.FORWARD);

        Mockito.verify(spyHandler).clearAllUI();
    }

    //==================================================================//
    // Test frames
    //==================================================================//

    public static class NoArgsPilotFrame extends PilotFrame
    {
        public NoArgsPilotFrame()
        {
            super(null);
        }
    }

    public static class TestUIFrame1 extends NoArgsPilotFrame
    {}

    public static class TestUIFrame2 extends NoArgsPilotFrame
    {}
}