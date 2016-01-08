package com.kodroid.pilot.lib.android.frameBacking;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Convenience FrameLayout base for BackedByFrame backed views. Custom views do not have to use this as
 * long as they conform to the {@link PilotFrameBackedUI} interface.
 *
 * @param <P> The BackedByFrame (controller / whatever) you want to use for this view. Support for thin
 *           views but easily usable with fragments.
 */
public abstract class PilotFrameBackedFrameLayout<P> extends FrameLayout implements PilotFrameBackedUI<P>
{
    //==================================================================//
    // Fields
    //==================================================================//

    private P mBackingPilotFrame;

    //==================================================================//
    // Constructor
    //==================================================================//

    public PilotFrameBackedFrameLayout(Context context)
    {
        super(context);
    }

    public PilotFrameBackedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    //==================================================================//
    // BackedByFrame IOC
    //==================================================================//

    /**
     * Subclasses can override this but will need to call super(). If any child views use child Presenters
     * these can be grabbed from this (parent) PilotFrame on this method call.
     *
     * @param backingPilotFrame
     * @return
     */
    public View setBackingPilotFrame(P backingPilotFrame)
    {
        mBackingPilotFrame = backingPilotFrame;
        return this;
    }

    protected P getBackingPilotFrame()
    {
        return mBackingPilotFrame;
    }
}
