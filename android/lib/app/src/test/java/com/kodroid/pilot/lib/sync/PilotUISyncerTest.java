package com.kodroid.pilot.lib.sync;

import com.kodroid.pilot.lib.android.PilotUISyncer;
import com.kodroid.pilot.lib.android.uiTypeHandler.UITypeHandler;
import com.kodroid.pilot.lib.statestack.StateFrame;
import com.kodroid.pilot.lib.statestack.StateStack;
import com.kodroid.pilot.lib.statestack.StateStackTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class PilotUISyncerTest
{
    //==================================================================//
    // Opaque frame tests
    //==================================================================//

    @Test
    public void pilotSyncer_newOpaqueFrameHandledBy2ndTypeHandler_shouldClear1stTypeHandler()
    {
        UITypeHandler stubHandler = new UITypeHandler() {
            @Override
            public boolean isFrameSupported(Class<? extends StateFrame> frameClass) {
                return false;
            }

            @Override
            public void renderFrame(StateFrame frame) {}

            @Override
            public boolean isFrameOpaque(StateFrame frame) {
                return true;
            }

            @Override
            public void clearAllUI() {}
        };

        UITypeHandler opaqueUiTypeHandler = new UITypeHandler() {
            @Override
            public boolean isFrameSupported(Class<? extends StateFrame> frameClass) {
                return frameClass == StateStackTest.TestUIFrame1.class;
            }

            @Override
            public void renderFrame(StateFrame frame) {}

            @Override
            public boolean isFrameOpaque(StateFrame frame) {
                return true;
            }

            @Override
            public void clearAllUI() {}
        };

        UITypeHandler spyHandler = Mockito.spy(stubHandler);
        PilotUISyncer pilotUISyncer = new PilotUISyncer(new StateStack(), spyHandler, opaqueUiTypeHandler);
        pilotUISyncer.topVisibleFrameUpdated(new StateStackTest.TestUIFrame1(), StateStack.TopFrameChangedListener.Direction.FORWARD);

        Mockito.verify(spyHandler).clearAllUI();
    }

    //==================================================================//
    // Rendering all currently visible frames
    //==================================================================//

    @Test
    public void renderAllCurrentlyVisibleFrames_stackOpaqueInMiddle_shouldAttemptRenderOfAllFramesThatAreNeedingDraw()
    {
        //create mixed stack
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new StateStackTest.TestUIFrame1());
        stateStack.pushFrame(new StateStackTest.TestUIFrame2()); //will be opaque in ui handler
        stateStack.pushFrame(new StateStackTest.TestHiddenDataFrame());
        stateStack.pushFrame(new StateStackTest.TestUIFrame3());

        //handler
        UITypeHandler uiTypeHandler = new UITypeHandler()
        {
            @Override
            public boolean isFrameSupported(Class<? extends StateFrame> frameClass)
            {
                return true;
            }

            @Override
            public void renderFrame(StateFrame frame)
            {
            }

            @Override
            public boolean isFrameOpaque(StateFrame frame)
            {
                return frame.getClass() == StateStackTest.TestUIFrame2.class; //this one opaque
            }

            @Override
            public void clearAllUI()
            {
            }
        };

        UITypeHandler spy = Mockito.spy(uiTypeHandler);

        //syncer
        PilotUISyncer syncer = new PilotUISyncer(stateStack, spy);
        syncer.renderAllCurrentlyVisibleFrames(stateStack);

        //verify
        InOrder inOrder = Mockito.inOrder(spy);
        inOrder.verify(spy, Mockito.times(1)).renderFrame(Mockito.isA(StateStackTest.TestUIFrame2.class));
        inOrder.verify(spy, Mockito.times(1)).renderFrame(Mockito.isA(StateStackTest.TestUIFrame3.class));
        inOrder.verify(spy, Mockito.never()).renderFrame(Mockito.isA(StateStackTest.TestUIFrame1.class));
    }
}