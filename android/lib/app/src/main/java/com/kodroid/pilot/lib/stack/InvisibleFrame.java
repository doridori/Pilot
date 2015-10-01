package com.kodroid.pilot.lib.stack;

import com.kodroid.pilot.lib.android.PresenterBasedFrameLayout;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be applied to a {@link PilotFrame} subclass which signifies that it should be
 * ignored for all stack callback operations. Very useful for handling scoped data within the stack
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InvisibleFrame{}
