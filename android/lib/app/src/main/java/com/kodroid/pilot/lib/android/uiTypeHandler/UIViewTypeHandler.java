package com.kodroid.pilot.lib.android.uiTypeHandler;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.android.frameBacking.PilotFrameBackedFrameLayout;
import com.kodroid.pilot.lib.android.frameBacking.BackedByFrameUtils;
import com.kodroid.pilot.lib.stack.PilotFrame;

import java.util.HashMap;
import java.util.Map;

public class UIViewTypeHandler implements UITypeHandler
{
    private final Displayer mDisplayer;
    private Map<Class<? extends PilotFrame>, Class<? extends PilotFrameBackedFrameLayout>> mFrameToViewMappings = new HashMap<>();

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param topLevelViews An array of views that make up the main first level views of your app.
     * @param displayer A {@link UIViewTypeHandler.Displayer} that will
     *                  handle showing your views. You can use the provided
     *                  {@link UIViewTypeHandler.SimpleDisplayer} here if needed.
     */
    public UIViewTypeHandler(Class<? extends PilotFrameBackedFrameLayout>[] topLevelViews, Displayer displayer)
    {
        mDisplayer = displayer;
        setupRootViewAndPilotFrameMappings(topLevelViews);
    }

    //==================================================================//
    // UITypeHandler Interface
    //==================================================================//

    @SuppressWarnings("unchecked")
    @Override
    public boolean onFrame(PilotFrame frame)
    {
        Class<? extends PilotFrame> frameClass = frame.getClass();
        if(mFrameToViewMappings.containsKey(frameClass)) //does handle this frame type
        {
            if(mDisplayer.isViewVisibleForFrame(frameClass)) //view will always have a BackedByFrame set as set on creation and views not recreated unless inside a Fragment (not supporting PilotStack which is Fragment hosted atm)
                return true;
            else
            {
                Class<? extends PilotFrameBackedFrameLayout> viewClass = mFrameToViewMappings.get(frameClass);
                PilotFrameBackedFrameLayout newView = createView(viewClass);
                newView.setBackingPilotFrame(frame);
                newView.backingFrameSet(frame);
                mDisplayer.makeVisible(newView);
                return true;
            }
        }
        else
            return false;
    }

    //==================================================================//
    // Private
    //==================================================================//

    private void setupRootViewAndPilotFrameMappings(Class<? extends PilotFrameBackedFrameLayout>[] rootViews)
    {
        //get view classes that make up the root level of the app
        for(Class<? extends PilotFrameBackedFrameLayout> viewClass : rootViews)
        {
            if(!PilotFrameBackedFrameLayout.class.isAssignableFrom(viewClass))
                throw new RuntimeException("Passed class does not extend PilotFrameBackedFrameLayout:"+viewClass.getCanonicalName());
            Class<? extends PilotFrame> pilotFrameClass = BackedByFrameUtils.getPilotFrameClass(viewClass);
            mFrameToViewMappings.put(pilotFrameClass, viewClass);
        }
    }

    private <T extends PilotFrameBackedFrameLayout> T createView(Class<T> viewClass)
    {
        try
        {
            return viewClass.getConstructor(Context.class).newInstance(mDisplayer.getViewContext());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    //==================================================================//
    // Displayer Delegation
    //==================================================================//

    /**
     * Integrators can supply their own Displayer or use/extend the suppled {@link UIViewTypeHandler.SimpleDisplayer}
     */
    public interface Displayer
    {
        boolean isViewVisibleForFrame(Class<? extends PilotFrame> frameClass);
        void makeVisible(View newView);
        Context getViewContext();
    }

    /**
     * A simple implementation of the {@link Displayer}
     * interface that can be setup with a managed root
     * view.
     *
     * This can be overridden if animations and or display logic needs to be tweaked. You may want to do this if
     *
     * - You have more that one {@link UITypeHandler} i.e. one for Views and one for FragmentDialogs and some UI syncronisation needs to take place between them.
     * - You have a master/detail flow and some subset of your views are to be placed in the detail area of the app (so you can manually handle this
     *
     * Otherwise you can roll your own via the {@link Displayer} interface.
     */
    public static class SimpleDisplayer implements Displayer
    {
        private FrameLayout mRootViewGroup;

        public SimpleDisplayer(FrameLayout rootViewGroup)
        {
            mRootViewGroup = rootViewGroup;
        }

        //============================//
        // Interface
        //============================//

        @Override
        public boolean isViewVisibleForFrame(Class<? extends PilotFrame> frameClass)
        {
            if(getCurrentView() == null) return false;
            final PilotFrameBackedFrameLayout currentView = (PilotFrameBackedFrameLayout) getCurrentView();
            return BackedByFrameUtils.getPilotFrameClass(currentView.getClass()).equals(frameClass);
        }

        @Override
        public void makeVisible(View newView)
        {
            setCurrentView(newView);
        }

        @Override
        public Context getViewContext()
        {
            return mRootViewGroup.getContext();
        }

        //============================//
        // Protected
        //============================//

        /**
         * Subclasses could override this if want to specify animations etc. Need to be careful that
         * any overrides don`t introduce any race conditions that can arise from quick succession of
         * stack transitions. I.e. ensure that {@link #getCurrentView()} always does return the latest
         * view that has been added / set to be displayed.
         *
         * @param newView
         */
        protected void setCurrentView(View newView)
        {
            //for now we will do a crude add / remove but could animate etc
            mRootViewGroup.removeAllViews();
            mRootViewGroup.addView(newView);
        }

        /**
         * If overriding see {@link #setCurrentView(View)}
         *
         * @return
         */
        protected View getCurrentView()
        {
            //simple as setCurrentView is simple and non-animating (i.e. only ever one view present at a time)
            return mRootViewGroup.getChildAt(0);
        }
    }
}
