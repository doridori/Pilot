package com.kodroid.pilotexample.android.frames.presenter;

import com.kodroid.pilot.lib.stack.PilotFrame;

/**
 * An example of a frame that will be displayed using a DialogFragment
 */
public class WarningPresenter extends PilotFrame
{
    public String getWarningMsg()
    {
        return "ITS GONNA BLOW!";
    }

    public void dismissed() { getParentStack().removeThisFrame(this);}
}
