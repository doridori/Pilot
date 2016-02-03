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

    private PilotStack parentStack;
    private Args args;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * No arg constructor used to catch when subclasses forget to call super
     */
    protected PilotFrame()
    {
        throw new RuntimeException("Subclasses need to call super(Args)");
    }

    /**
     * Subclasses must call super(args) while providing their own single `Args` constructor
     *
     * @param args can be null.
     */
    protected PilotFrame(Args args)
    {
        this.args = args;
    }

    //==================================================================//
    // Setters
    //==================================================================//

    /* package */ void setParentStack(PilotStack parentStack)
    {
        this.parentStack = parentStack;
    }

    //==================================================================//
    // Getters
    //==================================================================//

    public PilotStack getParentStack()
    {
        return parentStack;
    }

    /**
     * Used when persisting stack state
     *
     * @return
     */
    protected final Args getArgs()
    {
        return args;
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
