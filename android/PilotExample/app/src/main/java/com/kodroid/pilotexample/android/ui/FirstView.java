package com.kodroid.pilotexample.android.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.kodroid.pilot.R;
import com.kodroid.pilot.lib.android.Presenter;
import com.kodroid.pilot.lib.android.PresenterBasedFrameLayout;
import com.kodroid.pilotexample.android.frames.presenter.FirstViewPresenter;

@Presenter(FirstViewPresenter.class)
public class FirstView extends PresenterBasedFrameLayout<FirstViewPresenter>
{
    //==================================================================//
    // Constructor
    //==================================================================//

    public FirstView(Context context)
    {
        super(context);

        LayoutInflater.from(getContext()).inflate(
                R.layout.view_first,
                this,
                true);

        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getPresenter().mainViewClicked();
            }
        });
    }
}
