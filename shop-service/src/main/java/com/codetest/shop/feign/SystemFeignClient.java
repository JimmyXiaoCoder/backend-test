package com.codetest.shop.feign;

import com.codetest.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * OpenFeign RPC 调用 system-service
 */
@FeignClient(name = "system-service", url = "http://localhost:9091")
public interface SystemFeignClient {

    @GetMapping("/system/user/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long id);

    @GetMapping("/system/user/list")
    Result<java.util.List<Map<String, Object>>> listUsers();
}
