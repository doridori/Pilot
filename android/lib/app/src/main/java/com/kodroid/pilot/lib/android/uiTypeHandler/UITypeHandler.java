package com.kodroid.pilot.lib.android.uiTypeHandler;

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
}
