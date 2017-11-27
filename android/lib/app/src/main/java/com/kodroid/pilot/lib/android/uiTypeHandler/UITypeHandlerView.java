package com.kodroid.pilot.lib.android.uiTypeHandler;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.kodroid.pilot.lib.android.frameBacking.PilotFrameBackedUI;
import com.kodroid.pilot.lib.statestack.StateFrame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Code to create and display an {@link View} for a {@link StateFrame} that is declared to be handled by this {@link UITypeHandlerView}
 */
public class UITypeHandlerView implements UITypeHandler
{
    private final ViewCreator viewCreator;
    private final Displayer displayer;
    private final boolean enableLogging;

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     * @param displayer A {@link UITypeHandlerView.Displayer} that will
     *                  handle showing your views. You can use the provided
     *                  {@link UITypeHandlerView.SimpleDisplayer} here if needed.
     */
    public UITypeHandlerView(
            ViewCreator viewCreator,
            Displayer displayer,
            boolean enableLogging)
    {
        this.viewCreator = viewCreator;
        this.displayer = displayer;
        this.enableLogging = enableLogging;
    }

    //==================================================================//
    // UITypeHandler Interface
    //==================================================================//


    @Override
    public boolean isFrameSupported(Class<? extends StateFrame> frameClass) {
        return viewCreator.isFrameHandled(frameClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void renderFrame(StateFrame frame)
    {
        log("UITypeViewHandler:renderFrame(%s)", frame.toString());

        Class<? extends StateFrame> frameClass = frame.getClass();
        if(isFrameSupported(frameClass))
        {
            if(displayer.isViewAddedForFrameInstance(frame, viewCreator))
            {
                log("UITypeViewHandler:renderFrame(%s) already added", frame.toString());
            }
            else //view is not visible
            {
                log("UITypeViewHandler:renderFrame(%s) not added, adding new view", frame.toString());
                displayer.makeVisible(viewCreator.createViewForFrame(displayer.getDisplayConext(), frame));
            }
        }
        else
            throw new IllegalArgumentException("Frame type not supported. Use isFrameSupported() first!");
    }

    /**
     * Will return true for all views by default. Subclasses should override and return false for any non-opaque, non-fullscreen views.
     *
     * @param frame the frame being queried
     * @return true if the passed Frame is opaque
     */
    @Override
    public boolean isFrameOpaque(StateFrame frame) {
        return true;
    }

    @Override
    public void clearAllUI() {
        displayer.clearAllUI();
    }

    //==================================================================//
    // Logging
    //==================================================================//

    private void log(String format, String... args)
    {
        if(enableLogging)
        {
            Log.i("Pilot", String.format(format, args));
        }
    }

    //==================================================================//
    // Displayer Delegation
    //==================================================================//

    public static class ViewCreator
    {
        private Map<Class<? extends StateFrame>, Class<? extends View>> mappings = new HashMap<>();

        public ViewCreator(Map<Class<? extends StateFrame>, Class<? extends View>> mappings)
        {
            this.mappings = mappings;
        }

        private boolean isFrameHandled(Class<? extends StateFrame> pilotFrame)
        {
            return mappings.containsKey(pilotFrame);
        }

        /**
         * @param stateFrame the frame to get the View class for
         * @return Should always return a ViewClass or else throw an Exception
         */
        private Class<? extends View> getViewClassForFrame(StateFrame stateFrame)
        {
            for(Class<? extends StateFrame> forFrame: mappings.keySet())
            {
                if(forFrame.equals(stateFrame.getClass())) return mappings.get(forFrame);
            }

            throw new IllegalArgumentException(stateFrame.getClass()+" not supported");
        }

        /**
         * Return a new View instance, which has had the passed in {@link StateFrame} set.
         * @param stateFrame
         * @return
         */
        @SuppressWarnings("unchecked")
        private View createViewForFrame(Context context, StateFrame stateFrame)
        {
            View view = createView(context, getViewClassForFrame(stateFrame));
            ((PilotFrameBackedUI)view).setBackingPilotFrame(stateFrame);
            return view;
        }

        private <T extends View> T createView(Context context, Class<T> viewClass)
        {
            try
            {
                return viewClass.getConstructor(Context.class).newInstance(context);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Integrators can supply their own Displayer or use/extend the suppled {@link UITypeHandlerView.SimpleDisplayer}
     */
    public interface Displayer
    {
        /**
         * Checking if added is enough as opposed to checking if a Frame has been set, as a View
         * should never have a null frame. Views have Frames attached on creation, and are not
         * recreated on config-change *unless* a child view of a Fragment.
         *
         * @param frame
         * @param viewCreator
         * @return
         */
        boolean isViewAddedForFrameInstance(StateFrame frame, ViewCreator viewCreator);
        void makeVisible(View newView);
        void clearAllUI();
        Context getDisplayConext();
    }

    /**
     * A simple implementation of the {@link Displayer} interface that can be setup with a managed root
     * view.
     *
     * This can be overridden if animations and or display logic needs to be tweaked. You may want to do this if
     *
     * - You have more that one {@link UITypeHandler} i.e. one for Views and one for FragmentDialogs and some UI syncronisation needs to take place between them.
     * - You have a master/detail flow and some subset of your views are to be placed in the detail area of the app (so you can manually handle this
     *
     * Otherwise you can roll your own via the {@link Displayer} interface.
     *
     * This will not work if rendering Views into a Fragment (see {@link Displayer}
     */
    public static class SimpleDisplayer implements Displayer
    {
        private FrameLayout rootViewGroup;

        public SimpleDisplayer(FrameLayout rootViewGroup)
        {
            this.rootViewGroup = rootViewGroup;
        }

        //============================//
        // Interface
        //============================//

        @Override
        public boolean isViewAddedForFrameInstance(StateFrame frame, ViewCreator viewCreator)
        {
            if(getCurrentView() == null) return false;
            final Class<? extends View> currentViewClass = getCurrentView().getClass();
            boolean isAdded = viewCreator.getViewClassForFrame(frame).equals(currentViewClass);
            if(isAdded && !((PilotFrameBackedUI)getCurrentView()).hasBackingFrameSet())
                throw new IllegalStateException(
                        "Uh oh! This should not be possible. A View should never not have a Frame set " +
                                "apart from when first created, as on config change the view instance will " +
                                "NOT be persisted or automatically recreated by the system (default for " +
                                "programmatically added Views, unless contained withing a host Fragment), " +
                                "instead a new one will be created by Pilot and will still have UI-savedState " +
                                "restored as normal. ");
            return isAdded;
        }

        @Override
        public void makeVisible(View newView)
        {
            setCurrentView(newView);
        }

        @Override
        public void clearAllUI() {
            rootViewGroup.removeAllViews();
        }

        @Override
        public Context getDisplayConext()
        {
            return rootViewGroup.getContext();
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
            rootViewGroup.removeAllViews();
            rootViewGroup.addView(newView);
        }

        /**
         * If overriding see {@link #setCurrentView(View)}
         *
         * @return
         */
        protected View getCurrentView()
        {
            //simple as setCurrentView is simple and non-animating (i.e. only ever one view present at a time)
            return rootViewGroup.getChildAt(0);
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
    public static class AnimatingDisplayer implements UITypeHandlerView.Displayer
    {
        //==================================================================//
        // Builder
        //==================================================================//

        /**
         * Sets up a {@link UITypeHandlerView.Displayer} with simple fade in/out Animators.
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

        @Override
        public boolean isViewAddedForFrameInstance(StateFrame frame, ViewCreator viewCreator)
        {
            if(getCurrentView() == null) return false;
            final Class<? extends View> currentViewClass = getCurrentView().getClass();
            boolean isAdded = viewCreator.getViewClassForFrame(frame).equals(currentViewClass);
            if(isAdded && !((PilotFrameBackedUI)getCurrentView()).hasBackingFrameSet())
                throw new IllegalStateException(
                        "Uh oh! This should not be possible. A View should never not have a Frame set " +
                                "apart from when first created, as on config change the view instance will " +
                                "NOT be persisted or automatically recreated by the system (default for " +
                                "programmatically added Views, unless contained withing a host Fragment), " +
                                "instead a new one will be created by Pilot and will still have UI-savedState " +
                                "restored as normal. ");
            return isAdded;
        }

        @Override
        public void makeVisible(View newView)
        {
            setCurrentView(newView);
        }

        @Override
        public void clearAllUI() {
            animateOutViews(0);
        }

        @Override
        public Context getDisplayConext()
        {
            return rootViewGroup.getContext();
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
