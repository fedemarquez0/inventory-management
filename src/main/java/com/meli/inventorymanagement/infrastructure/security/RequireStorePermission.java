package com.meli.inventorymanagement.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireStorePermission {
    String storeIdParam() default "storeId";
    boolean adminOnly() default false;
}
