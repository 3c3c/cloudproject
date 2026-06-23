package com.cloud.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.order.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    /** 下单：Feign 调用产品服务校验产品并计算金额 */
    Order createOrder(Order order);

    /** 查询当前登录用户的订单 */
    List<Order> listMyOrders();
}
