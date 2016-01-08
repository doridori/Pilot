package com.kodroid.pilot.lib.android.frameBacking;

import com.kodroid.pilot.lib.stack.PilotFrame;

public class BackedByFrameUtils
{
    /**
     * Could be replaced with annotation
     *
     * @return class of pilotFrame
     */
    public static Class<? extends PilotFrame> getPilotFrameClass(Class<?> clazz)
    {
        if(hasBackedByFrameAnnotation(clazz))
            return clazz.getAnnotation(BackedByFrame.class).value();
        else
            throw new RuntimeException("This view does not declare a @BackedByFrame:"+clazz.getCanonicalName());
    }

    private static boolean hasBackedByFrameAnnotation(Class clazz)
    {
        return clazz.getAnnotation(BackedByFrame.class) != null;
    }
}
