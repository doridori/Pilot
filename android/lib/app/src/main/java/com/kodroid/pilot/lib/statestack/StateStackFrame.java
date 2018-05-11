package com.kodroid.pilot.lib.statestack;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Frame that lives in a {@link StateStack}.
 */
public abstract class StateStackFrame implements Serializable
{
    //==================================================================//
    // Fields
    //==================================================================//

    private StateStack parentStack;

    //==================================================================//
    // Parent StateStack
    //==================================================================//

    void setParentStack(StateStack parentStack)
    {
        this.parentStack = parentStack;
    }
    StateStack getParentStack()
    {
        return parentStack;
    }

    //==================================================================//
    // Lifecycle
    //==================================================================//

    /**
     * Frame lifecycle callback. {@link #getParentStack()} will contain a ref at this point. Only called once.
     */
    public void pushed(){}

    /**
     * Frame lifecycle callback. {@link #getParentStack()} will contain a ref until this method returns. Only called once7
     */
    public void popped(){}

    /**
     * Received between {@link #pushed()} and {@link #popped()}.
     *
     * @param frameViewVisible true if a view is visible-on-screen that's backed by this frame.
     *                         False if a previously visible-on-screen view is no longer visible but still on the stack.
     */
    public void frameViewVisible(boolean frameViewVisible){};

    //==================================================================//
    // Observable
    //==================================================================//

    private Set<Observer> observers = new HashSet<>();

    public void addObserver(Observer observer, boolean notifyOnAdd)
    {
        observers.add(observer);
        if(notifyOnAdd)
            observer.updated();
    }

    public void removeObserver(Observer observer)
    {
        observers.remove(observer);
    }

    protected void notifyObservers()
    {
        for(Observer observer : observers)
            observer.updated();
    }

    public interface Observer
    {
        void updated();
    }
}
