package com.kodroid.pilot.lib.android.presenter;

import com.kodroid.pilot.lib.stack.PilotFrame;

public class PresenterUtils
{
    /**
     * Could be replaced with annotation
     *
     * @return class of presenter
     */
    public static Class<? extends PilotFrame> getPresenterClass(Class<?> clazz)
    {
        if(hasPresenterAnnotation(clazz))
            return clazz.getAnnotation(Presenter.class).value();
        else
            throw new RuntimeException("This view does not declare a @Presenter:"+clazz.getCanonicalName());
    }

    private static boolean hasPresenterAnnotation(Class clazz)
    {
        return clazz.getAnnotation(Presenter.class) != null;
    }
}
