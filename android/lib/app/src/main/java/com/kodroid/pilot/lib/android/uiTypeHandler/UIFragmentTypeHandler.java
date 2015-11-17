package com.kodroid.pilot.lib.android.uiTypeHandler;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.kodroid.pilot.lib.android.presenter.PresenterBackedUI;
import com.kodroid.pilot.lib.android.presenter.PresenterUtils;
import com.kodroid.pilot.lib.stack.PilotFrame;

import java.util.HashMap;
import java.util.Map;

/**
 * For Fragments.
 *
 * Does not handle support lib fragments at present.
 *
 * Fragments managed by this type handler and Pilot will have their presenter frame made available
 * to them by the time {@link Fragment#onActivityCreated(Bundle)} is called.
 */
public class UIFragmentTypeHandler implements UITypeHandler
{
    private final Displayer mDisplayer;
    private Map<Class<? extends PilotFrame>, Class<? extends Fragment>> mFrameToFragmentMappings = new HashMap<>();

    //==================================================================//
    // Constructor
    //==================================================================//

    /**
     *
     * @param rootFragments all fragment classes passed here need to implement {@link PresenterBackedUI}
     * @param displayer A {@link UIFragmentTypeHandler.Displayer} that
     *                  is to be used to handle the displaying of your fragments. You can use
     *                  {@link UIFragmentTypeHandler.SimpleDisplayer} if required.
     */
    public UIFragmentTypeHandler(Class<? extends Fragment>[] rootFragments, Displayer displayer)
    {
        mDisplayer = displayer;
        setupRootFragmentAndPresenterMappings(rootFragments);
    }

    //==================================================================//
    // UITypeHandler Interface
    //==================================================================//

    @Override
    public boolean showUiForFrame(PilotFrame frame)
    {
        Class<? extends PilotFrame> frameClass = frame.getClass();
        if(mFrameToFragmentMappings.containsKey(frameClass)) //does handle this frame type
        {
            Fragment matchingVisibleFragment = mDisplayer.getVisibleFragmentForFrame(frame.getClass());
            if(matchingVisibleFragment != null)
            {
                //A fragment should only be visible and not have a presenter set already after a
                //config-change and therefore the Fragment having been recreated. If we are setting
                //the presenter frame at this point it will be from a call originating inside the
                //FragmentLifecycleManagers.onCreateDelegate, therefore when the Activity's onCreate()
                //has been called we are guaranteed that the Fragment will have a presenter frame set.
                //Therefore Fragment.OnActivityCreated is the first safest Fragment lifecycle method
                //that a presenter can be used.
                setFrameToFragment(matchingVisibleFragment, frame);
            }
            else
            {
                Class<? extends Fragment> fragClass = mFrameToFragmentMappings.get(frameClass);
                Fragment newFrag = createFragment(fragClass);
                setFrameToFragment(newFrag, frame); //set before any frag lifecycle methods
                mDisplayer.makeVisible(newFrag);
            }

            return true;
        }
        else
            return false;
    }

    //==================================================================//
    // Private
    //==================================================================//

    @SuppressWarnings("unchecked") //manual check
    private void setFrameToFragment(Fragment frag, PilotFrame frame)
    {
        if(!(frag instanceof PresenterBackedUI))
            throw new RuntimeException("Fragment does not implement the PresenterBackedUI interface");
        ((PresenterBackedUI)frag).setPresenter(frame);
    }

    private void setupRootFragmentAndPresenterMappings(Class<? extends Fragment>[] rootFragments)
    {
        //get view classes that make up the root level of the app
        for(Class<? extends Fragment> viewClass : rootFragments)
        {
            if(!PresenterBackedUI.class.isAssignableFrom(viewClass))
                throw new RuntimeException("Passed Fragment does not extend PresenterBasedUI:"+viewClass.getCanonicalName());
            Class<? extends PilotFrame> presenterClass = PresenterUtils.getPresenterClass(viewClass);
            mFrameToFragmentMappings.put(presenterClass, viewClass);
        }
    }

    private <T extends Fragment> T createFragment(Class<T> fragmentClass)
    {
        try
        {
            return fragmentClass.getConstructor().newInstance(); //fragments should ALWAYS have a default no-arg constructor
        }
        catch (Exception e)
        {
            throw new RuntimeException("fragments should ALWAYS have a default no-arg constructor. This is a requirement of Fragment and not introduced by this lib!", e);
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
        /**
         * @param frameClass
         * @return the visible fragment that corresponds to this frame class. Null otherwise. The
         * returned fragment will have its {@link PresenterBackedUI#setPresenter(Object)} method called.
         */
        Fragment getVisibleFragmentForFrame(Class<? extends PilotFrame> frameClass);
        void makeVisible(Fragment fragment);
    }

    public static class SimpleDisplayer implements Displayer
    {
        private final FragmentManager mFragmentManager;
        private final int mContainerViewId;

        public SimpleDisplayer(FragmentManager fragmentManager, int containerViewId)
        {
            mFragmentManager = fragmentManager;
            mContainerViewId = containerViewId;
        }

        @Override
        public Fragment getVisibleFragmentForFrame(Class<? extends PilotFrame> frameClass)
        {
            //get current fragment. Using the container ID should give us the fragment at the top
            //of the stack. Fragments added programmatically will be given the container id as id.
            //findFragmentById searches the internal mAdded fragment List from LRU order
            Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
            if(PresenterUtils.getPresenterClass(fragment.getClass()).equals(frameClass))
                return fragment;
            else
                return null;
        }

        @Override
        public void makeVisible(Fragment fragment)
        {
            mFragmentManager.beginTransaction()
                    .replace(mContainerViewId, fragment)
                    .disallowAddToBackStack() //back stack is handled by Pilot
                    //can add some animations here - maybe the displayer could have this as an optional setting?
                    .commit();
        }
    }
}
