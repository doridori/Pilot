package com.kodroid.pilot.lib.stack;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
 */
public class PilotStack
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
     * Clears whole stack. If the caller wants to perform any more stack operations after this point
     * then they will need to use the returned PilotStack as the calling
     * {@link PilotFrame#getParentStack()} will be null after this call.
     *
     * @param notifyListeners
     * @return
     */
    public PilotStack clearStack(boolean notifyListeners)
    {
        popAllFramesAboveIndex(0);

        if(notifyListeners && mStackEmptyListener != null)
            mStackEmptyListener.noVisibleFramesLeft();

        return this;
    }

    /**
     * @return top frame of the stack ignoring {@link InvisibleFrame} or null
     */
    public PilotFrame getTopVisibleFrame()
    {
        return getVisibleFrameFromTopDown(1);
    }

    /**
     * Calls {@link #pushFrame(Class, Args)} with null args.
     *
     * @param frameClassToPush
     * @return
     */
    public PilotStack pushFrame(Class<? extends PilotFrame> frameClassToPush)
    {
        return pushFrame(frameClassToPush, null);
    }

    /**
     * If passing args then the PilotFrame class passed need to have a one-arg (Args) constructor.
     * If null is passed then PilotFrame class needs to have one-arg constructor.
     *
     * @param frameClassToPush
     * @param args
     * @return
     */
    public PilotStack pushFrame(Class<? extends PilotFrame> frameClassToPush, Args args)
    {
        PilotFrame frameToPush = createFrame(frameClassToPush, args);

        //put on stack
        frameToPush.setParentStack(this);
        mStack.push(frameToPush);
        frameToPush.pushed();

        if(!isInvisibleFrame(frameToPush))
            notifyListenerVisibleFrameChange(frameToPush, TopFrameChangedListener.Direction.FORWARD);

        return this;
    }

    /**
     * Removes all frames (inc {@link InvisibleFrame} above the 2nd highest visible frame. If only
     * one visible frame exists in the stack the whole stack will be cleared. This is useful to
     * simulate back behaviour.
     */
    public PilotStack popToNextVisibleFrame()
    {
        PilotFrame nextVisibleFrame = getVisibleFrameFromTopDown(2);
        if(nextVisibleFrame == null)
            clearStack(true);
        else
            popAtFrameInstance(nextVisibleFrame, PopType.EXCLUSIVE, true);

        return this;
    }

    /**
     * Use this if popping a specific top frame. Will throw unchecked exception if the passed in
     * frame is not the top of the stack.
     *
     * @param frameToPop not null
     */
    public PilotStack popTopFrameInstance(PilotFrame frameToPop)
    {
        PilotFrame poppedFrame = mStack.pop();
        if(poppedFrame != frameToPop)
            throw new IllegalStateException(frameToPop.getClass().getName()+" instance was not the top of the stack");

        //frame callbacks
        poppedFrame.popped();
        poppedFrame.setParentStack(null);

        notifyListenersNewBackFrame();
        return this;
    }

    /**
     * Use this to remove a frame from the stack. This does not have to be the top frame. Useful as
     * sometimes frames may be dismissed that are not top of the stack.
     *
     * @param frameToRemove if this does not exist in the stack this will throw a {@link RuntimeException}
     */
    public PilotStack removeFrame(PilotFrame frameToRemove)
    {
        if(!mStack.remove(frameToRemove))
            throw new RuntimeException(frameToRemove.getClass().getName()+ " does not exist in the stack");

        frameToRemove.popped();
        frameToRemove.setParentStack(null);

        notifyListenersNewBackFrame();
        return this;
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

    public PilotStack popAtFrameInstance(PilotFrame pilotFrame, PopType popType, boolean notifyListeners)
    {
        for(int i = mStack.size()-1; i >= 0; i--)
        {
            //find the index in the stack that is the class type requested
            if(mStack.get(i) == pilotFrame)
            {
                //account for INCLUSIVE or EXCLUSIVE removal
                int removeFrom = (popType == PopType.INCLUSIVE ? i : i+1);
                boolean removedVisibleFrames = popAllFramesAboveIndex(removeFrom);

                //notify listeners
                if(!notifyListeners)
                    return this;

                if(!removedVisibleFrames) //no Visible frame change
                    return this;

                PilotFrame topVisibleFrame = getTopVisibleFrame();
                if(topVisibleFrame == null && mStackEmptyListener != null)
                    mStackEmptyListener.noVisibleFramesLeft();
                else if(mTopFrameChangedListener != null)
                    mTopFrameChangedListener.topVisibleFrameUpdated(topVisibleFrame, TopFrameChangedListener.Direction.BACK);

                //have found and popped at this point so now return
                return this;
            }
        }

        throw new IllegalStateException("Attempted to pop stack at "+pilotFrame.getClass().getCanonicalName()+" instance but was not found in stack");
    }

    /**
     * Pops everything in the stack above (and including) the passed in class. Will look from the TOP
     * of the stack and perform operation for the first matching {@link PilotFrame} found.
     *
     * @param clazz
     * @param popType
     * @param  {@Link PopType#INCLUSIVE} if should pop the passed frame also, {@Link PopType#EXCLUSIVE} if this frame should become the new top
     * @param notifyListeners true if should notify registered listeners for frame changes
     */
    public PilotStack popAtFrameType(Class<? extends PilotFrame> clazz, PopType popType, boolean notifyListeners)
    {
        for(int i = mStack.size()-1; i >= 0; i--)
        {
            //find the index in the stack that is the class type requested
            if(mStack.get(i).getClass() == clazz)
            {
                //account for INCLUSIVE or EXCLUSIVE removal
                int removeFrom = (popType == PopType.INCLUSIVE ? i : i+1);
                boolean removedVisibleFrames = popAllFramesAboveIndex(removeFrom);

                //notify listeners
                if(!notifyListeners)
                    return this;

                if(!removedVisibleFrames) //no Visible frame change
                    return this;

                PilotFrame topVisibleFrame = getTopVisibleFrame();
                if(topVisibleFrame == null && mStackEmptyListener != null)
                    mStackEmptyListener.noVisibleFramesLeft();
                else if(mTopFrameChangedListener != null)
                    mTopFrameChangedListener.topVisibleFrameUpdated(topVisibleFrame, TopFrameChangedListener.Direction.BACK);

                //have found and popped at this point so now return
                return this;
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

    /**
     * Sanity check
     *
     * @return
     */
    public boolean doesContainVisibleFrame()
    {
        for(PilotFrame pilotFrame : mStack)
        {
            if(!isInvisibleFrame(pilotFrame))
                return true;
        }

        return false;
    }

    public boolean isEmpty()
    {
        return mStack.isEmpty();
    }

    public int getSize()
    {
        return mStack.size();
    }

    //==================================================================//
    // Private methods
    //==================================================================//

    /**
     * Returns the x vis frame from the top of the stack. I.e. 1 = top vis, 2 = 2nd top vis etc
     *
     * @param positionFromTop > 0
     * @return PilotFrame or null is positionFromTop does not exist
     */
    public PilotFrame getVisibleFrameFromTopDown(int positionFromTop)
    {
        final int stackSize = mStack.size();
        int topDownVisCount = 0;
        for(int i = stackSize-1; i >= 0; i--)
        {
            PilotFrame currentFrame = mStack.elementAt(i);
            if(!isInvisibleFrame(currentFrame) && ++topDownVisCount == positionFromTop)
                return currentFrame;
        }
        return null;
    }

    private PilotFrame createFrame(Class<? extends PilotFrame> frameClassToPush, Args args) {
        try
        {
            if(args == null)
                return frameClassToPush.getConstructor().newInstance();
            else
                return frameClassToPush.getConstructor(Args.class).newInstance(args);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(getInovcationExceptionMsg(frameClassToPush, args));
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(getInovcationExceptionMsg(frameClassToPush, args));
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(getInovcationExceptionMsg(frameClassToPush, args));
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(getInovcationExceptionMsg(frameClassToPush, args));
        }
    }

    private String getInovcationExceptionMsg(Class<? extends PilotFrame> frameClassToPush, Args args) throws RuntimeException
    {
        return args == null ?
                frameClassToPush.getName()+" needs to have a no-arg constructor ()" :
                frameClassToPush.getName()+" needs to have a one-arg constructor (Args)";
    }

    private void notifyListenersNewBackFrame()
    {
        PilotFrame nextTopFrame = getTopVisibleFrame();
        if(nextTopFrame != null)
            notifyListenerVisibleFrameChange(nextTopFrame, TopFrameChangedListener.Direction.BACK);
        else
            notifyListenersNoVisibleFrames();
    }

    /**
     * @param index
     * @return true if any of the frames removed were not marked with {@link InvisibleFrame}
     * (therefore a visible frame change took place).
     */
    private boolean popAllFramesAboveIndex(int index)
    {
        boolean visibleFrameRemoved = false;

        while(mStack.size() > index)
        {
            PilotFrame poppedFrame = mStack.pop();
            poppedFrame.popped();
            poppedFrame.setParentStack(null);
            visibleFrameRemoved |= !isInvisibleFrame(poppedFrame); //remove from end as less internal element movement
        }

        return visibleFrameRemoved;
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
