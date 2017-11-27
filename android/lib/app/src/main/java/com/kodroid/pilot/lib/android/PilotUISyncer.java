package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.android.uiTypeHandler.UITypeHandler;
import com.kodroid.pilot.lib.statestack.StateFrame;
import com.kodroid.pilot.lib.statestack.StateStack;

import java.util.LinkedList;
import java.util.List;

/**
 * This class holds the {@link UITypeHandler} collection that is queried upon {@link StateStack}
 * changes.
 */
public class PilotUISyncer implements StateStack.TopFrameChangedListener
{
    private UITypeHandler[] uiTypeHandlers;
    private StateStack stateStack;
    private boolean hostActivityStarted;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param uiTypeHandlers UITypeHandlers passed in here have to have corresponding entries for
     *                       *all* StateFrame classes that exist in this project.
     */
    public PilotUISyncer(StateStack stateStack, UITypeHandler... uiTypeHandlers)
    {
        this.stateStack = stateStack;
        this.uiTypeHandlers = uiTypeHandlers;
    }

    //==================================================================//
    // Hosting Activity Visibility Events
    //==================================================================//

    public void hostActivityOnStarted()
    {
        hostActivityStarted = true;
        //notify all frames represented by views being drawn to the screen
        for(StateFrame frame : getCurrentlyVisibleFrames(stateStack, false))
            frame.frameViewVisible(true);
    }

    public void hostActivityOnStopped()
    {
        hostActivityStarted= false;
        //notify all frames represented by views being drawn to the screen
        for(StateFrame frame : getCurrentlyVisibleFrames(stateStack, false))
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
    private List<StateFrame> getCurrentlyVisibleFrames(StateStack stateStack, boolean ignoreTop)
    {
        List<StateFrame> currentlyVisibleFrames = new LinkedList<>();

        int count = 1; //1 is top of stack for below method call
        if(ignoreTop)
            count++; //2 is second from top of stack for below method call
        while(true)
        {
            StateFrame frame = stateStack.getVisibleFrameFromTopDown(count);
            if(frame == null) //EOL
                return currentlyVisibleFrames;
            currentlyVisibleFrames.add(frame);
            if(isFrameOpaque(frame))
                return currentlyVisibleFrames;
            count++;
        }
    }

    private boolean isFrameOpaque(StateFrame stateFrame)
    {
        //find the typeHandler that handles this frame
        for(UITypeHandler uiTypeHandler : uiTypeHandlers)
        {
            if (uiTypeHandler.isFrameSupported(stateFrame.getClass()))
            {
                return uiTypeHandler.isFrameOpaque(stateFrame);
            }
        }

        throw new IllegalStateException("No UITypeHandler registered for StateFrame of type "+ stateFrame.getClass().getName());
    }

    //==================================================================//
    // Frame Rendering
    //==================================================================//

    /**
     * Will render all passed frames, bottom up. Should be used after a config change.
     */
    public void renderAllCurrentlyVisibleFrames(StateStack stateStack)
    {
        List<StateFrame> framesToRender = getCurrentlyVisibleFrames(stateStack, false);
        for(int i = framesToRender.size() - 1; i >= 0; i--)
            topVisibleFrameUpdated(framesToRender.get(i), Direction.FORWARD);
    }

    @Override
    public void topVisibleFrameUpdated(StateFrame topVisibleFrame, Direction direction)
    {
        UITypeHandler handlingTypeHandler = null;

        //find the typeHandler that handles this frame
        for(UITypeHandler uiTypeHandler : uiTypeHandlers) {
            if (uiTypeHandler.isFrameSupported(topVisibleFrame.getClass()))
            {
                uiTypeHandler.renderFrame(topVisibleFrame);
                handlingTypeHandler = uiTypeHandler;
                break;
            }
        }

        //throw if nothing to handle this frame
        if(handlingTypeHandler == null)
            throw new IllegalStateException("No UITypeHandler registered for StateFrame of type "+topVisibleFrame.getClass().getName());

        //clear all other handlers IF the new frame is opaque
        if(handlingTypeHandler.isFrameOpaque(topVisibleFrame))
        {
            for(UITypeHandler uiTypeHandler : uiTypeHandlers) {
                if (uiTypeHandler != handlingTypeHandler) {
                    uiTypeHandler.clearAllUI();
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
            for(StateFrame frame : getCurrentlyVisibleFrames(stateStack, true))
                frame.frameViewVisible(false);

            //call started state on top frame
            topVisibleFrame.frameViewVisible(hostActivityStarted);
        }
        else if(direction == Direction.BACK)
        {
            for(StateFrame frame : getCurrentlyVisibleFrames(stateStack, false))
                frame.frameViewVisible(true);
        }
    }
}
