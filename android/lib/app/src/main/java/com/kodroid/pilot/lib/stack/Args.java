package com.kodroid.pilot.lib.stack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of Serializable KeyValue pairs that are auto-cast on get. Useful for passing a frame
 * initialisation data in such a way that is easy to store when persisting the stack.
 */
public class Args implements Serializable
{
    Map<String, Serializable> serializableMap = new HashMap<>();

    public static Args with(String key, Serializable value)
    {
        Args args = new Args();
        args.put(key, value);
        return args;
    }

    public Args put(String key, Serializable value)
    {
        serializableMap.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key)
    {
        Serializable value = serializableMap.get(key);
        if(value == null)
            throw new NullPointerException("No value exists for key:"+key);
        return (T) value;
    }
}