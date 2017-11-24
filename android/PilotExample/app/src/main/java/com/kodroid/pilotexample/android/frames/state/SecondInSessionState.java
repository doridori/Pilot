package com.kodroid.pilotexample.android.frames.state;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilotexample.android.frames.scope.SessionScopedData;

/**
 * Example of a frame that holds init data and the controller / presenter definition. This can be
 * any type of Presenter you prefer i.e. Passive View / Supervising Controller / Presentation Model
 * etc...
 *
 * However! IMHO nothing in this class should touch android.view.* package for easy testing :p
 *
 * This class is serializable so anything that is not be should be marked transient i.e. generally
 * my presenters have some sort of state machine (i.e. Dynamo) which I wouldn't want
 * to persist / serialize. I would store this here and mark as transient
 */
public class SecondInSessionState extends PilotFrame
{
    public SecondInSessionState()
    {
        super(null);
    }

    /**
     * A real app may access scoped session data this way to perform some other operations i.e. make a network call
     *
     * @return
     */
    public String getSessionDataToDisplay()
    {
        SessionScopedData sessionScopedData = getParentStack().getFrameOfType(SessionScopedData.class);
        return "Scoped session data:"+sessionScopedData.getSomeSessionData();
    }

    public void warnUser()
    {
        getParentStack().pushFrame(WarningState.class);
    }
}
