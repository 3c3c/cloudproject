package com.cloud.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal amount;
    /** 状态：1已下单 2已支付 3已取消 */
    private Integer status;
    private LocalDateTime createTime;
}
