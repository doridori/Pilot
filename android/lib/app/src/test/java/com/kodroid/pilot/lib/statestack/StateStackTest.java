package com.kodroid.pilot.lib.statestack;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class StateStackTest extends TestCase
{
    //[UnitOfWork_StateUnderTest_ExpectedBehavior]

    @Test
    public void classComparison()
    {
        Assert.assertTrue(Object.class == Object.class);
    }

    //==========================================================//
    // getTopFrame()
    //==========================================================//

    @Test
    public void getTopFrame_empty_shouldReturnNull()
    {
        Assert.assertNull(new StateStack().getTopVisibleFrame());
    }

    @Test
    public void getTopFrame_oneFrameSameClass_shouldReturn()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        StateStackFrame testFrame = stateStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopFrame_twoFrameOneUiOneHiddenData_shouldReturnUiFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        StateStackFrame testFrame = stateStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopVisibleFrame_oneFrameHidden_shouldReturnNull()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        StateStackFrame returnedFrame = stateStack.getTopVisibleFrame();
        Assert.assertNull(returnedFrame);
    }

    //==========================================================//
    // pop...()
    //==========================================================//

    @Test
    public void popToNextVisibleFrame_oneFrameSameInstance_shouldReturn()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.popToNextVisibleFrame();
        Assert.assertEquals(0, stateStack.getSize());
    }

    @Test(expected= IllegalStateException.class)
    public void popTopFrameInstance_oneFrameDiffInstance_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.popTopFrameInstance(new TestUIStackFrame1());
    }

    @Test(expected= IllegalStateException.class)
    public void popStackAtFrameType_oneFrameDiffClass_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.popAtFrameType(TestUIStackFrame2.class, StateStack.PopType.INCLUSIVE, false);
    }

    //==========================================================//
    // popToNextVisibleFrame()
    //==========================================================//

    @Test
    public void popToNextVisibleFrame_aboveSingleInivisibleFrame_invisibleFrameShouldPopAlso()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.popToNextVisibleFrame();
        Assert.assertTrue(stateStack.isEmpty());
    }

    @Test
    public void popToNextVisibleFrame_aboveDataAndInvisFrame_shouldPopToVisibleFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        stateStack.pushFrame(new TestUIStackFrame2());
        stateStack.popToNextVisibleFrame();
        Assert.assertTrue(stateStack.getTopVisibleFrame().getClass() == TestUIStackFrame1.class);
        Assert.assertEquals(stateStack.getSize(), 1);
    }

    //==========================================================//
    // getScopedDataFrame()
    //==========================================================//

    @Test
    public void getScopedDateFrame_noDataFramesOnStack_shouldReturnNull()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        StateStackFrame returnedFrame = stateStack.getFrameOfType(TestHiddenDataStackFrame.class);
        Assert.assertNull(returnedFrame);
    }

    @Test
    public void getScopedDateFrame_oneDataFrameOnStack_shouldReturn()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        StateStackFrame returnedFrame = stateStack.getFrameOfType(TestHiddenDataStackFrame.class);
        Assert.assertNotNull(returnedFrame);
    }

    //==========================================================//
    // popAtFrameType()
    //==========================================================//

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleInclusive_listenerCalledWithFirstFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestUIStackFrame2());
        stateStack.pushFrame(new TestUIStackFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIStackFrame2.class, StateStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, stateStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopBottomInclusive_listenerCalledEmpty()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestUIStackFrame2());
        stateStack.pushFrame(new TestUIStackFrame3());

        //add listener
        StateStack.StackEmptyListener mockedListener = Mockito.mock(StateStack.StackEmptyListener.class);
        stateStack.setStackEmptyListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIStackFrame1.class, StateStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(0, stateStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleExclusive_listenerCalledWithSecondFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestUIStackFrame2());
        stateStack.pushFrame(new TestUIStackFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIStackFrame2.class, StateStack.PopType.EXCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame2.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, stateStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopTopExclusive_listenerNotCalledAsNoChanges()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestUIStackFrame2());
        stateStack.pushFrame(new TestUIStackFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIStackFrame3.class, StateStack.PopType.EXCLUSIVE, true);

        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(3, stateStack.getSize());
    }

    //==========================================================//
    // removeThisFrame()
    //==========================================================//

    @Test
    public void removeThisFrame_threeFramesRemoveMiddle_listenerShouldBeRecalledWithTopFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestUIStackFrame2());
        TestUIStackFrame2 testUIFrame2 = (TestUIStackFrame2) stateStack.getTopVisibleFrame();
        stateStack.pushFrame(new TestUIStackFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.removeFrame(testUIFrame2);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame3.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));
        Assert.assertEquals(2, stateStack.getSize());
    }

    //==========================================================//
    // ClearStack
    //==========================================================//

    @Test
    public void clearStack_shouldPop()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrameLifecycleStub());
        stateStack.pushFrame(new TestUIStackFrameLifecycleStub());
        TestUIStackFrameLifecycleStub middleFrame = (TestUIStackFrameLifecycleStub) stateStack.getTopVisibleFrame();
        stateStack.pushFrame(new TestUIStackFrameLifecycleStub());
        stateStack.clearStack(false);

        Assert.assertTrue(middleFrame.popped);
    }

    //==================================================================//
    // Listener Tests
    //==================================================================//

    @Test
    public void pushFrame_pushFirstUiFrame_listenerShouldBeCalled()
    {
        StateStack stateStack = new StateStack();
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.pushFrame(new TestUIStackFrame1());

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.FORWARD));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void pushFrame_pushFirstDataFrame_listenerShouldNotBeCalled()
    {
        StateStack stateStack = new StateStack();
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popFirstFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        //add listener after push
        StateStack.StackEmptyListener mockedListener = Mockito.mock(StateStack.StackEmptyListener.class);
        stateStack.setStackEmptyListener(mockedListener);
        stateStack.popToNextVisibleFrame();
        //verify
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popUiFrameAboveDataFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        stateStack.pushFrame(new TestUIStackFrame1());
        //add listener after push
        StateStack.StackEmptyListener mockedListener = Mockito.mock(StateStack.StackEmptyListener.class);
        stateStack.setStackEmptyListener(mockedListener);
        stateStack.popToNextVisibleFrame();
        //verify
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObject_popUiFrameAboveDataAndUiFrame_listenerShouldBeCalledWithUiFrame()
    {
        StateStack stateStack = new StateStack();

        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        stateStack.pushFrame(new TestUIStackFrame1());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popToNextVisibleFrame();

        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObject_popSecondFrame_listenerShouldBeCalledWithUiFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestUIStackFrame2());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popToNextVisibleFrame();

        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popStackAtFrameType_UiDataUiDataStackPoppingTopUi_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        StateStack stateStack = new StateStack();

        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        stateStack.pushFrame(new TestUIStackFrame2());
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        Assert.assertEquals(4, stateStack.getSize());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popAtFrameType(TestUIStackFrame2.class, StateStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, stateStack.getSize());
    }

    @Test
    public void popAtFrameType_UiDataUiStackPoppingTopData_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        StateStack stateStack = new StateStack();

        stateStack.pushFrame(new TestUIStackFrame1());
        stateStack.pushFrame(new TestHiddenDataStackFrame());
        stateStack.pushFrame(new TestUIStackFrame2());
        Assert.assertEquals(3, stateStack.getSize());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popAtFrameType(TestHiddenDataStackFrame.class, StateStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIStackFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, stateStack.getSize());
    }

    //==================================================================//
    // Push chains and listener callback order
    //==================================================================//
    // See #40

    @Test
    public void pushFrame_firstFramePushesAnotherFrame_listenerShouldReceiveSameOrder()
    {
        final StateStack stateStack = new StateStack();

        //mock
        StateStack.TopFrameChangedListener mockListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockListener);
        InOrder inOrder = Mockito.inOrder(mockListener);

        //test
        stateStack.pushFrame(new ChainedPushStackFrame());
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(ChainedPushStackFrame.class), Matchers.eq(
                StateStack.TopFrameChangedListener.Direction.FORWARD));
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(TestUIStackFrame1.class), Matchers.eq(
                StateStack.TopFrameChangedListener.Direction.FORWARD));
    }

    static class ChainedPushStackFrame extends StateStackFrame
    {
        @Override
        public void pushed()
        {
            getParentStack().pushFrame(new TestUIStackFrame1());
        }
    }

    //==================================================================//
    // Test Frames
    //==================================================================//


    //to test causing trouble
    public static class TestStackFrameNoType extends StateStackFrame
    {}

    public static class TestUIStackFrame1 extends StateStackFrame
    {}

    public static class TestUIStackFrame2 extends StateStackFrame
    {}

    public static class TestUIStackFrame3 extends StateStackFrame
    {}

    //todo spy instead with frame factory
    public static class TestUIStackFrameLifecycleStub extends StateStackFrame
    {
        private boolean popped;

        @Override
        public void popped() {
            popped = true;
        }
    }

    @HiddenStackStackFrame
    public static class TestHiddenDataStackFrame extends StateStackFrame
    {}
}