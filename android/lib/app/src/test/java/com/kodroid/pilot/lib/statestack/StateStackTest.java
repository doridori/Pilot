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
        stateStack.pushFrame(TestUIFrame1.class);
        StateFrame testFrame = stateStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopFrame_twoFrameOneUiOneData_shouldReturnUiFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        StateFrame testFrame = stateStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopVisibleFrame_oneFrameNotVisible_shouldReturnNull()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestInvisibleDataFrame.class);
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
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.popToNextVisibleFrame();
        Assert.assertEquals(0, stateStack.getSize());
    }

    @Test(expected= IllegalStateException.class)
    public void popTopFrameInstance_oneFrameDiffInstance_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.popTopFrameInstance(new StateStackTest.TestUIFrame1());
    }

    @Test(expected= IllegalStateException.class)
    public void popStackAtFrameType_oneFrameDiffClass_shouldThrow()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.popAtFrameType(TestUIFrame2.class, StateStack.PopType.INCLUSIVE, false);
    }

    //==========================================================//
    // popToNextVisibleFrame()
    //==========================================================//

    @Test
    public void popToNextVisibleFrame_aboveSingleInivisibleFrame_invisibleFrameShouldPopAlso()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.popToNextVisibleFrame();
        Assert.assertTrue(stateStack.isEmpty());
    }

    @Test
    public void popToNextVisibleFrame_aboveDataAndInvisFrame_shouldPopToVisibleFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        stateStack.pushFrame(TestUIFrame2.class);
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
        stateStack.pushFrame(TestUIFrame1.class);
        StateFrame returnedFrame = stateStack.getFrameOfType(TestInvisibleDataFrame.class);
        Assert.assertNull(returnedFrame);
    }

    @Test
    public void getScopedDateFrame_oneDataFrameOnStack_shouldReturn()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        StateFrame returnedFrame = stateStack.getFrameOfType(TestInvisibleDataFrame.class);
        Assert.assertNotNull(returnedFrame);
    }

    //==========================================================//
    // popAtFrameType()
    //==========================================================//

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleInclusive_listenerCalledWithFirstFrame()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestUIFrame2.class);
        stateStack.pushFrame(TestUIFrame3.class);

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
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestUIFrame2.class);
        stateStack.pushFrame(TestUIFrame3.class);

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
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestUIFrame2.class);
        stateStack.pushFrame(TestUIFrame3.class);

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
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestUIFrame2.class);
        stateStack.pushFrame(TestUIFrame3.class);

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
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestUIFrame2.class);
        TestUIFrame2 testUIFrame2 = (TestUIFrame2) stateStack.getTopVisibleFrame();
        stateStack.pushFrame(TestUIFrame3.class);

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
        stateStack.pushFrame(TestUIFrameLifecycleStub.class);
        stateStack.pushFrame(TestUIFrameLifecycleStub.class);
        TestUIFrameLifecycleStub middleFrame = (TestUIFrameLifecycleStub) stateStack.getTopVisibleFrame();
        stateStack.pushFrame(TestUIFrameLifecycleStub.class);
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
        stateStack.pushFrame(TestUIFrame1.class);

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
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popFirstFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(TestUIFrame1.class);
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
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        stateStack.pushFrame(TestUIFrame1.class);
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

        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        stateStack.pushFrame(TestUIFrame1.class);

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
        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestUIFrame2.class);

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

        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        stateStack.pushFrame(TestUIFrame2.class);
        stateStack.pushFrame(TestInvisibleDataFrame.class);
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

        stateStack.pushFrame(TestUIFrame1.class);
        stateStack.pushFrame(TestInvisibleDataFrame.class);
        stateStack.pushFrame(TestUIFrame2.class);
        Assert.assertEquals(3, stateStack.getSize());

        //add listener after push
        StateStack.TopFrameChangedListener mockedListener = Mockito.mock(StateStack.TopFrameChangedListener.class);
        stateStack.addTopFrameChangedListener(mockedListener);
        stateStack.popAtFrameType(TestInvisibleDataFrame.class, StateStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(StateStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, stateStack.getSize());
    }

    //==================================================================//
    // Push Stack Constructor Arsg Invocation Tests
    //==================================================================//

    @Test
    public void pushFrame_notPassingArgs_shouldBeFineWithNoArgsConstructor()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(NoArgsStateFrame.class);
    }

    @Test
    public void pushFrame_passingArgs_shouldBeFineWithArgsConstructor()
    {
        StateStack stateStack = new StateStack();
        Args args = new Args();
        stateStack.pushFrame(ArgsStateFrame.class, args);
        Assert.assertEquals(args, ((ArgsStateFrame) stateStack.getTopVisibleFrame()).args);
    }

    @Test(expected = RuntimeException.class)
    public void pushFrame_passingArgs_shouldFailAsOnlyNoArgsConstructor()
    {
        StateStack stateStack = new StateStack();
        Args args = new Args();
        stateStack.pushFrame(NoArgsStateFrame.class, args);
    }

    @Test(expected = RuntimeException.class)
    public void pushFrame_notPassingArgs_shouldFailAsOnlyArgsConstructor()
    {
        StateStack stateStack = new StateStack();
        stateStack.pushFrame(ArgsStateFrame.class);
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
        stateStack.pushFrame(PushingFrame.class);
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(PushingFrame.class), Matchers.eq(
                StateStack.TopFrameChangedListener.Direction.FORWARD));
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(TestUIFrame1.class), Matchers.eq(
                StateStack.TopFrameChangedListener.Direction.FORWARD));
    }

    static class PushingFrame extends StateFrame
    {
        public PushingFrame()
        {
            super(null);
        }

        @Override
        public void pushed()
        {
            getParentStack().pushFrame(TestUIFrame1.class);
        }
    }

    //==================================================================//
    // Serializing Tests
    //==================================================================//

    //TODO

    //==================================================================//
    // Test Frames
    //==================================================================//

    public static class ArgsStateFrame extends StateFrame
    {
        private Args args;

        public ArgsStateFrame(Args args) {
            super(args);
            this.args = args;
        }

        public Args getArgsTest() {
            return args;
        }
    }

    public static class NoArgsStateFrame extends StateFrame
    {
        public NoArgsStateFrame()
        {
            super(null);
        }
    }

    //to test causing trouble
    public static class TestFrameNoType extends NoArgsStateFrame
    {}

    public static class TestUIFrame1 extends NoArgsStateFrame
    {}

    public static class TestUIFrame2 extends NoArgsStateFrame
    {}

    public static class TestUIFrame3 extends NoArgsStateFrame
    {}

    //todo spy instead with frame factory
    public static class TestUIFrameLifecycleStub extends NoArgsStateFrame
    {
        private boolean popped;

        @Override
        public void popped() {
            popped = true;
        }
    }

    @HiddenFrame
    public static class TestInvisibleDataFrame extends NoArgsStateFrame
    {}
}