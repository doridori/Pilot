package com.kodroid.pilot.lib.statestack;

/**
 * Factory for creating PilotFrames. Ease of testing. Normally just use the auto default.
 */
public interface StateFrameFactory
{
    StateFrame createFrame(Class<? extends StateFrame> frameClassToPush, Args args);
}
