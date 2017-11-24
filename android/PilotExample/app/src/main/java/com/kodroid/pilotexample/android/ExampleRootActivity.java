package com.kodroid.pilotexample.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.android.PilotActivityAdapter;
import com.kodroid.pilot.lib.android.PilotUISyncer;
import com.kodroid.pilot.lib.android.uiTypeHandler.UITypeHandlerGeneric;
import com.kodroid.pilot.lib.android.uiTypeHandler.UITypeHandlerView;
import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.stack.PilotStack;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.state.FirstState;
import com.kodroid.pilotexample.android.frames.state.SecondInSessionState;
import com.kodroid.pilotexample.android.frames.state.WarningState;
import com.kodroid.pilotexample.android.ui.view.FirstView;
import com.kodroid.pilotexample.android.ui.view.SecondInSessionView;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents an Activity which contains a whole application
 */
public class ExampleRootActivity extends Activity implements PilotStack.StackEmptyListener
{
    static PilotStack pilotStack = new PilotStack();

    /**
     * keep a reference to this old school dialog handler as simple dialogs need to be dismissed in
     * onDestroy otherwise the Activity context will be leaked
     */
    private ExampleUIDialogTypeHandler exampleUIDialogTypeHandler;
    private PilotActivityAdapter pilotActivityAdapter;

    //==================================================================//
    // Lifecycle
    //==================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
        FrameLayout rootView = (FrameLayout) findViewById(R.id.root_view);
        pilotActivityAdapter = new PilotActivityAdapter(
            pilotStack,
            buildPilotSyncer(rootView),
            FirstState.class,
            null,
            this);
        pilotActivityAdapter.onCreateDelegate(savedInstanceState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        pilotActivityAdapter.onDestroyDelegate(this);
        exampleUIDialogTypeHandler.removeAllDialogs(); //to avoid context leaking
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        pilotActivityAdapter.onSaveInstanceStateDelegate(outState);
    }

    @Override
    public void onBackPressed()
    {
        pilotActivityAdapter.onBackPressedDelegate();
    }

    //==========================================================//
    // Pilot
    //==========================================================//

    /**
     * Build the {@link PilotUISyncer} that will sync the UI to the {@link PilotStack} state.
     *
     * @param rootView
     * @return
     */
    private PilotUISyncer buildPilotSyncer(FrameLayout rootView)
    {
        exampleUIDialogTypeHandler = new ExampleUIDialogTypeHandler(this);

        return new PilotUISyncer(
                pilotStack,
                new UITypeHandlerView(buildViewCreator(), new UITypeHandlerView.SimpleDisplayer(rootView), true),
                exampleUIDialogTypeHandler);
    }

    private UITypeHandlerView.ViewCreator buildViewCreator()
    {
        Map<Class<? extends PilotFrame>, Class<? extends View>> mappings = new HashMap<>();
        mappings.put(FirstState.class, FirstView.class);
        mappings.put(SecondInSessionState.class, SecondInSessionView.class);
        return new UITypeHandlerView.ViewCreator(mappings);
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
    private static class ExampleUIDialogTypeHandler extends UITypeHandlerGeneric
    {
        private static final Class<? extends PilotFrame>[] HANDLED_FRAMES = new Class[] { WarningState.class };

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
            if(pilotFrame instanceof WarningState)
                showWarningDialog((WarningState)pilotFrame);
        }

        void removeAllDialogs()
        {
            if(mCurrentDialog != null)
                mCurrentDialog.dismiss();
        }

        private void showWarningDialog(final WarningState warningState)
        {
            mCurrentDialog = new AlertDialog.Builder(mContext)
                    .setMessage(warningState.getWarningMsg())
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            warningState.dismissed();
                            mCurrentDialog = null;
                        }
                    }).create();
            mCurrentDialog.show();
        }

        @Override
        public boolean isFrameOpaque(PilotFrame frame)
        {
            return true;
        }

        @Override
        public void clearAllUI()
        {
            if(mCurrentDialog != null)
                mCurrentDialog.cancel();
        }
    }
}
