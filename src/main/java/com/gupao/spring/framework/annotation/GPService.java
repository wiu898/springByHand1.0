package com.gupao.spring.framework.annotation;


import java.lang.annotation.*;

/**
 * 手写Spring 简单实现 自动注解类
 *
 * @author lichao chao.li07@hand-china.com 2021-01-10 15:22
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPService {
    String value() default "";
}

