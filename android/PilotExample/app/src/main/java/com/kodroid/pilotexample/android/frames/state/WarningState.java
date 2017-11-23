package com.kodroid.pilotexample.android.frames.state;

import com.kodroid.pilot.lib.stack.PilotFrame;

/**
 * An example of a frame that will be displayed using a Dialog
 */
public class WarningState extends PilotFrame
{
    public String getWarningMsg()
    {
        return "ITS GONNA BLOW!";
    }

    public void dismissed() { getParentStack().popTopFrameInstance(this);}
}
