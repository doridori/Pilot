package com.kodroid.pilot.lib.sync;

import com.kodroid.pilot.lib.android.PilotUISyncer;
import com.kodroid.pilot.lib.android.uiTypeHandler.UITypeHandler;
import com.kodroid.pilot.lib.statestack.Args;
import com.kodroid.pilot.lib.statestack.StateFrame;
import com.kodroid.pilot.lib.statestack.StateFrameFactory;
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
        stateStack.pushFrame(StateStackTest.TestUIFrame1.class);
        stateStack.pushFrame(StateStackTest.TestUIFrame2.class); //will be opaque in ui handler
        stateStack.pushFrame(StateStackTest.TestInvisibleDataFrame.class);
        stateStack.pushFrame(StateStackTest.TestUIFrame3.class);

        //handler
        UITypeHandler uiTypeHandler = new UITypeHandler() {
            @Override
            public boolean isFrameSupported(Class<? extends StateFrame> frameClass) {
                return true;
            }

            @Override
            public void renderFrame(StateFrame frame) {}

            @Override
            public boolean isFrameOpaque(StateFrame frame) {
                return frame.getClass() == StateStackTest.TestUIFrame2.class; //this one opaque
            }

            @Override
            public void clearAllUI() {}
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

    @Test
    public void frameViewVisible_testingHostActivityCallbacks_shouldReceiveUntilFramePopped()
    {
        //create mock pilotframe so we can verify behaviour on it
        final List<StateFrame> mock = new ArrayList<>();

        //create with spying factory
        StateStack stateStack = new StateStack(new StateFrameFactory() {
            @Override
            public StateFrame createFrame(Class<? extends StateFrame> frameClassToPush, Args args) {
                mock.add(Mockito.mock(frameClassToPush));
                return mock.get(mock.size()-1);
            }
        });

        //dummy syncer as the host activity events route through here
        PilotUISyncer syncer = new PilotUISyncer(stateStack, new UITypeHandler() {
            @Override
            public boolean isFrameSupported(Class<? extends StateFrame> frameClass) {
                return true;
            }

            @Override
            public void renderFrame(StateFrame frame) {}

            @Override
            public boolean isFrameOpaque(StateFrame frame) {
                return mock.size() == 1 || !frame.equals(mock.get(1)); //only second mock non-opaque
            }

            @Override
            public void clearAllUI() {}
        });

        syncer.hostActivityOnStarted();
        stateStack.addTopFrameChangedListener(syncer);

        //push first opaque frame
        stateStack.pushFrame(StateStackTest.TestUIFrame1.class);
        InOrder inOrder = Mockito.inOrder(mock.get(0));
        inOrder.verify(mock.get(0), Mockito.times(1)).frameViewVisible(true);

        //stopped state
        syncer.hostActivityOnStopped();
        //first frame one false call
        inOrder.verify(mock.get(0), Mockito.times(1)).frameViewVisible(false);

        //push second non-opaque frame
        stateStack.pushFrame(StateStackTest.TestUIFrame2.class);
        InOrder inOrder2 = Mockito.inOrder(mock.get(1));
        //first frame not called again for true
        inOrder.verify(mock.get(0), Mockito.times(0)).frameViewVisible(true);
        //second frame not called as false when added
        inOrder2.verify(mock.get(1), Mockito.times(0)).frameViewVisible(false);

        //started state
        syncer.hostActivityOnStarted();
        //first frame now called again for true
        inOrder.verify(mock.get(0), Mockito.times(1)).frameViewVisible(true);
        //second frame now has new callback for true
        inOrder2.verify(mock.get(1), Mockito.times(1)).frameViewVisible(true);

        //push third opaque frame
        //test that both vis frame below are notified hidden then vis again when top popped
        inOrder.verify(mock.get(0), Mockito.times(0)).frameViewVisible(false);
        inOrder2.verify(mock.get(1), Mockito.times(0)).frameViewVisible(false);
        stateStack.pushFrame(StateStackTest.TestUIFrame3.class);
        inOrder.verify(mock.get(0), Mockito.times(1)).frameViewVisible(false);
        inOrder2.verify(mock.get(1), Mockito.times(1)).frameViewVisible(false);

        //pop third frame
        inOrder.verify(mock.get(0), Mockito.times(0)).frameViewVisible(true);
        inOrder2.verify(mock.get(1), Mockito.times(0)).frameViewVisible(true);
        stateStack.popToNextVisibleFrame();
        inOrder.verify(mock.get(0), Mockito.times(1)).frameViewVisible(true);
        inOrder2.verify(mock.get(1), Mockito.times(1)).frameViewVisible(true);
    }

    //==================================================================//
    // Visibility Lifecycle Tests
    //==================================================================//

//    @Test
//    public void onVisibleFrameStatusChange_addingToTopOfStackAndChangingStackVis_shouldGetVisibleCallbacks()
//    {
//        final StateFrame[] mock = new StateFrame[1];
//
//        //create with spying factory
//        StateStack pilotStack = new StateStack(new StateFrameFactory() {
//            @Override
//            public StateFrame createFrame(Class<? extends StateFrame> frameClassToPush, Args args) {
//                mock[0] = Mockito.mock(frameClassToPush);
//                return mock[0];
//            }
//        });
//        pilotStack.setStackVisible(true);
//
//        //check initial vis state called
//        pilotStack.pushFrame(NoArgsStateFrame.class);
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
//        final StateFrame[] mock = new StateFrame[1];
//
//        //create with spying factory
//        StateStack pilotStack = new StateStack(new StateFrameFactory() {
//            @Override
//            public StateFrame createFrame(Class<? extends StateFrame> frameClassToPush, Args args) {
//                //only mock first frame
//                if(mock[0] != null) return new StateStack.InternalPilotFrameFactory().createFrame(frameClassToPush, args);
//                mock[0] = Mockito.mock(frameClassToPush);
//                return mock[0];
//            }
//        });
//        pilotStack.setStackVisible(true);
//
//        //check initial vis state called
//        pilotStack.pushFrame(NoArgsStateFrame.class);
//        InOrder inOrder = Mockito.inOrder(mock[0]);
//        inOrder.verify(mock[0]).pushed();
//        inOrder.verify(mock[0]).onVisibleFrameStatusChange(true);
//
//        //push another frame on top - mock should get invis callback
//        pilotStack.pushFrame(NoArgsStateFrame.class);
//        inOrder.verify(mock[0]).onVisibleFrameStatusChange(false);
//
//        //remove top frame
//        pilotStack.popToNextVisibleFrame();
//        inOrder.verify(mock[0]).onVisibleFrameStatusChange(true);
//    }
}