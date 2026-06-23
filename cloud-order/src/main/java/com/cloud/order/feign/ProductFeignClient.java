package com.cloud.order.feign;

import com.cloud.common.result.Result;
import com.cloud.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 产品服务 Feign 客户端（通过 Nacos 服务名解析实例）。
 */
@FeignClient(name = "cloud-product", configuration = FeignConfig.class)
public interface ProductFeignClient {

    @GetMapping("/products/{id}")
    Result<ProductDTO> getProduct(@PathVariable("id") Long id);
}
