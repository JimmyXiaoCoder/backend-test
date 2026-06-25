package com.codetest.shop.controller;

import com.codetest.common.Result;
import com.codetest.common.ResultUtils;
import com.codetest.shop.entity.Product;
import com.codetest.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return ResultUtils.success(productService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<Product>> listAll() {
        return ResultUtils.success(productService.listAll());
    }

    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        return ResultUtils.success(productService.create(product));
    }

    @PutMapping
    public Result<Product> update(@RequestBody Product product) {
        return ResultUtils.success(productService.update(product));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResultUtils.success();
    }
}
