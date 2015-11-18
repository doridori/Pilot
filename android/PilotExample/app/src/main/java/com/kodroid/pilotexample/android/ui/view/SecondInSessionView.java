package com.kodroid.pilotexample.android.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kodroid.pilot.lib.android.presenter.Presenter;
import com.kodroid.pilot.lib.android.presenter.PresenterBackedFrameLayout;
import com.kodroid.pilotexample.R;
import com.kodroid.pilotexample.android.frames.presenter.SecondInSessionViewPresenter;

@Presenter(SecondInSessionViewPresenter.class)
public class SecondInSessionView extends PresenterBackedFrameLayout<SecondInSessionViewPresenter>
{
    public SecondInSessionView(Context context)
    {
        super(context);

        LayoutInflater.from(getContext()).inflate(
                R.layout.view_second,
                this,
                true);

        final TextView tv = (TextView) findViewById(R.id.secondTxt);


        //quick n dirty click listeners
        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tv.setText(getPresenter().getSessionDataToDisplay());
            }
        });

        findViewById(R.id.warning_but).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getPresenter().warnUser();
            }
        });
    }
}
