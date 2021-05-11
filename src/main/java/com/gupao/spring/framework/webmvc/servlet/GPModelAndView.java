package com.gupao.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-17 09:29
 */
public class GPModelAndView {

    private String viewName;
    //传递给页面文件的map类型参数
    private Map<String,?> model;

    public GPModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public GPModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

}
