package com.gupao.spring.framework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-17 08:58
 */
public class GPHandlerMapping {

    private Pattern pattern;     //URL

    private Method method;      //URL对应方法

    private Object controller;  //Method对应的实例对象

    public GPHandlerMapping(Pattern pattern, Object controller, Method method) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }


    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}
