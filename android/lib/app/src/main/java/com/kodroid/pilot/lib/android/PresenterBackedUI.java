package com.kodroid.pilot.lib.android;

import android.view.View;

interface PresenterBackedUI<P>
{
    View setPresenter(P presenter);
}
