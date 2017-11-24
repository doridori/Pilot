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
public class FirstState extends PilotFrame
{
    //==================================================================//
    // Constructor
    //==================================================================//

    public FirstState()
    {
        super(null);
    }

    //==================================================================//
    // App transitions
    //==================================================================//

    /**
     * This represents some app nav action which happens via the parent FatStack.
     * Called by UI in production or called directly in tests. At this point you will generally want to
     *
     * 1) push another UI frame on the stack
     * 2) push a scoped-data frame on the stack and then a UI frame.
     */
    public void moveToNextState()
    {
        //example of pushing data frame then presenter frame
        getParentStack().pushFrame(SessionScopedData.class);
        getParentStack().pushFrame(SecondInSessionState.class);
    }
}


