package com.kodroid.pilot.lib.android.frameBacking;

import com.kodroid.pilot.lib.stack.PilotFrame;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to declare the class type of the BackedByFrame used for a {@link PilotFrameLayout}
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BackedByFrame
{
    Class<? extends PilotFrame> value();
}
