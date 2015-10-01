package com.kodroid.pilotexample.android;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.android.PilotActivity;
import com.kodroid.pilot.lib.android.PresenterBasedFrameLayout;
import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilotexample.android.frames.presenter.FirstViewPresenter;
import com.kodroid.pilotexample.android.ui.FirstView;
import com.kodroid.pilotexample.android.ui.SecondInSessionView;

/**
 * Our whole application will live in this activity
 */
public class ExampleRootActivity extends PilotActivity
{
    //==================================================================//
    // Lifecycle
    //==================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FrameLayout rootView = new FrameLayout(this);
        setContentView(rootView);
        init(rootView);
    }

    //==================================================================//
    // Pilot
    //==================================================================//

    @Override
    protected PilotFrame getLaunchPresenterFrame()
    {
        return new FirstViewPresenter("RandomInitData");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends PresenterBasedFrameLayout>[] getRootViewClasses()
    {
        //all root level views should go here
        return new Class[]{
                FirstView.class,
                SecondInSessionView.class
        };
    }
}
