package com.kodroid.pilotexample.android.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.android.frameBacking.PilotFrameBackedUI;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.state.FirstState;

public class FirstView extends FrameLayout implements PilotFrameBackedUI<FirstState>
{
    private FirstState backingPilotFrame;
    //==================================================================//
    // Constructor
    //==================================================================//

    public FirstView(Context context)
    {
        super(context);

        LayoutInflater.from(getContext()).inflate(
                R.layout.view_first,
                this,
                true);

        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                backingPilotFrame.moveToNextState();
            }
        });
    }

    @Override
    public View setBackingPilotFrame(FirstState backingPilotFrame)
    {
        this.backingPilotFrame = backingPilotFrame;
        return this;
    }

    @Override
    public boolean hasBackingFrameSet()
    {
        return backingPilotFrame != null;
    }
}
