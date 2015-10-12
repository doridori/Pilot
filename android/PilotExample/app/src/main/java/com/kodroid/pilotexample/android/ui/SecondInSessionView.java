package com.kodroid.pilotexample.android.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kodroid.pilot.lib.android.Presenter;
import com.kodroid.pilot.lib.android.PresenterBasedFrameLayout;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.presenter.SecondInSessionViewPresenter;

@Presenter(SecondInSessionViewPresenter.class)
public class SecondInSessionView extends PresenterBasedFrameLayout<SecondInSessionViewPresenter>
{
    public SecondInSessionView(Context context)
    {
        super(context);

        LayoutInflater.from(getContext()).inflate(
                R.layout.view_second,
                this,
                true);

        final TextView tv = (TextView) findViewById(R.id.secondTxt);

        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tv.setText(getPresenter().getSessionDataToDisplay());
            }
        });
    }
}
