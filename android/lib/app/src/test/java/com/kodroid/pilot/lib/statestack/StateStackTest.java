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
        stateStack.pushFrame(new TestUIFrame1());
        StateFrame testFrame = stateStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopFrame_twoFrameOneUiOneHiddenData_shouldReturnUiFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestHiddenDataFrame());
        StateFrame testFrame = stateStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopVisibleFrame_oneFrameHidden_shouldReturnNull()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestHiddenDataFrame());
        StateFrame returnedFrame = stateStack.getTopVisibleFrame();
        Assert.assertNull(returnedFrame);
    }

    //==========================================================//
    // pop...()
    //==========================================================//

    @Test
    public void popToNextVisibleFrame_oneFrameSameInstance_shouldReturn()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.popToNextVisibleFrame();
        Assert.assertEquals(0, stateStack.getSize());
    }

    @Test(expected= IllegalStateException.class)
    public void popTopFrameInstance_oneFrameDiffInstance_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.popTopFrameInstance(new StateStackTest.TestUIFrame1());
    }

    @Test(expected= IllegalStateException.class)
    public void popStackAtFrameType_oneFrameDiffClass_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.popAtFrameType(TestUIFrame2.class, StateStack.PopType.INCLUSIVE, false);
    }

    //==========================================================//
    // popToNextVisibleFrame()
    //==========================================================//

    @Test
    public void popToNextVisibleFrame_aboveSingleInivisibleFrame_invisibleFrameShouldPopAlso()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestHiddenDataFrame());
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.popToNextVisibleFrame();
        Assert.assertTrue(stateStack.isEmpty());
    }

    @Test
    public void popToNextVisibleFrame_aboveDataAndInvisFrame_shouldPopToVisibleFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestHiddenDataFrame());
        stateStack.pushFrame(new TestUIFrame2());
        stateStack.popToNextVisibleFrame();
        Assert.assertTrue(stateStack.getTopVisibleFrame().getClass() == TestUIFrame1.class);
        Assert.assertEquals(stateStack.getSize(), 1);
    }

    //==========================================================//
    // getScopedDataFrame()
    //==========================================================//

    @Test
    public void getScopedDateFrame_noDataFramesOnStack_shouldReturnNull()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        StateFrame returnedFrame = stateStack.getFrameOfType(TestHiddenDataFrame.class);
        Assert.assertNull(returnedFrame);
    }

    @Test
    public void getScopedDateFrame_oneDataFrameOnStack_shouldReturn()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestHiddenDataFrame());
        StateFrame returnedFrame = stateStack.getFrameOfType(TestHiddenDataFrame.class);
        Assert.assertNotNull(returnedFrame);
    }

    //==========================================================//
    // popAtFrameType()
    //==========================================================//

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleInclusive_listenerCalledWithFirstFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestUIFrame2());
        stateStack.pushFrame(new TestUIFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIFrame2.class, StateStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, stateStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopBottomInclusive_listenerCalledEmpty()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestUIFrame2());
        stateStack.pushFrame(new TestUIFrame3());

        //add listener
        StateStack.StackEmptyListener mockedListener = Mockito.mock(StateStack.StackEmptyListener.class);
        stateStack.setStackEmptyListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIFrame1.class, StateStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(0, stateStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleExclusive_listenerCalledWithSecondFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestUIFrame2());
        stateStack.pushFrame(new TestUIFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIFrame2.class, StateStack.PopType.EXCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame2.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, stateStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopTopExclusive_listenerNotCalledAsNoChanges()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestUIFrame2());
        stateStack.pushFrame(new TestUIFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.popAtFrameType(TestUIFrame3.class, StateStack.PopType.EXCLUSIVE, true);

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
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestUIFrame2());
        TestUIFrame2 testUIFrame2 = (TestUIFrame2) stateStack.getTopVisibleFrame();
        stateStack.pushFrame(new TestUIFrame3());

        //add listener
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        stateStack.removeFrame(testUIFrame2);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame3.class),
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
        stateStack.pushFrame(new TestUIFrameLifecycleStub());
        stateStack.pushFrame(new TestUIFrameLifecycleStub());
        TestUIFrameLifecycleStub middleFrame = (TestUIFrameLifecycleStub) stateStack.getTopVisibleFrame();
        stateStack.pushFrame(new TestUIFrameLifecycleStub());
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
        stateStack.pushFrame(new TestUIFrame1());

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.FORWARD));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void pushFrame_pushFirstDataFrame_listenerShouldNotBeCalled()
    {
        StateStack stateStack = new StateStack();
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.pushFrame(new TestHiddenDataFrame());
        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popFirstFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
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
        stateStack.pushFrame(new TestHiddenDataFrame());
        stateStack.pushFrame(new TestUIFrame1());
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

        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestHiddenDataFrame());
        stateStack.pushFrame(new TestUIFrame1());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popToNextVisibleFrame();

        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObject_popSecondFrame_listenerShouldBeCalledWithUiFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestUIFrame2());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popToNextVisibleFrame();

        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popStackAtFrameType_UiDataUiDataStackPoppingTopUi_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        StateStack stateStack = new StateStack();

        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestHiddenDataFrame());
        stateStack.pushFrame(new TestUIFrame2());
        stateStack.pushFrame(new TestHiddenDataFrame());
        Assert.assertEquals(4, stateStack.getSize());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popAtFrameType(TestUIFrame2.class, StateStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, stateStack.getSize());
    }

    @Test
    public void popAtFrameType_UiDataUiStackPoppingTopData_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        StateStack stateStack = new StateStack();

        stateStack.pushFrame(new TestUIFrame1());
        stateStack.pushFrame(new TestHiddenDataFrame());
        stateStack.pushFrame(new TestUIFrame2());
        Assert.assertEquals(3, stateStack.getSize());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popAtFrameType(TestHiddenDataFrame.class, StateStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
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
        stateStack.pushFrame(new ChainedPushFrame());
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(ChainedPushFrame.class), Matchers.eq(
                StateStack.TopFrameChangedListener.Direction.FORWARD));
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(TestUIFrame1.class), Matchers.eq(
                StateStack.TopFrameChangedListener.Direction.FORWARD));
    }

    static class ChainedPushFrame extends StateFrame
    {
        @Override
        public void pushed()
        {
            getParentStack().pushFrame(new TestUIFrame1());
        }
    }

    //==================================================================//
    // Test Frames
    //==================================================================//


    //to test causing trouble
    public static class TestFrameNoType extends StateFrame
    {}

    public static class TestUIFrame1 extends StateFrame
    {}

    public static class TestUIFrame2 extends StateFrame
    {}

    public static class TestUIFrame3 extends StateFrame
    {}

    //todo spy instead with frame factory
    public static class TestUIFrameLifecycleStub extends StateFrame
    {
        private boolean popped;

        @Override
        public void popped() {
            popped = true;
        }
    }

    @HiddenFrame
    public static class TestHiddenDataFrame extends StateFrame
    {}
}