package com.kodroid.pilot.lib.stack;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Frame that lives in a {@link PilotStack}. Has simple stack lifecycle methods and supports an optional
 * Visitor used to expose frame specific interfaces. Also supports Categorization via annotations for
 * category specific operations and listeners on the holding {@link PilotStack}
 */
public abstract class PilotFrame implements Serializable
{
    //==================================================================//
    // Fields
    //==================================================================//

    private PilotStack mParentStack;

    //==================================================================//
    // Setters
    //==================================================================//

    /* package */ void setParentStack(PilotStack parentStack)
    {
        mParentStack = parentStack;
    }

    //==================================================================//
    // Getters
    //==================================================================//

    public PilotStack getParentStack()
    {
        return mParentStack;
    }

    //==================================================================//
    // Lifecycle
    //==================================================================//

    /**
     * Frame lifecycle callback. {@link #getParentStack()} will contain a ref at this point.
     */
    public void pushed(){}

    /**
     * Frame lifecycle callback. {@link #getParentStack()} will contain a ref until this method returns
     */
    public void popped(){}
}
