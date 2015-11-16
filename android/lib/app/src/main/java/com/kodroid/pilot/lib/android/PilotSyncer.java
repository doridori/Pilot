package com.kodroid.pilot.lib.android;

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
        for(UITypeHandler uiTypeHandler : mUITypeHandlers)
        {
            if(uiTypeHandler.handle(topVisibleFrame))
                return;
        }

        throw new IllegalStateException("No handler registered for frame type "+topVisibleFrame.getClass().getName());
    }
}
