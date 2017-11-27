package com.kodroid.pilot.lib.android.uiTypeHandler;

import com.kodroid.pilot.lib.statestack.StateFrame;

/**
 * Interface for a class that can handle creation / presentor setting and placement for a UI type
 * (i.e. View, Fragment, Dialog, FragmentDialog, etc)
 */
public interface UITypeHandler
{
    /**
     *
     * @param frameClass
     * @return true if this handler can create a UI for that frame type. False if it does not handle this frame
     */
    boolean isFrameSupported(Class<? extends StateFrame> frameClass);

    /**
     * Handle the latest frame i.e. show it. Will throw a {@link IllegalArgumentException} if
     * {@link #isFrameSupported(Class<? extends StateFrame >)} is false
     *
     * @param frame
     */
    void renderFrame(StateFrame frame);

    /**
     * Check for the passed in frame being non-opaque or non-fullscreen
     *
     * @param frame
     * @return true if this frame is opaque, meaning it takes up the whole UI and no other UI elements should be rendered behind it.
     */
    boolean isFrameOpaque(StateFrame frame);

    /**
     * Called when another typeHandler has displayed an opaque fullscreen UI
     */
    void clearAllUI();
}
