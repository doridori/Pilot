package com.kodroid.pilot.lib.sync;

import com.kodroid.pilot.lib.android.StateStackRenderer;
import com.kodroid.pilot.lib.android.uiTypeHandler.StateStackFrameSetRenderer;
import com.kodroid.pilot.lib.statestack.StateStackFrame;
import com.kodroid.pilot.lib.statestack.StateStack;
import com.kodroid.pilot.lib.statestack.StateStackTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class PilotUISyncerTest
{
    //==================================================================//
    // Opaque frame tests
    //==================================================================//

    @Test
    public void pilotSyncer_newOpaqueFrameHandledBy2ndTypeHandler_shouldClear1stTypeHandler()
    {
        StateStackFrameSetRenderer stubHandler = new StateStackFrameSetRenderer() {
            @Override
            public boolean isFrameSupported(Class<? extends StateStackFrame> frameClass) {
                return false;
            }

            @Override
            public void renderFrame(StateStackFrame frame) {}

            @Override
            public boolean isFrameOpaque(StateStackFrame frame) {
                return true;
            }

            @Override
            public void clearAllUI() {}
        };

        StateStackFrameSetRenderer opaqueStateStackFrameSetRenderer = new StateStackFrameSetRenderer() {
            @Override
            public boolean isFrameSupported(Class<? extends StateStackFrame> frameClass) {
                return frameClass == StateStackTest.TestUIStackFrame1.class;
            }

            @Override
            public void renderFrame(StateStackFrame frame) {}

            @Override
            public boolean isFrameOpaque(StateStackFrame frame) {
                return true;
            }

            @Override
            public void clearAllUI() {}
        };

        StateStackFrameSetRenderer spyHandler = Mockito.spy(stubHandler);
        StateStackRenderer stateStackRenderer = new StateStackRenderer(new StateStack(), spyHandler,
                                                                       opaqueStateStackFrameSetRenderer);
        stateStackRenderer.topVisibleFrameUpdated(new StateStackTest.TestUIStackFrame1(), StateStack.TopFrameChangedListener.Direction.FORWARD);

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
        stateStack.pushFrame(new StateStackTest.TestUIStackFrame1());
        stateStack.pushFrame(new StateStackTest.TestUIStackFrame2()); //will be opaque in ui handler
        stateStack.pushFrame(new StateStackTest.TestHiddenDataStackFrame());
        stateStack.pushFrame(new StateStackTest.TestUIStackFrame3());

        //handler
        StateStackFrameSetRenderer stateStackFrameSetRenderer = new StateStackFrameSetRenderer()
        {
            @Override
            public boolean isFrameSupported(Class<? extends StateStackFrame> frameClass)
            {
                return true;
            }

            @Override
            public void renderFrame(StateStackFrame frame)
            {
            }

            @Override
            public boolean isFrameOpaque(StateStackFrame frame)
            {
                return frame.getClass() == StateStackTest.TestUIStackFrame2.class; //this one opaque
            }

            @Override
            public void clearAllUI()
            {
            }
        };

        StateStackFrameSetRenderer spy = Mockito.spy(stateStackFrameSetRenderer);

        //syncer
        StateStackRenderer syncer = new StateStackRenderer(stateStack, spy);
        syncer.renderAllCurrentlyVisibleFrames(stateStack);

        //verify
        InOrder inOrder = Mockito.inOrder(spy);
        inOrder.verify(spy, Mockito.times(1)).renderFrame(Mockito.isA(StateStackTest.TestUIStackFrame2.class));
        inOrder.verify(spy, Mockito.times(1)).renderFrame(Mockito.isA(StateStackTest.TestUIStackFrame3.class));
        inOrder.verify(spy, Mockito.never()).renderFrame(Mockito.isA(StateStackTest.TestUIStackFrame1.class));
    }
}