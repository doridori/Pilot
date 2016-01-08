package com.kodroid.pilot.lib.android.uiTypeHandler;

import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.sync.UITypeHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This can be used for anything i.e. show old school Dialogs etc.
 *
 * Just a simple non UI type specific handler which allows you to declare the handled {@link com.kodroid.pilot.lib.stack.PilotFrame} classes
 */
public abstract class UIGenericTypeHandler implements UITypeHandler
{
    private Set<Class<? extends PilotFrame>> mHandledFrames = new HashSet<>();

    public UIGenericTypeHandler(Class<? extends PilotFrame>[] handledFrames)
    {
        mHandledFrames.addAll(Arrays.asList(handledFrames));
    }

    @Override
    public boolean onFrame(PilotFrame frame)
    {
        if(mHandledFrames.contains(frame.getClass()))
        {
            showUiForFrame(frame);
            return true;
        }
        else
            return false;
    }

    /**
     * Subclasses should use this to display appropriate UI.
     *
     * Note this should not be used for Fragments or Activitys i.e anything that may persist on
     * config-change and this method will be called again on root Activity recreation.
     *
     * If you do want to show a new Activity as a result of a frame change then make sure you check
     * if that activity is already shown before doing so.
     *
     * If you want to show a Fragment use the forthcoming {@link UIFragmentTypeHandler}
     *
     * A good use case for something to show here is an old-school dialog as these are auto
     * dismissed on config-change by default, and this method will be called after a config-change
     * so simple to recreate.
     *
     * @param frame
     */
    protected abstract void showUiForFrame(PilotFrame frame);


}
