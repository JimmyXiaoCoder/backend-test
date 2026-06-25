package com.codetest.shop.mapper;

import com.codetest.shop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    Product selectById(@Param("id") Long id);
    List<Product> selectAll();
    int insert(Product product);
    int update(Product product);
    int deleteById(@Param("id") Long id);
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
