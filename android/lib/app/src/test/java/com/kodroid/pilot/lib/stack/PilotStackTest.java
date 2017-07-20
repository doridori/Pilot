package com.kodroid.pilot.lib.stack;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class PilotStackTest extends TestCase
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
        Assert.assertNull(new PilotStack().getTopVisibleFrame());
    }

    @Test
    public void getTopFrame_oneFrameSameClass_shouldReturn()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        PilotFrame testFrame = pilotStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopFrame_twoFrameOneUiOneData_shouldReturnUiFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        PilotFrame testFrame = pilotStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopVisibleFrame_oneFrameNotVisible_shouldReturnNull()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        PilotFrame returnedFrame = pilotStack.getTopVisibleFrame();
        Assert.assertNull(returnedFrame);
    }

    //==========================================================//
    // pop...()
    //==========================================================//

    @Test
    public void popToNextVisibleFrame_oneFrameSameInstance_shouldReturn()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.popToNextVisibleFrame();
        Assert.assertEquals(0, pilotStack.getSize());
    }

    @Test(expected= IllegalStateException.class)
    public void popTopFrameInstance_oneFrameDiffInstance_shouldThrow()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.popTopFrameInstance(new PilotStackTest.TestUIFrame1());
    }

    @Test(expected= IllegalStateException.class)
    public void popStackAtFrameType_oneFrameDiffClass_shouldThrow()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.popAtFrameType(TestUIFrame2.class, PilotStack.PopType.INCLUSIVE, false);
    }

    //==========================================================//
    // popToNextVisibleFrame()
    //==========================================================//

    @Test
    public void popToNextVisibleFrame_aboveSingleInivisibleFrame_invisibleFrameShouldPopAlso()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.popToNextVisibleFrame();
        Assert.assertTrue(pilotStack.isEmpty());
    }

    @Test
    public void popToNextVisibleFrame_aboveDataAndInvisFrame_shouldPopToVisibleFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        pilotStack.popToNextVisibleFrame();
        Assert.assertTrue(pilotStack.getTopVisibleFrame().getClass() == TestUIFrame1.class);
        Assert.assertEquals(pilotStack.getSize(), 1);
    }

    //==========================================================//
    // getScopedDataFrame()
    //==========================================================//

    @Test
    public void getScopedDateFrame_noDataFramesOnStack_shouldReturnNull()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        PilotFrame returnedFrame = pilotStack.getFrameOfType(TestInvisibleDataFrame.class);
        Assert.assertNull(returnedFrame);
    }

    @Test
    public void getScopedDateFrame_oneDataFrameOnStack_shouldReturn()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        PilotFrame returnedFrame = pilotStack.getFrameOfType(TestInvisibleDataFrame.class);
        Assert.assertNotNull(returnedFrame);
    }

    //==========================================================//
    // popAtFrameType()
    //==========================================================//

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleInclusive_listenerCalledWithFirstFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        pilotStack.pushFrame(TestUIFrame3.class);

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.popAtFrameType(TestUIFrame2.class, PilotStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, pilotStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopBottomInclusive_listenerCalledEmpty()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        pilotStack.pushFrame(TestUIFrame3.class);

        //add listener
        PilotStack.StackEmptyListener mockedListener = Mockito.mock(PilotStack.StackEmptyListener.class);
        pilotStack.setStackEmptyListener(mockedListener);

        //perform pop
        pilotStack.popAtFrameType(TestUIFrame1.class, PilotStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(0, pilotStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleExclusive_listenerCalledWithSecondFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        pilotStack.pushFrame(TestUIFrame3.class);

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.popAtFrameType(TestUIFrame2.class, PilotStack.PopType.EXCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame2.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, pilotStack.getSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopTopExclusive_listenerNotCalledAsNoChanges()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        pilotStack.pushFrame(TestUIFrame3.class);

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.popAtFrameType(TestUIFrame3.class, PilotStack.PopType.EXCLUSIVE, true);

        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(3, pilotStack.getSize());
    }

    //==========================================================//
    // removeThisFrame()
    //==========================================================//

    @Test
    public void removeThisFrame_threeFramesRemoveMiddle_listenerShouldBeRecalledWithTopFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        TestUIFrame2 testUIFrame2 = (TestUIFrame2) pilotStack.getTopVisibleFrame();
        pilotStack.pushFrame(TestUIFrame3.class);

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.removeFrame(testUIFrame2);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame3.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.BACK));
        Assert.assertEquals(2, pilotStack.getSize());
    }

    //==========================================================//
    // ClearStack
    //==========================================================//

    @Test
    public void clearStack_shouldPop()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrameLifecycleStub.class);
        pilotStack.pushFrame(TestUIFrameLifecycleStub.class);
        TestUIFrameLifecycleStub middleFrame = (TestUIFrameLifecycleStub) pilotStack.getTopVisibleFrame();
        pilotStack.pushFrame(TestUIFrameLifecycleStub.class);
        pilotStack.clearStack(false);

        Assert.assertTrue(middleFrame.popped);
    }

    //==================================================================//
    // Listener Tests
    //==================================================================//

    @Test
    public void pushFrame_pushFirstUiFrame_listenerShouldBeCalled()
    {
        PilotStack pilotStack = new PilotStack();
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);
        pilotStack.pushFrame(TestUIFrame1.class);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.FORWARD));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void pushFrame_pushFirstDataFrame_listenerShouldNotBeCalled()
    {
        PilotStack pilotStack = new PilotStack();
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popFirstFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        //add listener after push
        PilotStack.StackEmptyListener mockedListener = Mockito.mock(PilotStack.StackEmptyListener.class);
        pilotStack.setStackEmptyListener(mockedListener);
        pilotStack.popToNextVisibleFrame();
        //verify
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popUiFrameAboveDataFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        pilotStack.pushFrame(TestUIFrame1.class);
        //add listener after push
        PilotStack.StackEmptyListener mockedListener = Mockito.mock(PilotStack.StackEmptyListener.class);
        pilotStack.setStackEmptyListener(mockedListener);
        pilotStack.popToNextVisibleFrame();
        //verify
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObject_popUiFrameAboveDataAndUiFrame_listenerShouldBeCalledWithUiFrame()
    {
        PilotStack pilotStack = new PilotStack();

        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        pilotStack.pushFrame(TestUIFrame1.class);

        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);
        pilotStack.popToNextVisibleFrame();

        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObject_popSecondFrame_listenerShouldBeCalledWithUiFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestUIFrame2.class);

        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);
        pilotStack.popToNextVisibleFrame();

        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popStackAtFrameType_UiDataUiDataStackPoppingTopUi_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        PilotStack pilotStack = new PilotStack();

        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        Assert.assertEquals(4, pilotStack.getSize());

        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);
        pilotStack.popAtFrameType(TestUIFrame2.class, PilotStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, pilotStack.getSize());
    }

    @Test
    public void popAtFrameType_UiDataUiStackPoppingTopData_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        PilotStack pilotStack = new PilotStack();

        pilotStack.pushFrame(TestUIFrame1.class);
        pilotStack.pushFrame(TestInvisibleDataFrame.class);
        pilotStack.pushFrame(TestUIFrame2.class);
        Assert.assertEquals(3, pilotStack.getSize());

        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockedListener);
        pilotStack.popAtFrameType(TestInvisibleDataFrame.class, PilotStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(
                Matchers.isA(TestUIFrame1.class),
                Matchers.eq(PilotStack.TopFrameChangedListener.Direction.BACK));

        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, pilotStack.getSize());
    }

    //==================================================================//
    // Push Stack Constructor Arsg Invocation Tests
    //==================================================================//

    @Test
    public void pushFrame_notPassingArgs_shouldBeFineWithNoArgsConstructor()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(NoArgsPilotFrame.class);
    }

    @Test
    public void pushFrame_passingArgs_shouldBeFineWithArgsConstructor()
    {
        PilotStack pilotStack = new PilotStack();
        Args args = new Args();
        pilotStack.pushFrame(ArgsPilotFrame.class, args);
        Assert.assertEquals(args, ((ArgsPilotFrame)pilotStack.getTopVisibleFrame()).args);
    }

    @Test(expected = RuntimeException.class)
    public void pushFrame_passingArgs_shouldFailAsOnlyNoArgsConstructor()
    {
        PilotStack pilotStack = new PilotStack();
        Args args = new Args();
        pilotStack.pushFrame(NoArgsPilotFrame.class, args);
    }

    @Test(expected = RuntimeException.class)
    public void pushFrame_notPassingArgs_shouldFailAsOnlyArgsConstructor()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(ArgsPilotFrame.class);
    }

    //==================================================================//
    // Push chains and listener callback order
    //==================================================================//
    // See #40

    @Test
    public void pushFrame_firstFramePushesAnotherFrame_listenerShouldReceiveSameOrder()
    {
        final PilotStack pilotStack = new PilotStack();

        //mock
        PilotStack.TopFrameChangedListener mockListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.addTopFrameChangedListener(mockListener);
        InOrder inOrder = Mockito.inOrder(mockListener);

        //test
        pilotStack.pushFrame(PushingFrame.class);
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(PushingFrame.class), Matchers.eq(PilotStack.TopFrameChangedListener.Direction.FORWARD));
        inOrder.verify(mockListener).topVisibleFrameUpdated(Matchers.isA(TestUIFrame1.class), Matchers.eq(PilotStack.TopFrameChangedListener.Direction.FORWARD));
    }

    static class PushingFrame extends PilotFrame
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

    public static class ArgsPilotFrame extends PilotFrame
    {
        private Args args;

        public ArgsPilotFrame(Args args) {
            super(args);
            this.args = args;
        }

        public Args getArgsTest() {
            return args;
        }
    }

    public static class NoArgsPilotFrame extends PilotFrame
    {
        public NoArgsPilotFrame()
        {
            super(null);
        }
    }

    //to test causing trouble
    public static class TestFrameNoType extends NoArgsPilotFrame
    {}

    public static class TestUIFrame1 extends NoArgsPilotFrame
    {}

    public static class TestUIFrame2 extends NoArgsPilotFrame
    {}

    public static class TestUIFrame3 extends NoArgsPilotFrame
    {}

    //todo spy instead with frame factory
    public static class TestUIFrameLifecycleStub extends NoArgsPilotFrame
    {
        private boolean popped;

        @Override
        public void popped() {
            popped = true;
        }
    }

    @InvisibleFrame
    public static class TestInvisibleDataFrame extends NoArgsPilotFrame
    {}
}