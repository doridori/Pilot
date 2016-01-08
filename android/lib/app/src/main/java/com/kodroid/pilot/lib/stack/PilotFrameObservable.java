package com.kodroid.pilot.lib.stack;

import java.util.HashSet;
import java.util.Set;

/**
 * Add observable functionality to {@link PilotFrame}
 */
public abstract class PilotFrameObservable extends PilotFrame
{
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