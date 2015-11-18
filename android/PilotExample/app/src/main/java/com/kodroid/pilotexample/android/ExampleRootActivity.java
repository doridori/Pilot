package com.kodroid.pilotexample.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;


import com.kodroid.pilot.lib.android.PilotLifecycleManager;
import com.kodroid.pilot.lib.android.PilotSyncer;

import com.kodroid.pilot.lib.android.presenter.PresenterBackedFrameLayout;
import com.kodroid.pilot.lib.android.uiTypeHandler.UIFragmentTypeHandler;
import com.kodroid.pilot.lib.android.uiTypeHandler.UIGenericTypeHandler;
import com.kodroid.pilot.lib.android.uiTypeHandler.UIViewTypeHandler;
import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.presenter.FirstViewPresenter;
import com.kodroid.pilotexample.android.frames.presenter.WarningPresenter;
import com.kodroid.pilotexample.android.ui.fragment.WarningDialogFragment;
import com.kodroid.pilotexample.android.ui.view.FirstView;
import com.kodroid.pilotexample.android.ui.view.SecondInSessionView;

/**
 * This represents an Activity which contains a whole application
 */
public class ExampleRootActivity extends Activity implements PilotStack.StackEmptyListener
{
    //==================================================================//
    // Pilot Config
    //==================================================================//

    /**
     * The lifecycle manager is held statically so it easily persists across config changes so you
     * dont need to worry about handling this yourself
     */
    private static PilotLifecycleManager sPilotLifecycleManager = new PilotLifecycleManager(FirstViewPresenter.class);

    /**
     * Define an array of top level views. These are views of states that are represented by a PilotFrame
     */
    @SuppressWarnings("unchecked")
    static final Class<? extends PresenterBackedFrameLayout>[] topLevelViews = new Class[]
            {
                FirstView.class,
                SecondInSessionView.class
            };

    static final Class<? extends Fragment>[] topLevelFragments = new Class[]
            {
                WarningDialogFragment.class
            };

    /**
     * keep a reference to this old school dialog handler as simple dialogs need to be dismissed in
     * onDestroy otherwise the Activity context will be leaked
     */
    private ExampleUIDialogTypeHandler mExampleUIDialogTypeHandler;

    /**
     * Build the {@link PilotSyncer} that will sync the UI to the {@link PilotStack} state.
     *
     * @param rootView
     * @return
     */
    private PilotSyncer buildPilotSyncer(FrameLayout rootView)
    {
        mExampleUIDialogTypeHandler = new ExampleUIDialogTypeHandler(this);

        return new PilotSyncer(
                new UIViewTypeHandler(topLevelViews, new UIViewTypeHandler.SimpleDisplayer(rootView)),
                new UIFragmentTypeHandler(topLevelFragments, new UIFragmentTypeHandler.SimpleDisplayer(getFragmentManager(), R.id.fragment_container))
                //mExampleUIDialogTypeHandler
        );
    }

    //==================================================================//
    // Lifecycle
    //==================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        FrameLayout rootView = (FrameLayout) findViewById(R.id.root_view);
        sPilotLifecycleManager.onCreateDelegate(savedInstanceState, buildPilotSyncer(rootView), this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        sPilotLifecycleManager.onDestroyDelegate(this);
        mExampleUIDialogTypeHandler.removeAllDialogs(); //to avoid context leaking
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

    //==================================================================//
    // Dialogs (old school)
    //==================================================================//

    /**
     * An example of a type handler that is responsible for showing dialogs. Dialogs can be shown directly
     * from Views - this typeHandler approach can be used if the use-case is to display a dialog as a direct result of
     * a frame being pushed onto the stack.
     */
    private static class ExampleUIDialogTypeHandler extends UIGenericTypeHandler
    {
        private static final Class<? extends PilotFrame>[] HANDLED_FRAMES = new Class[] { WarningPresenter.class };

        private Context mContext;
        private Dialog mCurrentDialog;

        public ExampleUIDialogTypeHandler(Context context)
        {
            super(HANDLED_FRAMES);
            mContext = context;
        }

        @Override
        protected void showUiForFrame(PilotFrame pilotFrame)
        {
            if(pilotFrame instanceof WarningPresenter)
                showWarningDialog((WarningPresenter)pilotFrame);
        }

        void removeAllDialogs()
        {
            if(mCurrentDialog != null)
                mCurrentDialog.dismiss();
        }

        private void showWarningDialog(final WarningPresenter warningPresenter)
        {
            mCurrentDialog = new AlertDialog.Builder(mContext)
                    .setMessage(warningPresenter.getWarningMsg())
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            warningPresenter.dismissed();
                            mCurrentDialog = null;
                        }
                    }).create();
            mCurrentDialog.show();
        }
    }
}
