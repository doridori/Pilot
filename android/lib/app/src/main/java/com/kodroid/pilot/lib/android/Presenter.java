package com.kodroid.pilot.lib.android;

import com.kodroid.pilot.lib.stack.PilotFrame;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to declare the class type of the Presenter used for a {@link PresenterBasedFrameLayout}
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Presenter
{
    public Class<? extends PilotFrame> value();

}
