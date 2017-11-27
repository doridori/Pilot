package com.kodroid.pilot.lib.android.frameBacking;

import android.view.View;

public interface StateFrameBackedUI<P>
{
    View setBackingStateFrame(P backingStateFrame);

    /**
     * For offensive programming checks.
     * @return True if has any backing frame set
     */
    boolean hasBackingStateFrameSet();
}
