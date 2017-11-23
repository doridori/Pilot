package com.kodroid.pilotexample.android.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kodroid.pilot.lib.android.frameBacking.PilotFrameBackedUI;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.state.SecondInSessionState;

public class SecondInSessionView extends FrameLayout implements PilotFrameBackedUI<SecondInSessionState>
{
    private SecondInSessionState backingPilotFrame;

    public SecondInSessionView(Context context)
    {
        super(context);

        LayoutInflater.from(getContext()).inflate(
                R.layout.view_second,
                this,
                true);

        final TextView tv = (TextView) findViewById(R.id.secondTxt);


        //quick n dirty click listeners
        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tv.setText(backingPilotFrame.getSessionDataToDisplay());
            }
        });

        findViewById(R.id.warning_but).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                backingPilotFrame.warnUser();
            }
        });
    }

    @Override
    public View setBackingPilotFrame(SecondInSessionState backingPilotFrame)
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
