package com.cloud.order.controller;

import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.cloud.order.entity.Order;
import com.cloud.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单接口，方法级 RBAC：查询需 order:query，下单需 order:create。
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasAuthority('order:query')")
    @GetMapping
    public Result<List<Order>> list() {
        return Result.success(orderService.listMyOrders());
    }

    @PreAuthorize("hasAuthority('order:query')")
    @GetMapping("/{id}")
    public Result<Order> get(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return Result.success(order);
    }

    @PreAuthorize("hasAuthority('order:create')")
    @PostMapping
    public Result<Order> create(@RequestBody Order order) {
        return Result.success(orderService.createOrder(order));
    }
}
