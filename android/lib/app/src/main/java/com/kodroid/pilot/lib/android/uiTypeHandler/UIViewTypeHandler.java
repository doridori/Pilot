package com.kodroid.pilot.lib.android.uiTypeHandler;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.android.frameBacking.PilotFrameLayout;
import com.kodroid.pilot.lib.android.frameBacking.BackedByFrameUtils;
import com.kodroid.pilot.lib.stack.PilotFrame;
import com.kodroid.pilot.lib.sync.UITypeHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UIViewTypeHandler implements UITypeHandler
{
    private final Displayer displayer;
    private final boolean enableLogging;
    private Map<Class<? extends PilotFrame>, Class<? extends PilotFrameLayout>> frameToViewMappings = new HashMap<>();

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param topLevelViews An array of views that make up the main first level views of your app.
     * @param displayer A {@link UIViewTypeHandler.Displayer} that will
     *                  handle showing your views. You can use the provided
     *                  {@link UIViewTypeHandler.SimpleDisplayer} here if needed.
     */
    public UIViewTypeHandler(Class<? extends PilotFrameLayout>[] topLevelViews, Displayer displayer, boolean enableLogging)
    {
        this.displayer = displayer;
        this.enableLogging = enableLogging;
        setupRootViewAndPilotFrameMappings(topLevelViews);
    }

    //==================================================================//
    // UITypeHandler Interface
    //==================================================================//


    @Override
    public boolean isFrameSupported(Class<? extends PilotFrame> frameClass) {
        return frameToViewMappings.containsKey(frameClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void renderFrame(PilotFrame frame)
    {
        log("UITypeViewHandler:renderFrame(%s)", frame.toString());

        Class<? extends PilotFrame> frameClass = frame.getClass();
        if(isFrameSupported(frameClass)) //does handle this frame type
        {
            if(displayer.isViewVisibleForFrameInstance(frame)) //view will always have a BackedByFrame set as set on creation and views not recreated unless inside a Fragment (not supporting PilotStack which is Fragment hosted atm)
            {
                log("UITypeViewHandler:renderFrame(%s) already visible", frame.toString());
                return;
            }
            else
            {
                log("UITypeViewHandler:renderFrame(%s) not visible, adding new view", frame.toString());
                Class<? extends PilotFrameLayout> viewClass = frameToViewMappings.get(frameClass);
                PilotFrameLayout newView = createView(viewClass);
                newView.setBackingPilotFrame(frame);
                newView.backingFrameSet(frame);
                displayer.makeVisible(newView);
                return;
            }
        }
        else
            throw new IllegalArgumentException("Frame type not supported. Use isFrameSupported() first!");
    }

    /**
     * Will return true for all views by default. Subclasses should override and return false for any non-opaque, non-fullscreen views.
     *
     * @param frame
     * @return
     */
    @Override
    public boolean isFrameOpaque(PilotFrame frame) {
        return true;
    }

    @Override
    public void clearAllUI() {
        displayer.clearAllUI();
    }

    //==================================================================//
    // Private
    //==================================================================//

    private void log(String format, String... args)
    {
        if(enableLogging)
        {
            Log.i("Pilot", String.format(format, args));
        }
    }

    private void setupRootViewAndPilotFrameMappings(Class<? extends PilotFrameLayout>[] rootViews)
    {
        //get view classes that make up the root level of the app
        for(Class<? extends PilotFrameLayout> viewClass : rootViews)
        {
            if(!PilotFrameLayout.class.isAssignableFrom(viewClass))
                throw new RuntimeException("Passed class does not extend PilotFrameLayout:"+viewClass.getCanonicalName());
            Class<? extends PilotFrame> pilotFrameClass = BackedByFrameUtils.getPilotFrameClass(viewClass);
            frameToViewMappings.put(pilotFrameClass, viewClass);
        }
    }

    private <T extends PilotFrameLayout> T createView(Class<T> viewClass)
    {
        try
        {
            return viewClass.getConstructor(Context.class).newInstance(displayer.getViewContext());
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
        boolean isViewVisibleForFrameInstance(PilotFrame frame);
        void makeVisible(View newView);
        Context getViewContext();
        void clearAllUI();
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
        public boolean isViewVisibleForFrameInstance(PilotFrame frame)
        {
            if(getCurrentView() == null) return false;
            final PilotFrameLayout currentView = (PilotFrameLayout) getCurrentView();
            if(BackedByFrameUtils.getPilotFrameClass(currentView.getClass()).equals(frame.getClass()))
            {
                //defensive - at worst a new view would be created on rotation - needs testing
                if(currentView.getBackingPilotFrame() == null)
                    return false;

                return currentView.getBackingPilotFrame().equals(frame);
            }

            return false;
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

        @Override
        public void clearAllUI() {
            mRootViewGroup.removeAllViews();
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

    /**
     * Animator that allows Animator defs to be used to declare view entry / exit animations.
     *
     * Note that if a View is scheduled to be removed and therefore being animated out by this class its
     * backing frame will no longer be on the stack. Make sure a view does not attempt to call into its
     * backing frame without checking this first otherwise button presses on out animations will crash.
     *
     * A LayoutTransition object would've been useful here if it did not cancel running animations when
     * Views are added / removed.
     *
     * This class can be improved by reversing the in anim if still running when the view is removed.
     * Alternatively Views should be visible for at least the min time that equals the in anim IN duration.
     */
    public static class AnimatingDisplayer implements UIViewTypeHandler.Displayer
    {
        //==================================================================//
        // Builder
        //==================================================================//

        /**
         * Sets up a {@link UIViewTypeHandler.Displayer} with simple fade in/out Animators.
         *
         * As mentioned in the class docs your Views should probably be around for at least
         * <code>duration</code>*2 otherwise quick transitions may cause unsightly animation combinations.
         *
         * @param rootViewGroup
         * @return
         */
        public static AnimatingDisplayer buildWithFade(FrameLayout rootViewGroup, int duration)
        {
            ObjectAnimator in = new ObjectAnimator();
            in.setFloatValues(0, 0, 1);
            //not using startDelay as no fillBefore
            in.setDuration(duration*2);
            in.setPropertyName("alpha");

            ObjectAnimator out = new ObjectAnimator();
            out.setFloatValues(1, 0);
            out.setDuration(duration);
            out.setPropertyName("alpha");

            return new AnimatingDisplayer(rootViewGroup, in, out);
        }

        //==================================================================//
        // Fields
        //==================================================================//

        private Set<View> animatingOutViews = new HashSet<>();

        private FrameLayout rootViewGroup;
        private final Animator animateIn;
        private final Animator animateOut;

        //==================================================================//
        // Constructor
        //==================================================================//

        public AnimatingDisplayer(
                FrameLayout rootViewGroup,
                Animator animateIn,
                Animator animateOut)
        {
            this.rootViewGroup = rootViewGroup;
            this.animateIn = animateIn;
            this.animateOut = animateOut;
        }

        //==================================================================//
        // Super Interface
        //==================================================================//

        @Override
        public boolean isViewVisibleForFrameInstance(PilotFrame frame)
        {
            if(getCurrentView() == null) return false;
            final PilotFrameLayout currentView = (PilotFrameLayout) getCurrentView();
            if(BackedByFrameUtils.getPilotFrameClass(currentView.getClass()).equals(frame.getClass()))
            {
                //defensive - at worst a new view would be created on rotation - needs testing
                if(currentView.getBackingPilotFrame() == null)
                    return false;

                return currentView.getBackingPilotFrame().equals(frame);
            }

            return false;
        }

        @Override
        public void makeVisible(View newView)
        {
            setCurrentView(newView);
        }

        @Override
        public Context getViewContext()
        {
            return rootViewGroup.getContext();
        }

        @Override
        public void clearAllUI() {
            animateOutViews(0);
        }

        //==================================================================//
        // Private
        //==================================================================//

        /**
         * Subclasses could override this if want to specify animations etc. Need to be careful that
         * any overrides don`t introduce any race conditions that can arise from quick succession of
         * stack transitions. I.e. ensure that {@link #getCurrentView()} always does return the latest
         * view that has been added / set to be displayed.
         *
         * @param newView
         */
        private void setCurrentView(final View newView)
        {
            //animate in
            rootViewGroup.addView(newView, 0);
            Animator animInClone = animateIn.clone();
            animInClone.setTarget(newView);
            animInClone.start();

            //animate out all others
            animateOutViews(1);
        }

        private void animateOutViews(int startingIndex)
        {
            //animate all others out
            for(int i = startingIndex; i < rootViewGroup.getChildCount(); i++)
            {
                final View oldView = rootViewGroup.getChildAt(i);

                if(animatingOutViews.contains(oldView))
                    continue;
                else
                    animatingOutViews.add(oldView);

                Animator animOutClone = animateOut.clone();
                animOutClone.setTarget(oldView);
                animOutClone.addListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation){}

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        removeView();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {
                        removeView();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation){}

                    private void removeView()
                    {
                        animatingOutViews.remove(oldView);
                        if (rootViewGroup.indexOfChild(oldView) >= 0)
                            rootViewGroup.removeView(oldView);
                    }
                });
                animOutClone.start();
            }
        }

        /**
         * If overriding see {@link #setCurrentView(View)}
         *
         * @return
         */
        private View getCurrentView()
        {
            //we are always adding new views at 0 so we will just look at this
            return rootViewGroup.getChildAt(0);
        }
    }
}
