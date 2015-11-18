package com.kodroid.pilotexample.android.ui.fragment;

import android.app.DialogFragment;

public class WarningDialogFragment extends DialogFragment
{
    /** TODO
     fine on first run but oncreateDialog called before onCreateView so when it auto readds will have problems
    may be simpler to avoid the shitty fragments and just use old school dialogs in stead - however we will have
    a similar problem with strait fragments. Actually these are not as bad as onCreateView can just set a root
    view in onCreate and the real view in onAttach?

    **Should write this example with dialogs shown via a custom UI type handler and then think about
    frags - can then start using - create issue for fragment support and remove frag type handler to seperate branch**

    need to actually test the order of lifcycle methods and pilot processing methods when adding
    frag and when its auto readded http://staticfree.info/~steve/complete_android_fragment_lifecycle.png
    */

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