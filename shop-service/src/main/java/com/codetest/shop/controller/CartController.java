package com.codetest.shop.controller;

import com.codetest.common.Result;
import com.codetest.common.ResultUtils;
import com.codetest.shop.entity.CartItem;
import com.codetest.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list/{userId}")
    public Result<List<CartItem>> listByUser(@PathVariable Long userId) {
        return ResultUtils.success(cartService.listByUser(userId));
    }

    @PostMapping
    public Result<CartItem> add(@RequestParam Long userId,
                                 @RequestParam Long productId,
                                 @RequestParam(defaultValue = "1") Integer quantity) {
        return ResultUtils.success(cartService.add(userId, productId, quantity));
    }

    @PutMapping("/{id}")
    public Result<CartItem> updateQuantity(@PathVariable Long id,
                                            @RequestParam Integer quantity) {
        return ResultUtils.success(cartService.updateQuantity(id, quantity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(id);
        return ResultUtils.success();
    }

    @DeleteMapping("/clear/{userId}")
    public Result<Void> clear(@PathVariable Long userId) {
        cartService.clear(userId);
        return ResultUtils.success();
    }
}
