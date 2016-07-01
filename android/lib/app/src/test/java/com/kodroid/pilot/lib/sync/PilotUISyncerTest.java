package com.kodroid.pilot.lib.sync;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilot.lib.stack.PilotStackTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.InstanceOf;

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
                return frame instanceof PilotStackTest.TestUIFrame1;
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
        PilotUISyncer pilotUISyncer = new PilotUISyncer(new PilotStack(), spyHandler, opaqueUiTypeHandler);
        pilotUISyncer.topVisibleFrameUpdated(new PilotStackTest.TestUIFrame1(), PilotStack.TopFrameChangedListener.Direction.FORWARD);

        Mockito.verify(spyHandler).clearAllUI();
    }

    //==================================================================//
    // Rendering all currently visible frames
    //==================================================================//

    @Test
    public void renderAllCurrentlyVisibleFrames_stackOpaqueInMiddle_shouldAttemptRenderOfAllFramesThatAreNeedingDraw()
    {
        //create mixed stack
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(PilotStackTest.TestUIFrame1.class);
        pilotStack.pushFrame(PilotStackTest.TestUIFrame2.class); //will be opaque in ui handler
        pilotStack.pushFrame(PilotStackTest.TestInvisibleDataFrame.class);
        pilotStack.pushFrame(PilotStackTest.TestUIFrame3.class);

        //handler
        UITypeHandler uiTypeHandler = new UITypeHandler() {
            @Override
            public boolean onFrame(PilotFrame frame) {
                return true; //supports all in test
            }

            @Override
            public boolean isFrameOpaque(PilotFrame frame) {
                return frame.getClass() == PilotStackTest.TestUIFrame2.class; //this one opaque
            }

            @Override
            public void clearAllUI() {

            }
        };

        UITypeHandler spy = Mockito.spy(uiTypeHandler);

        //syncer
        PilotUISyncer syncer = new PilotUISyncer(pilotStack, spy);
        syncer.renderAllCurrentlyVisibleFrames(pilotStack);

        //verify
        InOrder inOrder = Mockito.inOrder(spy);
        inOrder.verify(spy, Mockito.times(1)).onFrame(Mockito.isA(PilotStackTest.TestUIFrame2.class));
        inOrder.verify(spy, Mockito.times(1)).onFrame(Mockito.isA(PilotStackTest.TestUIFrame3.class));
        inOrder.verify(spy, Mockito.never()).onFrame(Mockito.isA(PilotStackTest.TestUIFrame1.class));
    }

    //todo test vis lifecycle callbacks for exiting frames
    //todo test vis lifecycle callbacks for new frames

    //==================================================================//
    // Visibility Lifecycle Tests
    //==================================================================//

//    @Test
//    public void onVisibleFrameStatusChange_addingToTopOfStackAndChangingStackVis_shouldGetVisibleCallbacks()
//    {
//        final PilotFrame[] mock = new PilotFrame[1];
//
//        //create with spying factory
//        PilotStack pilotStack = new PilotStack(new PilotFrameFactory() {
//            @Override
//            public PilotFrame createFrame(Class<? extends PilotFrame> frameClassToPush, Args args) {
//                mock[0] = Mockito.mock(frameClassToPush);
//                return mock[0];
//            }
//        });
//        pilotStack.setStackVisible(true);
//
//        //check initial vis state called
//        pilotStack.pushFrame(NoArgsPilotFrame.class);
//        Mockito.verify(mock[0]).pushed();
//        Mockito.verify(mock[0]).onVisibleFrameStatusChange(true);
//
//        //check change to false called
//        Mockito.verify(mock[0], Mockito.never()).onVisibleFrameStatusChange(false);
//        pilotStack.setStackVisible(false);
//        Mockito.verify(mock[0]).onVisibleFrameStatusChange(false);
//
//        //check popped
//        Mockito.verify(mock[0], Mockito.never()).popped();
//        pilotStack.clearStack(false);
//        Mockito.verify(mock[0]).popped();
//
//        //should ignore visibility changes now
//        pilotStack.setStackVisible(false);
//        //todo check ignored
//    }
//
//    @Test
//    public void onVisibleFrameStatusChange_moveBetweenTopAndBottomOfVisibleStack_shouldReceiveLifecycleCallbacks()
//    {
//        final PilotFrame[] mock = new PilotFrame[1];
//
//        //create with spying factory
//        PilotStack pilotStack = new PilotStack(new PilotFrameFactory() {
//            @Override
//            public PilotFrame createFrame(Class<? extends PilotFrame> frameClassToPush, Args args) {
//                //only mock first frame
//                if(mock[0] != null) return new PilotStack.InternalPilotFrameFactory().createFrame(frameClassToPush, args);
//                mock[0] = Mockito.mock(frameClassToPush);
//                return mock[0];
//            }
//        });
//        pilotStack.setStackVisible(true);
//
//        //check initial vis state called
//        pilotStack.pushFrame(NoArgsPilotFrame.class);
//        InOrder inOrder = Mockito.inOrder(mock[0]);
//        inOrder.verify(mock[0]).pushed();
//        inOrder.verify(mock[0]).onVisibleFrameStatusChange(true);
//
//        //push another frame on top - mock should get invis callback
//        pilotStack.pushFrame(NoArgsPilotFrame.class);
//        inOrder.verify(mock[0]).onVisibleFrameStatusChange(false);
//
//        //remove top frame
//        pilotStack.popToNextVisibleFrame();
//        inOrder.verify(mock[0]).onVisibleFrameStatusChange(true);
//    }
}