package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.android.uiTypeHandler.StateStackFrameSetRenderer;
import com.kodroid.pilot.lib.statestack.StateStackFrame;
import com.kodroid.pilot.lib.statestack.StateStack;

import java.util.LinkedList;
import java.util.List;

/**
 * This class holds the {@link StateStackFrameSetRenderer} collection that is queried upon {@link StateStack}
 * changes.
 */
public class StateStackRenderer implements StateStack.TopFrameChangedListener
{
    private StateStackFrameSetRenderer[] stateStackFrameSetRenderers;
    private StateStack stateStack;
    private boolean hostActivityStarted;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param stateStackFrameSetRenderers UITypeHandlers passed in here have to have corresponding entries for
     *                       *all* StateStackFrame classes that exist in this project.
     */
    public StateStackRenderer(StateStack stateStack, StateStackFrameSetRenderer... stateStackFrameSetRenderers)
    {
        this.stateStack = stateStack;
        this.stateStackFrameSetRenderers = stateStackFrameSetRenderers;
    }

    //==================================================================//
    // Hosting Activity Visibility Events
    //==================================================================//

    public void hostActivityOnStarted()
    {
        hostActivityStarted = true;
        //notify all frames represented by views being drawn to the screen
        for(StateStackFrame frame : getCurrentlyVisibleFrames(stateStack, false))
            frame.frameViewVisible(true);
    }

    public void hostActivityOnStopped()
    {
        hostActivityStarted= false;
        //notify all frames represented by views being drawn to the screen
        for(StateStackFrame frame : getCurrentlyVisibleFrames(stateStack, false))
            frame.frameViewVisible(false);
    }

    //==================================================================//
    // Frame View visibility checking
    //==================================================================//

    /**
     * Will iterate the stack top down, and return a list of frames that are all represented by
     * Views that are currently being drawn to screen (Views behind not full screen opaque view are
     * treated as visible in this context).
     *
     * Top frame index 0
     *
     * @param stateStack
     * @return
     */
    private List<StateStackFrame> getCurrentlyVisibleFrames(StateStack stateStack, boolean ignoreTop)
    {
        List<StateStackFrame> currentlyVisibleFrames = new LinkedList<>();

        int count = 1; //1 is top of stack for below method call
        if(ignoreTop)
            count++; //2 is second from top of stack for below method call
        while(true)
        {
            StateStackFrame frame = stateStack.getVisibleFrameFromTopDown(count);
            if(frame == null) //EOL
                return currentlyVisibleFrames;
            currentlyVisibleFrames.add(frame);
            if(isFrameOpaque(frame))
                return currentlyVisibleFrames;
            count++;
        }
    }

    private boolean isFrameOpaque(StateStackFrame stateStackFrame)
    {
        //find the typeHandler that handles this frame
        for(StateStackFrameSetRenderer stateStackFrameSetRenderer : stateStackFrameSetRenderers)
        {
            if (stateStackFrameSetRenderer.isFrameSupported(stateStackFrame.getClass()))
            {
                return stateStackFrameSetRenderer.isFrameOpaque(stateStackFrame);
            }
        }

        throw new IllegalStateException("No StateStackFrameSetRenderer registered for StateStackFrame of type "+ stateStackFrame.getClass().getName());
    }

    //==================================================================//
    // Frame Rendering
    //==================================================================//

    /**
     * Will render all passed frames, bottom up. Should be used after a config change.
     */
    public void renderAllCurrentlyVisibleFrames(StateStack stateStack)
    {
        List<StateStackFrame> framesToRender = getCurrentlyVisibleFrames(stateStack, false);
        for(int i = framesToRender.size() - 1; i >= 0; i--)
            topVisibleFrameUpdated(framesToRender.get(i), Direction.FORWARD);
    }

    @Override
    public void topVisibleFrameUpdated(StateStackFrame topVisibleFrame, Direction direction)
    {
        StateStackFrameSetRenderer handlingTypeHandler = null;

        //find the typeHandler that handles this frame
        for(StateStackFrameSetRenderer stateStackFrameSetRenderer : stateStackFrameSetRenderers) {
            if (stateStackFrameSetRenderer.isFrameSupported(topVisibleFrame.getClass()))
            {
                stateStackFrameSetRenderer.renderFrame(topVisibleFrame);
                handlingTypeHandler = stateStackFrameSetRenderer;
                break;
            }
        }

        //throw if nothing to handle this frame
        if(handlingTypeHandler == null)
            throw new IllegalStateException("No StateStackFrameSetRenderer registered for StateStackFrame of type "+topVisibleFrame.getClass().getName());

        //clear all other handlers IF the new frame is opaque
        if(handlingTypeHandler.isFrameOpaque(topVisibleFrame))
        {
            for(StateStackFrameSetRenderer stateStackFrameSetRenderer : stateStackFrameSetRenderers) {
                if (stateStackFrameSetRenderer != handlingTypeHandler) {
                    stateStackFrameSetRenderer.clearAllUI();
                }
            }
        }

        //View Frame visibility callbacks
        //first set all old visible frames to invis
        if(direction == Direction.FORWARD)
        {
            if(!hostActivityStarted)
                return; //i.e. don't bother calling false when added

            //get all the vis frame stack ignoring the top frame and call false
            for(StateStackFrame frame : getCurrentlyVisibleFrames(stateStack, true))
                frame.frameViewVisible(false);

            //call started state on top frame
            topVisibleFrame.frameViewVisible(hostActivityStarted);
        }
        else if(direction == Direction.BACK)
        {
            for(StateStackFrame frame : getCurrentlyVisibleFrames(stateStack, false))
                frame.frameViewVisible(true);
        }
    }
}
