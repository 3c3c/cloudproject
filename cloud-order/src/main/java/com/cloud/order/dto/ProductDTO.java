package com.cloud.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 产品 DTO：Feign 调用产品服务后的反序列化对象（与 product 服务字段保持一致，解耦不直接依赖 product 模块）。
 */
@Data
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
}
