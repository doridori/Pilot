package com.kodroid.pilot.lib.android.frameBacking;

import android.view.View;

public interface PilotFrameBackedUI<P>
{
    View setBackingPilotFrame(P backingPilotFrame);

    /**
     * For offensive programming checks.
     * @return True if has any backing frame set
     */
    boolean hasBackingFrameSet();
}
