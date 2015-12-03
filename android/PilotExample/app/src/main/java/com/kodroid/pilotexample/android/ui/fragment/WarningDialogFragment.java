package com.kodroid.pilotexample.android.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodroid.pilot.lib.android.presenter.Presenter;
import com.kodroid.pilot.lib.android.presenter.PresenterBackedUI;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.presenter.WarningPresenter;

@Presenter(WarningPresenter.class)
public class WarningDialogFragment extends Fragment implements PresenterBackedUI<WarningPresenter>
{
    private WarningPresenter mWarningPresenter;

    @Override
    public View setPresenter(WarningPresenter warningPresenter)
    {
        Log.d("TAG..", "setting presenter");
        mWarningPresenter = warningPresenter;
        return null; //TODO change the interface return to remove view
    }

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