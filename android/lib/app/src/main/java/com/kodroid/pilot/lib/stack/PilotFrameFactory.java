package com.kodroid.pilot.lib.stack;

/**
 * Factory for creating PilotFrames. Ease of testing.
 */
interface PilotFrameFactory
{
    PilotFrame createFrame(Class<? extends PilotFrame> frameClassToPush, Args args);
}
