package com.kodroid.pilot.lib.sync;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;

/**
 * This class holds the {@link UITypeHandler} collection that is queried upon {@link PilotStack}
 * changes.
 */
public class PilotSyncer implements PilotStack.TopFrameChangedListener
{
    private UITypeHandler[] mUITypeHandlers;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param uiTypeHandlers UITypeHandlers passed in here have to have corresponding entries for
     *                       *all* PilotFrame classes that exist in this project.
     */
    public PilotSyncer(UITypeHandler... uiTypeHandlers)
    {
        mUITypeHandlers = uiTypeHandlers;
    }

    //==================================================================//
    // PilotStack.EventListener
    //==================================================================//

    @Override
    public void topVisibleFrameUpdated(PilotFrame topVisibleFrame, Direction direction)
    {
        UITypeHandler handlingTypeHandler = null;

        //find the typeHandler that handles this frame
        for(UITypeHandler uiTypeHandler : mUITypeHandlers) {
            if (uiTypeHandler.onFrame(topVisibleFrame)) {
                handlingTypeHandler = uiTypeHandler;
                break;
            }
        }

        //throw if nothing to handle this frame
        if(handlingTypeHandler == null)
            throw new IllegalStateException("No UITypeHandler registered for PilotFrame of type "+topVisibleFrame.getClass().getName());

        //clear all other handlers
        for(UITypeHandler uiTypeHandler : mUITypeHandlers) {
            if (uiTypeHandler != handlingTypeHandler) {
                uiTypeHandler.clearAllUI();
            }
        }
    }
}
