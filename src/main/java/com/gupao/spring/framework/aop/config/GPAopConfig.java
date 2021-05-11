package com.gupao.spring.framework.aop.config;

import lombok.Data;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-21 15:53
 */
@Data
public class GPAopConfig {

    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;

}
