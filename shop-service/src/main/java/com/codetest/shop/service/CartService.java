package com.codetest.shop.service;

import com.codetest.common.BusinessException;
import com.codetest.common.ResultCode;
import com.codetest.shop.entity.CartItem;
import com.codetest.shop.entity.Product;
import com.codetest.shop.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductService productService;

    public List<CartItem> listByUser(Long userId) {
        return cartMapper.selectByUserId(userId);
    }

    @Transactional
    public CartItem add(Long userId, Long productId, Integer quantity) {
        // 校验商品是否存在且库存充足
        Product product = productService.getById(productId);
        if (product.getStock() < quantity) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH, "当前库存: " + product.getStock());
        }
        // 购物车已存在则累加数量
        CartItem exist = cartMapper.selectByUserAndProduct(userId, productId);
        if (exist != null) {
            cartMapper.updateQuantity(exist.getId(), exist.getQuantity() + quantity);
            return cartMapper.selectById(exist.getId());
        }
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        cartMapper.insert(item);
        return item;
    }

    public CartItem updateQuantity(Long cartId, Integer quantity) {
        CartItem exist = cartMapper.selectById(cartId);
        if (exist == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }
        cartMapper.updateQuantity(cartId, quantity);
        return cartMapper.selectById(cartId);
    }

    public void remove(Long cartId) {
        CartItem exist = cartMapper.selectById(cartId);
        if (exist == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }
        cartMapper.deleteById(cartId);
    }

    public void clear(Long userId) {
        cartMapper.deleteByUserId(userId);
    }
}
