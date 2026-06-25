package com.codetest.shop.mapper;

import com.codetest.shop.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CartMapper {
    CartItem selectById(@Param("id") Long id);
    List<CartItem> selectByUserId(@Param("userId") Long userId);
    CartItem selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
    int insert(CartItem item);
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    int deleteById(@Param("id") Long id);
    int deleteByUserId(@Param("userId") Long userId);
}
