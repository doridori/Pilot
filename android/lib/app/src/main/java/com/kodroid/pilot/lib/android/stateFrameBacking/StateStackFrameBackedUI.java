package com.kodroid.pilot.lib.android.stateFrameBacking;

import android.view.View;

public interface StateStackFrameBackedUI<P>
{
    View setBackingStateFrame(P backingStateFrame);

    /**
     * For offensive programming checks.
     * @return True if has any backing frame set
     */
    boolean hasBackingStateFrameSet();
}
