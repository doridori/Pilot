package com.kodroid.pilot.lib.stack;

/**
 * Factory for creating PilotFrames. Ease of testing. Normally just use the auto default.
 */
public interface PilotFrameFactory
{
    PilotFrame createFrame(Class<? extends PilotFrame> frameClassToPush, Args args);
}
