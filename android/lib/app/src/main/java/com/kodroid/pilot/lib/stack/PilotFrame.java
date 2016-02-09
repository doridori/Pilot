package com.kodroid.pilot.lib.stack;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

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

    /**
     * This is called by the parent PilotStack so no need to call yourself. Useful for testing.
     *
     * @param parentStack
     */
    public void setParentStack(PilotStack parentStack)
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

    //==================================================================//
    // Observable
    //==================================================================//

    private Set<Observer> mObservers = new HashSet<>();

    public void addObserver(Observer observer, boolean notifyOnAdd)
    {
        mObservers.add(observer);
        if(notifyOnAdd)
            observer.updated();
    }

    public void removeObserver(Observer observer)
    {
        mObservers.remove(observer);
    }

    protected void notifyObservers()
    {
        for(Observer observer : mObservers)
            observer.updated();
    }

    public interface Observer
    {
        void updated();
    }
}
