package com.kodroid.pilot.lib.statestack;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A simple abstraction of a stack of objects which have:
 *
 * - stack-lifecycle event callbacks inside frames
 * - support for frames that are marked as VISIBLE (default) or {@link HiddenStateStackFrame} (useful for scoped data)
 * - support for slightly more complex stack operations when adding or removing frames
 *
 * plus the stack provides listeners which are notified of VISIBLE frame stack change events. See {@link TopFrameChangedListener}.
 *
 * Not thread safe.
 */
public class StateStack
{
    private Stack<StateStackFrame> stack = new Stack<>();

    private List<StackEmptyListener> stackEmptyListeners = new ArrayList<>();
    private List<TopFrameChangedListener> topFrameChangedListeners = new ArrayList<>();

    //==================================================================//
    // Stack Operations (public)
    //==================================================================//

    /**
     * Clears whole stack. If the caller wants to perform any more stack operations after this point
     * then they will need to use the returned StateStack as the calling
     * {@link StateStackFrame#getParentStack()} will be null after this call.
     *
     * @param notifyListeners
     * @return
     */
    public StateStack clearStack(boolean notifyListeners)
    {
        popAllFramesAboveIndex(0);

        if(!notifyListeners)
            return this;

        notifyListenersNoVisibleFramesLeft();

        return this;
    }

    /**
     * @return top frame of the stack ignoring {@link HiddenStateStackFrame} or null
     */
    public StateStackFrame getTopVisibleFrame()
    {
        return getVisibleFrameFromTopDown(1);
    }

    public StateStack pushFrame(StateStackFrame frameToPush)
    {
        //put on stack
        frameToPush.setParentStack(this);
        stack.push(frameToPush);

        //notify listeners before making the pushed() call below to avoid possible race-condition #40
        if(!isInvisibleFrame(frameToPush))
            notifyListenersTopVisibleFrameUpdated(frameToPush, TopFrameChangedListener.Direction.FORWARD);

        frameToPush.pushed();

        return this;
    }

