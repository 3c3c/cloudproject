package com.cloud.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.cloud.common.security.SecurityUtils;
import com.cloud.order.dto.ProductDTO;
import com.cloud.order.entity.Order;
import com.cloud.order.feign.ProductFeignClient;
import com.cloud.order.mapper.OrderMapper;
import com.cloud.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProductFeignClient productFeignClient;

    @Override
    public Order createOrder(Order order) {
        // 1. Feign 调用产品服务校验产品存在（认证头已由 FeignConfig 自动透传）
        Result<ProductDTO> result = productFeignClient.getProduct(order.getProductId());
        if (result == null || result.getData() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "产品不存在");
        }
        ProductDTO product = result.getData();

        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            order.setQuantity(1);
        }
        // 2. 组装订单
        order.setOrderNo("ORD" + System.currentTimeMillis() + RANDOM.nextInt(1000));
        order.setUserId(SecurityUtils.getCurrentUserId());
        order.setProductName(product.getName());
        order.setAmount(product.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        order.setStatus(1);
        save(order);
        return order;
    }

    @Override
    public List<Order> listMyOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return list(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getId));
    }
}
