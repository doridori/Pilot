package com.kodroid.pilot.lib.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.stack.PilotFrame;

/**
 * @param <P> The Presenter (controller / whatever) you want to use for this view. Support for thin
 *           views but easily usable with fragments.
 */
public abstract class PresenterBasedFrameLayout<P> extends FrameLayout
{
    //==================================================================//
    // Fields
    //==================================================================//

    private P mPresenter;

    //==================================================================//
    // Constructor
    //==================================================================//

    public PresenterBasedFrameLayout(Context context)
    {
        super(context);
    }

    public PresenterBasedFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PresenterBasedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    //==================================================================//
    // Presenter IOC
    //==================================================================//

    /**
     * Subclasses can override this but will need to call super(). If any child views use Presenters
     * these can be grabbed from this (parent) presenter on this method call.
     *
     * @param presenter
     * @return
     */
    View setPresenter(P presenter)
    {
        mPresenter = presenter;
        return this;
    }

    protected P getPresenter()
    {
        return mPresenter;
    }

    public Class<? extends PilotFrame> getPresenterClass()
    {
        return getPresenterClass(getClass());
    }

    /**
     * Could be replaced with annotation
     *
     * @return class of presenter
     */
    public static Class<? extends PilotFrame> getPresenterClass(Class<? extends PresenterBasedFrameLayout> clazz)
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
