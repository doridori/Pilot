package com.kodroid.pilot.lib.android.presenter;

import android.view.View;

public interface PresenterBackedUI<P>
{
    View setPresenter(P presenter);
    void presenterSet();
}
