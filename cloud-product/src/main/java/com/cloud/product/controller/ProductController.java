package com.cloud.product.controller;

import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.cloud.product.entity.Product;
import com.cloud.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品接口，方法级 RBAC：每个操作对应 product:* 权限。
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAuthority('product:query')")
    @GetMapping
    public Result<List<Product>> list() {
        return Result.ok(productService.list());
    }

    @PreAuthorize("hasAuthority('product:query')")
    @GetMapping("/{id}")
    public Result<Product> get(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return Result.ok(product);
    }

    @PreAuthorize("hasAuthority('product:add')")
    @PostMapping
    public Result<Product> add(@Valid @RequestBody Product product) {
        productService.save(product);
        return Result.ok(product);
    }

    @PreAuthorize("hasAuthority('product:update')")
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Product product) {
        return Result.ok(productService.updateById(product));
    }

    @PreAuthorize("hasAuthority('product:delete')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(productService.removeById(id));
    }
}
