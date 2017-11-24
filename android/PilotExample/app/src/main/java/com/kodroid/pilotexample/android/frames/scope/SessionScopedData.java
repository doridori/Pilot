package com.kodroid.pilotexample.android.frames.scope;

import com.kodroid.pilot.lib.stack.InvisibleFrame;
import com.kodroid.pilot.lib.stack.PilotFrame;

/**
 * This represents an example scoped data frame. This can be inserted on the stack for some operation
 * that may last more than one UI frame. This is useful when dealing with things like sessions or tasks
 * and you want to provide or set up dependencies for everything that sits above this frame on the stack.
 *
 * This also has security benefits of being able to explicitly clear data associated with a scope when a
 * data-frame is popped off the stack.
 *
 * Regarding dependency insertion to the rest of the app this can be:
 *
 * 1. homegrown (i.e. adding and removing data to a Session Singleton in your app)
 * 2. Pull based - allow this scope frame to be pulled from the stack and queried
 * 3. Dagger based (i.e. Dagger 1 allows you to manipulate whats available on the ObjectGraph at runtime)
 *
 * You do not have to limit a scope frame to holding data / adding/removing dependencies only, you could
 * perform logic here like a task timeout (like max-session length) and on timeout clear this (and all
 * frames above it) from the application stack allowing the UI sitting below it (i.e. login screen) to
 * be shown by whoever is being notified of stack changes.
 */
@InvisibleFrame
public class SessionScopedData extends PilotFrame
{
    /**
     * i.e. a session key etc
     */
    private String mSomeSessionData;

    public SessionScopedData()
    {
        super(null);
        mSomeSessionData = "some fake data";
    }

    public String getSomeSessionData()
    {
        return mSomeSessionData;
    }
}
