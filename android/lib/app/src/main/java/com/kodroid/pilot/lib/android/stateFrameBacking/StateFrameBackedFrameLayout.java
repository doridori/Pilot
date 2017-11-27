package com.kodroid.pilot.lib.android.stateFrameBacking;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.statestack.StateFrame;

public abstract class StateFrameBackedFrameLayout<P extends StateFrame> extends FrameLayout implements StateFrameBackedUI<P>, StateFrame.Observer
{
    private P backingPilotFrame;

    public StateFrameBackedFrameLayout(Context context)
    {
        super(context);
    }

    public StateFrameBackedFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public StateFrameBackedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View setBackingStateFrame(P backingPilotFrame)
    {
        this.backingPilotFrame = backingPilotFrame;
        backingStateFrameSet(backingPilotFrame);
        return this;
    }

    public void backingStateFrameSet(P backingStateFrame){}

    @Override
    public boolean hasBackingStateFrameSet()
    {
        return backingPilotFrame != null;
    }

    public P getBackingPilotFrame()
    {
        if(backingPilotFrame == null)
            throw new NullPointerException("Backing pilot frame is null");
        return backingPilotFrame;
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        getBackingPilotFrame().addObserver(this, true);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        getBackingPilotFrame().removeObserver(this);
    }

    @Override
    public void updated(){};
}