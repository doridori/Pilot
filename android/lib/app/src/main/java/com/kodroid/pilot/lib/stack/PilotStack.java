package com.kodroid.pilot.lib.stack;

import java.io.Serializable;
import java.util.Stack;

/**
 * A simple abstraction of a stack of objects which have:
 *
 * - stack-lifecycle event callbacks inside frames
 * - support for frames that are marked as VISIBLE (default) or {@link InvisibleFrame} (useful for scoped data)
 * - support for slightly more complex stack operations when adding or removing frames
 *
 * plus the stack provides listeners which are notified of VISIBLE frame stack change events. See {@link TopFrameChangedListener}.
 *
 * Not thread safe.
 *
 * //todo test serializing and deserialize
 */
public class PilotStack implements Serializable
{
    private Stack<PilotFrame> mStack = new Stack<>();

    /**
     * stack event listener
     */
    private transient TopFrameChangedListener mTopFrameChangedListener;
    private transient StackEmptyListener mStackEmptyListener;

    //==================================================================//
    // Stack Operations (public)
    //==================================================================//

    /**
     * @return top frame of the stack ignoring {@link InvisibleFrame}
     */
    public PilotFrame getTopVisibleFrame()
    {
        final int stackSize = mStack.size();
        for(int i = stackSize-1; i >= 0; i--)
        {
            PilotFrame currentFrame = mStack.elementAt(i);
            if(!isInvisibleFrame(currentFrame))
                return currentFrame;
        }
        return null;
    }

    public void pushFrame(PilotFrame frameToPush)
    {
        frameToPush.setParentStack(this);
        mStack.push(frameToPush);
        frameToPush.pushed();

        if(!isInvisibleFrame(frameToPush))
            notifyListenerVisibleFrameChange(frameToPush, TopFrameChangedListener.Direction.FORWARD);
    }

    /**
     * Pops the top (non {@link InvisibleFrame}) frame in the stack and will also remove any frame above
     * this index in the stack.
     *
     * If no non {@link InvisibleFrame} frames exist nothing will happen.
     */
    public void popTopVisibleFrame()
    {
        Class<? extends PilotFrame> frameClazz = getTopVisibleFrame().getClass();
        popStackAtFrameType(frameClazz, PopType.INCLUSIVE, true);
    }

    /**
     * Use this if popping a specific top frame. Will throw unchecked exception if the passed in
     * frame is not the top of the stack.
     *
     * @param frameToPop not null
     */
    public void popTopVisibleFrame(PilotFrame frameToPop)
    {
        PilotFrame poppedFrame = mStack.pop();
        if(poppedFrame != frameToPop)
            throw new IllegalStateException(frameToPop.getClass().getName()+" instance was not the top of the stack");

        //frame callbacks
        poppedFrame.popped();
        poppedFrame.setParentStack(null);

        PilotFrame nextTopFrame = getTopVisibleFrame();
        if(nextTopFrame != null)
            notifyListenerVisibleFrameChange(nextTopFrame, TopFrameChangedListener.Direction.BACK);
        else
            notifyListenersNoVisibleFrames();

    }

    /**
     * Flags to indicate if the passed frame should be included in the pop operation
     */
    public enum PopType
    {
        /**
         * Include the passed frame (will be removed from stack)
         */
        INCLUSIVE,
        /**
         * Don't include the past frame (will remain in stack)
         */
        EXCLUSIVE;
    }

    /**
     * Pops everything in the stack above (and including) the passed in class. Will look from the bottom
     * of the stack and perform operation for the first matching {@link PilotFrame} found.
     *
     * @param clazz
     * @param popType
     * @param  {@Link PopType#INCLUSIVE} if should pop the passed frame also, {@Link PopType#EXCLUSIVE} if this frame should become the new top
     * @param notifyListeners true if should notify registered listeners for frame changes
     */
    public void popStackAtFrameType(Class<? extends PilotFrame> clazz, PopType popType, boolean notifyListeners)
    {
        for(int i = 0; i < mStack.size(); i++)
        {
            //find the index in the stack that is the class type requested
            if(mStack.get(i).getClass() == clazz)
            {
                //account for INCLUSIVE or EXCLUSIVE removal
                int removeFrom = (popType == PopType.INCLUSIVE ? i : i+1);
                boolean removedVisibleFrames = removeAllFramesAboveIndex(removeFrom);

                //notify listeners
                if(!notifyListeners)
                    return;

                if(!removedVisibleFrames) //no Visible frame change
                    return;

                PilotFrame topVisibleFrame = getTopVisibleFrame();
                if(topVisibleFrame == null && mStackEmptyListener != null)
                    mStackEmptyListener.noVisibleFramesLeft();
                else if(mTopFrameChangedListener != null)
                    mTopFrameChangedListener.topVisibleFrameUpdated(topVisibleFrame, TopFrameChangedListener.Direction.BACK);

                //have found and popped at this point so now return
                return;
            }
        }

        throw new IllegalStateException("Attempted to pop stack at "+clazz.getCanonicalName()+" but was not found in stack");
    }

    /**
     * Returns the frame of type that exists in the stack. Not defined behaviour if more than one
     * frame of the same type exists in the stack.
     *
     * //TODO defensive approach and optionally only return top of stack?
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends PilotFrame> T getFrameOfType(Class<T> clazz)
    {
        for(PilotFrame pilotFrame : mStack)
        {
            if(pilotFrame.getClass() == clazz)
                return (T) pilotFrame;
        }

        return null;
    }

    //==================================================================//
    // Private methods
    //==================================================================//

    /**
     * @param index
     * @return true if any of the frames removed were not marked with {@link InvisibleFrame}
     * (therefore a visible frame change took place).
     */
    private boolean removeAllFramesAboveIndex(int index)
    {
        boolean visibleFrameRemoved = false;

        while(mStack.size() > index)
            visibleFrameRemoved |= !isInvisibleFrame(mStack.pop()); //remove from end as less internal element movement

        return visibleFrameRemoved;
    }

    int getFrameSize()
    {
        return mStack.size();
    }

    private void notifyListenerVisibleFrameChange(PilotFrame pilotFrame, TopFrameChangedListener.Direction direction)
    {
        if(mTopFrameChangedListener != null)
            mTopFrameChangedListener.topVisibleFrameUpdated(pilotFrame, direction);
    }

    private void notifyListenersNoVisibleFrames()
    {
        if (mStackEmptyListener != null)
            mStackEmptyListener.noVisibleFramesLeft();
    }

    private boolean isInvisibleFrame(PilotFrame pilotFrame)
    {
        return isInvisibleFrame(pilotFrame.getClass());
    }

    private boolean isInvisibleFrame(Class<? extends PilotFrame> clazz)
    {
        return clazz.isAnnotationPresent(InvisibleFrame.class);
    }

    //==================================================================//
    // Event listener
    //==================================================================//

    public void setTopFrameChangedListener(TopFrameChangedListener topFrameChangedListener)
    {
        mTopFrameChangedListener = topFrameChangedListener;
    }

    public void setStackEmptyListener(StackEmptyListener stackEmptyListener)
    {
        mStackEmptyListener = stackEmptyListener;
    }

    public void deleteListeners()
    {
        mTopFrameChangedListener = null;
        mStackEmptyListener = null;
    }

    /**
     * Useful for your controlling Activity to listen to so can show appropriate views
     */
    public interface TopFrameChangedListener
    {
        void topVisibleFrameUpdated(PilotFrame topVisibleFrame, Direction direction);

        enum Direction
        {
            FORWARD, BACK;
        }
    }

    public interface StackEmptyListener
    {
        void noVisibleFramesLeft();
    }



}
