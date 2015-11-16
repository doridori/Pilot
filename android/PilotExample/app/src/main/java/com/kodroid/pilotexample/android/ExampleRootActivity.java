package com.kodroid.pilotexample.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;


import com.kodroid.pilot.lib.android.PilotLifecycleManager;
import com.kodroid.pilot.lib.android.PilotSyncer;
import com.kodroid.pilot.lib.android.PresenterBasedFrameLayout;
import com.kodroid.pilot.lib.android.ViewUITypeHandler;
import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilotexample.android.frames.presenter.FirstViewPresenter;
import com.kodroid.pilotexample.android.ui.FirstView;
import com.kodroid.pilotexample.android.ui.SecondInSessionView;

/**
 * This represents an Activity which contains a whole application
 */
public class ExampleRootActivity extends Activity implements PilotStack.StackEmptyListener
{
    //==================================================================//
    // Pilot Config
    //==================================================================//

    private static PilotLifecycleManager sPilotLifecycleManager = new PilotLifecycleManager(FirstViewPresenter.class);

    @SuppressWarnings("unchecked")
    static final Class<? extends PresenterBasedFrameLayout>[] rootViews = new Class[]{
            FirstView.class,
            SecondInSessionView.class
    };

    private PilotSyncer buildPilotSyncer(FrameLayout rootView)
    {
        return new PilotSyncer(new ViewUITypeHandler(rootViews, new ViewUITypeHandler.SimpleDisplayer(rootView)));
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
        sPilotLifecycleManager.onCreateDelegate(savedInstanceState, buildPilotSyncer(rootView), this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        sPilotLifecycleManager.onDestroyDelegate(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        sPilotLifecycleManager.onSaveInstanceStateDelegate(outState);
    }

    @Override
    public void onBackPressed()
    {
        sPilotLifecycleManager.onBackPressedDelegate();
    }

    //==================================================================//
    // PilotStack listener methods
    //==================================================================//

    @Override
    public void noVisibleFramesLeft()
    {
        finish();
    }
}
