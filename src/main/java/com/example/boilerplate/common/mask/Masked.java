package com.example.boilerplate.common.mask;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tools.jackson.databind.annotation.JsonSerialize;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = MaskedSerializer.class)
public @interface Masked {
    MaskType value();
}
