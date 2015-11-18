package com.kodroid.pilot.lib.stack;

import junit.framework.TestCase;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.Serializable;

@RunWith(JUnit4.class)
public class PilotStackTest extends TestCase
{
    //[UnitOfWork_StateUnderTest_ExpectedBehavior]

    //==================================================================//
    // Instance method tests
    //==================================================================//

    @Test
    public void classComparison()
    {
        Assert.assertTrue(Object.class == Object.class);
    }

    public void getTopFrame_empty_shouldThrow()
    {
        Assert.assertNull(new PilotStack().getTopVisibleFrame());
    }

    @Test
    public void getTopFrame_oneFrameSameClass_shouldReturn()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(new TestUIFrame1());
        PilotFrame testFrame = pilotStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopFrame_twoFrameOneUiOneData_shouldReturnUiFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(new TestUIFrame1());
        pilotStack.pushFrame(new TestInvisibleDataFrame());
        PilotFrame testFrame = pilotStack.getTopVisibleFrame();
        Assert.assertNotNull(testFrame);
    }

    @Test
    public void getTopVisibleFrame_oneFrameNotVisible_shouldReturnNull()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(new TestInvisibleDataFrame());
        PilotFrame returnedFrame = pilotStack.getTopVisibleFrame();
        Assert.assertNull(returnedFrame);
    }

    @Test
    public void popTopFrame_oneFrameSameInstance_shouldReturn()
    {
        PilotStack pilotStack = new PilotStack();
        TestUIFrame1 testFrame = new TestUIFrame1();
        pilotStack.pushFrame(testFrame);
        pilotStack.popTopVisibleFrame(testFrame);
        Assert.assertEquals(0, pilotStack.getFrameSize());
    }

    @Test(expected= IllegalStateException.class)
    public void popTopFrame_oneFrameDiffInstance_shouldThrow()
    {
        PilotStack pilotStack = new PilotStack();
        TestUIFrame1 testFrame = new TestUIFrame1();
        pilotStack.pushFrame(testFrame);
        pilotStack.popTopVisibleFrame(new TestUIFrame1());
    }

    @Test(expected= IllegalStateException.class)
    public void popTopFrame_oneFrameDiffClass_shouldThrow()
    {
        PilotStack pilotStack = new PilotStack();
        TestUIFrame1 testFrame = new TestUIFrame1();
        pilotStack.pushFrame(testFrame);
        pilotStack.popTopVisibleFrame(new TestUIFrame2());
    }

    @Test
    public void getScopedDateFrame_noDataFramesOnStack_shouldReturnNull()
    {
        PilotStack pilotStack = new PilotStack();
        TestUIFrame1 testFrame = new TestUIFrame1();
        pilotStack.pushFrame(testFrame);
        PilotFrame returnedFrame = pilotStack.getFrameOfType(TestInvisibleDataFrame.class);
        Assert.assertNull(returnedFrame);
    }

    @Test
    public void getScopedDateFrame_oneDataFrameOnStack_shouldReturnFrame()
    {
        PilotStack pilotStack = new PilotStack();
        TestInvisibleDataFrame testFrame = new TestInvisibleDataFrame();
        pilotStack.pushFrame(testFrame);
        PilotFrame returnedFrame = pilotStack.getFrameOfType(TestInvisibleDataFrame.class);
        Assert.assertEquals(testFrame, returnedFrame);
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleInclusive_listenerCalledWithFirstFrame()
    {
        PilotStack pilotStack = new PilotStack();
        PilotFrame testUIFrame1 = new TestUIFrame1();
        pilotStack.pushFrame(testUIFrame1);
        PilotFrame testUIFrame2 = new TestUIFrame2();
        pilotStack.pushFrame(testUIFrame2);
        pilotStack.pushFrame(new TestUIFrame3());

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.popStackAtFrameType(TestUIFrame2.class, PilotStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testUIFrame1, PilotStack.TopFrameChangedListener.Direction.BACK);
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, pilotStack.getFrameSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopBottomInclusive_listenerCalledEmpty()
    {
        PilotStack pilotStack = new PilotStack();
        PilotFrame testUIFrame1 = new TestUIFrame1();
        pilotStack.pushFrame(testUIFrame1);
        PilotFrame testUIFrame2 = new TestUIFrame2();
        pilotStack.pushFrame(testUIFrame2);
        pilotStack.pushFrame(new TestUIFrame3());

        //add listener
        PilotStack.StackEmptyListener mockedListener = Mockito.mock(PilotStack.StackEmptyListener.class);
        pilotStack.setStackEmptyListener(mockedListener);

        //perform pop
        pilotStack.popStackAtFrameType(TestUIFrame1.class, PilotStack.PopType.INCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(0, pilotStack.getFrameSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopMiddleExclusive_listenerCalledWithSecondFrame()
    {
        PilotStack pilotStack = new PilotStack();
        PilotFrame testUIFrame1 = new TestUIFrame1();
        pilotStack.pushFrame(testUIFrame1);
        PilotFrame testUIFrame2 = new TestUIFrame2();
        pilotStack.pushFrame(testUIFrame2);
        pilotStack.pushFrame(new TestUIFrame3());

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.popStackAtFrameType(TestUIFrame2.class, PilotStack.PopType.EXCLUSIVE, true);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testUIFrame2, PilotStack.TopFrameChangedListener.Direction.BACK);
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, pilotStack.getFrameSize());
    }

    @Test
    public void popStackAtFrameType_threeFramesSameTypePopTopExclusive_listenerNotCalledAsNoChanges()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(new TestUIFrame1());
        pilotStack.pushFrame(new TestUIFrame2());
        PilotFrame testUIFrame3 = new TestUIFrame3();
        pilotStack.pushFrame(testUIFrame3);

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.popStackAtFrameType(TestUIFrame3.class, PilotStack.PopType.EXCLUSIVE, true);

        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(3, pilotStack.getFrameSize());
    }

    @Test
    public void removeThisFrame_threeFramesRemoveMiddle_listenerShouldBeRecalledWithTopFrame()
    {
        PilotStack pilotStack = new PilotStack();
        pilotStack.pushFrame(new TestUIFrame1());
        PilotFrame testUIFrame2 = new TestUIFrame2();
        pilotStack.pushFrame(testUIFrame2);
        PilotFrame testUIFrame3 = new TestUIFrame3();
        pilotStack.pushFrame(testUIFrame3);

        //add listener
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);

        //perform pop
        pilotStack.removeThisFrame(testUIFrame2);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testUIFrame3, PilotStack.TopFrameChangedListener.Direction.BACK);
        Assert.assertEquals(2, pilotStack.getFrameSize());
    }

    //==================================================================//
    // Listener Tests
    //==================================================================//

    @Test
    public void pushFrame_pushFirstUiFrame_listenerShouldBeCalled()
    {
        PilotStack pilotStack = new PilotStack();
        TestUIFrame1 testUIFrame1 = new TestUIFrame1();
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);
        pilotStack.pushFrame(testUIFrame1);

        //verify listener method called
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testUIFrame1, PilotStack.TopFrameChangedListener.Direction.FORWARD);
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void pushFrame_pushFirstDataFrame_listenerShouldNotBeCalled()
    {
        PilotStack pilotStack = new PilotStack();
        PilotFrame testFrame = new TestInvisibleDataFrame();
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);
        pilotStack.pushFrame(testFrame);
        //verify listener method called
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popFirstFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        PilotStack pilotStack = new PilotStack();
        TestUIFrame1 testFrame = new TestUIFrame1();
        pilotStack.pushFrame(testFrame);
        //add listener after push
        PilotStack.StackEmptyListener mockedListener = Mockito.mock(PilotStack.StackEmptyListener.class);
        pilotStack.setStackEmptyListener(mockedListener);
        pilotStack.popTopVisibleFrame(testFrame);
        //verify
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObj_popUiFrameAboveDataFrame_listenerShouldBeCalledWithNoUiFrames()
    {
        PilotStack pilotStack = new PilotStack();
        TestInvisibleDataFrame testInvisibleDataFrame = new TestInvisibleDataFrame();
        TestUIFrame1 testUiFrame1 = new TestUIFrame1();
        pilotStack.pushFrame(testInvisibleDataFrame);
        pilotStack.pushFrame(testUiFrame1);
        //add listener after push
        PilotStack.StackEmptyListener mockedListener = Mockito.mock(PilotStack.StackEmptyListener.class);
        pilotStack.setStackEmptyListener(mockedListener);
        pilotStack.popTopVisibleFrame(testUiFrame1);
        //verify
        Mockito.verify(mockedListener).noVisibleFramesLeft();
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObject_popUiFrameAboveDataAndUiFrame_listenerShouldBeCalledWithUiFrame()
    {
        PilotStack pilotStack = new PilotStack();

        TestInvisibleDataFrame testInvisibleDataFrame = new TestInvisibleDataFrame();
        TestUIFrame1 testUiFrame1 = new TestUIFrame1();
        TestUIFrame1 testUiFrame12 = new TestUIFrame1();

        pilotStack.pushFrame(testUiFrame1);
        pilotStack.pushFrame(testInvisibleDataFrame);
        pilotStack.pushFrame(testUiFrame12);

        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);
        pilotStack.popTopVisibleFrame(testUiFrame12);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testUiFrame1, PilotStack.TopFrameChangedListener.Direction.BACK);
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameObject_popSecondFrame_listenerShouldBeCalledWithUiFrame()
    {
        PilotStack pilotStack = new PilotStack();
        TestUIFrame1 testFrame = new TestUIFrame1();
        pilotStack.pushFrame(testFrame);
        TestUIFrame2 otherTestFrame = new TestUIFrame2();
        pilotStack.pushFrame(otherTestFrame);
        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);
        pilotStack.popTopVisibleFrame(otherTestFrame);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testFrame, PilotStack.TopFrameChangedListener.Direction.BACK);
        Mockito.verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void popTopFrameOfCategory_UiDataUiDataStackPoppingTopUi_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        PilotStack pilotStack = new PilotStack();

        TestInvisibleDataFrame testInvisibleDataFrame = new TestInvisibleDataFrame();
        TestUIFrame1 testUiFrame1 = new TestUIFrame1();
        TestUIFrame2 testUiFrame2 = new TestUIFrame2();

        pilotStack.pushFrame(testUiFrame1);
        pilotStack.pushFrame(testInvisibleDataFrame);
        pilotStack.pushFrame(testUiFrame2);
        pilotStack.pushFrame(testInvisibleDataFrame);
        Assert.assertEquals(4, pilotStack.getFrameSize());

        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);
        pilotStack.popTopVisibleFrame();
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testUiFrame1, PilotStack.TopFrameChangedListener.Direction.BACK);
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(2, pilotStack.getFrameSize());
    }

    @Test
    public void popAtFrameType_UiDataUiStackPoppingTopData_topTwoFramesRemovedListenerShouldBeCalledWithBottomUiFrame()
    {
        PilotStack pilotStack = new PilotStack();

        TestUIFrame1 testUiFrame1 = new TestUIFrame1();
        TestInvisibleDataFrame testInvisibleDataFrame = new TestInvisibleDataFrame();
        TestUIFrame2 testUiFrame2 = new TestUIFrame2();

        pilotStack.pushFrame(testUiFrame1);
        pilotStack.pushFrame(testInvisibleDataFrame);
        pilotStack.pushFrame(testUiFrame2);
        Assert.assertEquals(3, pilotStack.getFrameSize());

        //add listener after push
        PilotStack.TopFrameChangedListener mockedListener = Mockito.mock(PilotStack.TopFrameChangedListener.class);
        pilotStack.setTopFrameChangedListener(mockedListener);
        pilotStack.popStackAtFrameType(testInvisibleDataFrame.getClass(), PilotStack.PopType.INCLUSIVE, true);
        //verify
        Mockito.verify(mockedListener).topVisibleFrameUpdated(testUiFrame1, PilotStack.TopFrameChangedListener.Direction.BACK);
        Mockito.verifyNoMoreInteractions(mockedListener);
        Assert.assertEquals(1, pilotStack.getFrameSize());
    }

    //==================================================================//
    // Serializing Tests
    //==================================================================//

    @Test
    public void serializingStack_callbacksPreservedOnDeserialization_callbackOperationCompleted()
    {
        final PilotStack pilotStack = new PilotStack();

        TestUIFrameThatSetsCallback frameThatSetsCallback = new TestUIFrameThatSetsCallback();
        pilotStack.pushFrame(frameThatSetsCallback);
        frameThatSetsCallback.addNextFrameWithCallback();

        //test serialization flow (would happen as part of saved state bundle)
        byte[] serializedStack = SerializationUtils.serialize(pilotStack);
        PilotStack deserializedStack = (PilotStack) SerializationUtils.deserialize(serializedStack);

        //make callback and check operation is performed
        ((TestUIFrameWithCallback)deserializedStack.getTopVisibleFrame()).callback.done();

        //assert callback operation was completed
        Assert.assertTrue(deserializedStack.getTopVisibleFrame().getClass().getCanonicalName()+" unexpected type", deserializedStack.getTopVisibleFrame().getClass() == TestUIFrame1.class);
    }

    //==================================================================//
    // Test Frames
    //==================================================================//

    //to test causing trouble
    public static class TestFrameNoType extends PilotFrame
    {}

    public static class TestUIFrame1 extends PilotFrame
    {}

    public static class TestUIFrame2 extends PilotFrame
    {}

    public static class TestUIFrame3 extends PilotFrame
    {}

    @InvisibleFrame
    public static class TestInvisibleDataFrame extends PilotFrame
    {}

    /**
     * Used in {@link #serializingStack_callbacksPreservedOnDeserialization_callbackOperationCompleted}
     */
    public static class TestUIFrameThatSetsCallback extends PilotFrame
    {
        void addNextFrameWithCallback()
        {
            TestUIFrameWithCallback frameWithCallback = new TestUIFrameWithCallback(new TestUIFrameWithCallback.Callback()
            {
                @Override
                public void done()
                {
                    //perform some operation we can verify the result of
                    getParentStack().pushFrame(new TestUIFrame1());
                }
            });

            getParentStack().pushFrame(frameWithCallback);
        }
    }

    /**
     * Used in {@link #serializingStack_callbacksPreservedOnDeserialization_callbackOperationCompleted}
     */
    public static class TestUIFrameWithCallback extends PilotFrame
    {
        private Callback callback;

        public TestUIFrameWithCallback(Callback callback)
        {
            this.callback = callback;
        }

        public interface Callback extends Serializable
        {
            void done();
        }
    }


}