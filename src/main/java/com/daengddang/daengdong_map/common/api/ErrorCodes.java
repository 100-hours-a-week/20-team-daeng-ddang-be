package com.daengddang.daengdong_map.common.api;

import com.daengddang.daengdong_map.common.ErrorCode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorCodes {

    ErrorCode[] value() default {};

    boolean includeCommon() default true;
}
