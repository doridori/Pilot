package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.android.uiTypeHandler.UITypeHandler;
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
        for(UITypeHandler uiTypeHandler : mUITypeHandlers)
        {
            if(uiTypeHandler.onFrame(topVisibleFrame))
                return;
        }

        throw new IllegalStateException("No UITypeHandler registered for PilotFrame of type "+topVisibleFrame.getClass().getName());
    }
}
