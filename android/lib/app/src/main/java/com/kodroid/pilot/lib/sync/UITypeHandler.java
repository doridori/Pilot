package com.kodroid.pilot.lib.sync;

import com.kodroid.pilot.lib.stack.PilotFrame;

/**
 * Interface for a class that can handle creation / presentor setting and placement for a UI type
 * (i.e. View, Fragment, Dialog, FragmentDialog, etc)
 */
public interface UITypeHandler
{
    /**
     * Handle the latest frame i.e. show it
     *
     * @param frame
     * @return true if this handler can create a UI for that frame type. False if it does not handle this frame
     */
    boolean onFrame(PilotFrame frame);

    /**
     * Check for the passed in frame being non-opaque or non-fullscreen
     *
     * @param frame
     * @return true if this frame is opaque, meaning it takes up the whole UI and no other UI elements should be rendered behind it.
     */
    boolean isFrameOpaque(PilotFrame frame);

    /**
     * Called when another typeHandler has displayed an opaque fullscreen UI
     */
    void clearAllUI();
}
