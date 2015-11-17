package com.kodroid.pilot.lib.android.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Convenience FrameLayout base for Presenter backed views. Custom views do not have to use this as
 * long as they conform to the {@link PresenterBackedUI} interface.
 *
 * @param <P> The Presenter (controller / whatever) you want to use for this view. Support for thin
 *           views but easily usable with fragments.
 */
public abstract class PresenterBackedFrameLayout<P> extends FrameLayout implements PresenterBackedUI<P>
{
    //==================================================================//
    // Fields
    //==================================================================//

    private P mPresenter;

    //==================================================================//
    // Constructor
    //==================================================================//

    public PresenterBackedFrameLayout(Context context)
    {
        super(context);
    }

    public PresenterBackedFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PresenterBackedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
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
    public View setPresenter(P presenter)
    {
        mPresenter = presenter;
        return this;
    }

    protected P getPresenter()
    {
        return mPresenter;
    }
}
