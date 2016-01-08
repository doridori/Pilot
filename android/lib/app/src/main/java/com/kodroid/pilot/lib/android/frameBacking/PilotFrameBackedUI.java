package com.kodroid.pilot.lib.android.frameBacking;

import android.view.View;

public interface PilotFrameBackedUI<P>
{
    View setBackingPilotFrame(P backingPilotFrame);

    /**
     * Convenience call for any UI setup that requires the backing frame
     *
     * @param backingPilotFrame
     */
    void backingFrameSet(P backingPilotFrame);
}
