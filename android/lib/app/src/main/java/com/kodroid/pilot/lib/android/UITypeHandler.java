package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.stack.PilotFrame;

/**
 * Interface for a class that can handle creation / presentor setting and placement for a UI type
 * (i.e. View, Fragment, Dialog, FragmentDialog, etc)
 */
public interface UITypeHandler
{
    /**
     * @param frameClass
     * @return true if this handler can create a UI for that frame type
     */
    boolean handle(PilotFrame frame);
}