    /**
     * Removes all frames (inc {@link HiddenStateStackFrame} above the 2nd highest visible frame. If only
     * one visible frame exists in the stack the whole stack will be cleared. This is useful to
     * simulate back behaviour.
     */
    public StateStack popToNextVisibleFrame()
    {
        StateStackFrame nextVisibleFrame = getVisibleFrameFromTopDown(2);
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
    public StateStack popTopFrameInstance(StateStackFrame frameToPop)
    {
        StateStackFrame poppedFrame = stack.pop();
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
    public StateStack removeFrame(StateStackFrame frameToRemove)
    {
        if(!stack.remove(frameToRemove))
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

    public StateStack popAtFrameInstance(StateStackFrame stateStackFrame, PopType popType, boolean notifyListeners)
    {
        for(int i = stack.size()-1; i >= 0; i--)
        {
            //find the index in the stack that is the class type requested
            if(stack.get(i) == stateStackFrame)
            {
                //account for INCLUSIVE or EXCLUSIVE removal
                int removeFrom = (popType == PopType.INCLUSIVE ? i : i+1);
                boolean removedVisibleFrames = popAllFramesAboveIndex(removeFrom);

                //notify listeners
                if(!notifyListeners)
                    return this;

                if(!removedVisibleFrames) //no Visible frame change
                    return this;

                StateStackFrame topVisibleFrame = getTopVisibleFrame();
                if(topVisibleFrame == null)
                {
                    notifyListenersNoVisibleFramesLeft();
                }
                else
                {
                    notifyListenersTopVisibleFrameUpdated(topVisibleFrame, TopFrameChangedListener.Direction.BACK);
                }

                //have found and popped at this point so now return
                return this;
            }
        }

        throw new IllegalStateException("Attempted to pop stack at "+ stateStackFrame.getClass().getCanonicalName()+" instance but was not found in stack");
    }

    /**
     * Pops everything in the stack above (and including) the passed in class. Will look from the TOP
     * of the stack and perform operation for the first matching {@link StateStackFrame} found.
     *
     * @param clazz
     * @param popType
     * @param  {@Link PopType#INCLUSIVE} if should pop the passed frame also, {@Link PopType#EXCLUSIVE} if this frame should become the new top
     * @param notifyListeners true if should notify registered listeners for frame changes
     */
    public StateStack popAtFrameType(Class<? extends StateStackFrame> clazz, PopType popType, boolean notifyListeners)
    {
        for(int i = stack.size()-1; i >= 0; i--)
        {
            //find the index in the stack that is the class type requested
            if(stack.get(i).getClass() == clazz)
            {
                //account for INCLUSIVE or EXCLUSIVE removal
                int removeFrom = (popType == PopType.INCLUSIVE ? i : i+1);
                boolean removedVisibleFrames = popAllFramesAboveIndex(removeFrom);

                //notify listeners
                if(!notifyListeners)
                    return this;

                if(!removedVisibleFrames) //no Visible frame change
                    return this;

                StateStackFrame topVisibleFrame = getTopVisibleFrame();
                if(topVisibleFrame == null)
                    notifyListenersNoVisibleFramesLeft();
                else
                    notifyListenersTopVisibleFrameUpdated(topVisibleFrame, TopFrameChangedListener.Direction.BACK);

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
    public <T extends StateStackFrame> T getFrameOfType(Class<T> clazz)
    {
        for(StateStackFrame stateStackFrame : stack)
        {
            if(stateStackFrame.getClass() == clazz)
                return (T) stateStackFrame;
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
        for(StateStackFrame stateStackFrame : stack)
        {
            if(!isInvisibleFrame(stateStackFrame))
                return true;
        }

        return false;
    }

    public boolean isEmpty()
    {
        return stack.isEmpty();
    }

    public int getSize()
    {
        return stack.size();
    }

    /**
     * Returns the x vis frame from the top of the stack. I.e. 1 = top vis, 2 = 2nd top vis etc
     *
     * @param positionFromTop > 0
     * @return StateStackFrame or null is positionFromTop does not exist
     */
    public StateStackFrame getVisibleFrameFromTopDown(int positionFromTop)
    {
        final int stackSize = stack.size();
        int topDownVisCount = 0;
        for(int i = stackSize-1; i >= 0; i--)
        {
            StateStackFrame currentFrame = stack.elementAt(i);
            if(!isInvisibleFrame(currentFrame))
            {
                topDownVisCount++;
                if(topDownVisCount == positionFromTop)
                    return currentFrame;
            }
        }
        return null;
    }

    //==================================================================//
    // Private methods
    //==================================================================//

    private void notifyListenersTopVisibleFrameUpdated(StateStackFrame topVisibleFrame, TopFrameChangedListener.Direction direction)
    {
        for(TopFrameChangedListener topFrameChangedListener : topFrameChangedListeners)
            topFrameChangedListener.topVisibleFrameUpdated(topVisibleFrame, direction);
    }

    private void notifyListenersNoVisibleFramesLeft()
    {
        for(StackEmptyListener stackEmptyListener : stackEmptyListeners)
            stackEmptyListener.noVisibleFramesLeft();
    }

    private void notifyListenersNewBackFrame()
    {
        StateStackFrame nextTopFrame = getTopVisibleFrame();
        if(nextTopFrame != null)
            notifyListenersTopVisibleFrameUpdated(nextTopFrame, TopFrameChangedListener.Direction.BACK);
        else
            notifyListenersNoVisibleFramesLeft();
    }

    /**
     * @param index
     * @return true if any of the frames removed were not marked with {@link HiddenStateStackFrame}
     * (therefore a visible frame change took place).
     */
    private boolean popAllFramesAboveIndex(int index)
    {
        boolean visibleFrameRemoved = false;

        while(stack.size() > index)
        {
            StateStackFrame poppedFrame = stack.pop();
            poppedFrame.popped();
            poppedFrame.setParentStack(null);
            visibleFrameRemoved |= !isInvisibleFrame(poppedFrame); //remove from end as less internal element movement
        }

        return visibleFrameRemoved;
    }

    private boolean isInvisibleFrame(StateStackFrame stateStackFrame)
    {
        return isInvisibleFrame(stateStackFrame.getClass());
    }

    private boolean isInvisibleFrame(Class<? extends StateStackFrame> clazz)
    {
        return clazz.isAnnotationPresent(HiddenStateStackFrame.class);
    }

    //==================================================================//
    // Logging
    //==================================================================//

    public void printStack()
    {
        Log.i("Pilot", "Printing Stack");

        for(StateStackFrame frame : stack)
        {
            Log.i("Pilot", "-- "+frame.toString());
        }
    }

    //==================================================================//
    // Event listener
    //==================================================================//

    public void addTopFrameChangedListener(TopFrameChangedListener topFrameChangedListener)
    {
        topFrameChangedListeners.add(topFrameChangedListener);
    }

    public void setStackEmptyListener(StackEmptyListener stackEmptyListener)
    {
        stackEmptyListeners.add(stackEmptyListener);
    }

    public void deleteListeners(TopFrameChangedListener topFrameChangedListener, StackEmptyListener stackEmptyListener)
    {
        topFrameChangedListeners.remove(topFrameChangedListener);
        stackEmptyListeners.remove(stackEmptyListener);
    }

    /**
     * Useful for your controlling Activity to listen to so can show appropriate views
     */
    public interface TopFrameChangedListener
    {
        void topVisibleFrameUpdated(StateStackFrame topVisibleFrame, Direction direction);

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
