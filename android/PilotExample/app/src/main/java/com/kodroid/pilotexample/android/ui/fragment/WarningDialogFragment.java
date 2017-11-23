package com.kodroid.pilotexample.android.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodroid.pilot.lib.android.frameBacking.PilotFrameBackedUI;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.state.WarningState;

public class WarningDialogFragment extends Fragment implements PilotFrameBackedUI<WarningState>
{
    private WarningState mWarningState;
    private WarningState backingPilotFrame;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d("TAG..", "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_test_warning_dialog, container, false);
    }

    @Override
    public View setBackingPilotFrame(WarningState backingPilotFrame)
    {
        this.backingPilotFrame = backingPilotFrame;
        return null;//todo need to rework this interface as does not make sense to return view here. See #49
    }

    @Override
    public boolean hasBackingFrameSet()
    {
        return backingPilotFrame != null;
    }

    //    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        int title = getArguments().getInt("title");
//
//        return new AlertDialog.Builder(getActivity())
//                .setIcon(R.drawable.alert_dialog_icon)
//                .setTitle(title)
//                .setPositiveButton(R.string.alert_dialog_ok,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                ((FragmentAlertDialog)getActivity()).doPositiveClick();
//                            }
//                        }
//                )
//                .setNegativeButton(R.string.alert_dialog_cancel,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                ((FragmentAlertDialog)getActivity()).doNegativeClick();
//                            }
//                        }
//                )
//                .create();
//    }
}