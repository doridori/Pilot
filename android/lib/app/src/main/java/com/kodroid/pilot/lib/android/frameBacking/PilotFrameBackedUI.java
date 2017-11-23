package com.kodroid.pilot.lib.android.frameBacking;

public interface PilotFrameBackedUI<P>
{
    void setBackingPilotFrame(P backingPilotFrame);

    /**
     * For offensive programming checks.
     * @return True if has any backing frame set
     */
    boolean hasBackingFrameSet();
}
