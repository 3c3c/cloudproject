package com.cloud.common.entity;

import lombok.Data;

/**
 * 分页请求基类
 * 可以被具体的请求DTO继承，也可以单独使用
 */
@Data
public class BasePage {

    /**
     * 当前页码，默认第1页
     */
    private Integer current = 1;

    /**
     * 每页大小，默认10条
     */
    private Integer size = 10;
}
