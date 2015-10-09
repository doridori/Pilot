package com.kodroid.pilotexample.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.android.PilotManager;
import com.kodroid.pilot.lib.android.PresenterBasedFrameLayout;
import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilotexample.android.frames.presenter.FirstViewPresenter;
import com.kodroid.pilotexample.android.ui.FirstView;
import com.kodroid.pilotexample.android.ui.SecondInSessionView;

/**
 * This represents an Activity which contains a whole application
 */
public class ExampleRootActivity extends Activity implements PilotManager.ActivityDelegate
{
    private static PilotManager sPilotManager;

    static
    {
        //not worrying too much about this warning as for now just want a quick way to specify a load
        //of classes - the generic declaration is almost just a note for the developer of the type of
        //this array. Anything specified here will be type checked by the PilotManager so any type
        //issues will be picked up on Activity start.
        @SuppressWarnings("unchecked")
        Class<? extends PresenterBasedFrameLayout>[] rootViews = new Class[]{
                FirstView.class,
                SecondInSessionView.class
        };

        sPilotManager = new PilotManager(rootViews, FirstViewPresenter.class);
    }

    //==================================================================//
    // Lifecycle
    //==================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FrameLayout rootView = new FrameLayout(this);
        setContentView(rootView);
        sPilotManager.onCreateDelegate(savedInstanceState, rootView, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        sPilotManager.onDestroyDelegate(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        sPilotManager.onSaveInstanceStateDelegate(outState);
    }

    @Override
    public void onBackPressed()
    {
        sPilotManager.onBackPressedDelegate();
    }

    //==================================================================//
    // PilotManager.ActivityDelegate methods
    //==================================================================//

    @Override
    public boolean interceptTopFrameUpdatedForCategory(PilotFrame pilotFrame, PilotStack.EventListener.Direction direction)
    {
        return false;
    }

    @Override
    public void noVisibleFramesLeft()
    {
        finish();
    }
}
