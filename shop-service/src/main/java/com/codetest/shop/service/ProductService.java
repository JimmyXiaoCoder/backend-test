package com.codetest.shop.service;

import com.codetest.common.BusinessException;
import com.codetest.common.ResultCode;
import com.codetest.shop.entity.Product;
import com.codetest.shop.mapper.ProductMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "product:";
    private static final String CACHE_LIST_KEY = "product:list";
    private static final long CACHE_TTL = 30;

    public Product getById(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        Product cached = (Product) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("Product {} hit cache", id);
            return cached;
        }
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        redisTemplate.opsForValue().set(key, product, CACHE_TTL, TimeUnit.MINUTES);
        return product;
    }

    @SuppressWarnings("unchecked")
    public List<Product> listAll() {
        List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(CACHE_LIST_KEY);
        if (cached != null) {
            log.debug("Product list hit cache");
            return cached;
        }
        List<Product> list = productMapper.selectAll();
        redisTemplate.opsForValue().set(CACHE_LIST_KEY, list, CACHE_TTL, TimeUnit.MINUTES);
        return list;
    }

    public Product create(Product product) {
        productMapper.insert(product);
        redisTemplate.delete(CACHE_LIST_KEY);
        return product;
    }

    public Product update(Product product) {
        Product exist = productMapper.selectById(product.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        productMapper.update(product);
        redisTemplate.delete(CACHE_KEY_PREFIX + product.getId());
        redisTemplate.delete(CACHE_LIST_KEY);
        return productMapper.selectById(product.getId());
    }

    public void delete(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        productMapper.deleteById(id);
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
        redisTemplate.delete(CACHE_LIST_KEY);
    }
}
